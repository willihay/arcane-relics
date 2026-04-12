package org.bensam.arcanerelics.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.bensam.arcanerelics.ArcaneRelics;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class ItemLightningWand extends AbstractChargedWandItem implements WandEnchantingTableOutput {
    private static final int WAND_RANGE = 50;
    private static final int LIGHTNING_ROD_RECHARGE_RADIUS = 12;
    private static final float BASE_EXPLOSION_POWER = 0.75f;
    private static final float MAX_EXPLOSION_POWER = 2.0f;

    public ItemLightningWand(Properties properties, WandDefinition definition) {
        super(properties, definition);
    }

    @Override
    public boolean canBeProducedOrRechargedBy(ItemStack stack) {
        return stack.is(Items.ENCHANTED_BOOK) && hasEnchantment(stack, Enchantments.CHANNELING);
    }

    @Override
    public List<ItemStack> getEnchantmentItems(Level level) {
        return getAllEnchantedBooks(level, Enchantments.CHANNELING);
    }

    @Override
    public int getLevelOfEnchantmentItem(ItemStack stack) {
        return getEnchantmentLevel(stack, Enchantments.CHANNELING);
    }

    @Override
    protected int getPowerUpCost(Level level, Player player, ItemStack stack, int chargeTicks, boolean fullyPowered) {
        return level.isThundering() ? this.getNormalCastCost() : super.getPowerUpCost(level, player, stack, chargeTicks, fullyPowered);
    }

    //region Recharge Methods
    @Override
    protected RechargeContext tryRecharge(Level level, Player player, ItemStack wandStack) {
        if (this.isFullyCharged(wandStack)) {
            return new RechargeContext(RechargeResult.ALREADY_FULL, 0, null, this.getAlreadyFullMessagePath());
        }

        BlockPos lightningRodPos = findNearbyLightningRod(level, player.blockPosition());
        if (lightningRodPos != null) {
            if (level.isThundering()) {
                this.setCharges(wandStack, this.getMaxCharges());
                return new RechargeContext(RechargeResult.RECHARGE_SUCCESS, 0, lightningRodPos, "lightning_wand.recharge.success");
            }
            else {
                return new RechargeContext(RechargeResult.RECHARGE_FAIL, 0, null, "lightning_wand.recharge.no_thunder");
            }
        }

        return new RechargeContext(RechargeResult.RECHARGE_FAIL, 0, null, "lightning_wand.recharge.no_lightning_rod");
    }

    protected static @Nullable BlockPos findNearbyLightningRod(Level level, BlockPos center) {
        BlockPos closestRod = null;
        double closestDistanceSq = Double.MAX_VALUE;
        int radius = LIGHTNING_ROD_RECHARGE_RADIUS;

        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-radius, -radius, -radius),
                center.offset(radius, radius, radius)
        )) {
            if (!level.getBlockState(pos).is(BlockTags.LIGHTNING_RODS)) {
                continue;
            }

            if (!level.canSeeSky(pos.above())) {
                continue;
            }

            double distanceSq = pos.distSqr(center);
            if (distanceSq < closestDistanceSq) {
                closestDistanceSq = distanceSq;
                closestRod = pos.immutable();
            }
        }

        return closestRod;
    }

    @Override
    protected void playRechargeEffects(
            ServerLevel level,
            Player player,
            InteractionHand hand,
            ItemStack stack,
            RechargeContext rechargeContext
    ) {
        if (rechargeContext.result() == RechargeResult.RECHARGE_SUCCESS) {
            level.playSound(
                    null,
                    player.blockPosition(),
                    SoundEvents.LIGHTNING_BOLT_THUNDER,
                    SoundSource.PLAYERS,
                    1.0f, // volume
                    1.0f // pitch
            );

            if (rechargeContext.sourcePos() != null) {
                // Create recharge particle trail from sky to rod.
                Vec3 skyStart = Vec3.atBottomCenterOf(rechargeContext.sourcePos()).add(0.0, 12.0, 0.0);
                Vec3 rodTop = Vec3.atBottomCenterOf(rechargeContext.sourcePos()).add(Vec3.Y_AXIS);
                spawnParticleTrail(level, ParticleTypes.ELECTRIC_SPARK, skyStart, rodTop, 12, 4, 0.05);

                // Create recharge particle trail from rod to player's wand.
                Vec3 wandTip = getWandTipPosition(player, hand);
                spawnParticleTrail(level, ParticleTypes.ELECTRIC_SPARK, rodTop, wandTip, 12, 4, 0.04);
            }
        }

        // Play default effects.
        super.playRechargeEffects(level, player, hand, stack, rechargeContext);
    }
    //endregion

    //region Cast Methods
    @Override
    protected boolean performCast(ServerLevel level, Player player, ItemStack stack, float powerUpPercentage, boolean isFullyPowered) {
        // Check if dimension has skylight. Can't cast lightning in the nether.
        if (!level.dimensionType().hasSkyLight()) {
            player.displayClientMessage(
                    Component.translatable("message." + ArcaneRelics.MOD_ID + ".lightning_wand.cast.no_skylight"),
                    true
            );
            return false;
        }

        float explosionPower = Mth.lerp(powerUpPercentage, BASE_EXPLOSION_POWER, MAX_EXPLOSION_POWER);
        return this.summonChargedLightning(level, player, explosionPower, isFullyPowered);
    }

    protected boolean summonChargedLightning(Level level, Player player, float explosionPower, boolean withBlockBreak) {
        TargetResult target = getTarget(player, WAND_RANGE);
        if (target == null) {
            return false;
        }
        Vec3 strikePos = Vec3.atBottomCenterOf(target.blockPos());

        // Check if we can see sky above the target.
        if (!hasSkyAccess(level, target.blockPos().above(), true)) {
            player.displayClientMessage(
                    Component.translatable("message." + ArcaneRelics.MOD_ID + ".lightning_wand.cast.no_sky_above_target"),
                    true
            );
            return false;
        }

        // Create the lightning bolt.
        LightningBolt lightningBolt = new LightningBolt(EntityType.LIGHTNING_BOLT, level);
        lightningBolt.setPos(strikePos);
        level.addFreshEntity(lightningBolt);

        // Create explosion.
        Level.ExplosionInteraction interaction = withBlockBreak
                ? Level.ExplosionInteraction.BLOCK
                : Level.ExplosionInteraction.NONE;
        level.explode(player, strikePos.x, strikePos.y, strikePos.z, explosionPower, false, interaction);

        return true;
    }

    //endregion
}
