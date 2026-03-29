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
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import org.bensam.arcanerelics.ArcaneRelics;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class ItemLightningWand extends AbstractChargedWandItem<ItemLightningWand.LightningRechargeResult> implements WandEnchantingTableOutput {
    public static final int INITIAL_CHARGES = 20;
    public static final int MAX_CHARGES = 40;
    private static final int RECHARGE_AMOUNT = 20;
    private static final int WAND_RANGE = 60;

    private static final int NORMAL_CAST_COST = 1;
    private static final int FULL_POWER_CAST_COST = 2;
    private static final int FULL_POWER_TICKS = 60;

    private static final int LIGHTNING_ROD_RECHARGE_RADIUS = 12;

    private static final float BASE_EXPLOSION_POWER = 0.75f;
    private static final float MAX_EXPLOSION_POWER = 2.5f;

    public ItemLightningWand(Properties properties) {
        super(properties, INITIAL_CHARGES, MAX_CHARGES);
    }

    //region Helper Methods
    @Override
    protected int getFullPowerCastCost() {
        return FULL_POWER_CAST_COST;
    }

    @Override
    protected int getFullPowerTicks() {
        return FULL_POWER_TICKS;
    }

    @Override
    protected int getNormalCastCost() {
        return NORMAL_CAST_COST;
    }

    @Override
    protected int getPowerUpCost(Level level, Player player, ItemStack stack, int chargeTicks, boolean fullyCharged) {
        return level.isThundering() ? 0 : super.getPowerUpCost(level, player, stack, chargeTicks, fullyCharged);
    }

    @Override
    public int getRechargeChargeAmount() { return RECHARGE_AMOUNT; }
    //endregion

    //region Recharge Methods
    public enum LightningRechargeResult implements RechargeResult {
        ALREADY_FULL,
        LIGHTNING_ROD_SUCCESS,
        NO_THUNDER,
        NO_LIGHTNING_ROD
    }

    @Override
    public boolean canBeProducedOrRechargedBy(ItemStack stack) {
        return stack.is(Items.ENCHANTED_BOOK) && AbstractChargedWandItem.hasEnchantment(stack, Enchantments.CHANNELING);
    }

    @Override
    protected RechargeContext<LightningRechargeResult> tryRecharge(Level level, Player player, ItemStack wandStack) {
        if (this.isFullyCharged(wandStack)) {
            return new RechargeContext<>(LightningRechargeResult.ALREADY_FULL, null);
        }

        boolean isThundering = level.isThundering();

        BlockPos lightningRodPos = findNearbyLightningRod(level, player.blockPosition());
        if (lightningRodPos != null) {
            if (isThundering) {
                this.setCharges(wandStack, this.getMaxCharges());
                return new RechargeContext<>(LightningRechargeResult.LIGHTNING_ROD_SUCCESS, lightningRodPos);
            }
            else {
                return new RechargeContext<>(LightningRechargeResult.NO_THUNDER, null);
            }
        }

        return new RechargeContext<>(LightningRechargeResult.NO_LIGHTNING_ROD, null);
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
    protected void playRechargeContextEffects(
            ServerLevel level,
            Player player,
            InteractionHand hand,
            ItemStack stack,
            @NonNull RechargeContext<LightningRechargeResult> rechargeContext
    ) {
        if (rechargeContext.result() == LightningRechargeResult.LIGHTNING_ROD_SUCCESS) {
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
                this.spawnParticleTrail(level, ParticleTypes.ELECTRIC_SPARK, skyStart, rodTop, 12, 4, 0.05);

                // Create recharge particle trail from rod to player's wand.
                Vec3 wandTip = getWandTipPosition(player, hand);
                this.spawnParticleTrail(level, ParticleTypes.ELECTRIC_SPARK, rodTop, wandTip, 12, 4, 0.04);
            }
        }

        // Play default effects.
        super.playRechargeContextEffects(level, player, hand, stack, rechargeContext);
    }

    @Override
    protected void sendRechargeFeedback(Player player, LightningRechargeResult result) {
        switch (result) {
            case ALREADY_FULL -> player.displayClientMessage(
                    this.getFullyChargedMessage(),
                    true
            );
            case LIGHTNING_ROD_SUCCESS -> player.displayClientMessage(
                    Component.translatable("message." + ArcaneRelics.MOD_ID + ".lightning_wand.recharge.lightning_rod"),
                    true
            );
            case NO_THUNDER -> player.displayClientMessage(
                    Component.translatable("message." + ArcaneRelics.MOD_ID + ".lightning_wand.recharge.no_thunder"),
                    true
            );
            case NO_LIGHTNING_ROD -> player.displayClientMessage(
                    Component.translatable("message." + ArcaneRelics.MOD_ID + ".lightning_wand.recharge.no_lightning_rod"),
                    true
            );
        }
    }
    //endregion

    //region Cast Methods
    @Override
    protected boolean performCast(Level level, Player player, ItemStack stack, float powerUpPercentage, boolean isFullyPowered) {
        float explosionPower = Mth.lerp(powerUpPercentage, BASE_EXPLOSION_POWER, MAX_EXPLOSION_POWER);
        return this.summonChargedLightning(level, player, explosionPower, isFullyPowered);
    }

    protected boolean summonChargedLightning(Level level, Player player, float explosionPower, boolean withBlockBreak) {
        TargetResult target = getTarget(player, WAND_RANGE);
        if (target == null) {
            return false;
        }
        Vec3 strikePos = Vec3.atBottomCenterOf(target.blockPos());

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
