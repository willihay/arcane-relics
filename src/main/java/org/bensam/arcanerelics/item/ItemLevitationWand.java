package org.bensam.arcanerelics.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.bensam.arcanerelics.ArcaneRelics;
import org.jspecify.annotations.NonNull;

public class ItemLevitationWand extends AbstractChargedWandItem<ItemLevitationWand.LevitationRechargeResult> implements WandEnchantingTableOutput {
    public static final int INITIAL_CHARGES = 20;
    public static final int MAX_CHARGES = 40;
    private static final int RECHARGE_AMOUNT = 20;
    private static final int WAND_RANGE = 60;

    private static final int NORMAL_CAST_COST = 1;
    private static final int FULL_POWER_CAST_COST = 1;
    private static final int FULL_POWER_TICKS = 20;

    private static final int SHULKER_EXTRACTION_RECHARGE_RADIUS = 8;

    public ItemLevitationWand(Properties properties) { super(properties, INITIAL_CHARGES, MAX_CHARGES); }

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
    public int getRechargeChargeAmount() {
        return RECHARGE_AMOUNT;
    }
    //endregion

    //region Recharge Methods
    public enum LevitationRechargeResult implements RechargeResult {
        ALREADY_FULL,
        SHULKER_EXTRACTION_SUCCESS,
        NO_MOB_FUEL
    }

    @Override
    public boolean canBeProducedOrRechargedBy(ItemStack stack) {
        return stack.is(Items.SHULKER_SHELL);
    }

    @Override
    protected RechargeContext<LevitationRechargeResult> tryRecharge(Level level, Player player, ItemStack wandStack) {
        if (this.isFullyCharged(wandStack)) {
            return new RechargeContext<>(LevitationRechargeResult.ALREADY_FULL, null);
        }

        RechargeContext<LevitationRechargeResult> mobFuelSearchResult = findNearbyMobFuel(level, player.blockPosition());
        if (mobFuelSearchResult.result() != LevitationRechargeResult.NO_MOB_FUEL) {
            this.setCharges(wandStack, this.getMaxCharges());
        }

        return mobFuelSearchResult;
    }

    protected static RechargeContext<LevitationRechargeResult> findNearbyMobFuel(Level level, BlockPos center) {
        BlockPos closestMob = findClosestMobOfType(level, center, SHULKER_EXTRACTION_RECHARGE_RADIUS, Shulker.class);
        if (closestMob != null) {
            return new RechargeContext<>(LevitationRechargeResult.SHULKER_EXTRACTION_SUCCESS, closestMob);
        }

        return new RechargeContext<>(LevitationRechargeResult.NO_MOB_FUEL, null);
    }

    @Override
    protected void playRechargeContextEffects(
            ServerLevel level,
            Player player,
            InteractionHand hand,
            ItemStack stack,
            @NonNull RechargeContext<LevitationRechargeResult> rechargeContext
    ) {
        if (rechargeContext.result() == LevitationRechargeResult.SHULKER_EXTRACTION_SUCCESS) {
            // Play sound effects.
            level.playSound(
                    null,
                    player.blockPosition(),
                    SoundEvents.SHULKER_AMBIENT,
                    SoundSource.PLAYERS,
                    1.0f, // volume
                    1.0f // pitch
            );

            // Create recharge particle effects.
            if (rechargeContext.sourcePos() != null) {
                // Create recharge particle trail from mob to player's wand.
                Vec3 mobStart = Vec3.atCenterOf(rechargeContext.sourcePos());
                Vec3 wandTip = getWandTipPosition(player, hand);
                this.spawnParticleTrail(level, ParticleTypes.ENCHANTED_HIT, mobStart, wandTip, 12, 5, 0.04);
            }
        }

        // Play default effects.
        super.playRechargeContextEffects(level, player, hand, stack, rechargeContext);
    }

    @Override
    protected void sendRechargeFeedback(Player player, LevitationRechargeResult result) {
        switch (result) {
            case ALREADY_FULL -> player.displayClientMessage(
                    this.getFullyChargedMessage(),
                    true
            );
            case SHULKER_EXTRACTION_SUCCESS -> player.displayClientMessage(
                    Component.translatable("message." + ArcaneRelics.MOD_ID + ".levitation_wand.recharge.shulker"),
                    true
            );
            case NO_MOB_FUEL -> player.displayClientMessage(
                    Component.translatable("message." + ArcaneRelics.MOD_ID + ".levitation_wand.recharge.no_mob_fuel"),
                    true
            );
        }
    }
    //endregion

    //region Cast Methods

    @Override
    protected boolean performCast(Level level, Player player, ItemStack stack, float powerUpPercentage, boolean isFullyPowered) {
        TargetResult target = getTarget(player, WAND_RANGE);
        if (target == null || target.entity() == null || !(target.entity() instanceof LivingEntity targetEntity)) {
            return false;
        }

        // Fire shulker bullet.
        Vec3 look = player.getLookAngle().normalize();
        Vec3 projectilePos = player.getEyePosition(1.0f).add(look.scale(2));
        ShulkerBullet projectile = new ShulkerBullet(level, player, targetEntity, Direction.UP.getAxis());

        projectile.setPos(projectilePos);
        level.addFreshEntity(projectile);
        return true;
    }

    @Override
    protected void playCastSuccessEffects(ServerLevel level, Player player, ItemStack stack) {
        level.playSound(
                null,
                player.blockPosition(),
                SoundEvents.SHULKER_SHOOT,
                SoundSource.PLAYERS,
                1.0f, // volume
                1.0f // pitch
        );
    }
//endregion
}
