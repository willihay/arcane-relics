package org.bensam.arcanerelics.item;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.bensam.arcanerelics.ArcaneRelics;
import org.jspecify.annotations.NonNull;

public class ItemArcaneWand extends AbstractChargedWandItem<ItemArcaneWand.ArcaneRechargeResult> {
    public static final int INITIAL_CHARGES = 0;
    public static final int MAX_CHARGES = 0;

    private static final int NORMAL_CAST_COST = 1;
    private static final int FULL_POWER_TICKS = Integer.MAX_VALUE;
    private static final int FULL_POWER_CAST_COST = 1;

    public ItemArcaneWand(Properties properties) { super(properties, 0, 0); }

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
    public Component getNoChargesMessage() {
        return Component.translatable("message." + ArcaneRelics.MOD_ID + ".arcane_wand.cast.no_power");
    }
    //endregion

    //region Recharge Methods
    public enum ArcaneRechargeResult implements RechargeResult {
        NO_POWER
    }

    @Override
    protected RechargeContext<ArcaneRechargeResult> tryRecharge(Level level, Player player, ItemStack wandStack) {
        return new RechargeContext<>(ArcaneRechargeResult.NO_POWER, null);
    }

    @Override
    protected void playRechargeContextEffects(
            ServerLevel level,
            Player player,
            InteractionHand hand,
            ItemStack stack, @NonNull RechargeContext<ArcaneRechargeResult> rechargeContext
    ) {
        // Create recharge fizzle particles.
        Vec3 wandTip = getWandTipPosition(player, hand);
        level.sendParticles(
                ParticleTypes.ELECTRIC_SPARK,
                wandTip.x, wandTip.y, wandTip.z, // position
                5, // # of particles
                0.03,0.03,0.03, // particle spread
                0.02 // particle speed
        );

        super.playRechargeContextEffects(level, player, hand, stack, rechargeContext);
    }

    @Override
    protected void sendRechargeFeedback(Player player, ArcaneRechargeResult result) {
        player.displayClientMessage(
                Component.translatable("message." + ArcaneRelics.MOD_ID + ".arcane_wand.recharge.no_power"),
                true
        );
    }
    //endregion

    //region Cast Methods
    @Override
    protected boolean performCast(Level level, Player player, ItemStack stack, float powerUpPercentage, boolean isFullyPowered) {
        return false;
    }
    //endregion
}
