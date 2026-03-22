package org.bensam.arcanerelics.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.bensam.arcanerelics.ArcaneRelics;
import org.jspecify.annotations.NonNull;

public class ItemFireWand extends AbstractChargedWandItem<ItemFireWand.FireRechargeResult> {
    public static final int INITIAL_CHARGES = 30;
    public static final int MAX_CHARGES = 50;

    private static final int NORMAL_CAST_COST = 1;
    private static final int FULL_POWER_TICKS = 20;
    private static final int FULL_POWER_CAST_COST = 2;

    private static final int BLAZE_EXTRACTION_RECHARGE_RADIUS = 8;
    private static final int BLAZE_ROD_RECHARGE_AMOUNT = 20;
    private static final int GHAST_EXTRACTION_RECHARGE_RADIUS = 20;

    private static final float BASE_EXPLOSION_POWER = 0.5f;
    private static final float MAX_EXPLOSION_POWER = 2.0f;

    public ItemFireWand(Properties properties) {
        super(properties, INITIAL_CHARGES, MAX_CHARGES);
    }

    //region Helper Functions
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
    //endregion

    //region Recharge Functions
    public enum FireRechargeResult implements RechargeResult {
        ALREADY_FULL,
        BLAZE_EXTRACTION_SUCCESS,
        GHAST_EXTRACTION_SUCCESS,
        NO_MOB_FUEL,
        BLAZE_ROD_SUCCESS,
        NO_FUEL
    }

    @Override
    protected RechargeContext<FireRechargeResult> tryRecharge(Level level, Player player, ItemStack wandStack) {
        if (this.isFullyCharged(wandStack)) {
            return new RechargeContext<>(FireRechargeResult.ALREADY_FULL, null);
        }

        RechargeContext<FireRechargeResult> mobFuelSearchResult = findNearbyMobFuel(level, player.blockPosition());
        if (mobFuelSearchResult.result() != FireRechargeResult.NO_MOB_FUEL) {
            this.setCharges(wandStack, this.getMaxCharges());
            return mobFuelSearchResult;
        }

        return new RechargeContext<>(this.tryBlazeRodRecharge(player, wandStack), null);
    }

    protected static RechargeContext<FireRechargeResult> findNearbyMobFuel(Level level, BlockPos center) {
        BlockPos closestMob = findClosestMobOfType(level, center, GHAST_EXTRACTION_RECHARGE_RADIUS, Ghast.class);
        if (closestMob != null) {
            return new RechargeContext<>(FireRechargeResult.GHAST_EXTRACTION_SUCCESS, closestMob);
        }

        closestMob = findClosestMobOfType(level, center, GHAST_EXTRACTION_RECHARGE_RADIUS, HappyGhast.class);
        if (closestMob != null) {
            return new RechargeContext<>(FireRechargeResult.GHAST_EXTRACTION_SUCCESS, closestMob);
        }

        closestMob = findClosestMobOfType(level, center, BLAZE_EXTRACTION_RECHARGE_RADIUS, Blaze.class);
        if (closestMob != null) {
            return new RechargeContext<>(FireRechargeResult.BLAZE_EXTRACTION_SUCCESS, closestMob);
        }

        return new RechargeContext<>(FireRechargeResult.NO_MOB_FUEL, null);
    }

    protected static <T extends Entity> BlockPos findClosestMobOfType(Level level, BlockPos center, int radius, Class<T> mobType) {
        BlockPos closestMob = null;
        double closestDistanceSq = Double.MAX_VALUE;

        AABB searchBox = new AABB(
                center.getX() - radius, center.getY() - radius, center.getZ() - radius,
                center.getX() + radius + 1, center.getY() + radius + 1, center.getZ() + radius + 1
        );

        for (T mob : level.getEntitiesOfClass(mobType, searchBox)) {
            if (!mob.isAlive()) {
                continue;
            }

            double distanceSq = mob.blockPosition().distSqr(center);
            if (distanceSq < closestDistanceSq) {
                closestDistanceSq = distanceSq;
                closestMob = mob.blockPosition().immutable();
            }
        }

        return closestMob;
    }

