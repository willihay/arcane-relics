package org.bensam.arcanerelics.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class ItemFireballWand extends AbstractChargedWandItem implements WandEnchantingTableOutput {
    private static final int BLAZE_EXTRACTION_RADIUS = 8;
    private static final int GHAST_EXTRACTION_RADIUS = 20;
    private static final int RECHARGE_CONTEXT_DATA_GHAST_EXTRACTION = 1;
    private static final int RECHARGE_CONTEXT_DATA_BLAZE_EXTRACTION = 2;
    private static final float BASE_EXPLOSION_POWER = 0.5f;
    private static final float MAX_EXPLOSION_POWER = 2.0f;

    public ItemFireballWand(Properties properties, WandDefinition definition) {
        super(properties, definition);
    }

    @Override
    public boolean canBeProducedOrRechargedBy(ItemStack stack) {
        return stack.is(Items.ENCHANTED_BOOK) && hasEnchantment(stack, Enchantments.FLAME);
    }

    @Override
    public List<ItemStack> getEnchantmentItems(Level level) {
        return getAllEnchantedBooks(level, Enchantments.FLAME);
    }

    @Override
    public int getLevelOfEnchantmentItem(ItemStack stack) {
        return getEnchantmentLevel(stack, Enchantments.FLAME);
    }

    //region Recharge Methods
    @Override
    protected RechargeContext tryRecharge(Level level, Player player, ItemStack wandStack) {
        if (this.isFullyCharged(wandStack)) {
            return new RechargeContext(RechargeResult.ALREADY_FULL, 0, null, this.getAlreadyFullMessagePath());
        }

        RechargeContext mobFuelSearchResult = findNearbyMobFuel(level, player.blockPosition());
        if (mobFuelSearchResult.result() != RechargeResult.RECHARGE_FAIL) {
            this.setCharges(wandStack, this.getMaxCharges());
        }

        return mobFuelSearchResult;
    }

    protected static RechargeContext findNearbyMobFuel(Level level, BlockPos center) {
        BlockPos closestMob = findClosestMobOfType(level, center, GHAST_EXTRACTION_RADIUS, EntityType.GHAST);
        if (closestMob != null) {
            return new RechargeContext(
                    RechargeResult.RECHARGE_SUCCESS,
                    RECHARGE_CONTEXT_DATA_GHAST_EXTRACTION,
                    closestMob,
                    "fireball_wand.recharge.ghast");
        }

        closestMob = findClosestMobOfType(level, center, GHAST_EXTRACTION_RADIUS, EntityType.HAPPY_GHAST);
        if (closestMob != null) {
            return new RechargeContext(
                    RechargeResult.RECHARGE_SUCCESS,
                    RECHARGE_CONTEXT_DATA_GHAST_EXTRACTION,
                    closestMob,
                    "fireball_wand.recharge.ghast");
        }

        closestMob = findClosestMobOfType(level, center, BLAZE_EXTRACTION_RADIUS, EntityType.BLAZE);
        if (closestMob != null) {
            return new RechargeContext(
                    RechargeResult.RECHARGE_SUCCESS,
                    RECHARGE_CONTEXT_DATA_BLAZE_EXTRACTION,
                    closestMob,
                    "fireball_wand.recharge.blaze");
        }

        return new RechargeContext(
                RechargeResult.RECHARGE_FAIL,
                0,
                null,
                "fireball_wand.recharge.fail");
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
            // Play sound effects.
            level.playSound(
                    null,
                    player.blockPosition(),
                    rechargeContext.contextData() == RECHARGE_CONTEXT_DATA_BLAZE_EXTRACTION ? SoundEvents.BLAZE_AMBIENT : SoundEvents.GHAST_SCREAM,
                    SoundSource.PLAYERS,
                    1.0f, // volume
                    1.0f // pitch
            );

            // Create recharge particle effects.
            if (rechargeContext.sourcePos() != null) {
                // Create recharge particle trail from mob to player's wand.
                Vec3 mobStart = Vec3.atCenterOf(rechargeContext.sourcePos());
                Vec3 wandTip = getWandTipPosition(player, hand);
                spawnParticleTrail(level, ParticleTypes.SMALL_FLAME, mobStart, wandTip, 12, 5, 0.04);
            }
        }

        // Play default effects.
        super.playRechargeEffects(level, player, hand, stack, rechargeContext);
    }
    //endregion

    //region Cast Methods
    @Override
    protected boolean performCast(ServerLevel level, Player player, ItemStack stack, float powerUpPercentage, boolean isFullyPowered) {
        int explosionPower = Math.round(Mth.lerp(powerUpPercentage, BASE_EXPLOSION_POWER, MAX_EXPLOSION_POWER));
        this.shootFireball(level, player, explosionPower, isFullyPowered);
        return true;
    }

    protected void shootFireball(Level level, Player player, int explosionPower, boolean isFullyPowered) {
        Vec3 look = player.getLookAngle().normalize();
        Vec3 fireballPos = player.getEyePosition(1.0f).add(look.scale(2));

        if (isFullyPowered) {
            LargeFireball fireball = new LargeFireball(level, player, look, explosionPower);
            fireball.setPos(fireballPos);
            level.addFreshEntity(fireball);
        }
        else {
            SmallFireball fireball = new SmallFireball(level, player, look);
            fireball.setPos(fireballPos);
            level.addFreshEntity(fireball);
        }
    }

    @Override
    protected void playCastSuccessEffects(ServerLevel level, Player player, ItemStack stack) {
        level.playSound(
                null,
                player.blockPosition(),
                SoundEvents.GHAST_SHOOT,
                SoundSource.PLAYERS,
                1.0f, // volume
                1.0f // pitch
        );
    }
    //endregion
}
