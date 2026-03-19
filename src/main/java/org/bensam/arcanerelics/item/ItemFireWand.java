package org.bensam.arcanerelics.item;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.bensam.arcanerelics.ArcaneRelics;
import org.jspecify.annotations.Nullable;

public class ItemFireWand extends AbstractChargedWandItem<ItemFireWand.FireRechargeResult> {
    public static final int INITIAL_CHARGES = 30;
    public static final int MAX_CHARGES = 50;

    private static final int NORMAL_CAST_COST = 1;
    private static final int FULL_POWER_TICKS = 20;
    private static final int FULL_POWER_CAST_COST = 2;

    private static final int GHAST_EXTRACTION_RECHARGE_RADIUS = 20;
    private static final int GHAST_TEAR_RECHARGE_AMOUNT = 30;
    private static final int GHAST_TEAR_RECHARGE_THRESHOLD = MAX_CHARGES - (GHAST_TEAR_RECHARGE_AMOUNT / 2);

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
        GHAST_EXTRACTION_SUCCESS,
        GHAST_TEAR_SUCCESS,
        NO_GHAST_TEAR,
        TOO_CHARGED_FOR_GHAST_TEAR,
        ALREADY_FULL
    }

    @Override
    protected FireRechargeResult tryRecharge(Level level, Player player, ItemStack wandStack) {
        if (this.isFullyCharged(wandStack)) {
            return FireRechargeResult.ALREADY_FULL;
        }

        BlockPos ghastPos = findNearbyGhast(level, player.blockPosition(), GHAST_EXTRACTION_RECHARGE_RADIUS);
        if (ghastPos != null) {
            this.setCharges(wandStack, this.getMaxCharges());
            return FireRechargeResult.GHAST_EXTRACTION_SUCCESS;
        }

        return this.tryGhastTearRecharge(player, wandStack);
    }

    private static @Nullable BlockPos findNearbyGhast(Level level, BlockPos center, int radius) {
        BlockPos closestGhast = null;
        double closestDistanceSq = Double.MAX_VALUE;

        AABB searchBox = new AABB(
                center.getX() - radius, center.getY() - radius, center.getZ() - radius,
                center.getX() + radius + 1, center.getY() + radius + 1, center.getZ() + radius + 1
        );

        for (Ghast ghast : level.getEntitiesOfClass(Ghast.class, searchBox)) {
            if (!ghast.isAlive()) {
                continue;
            }

            double distanceSq = ghast.blockPosition().distSqr(center);
            if (distanceSq < closestDistanceSq) {
                closestDistanceSq = distanceSq;
                closestGhast = ghast.blockPosition().immutable();
            }
        }

        for (HappyGhast ghast : level.getEntitiesOfClass(HappyGhast.class, searchBox)) {
            if (!ghast.isAlive()) {
                continue;
            }

            double distanceSq = ghast.blockPosition().distSqr(center);
            if (distanceSq < closestDistanceSq) {
                closestDistanceSq = distanceSq;
                closestGhast = ghast.blockPosition().immutable();
            }
        }

        return closestGhast;
    }

    private FireRechargeResult tryGhastTearRecharge(Player player, ItemStack stack) {
        if (this.getCharges(stack) > GHAST_TEAR_RECHARGE_THRESHOLD) {
            return FireRechargeResult.TOO_CHARGED_FOR_GHAST_TEAR;
        }

        if (consumeArcaneFuelFromInventory(player, Items.GHAST_TEAR)) {
            this.addCharges(stack, GHAST_TEAR_RECHARGE_AMOUNT);
            return FireRechargeResult.GHAST_TEAR_SUCCESS;
        }

        return FireRechargeResult.NO_GHAST_TEAR;
    }

    @Override
    protected void sendRechargeFeedback(Player player, FireRechargeResult result) {
        switch (result) {
            case FireRechargeResult.GHAST_EXTRACTION_SUCCESS -> player.displayClientMessage(
                    Component.translatable("message." + ArcaneRelics.MOD_ID + ".fire_wand.recharge.ghast"),
                    true
            );
            case FireRechargeResult.GHAST_TEAR_SUCCESS -> player.displayClientMessage(
                    Component.translatable("message." + ArcaneRelics.MOD_ID + ".fire_wand.recharge.ghast_tear"),
                    true
            );
            case FireRechargeResult.TOO_CHARGED_FOR_GHAST_TEAR -> player.displayClientMessage(
                    Component.translatable("message." + ArcaneRelics.MOD_ID + ".fire_wand.recharge.too_charged"),
                    true
            );
            case FireRechargeResult.NO_GHAST_TEAR -> player.displayClientMessage(
                    Component.translatable("message." + ArcaneRelics.MOD_ID + ".fire_wand.recharge.no_ghast_tear"),
                    true
            );
            case FireRechargeResult.ALREADY_FULL -> player.displayClientMessage(
                    this.getFullyChargedMessage(),
                    true
            );
        }
    }
    //endregion

    //region Cast Methods
    @Override
    protected boolean performCast(Level level, Player player, ItemStack stack, float powerUpPercentage, boolean isFullyPowered) {
        int explosionPower = Math.round(Mth.lerp(powerUpPercentage, BASE_EXPLOSION_POWER, MAX_EXPLOSION_POWER));
        this.shootFireball(level, player, explosionPower);
        return true;
    }

    private void shootFireball(Level level, Player player, int explosionPower) {
        Vec3 look = player.getLookAngle();
        Vec3 fireballPos = player.getRopeHoldPosition(1);
        fireballPos = fireballPos.add(look.normalize().scale(2));

        LargeFireball fireball = new LargeFireball(level, player, look, explosionPower);
        fireball.setPos(fireballPos);
        level.addFreshEntity(fireball);
    }
    //endregion
}