    private FireRechargeResult tryBlazeRodRecharge(Player player, ItemStack stack) {
        if (this.consumeArcaneFuelFromInventory(player, Items.BLAZE_ROD)) {
            this.addCharges(stack, BLAZE_ROD_RECHARGE_AMOUNT);
            return FireRechargeResult.BLAZE_ROD_SUCCESS;
        }

        return FireRechargeResult.NO_FUEL;
    }

    @Override
    protected void playRechargeContextEffects(
            ServerLevel level,
            Player player,
            InteractionHand hand,
            ItemStack stack, @NonNull RechargeContext<FireRechargeResult> rechargeContext
    ) {
        // Play sound effects.
        SoundEvent soundRechargeEvent = null;
        switch (rechargeContext.result()) {
            case BLAZE_EXTRACTION_SUCCESS -> soundRechargeEvent = SoundEvents.BLAZE_AMBIENT;
            case GHAST_EXTRACTION_SUCCESS -> soundRechargeEvent = SoundEvents.GHAST_SCREAM; // HARNESS_GOGGLES_DOWN
            case BLAZE_ROD_SUCCESS -> soundRechargeEvent = SoundEvents.ENCHANTMENT_TABLE_USE;
        }
        if (soundRechargeEvent != null) {
            level.playSound(
                    null,
                    player.blockPosition(),
                    soundRechargeEvent,
                    SoundSource.PLAYERS,
                    1.0f, // volume
                    1.0f // pitch
            );
        }

        // Create recharge particle effects.
        switch (rechargeContext.result()) {
            case BLAZE_EXTRACTION_SUCCESS, GHAST_EXTRACTION_SUCCESS -> {
                if (rechargeContext.sourcePos() != null) {
                    // Create recharge particle trail from mob to player's wand.
                    Vec3 mobStart = Vec3.atCenterOf(rechargeContext.sourcePos());
                    Vec3 wandTip = getWandTipPosition(player, hand);
                    this.spawnParticleTrail(level, ParticleTypes.SMALL_FLAME, mobStart, wandTip, 12, 5, 0.04);
                }
            }

            case BLAZE_ROD_SUCCESS -> {
                Vec3 wandTip = getWandTipPosition(player, hand);
                level.sendParticles(
                        ParticleTypes.SMALL_FLAME,
                        wandTip.x, wandTip.y, wandTip.z, // position
                        10, // # of particles
                        0.05,0.05,0.05, // particle spread
                        0.05 // particle speed
                );
            }
        }

        super.playRechargeContextEffects(level, player, hand, stack, rechargeContext);
    }

    @Override
    protected void sendRechargeFeedback(Player player, FireRechargeResult result) {
        switch (result) {
            case FireRechargeResult.ALREADY_FULL -> player.displayClientMessage(
                    this.getFullyChargedMessage(),
                    true
            );
            case FireRechargeResult.BLAZE_EXTRACTION_SUCCESS -> player.displayClientMessage(
                    Component.translatable("message." + ArcaneRelics.MOD_ID + ".fire_wand.recharge.blaze"),
                    true
            );
            case FireRechargeResult.GHAST_EXTRACTION_SUCCESS -> player.displayClientMessage(
                    Component.translatable("message." + ArcaneRelics.MOD_ID + ".fire_wand.recharge.ghast"),
                    true
            );
            case FireRechargeResult.BLAZE_ROD_SUCCESS -> player.displayClientMessage(
                    Component.translatable("message." + ArcaneRelics.MOD_ID + ".fire_wand.recharge.fuel"),
                    true
            );
            case FireRechargeResult.NO_FUEL -> player.displayClientMessage(
                    this.getNoRechargeFuelMessage(),
                    true
            );
        }
    }
    //endregion

    //region Cast Methods
    @Override
    protected boolean performCast(Level level, Player player, ItemStack stack, float powerUpPercentage, boolean isFullyPowered) {
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
