package org.bensam.arcanerelics.item;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
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

    @Override
    public boolean isFullyCharged(Level level, ItemStack stack) {
        return false;
    }

    //region Recharge Methods
    @Override
    protected RechargeContext tryRecharge(Level level, Player player, ItemStack wandStack) {
        return new RechargeContext(false, 0, null, null);
    }

    @Override
    protected void sendRechargeFeedback(Player player, RechargeContext rechargeContext) {
        player.displayClientMessage(
                Component.translatable("message." + ArcaneRelics.MOD_ID + ".arcane_wand.recharge.no_power"),
                true
        );
    }
    //endregion

    //region Cast Methods
    @Override
    protected boolean performCast(ServerLevel level, Player player, ItemStack stack, float powerUpPercentage, boolean isFullyPowered) {
        return false;
    }
    //endregion
}
