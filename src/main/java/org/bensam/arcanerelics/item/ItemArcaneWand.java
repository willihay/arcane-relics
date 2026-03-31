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

public class ItemArcaneWand extends AbstractChargedWandItem {

    public ItemArcaneWand(Properties properties, WandDefinition definition) {
        super(properties, definition);
    }

    @Override
    public int getNewWandXpCost() { return 0; }

    @Override
    public int getRechargeXpCost() { return 0; }

    @Override
    protected Component getNoChargesMessage() {
        return Component.translatable("message." + ArcaneRelics.MOD_ID + ".arcane_wand.cast.no_power");
    }

    //region Recharge Methods
    @Override
    protected RechargeContext tryRecharge(Level level, Player player, ItemStack wandStack) {
        return new RechargeContext(RechargeResult.RECHARGE_FAIL, 0, null, "arcane_wand.recharge.no_power");
    }

    @Override
    protected void playRechargeEffects(
            ServerLevel level,
            Player player,
            InteractionHand hand,
            ItemStack stack,
            RechargeContext rechargeContext
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

        super.playRechargeEffects(level, player, hand, stack, rechargeContext);
    }
    //endregion

    //region Cast Methods
    @Override
    protected boolean performCast(Level level, Player player, ItemStack stack, float powerUpPercentage, boolean isFullyPowered) {
        return false;
    }
    //endregion
}
