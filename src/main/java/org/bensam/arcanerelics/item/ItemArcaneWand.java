package org.bensam.arcanerelics.item;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.bensam.arcanerelics.ArcaneRelics;
import org.bensam.arcanerelics.config.ModServerConfig;
import org.bensam.arcanerelics.config.WandBalanceConfig;

public class ItemArcaneWand extends AbstractChargedWandItem {
    public static final int INITIAL_CHARGES = 0;
    private static final int MAX_CHARGES = 0;
    private static final int NORMAL_CAST_COST = 1;
    private static final int FULL_POWER_CAST_COST = 1;
    private static final int FULL_POWER_TICKS = Integer.MAX_VALUE;
    private static final int RECHARGE_AMOUNT = 0;

    public ItemArcaneWand(Properties properties, WandDefinition definition) {
        super(properties, definition);
    }

    //region Config Accessors
    @Override
    protected WandBalanceConfig getBalanceConfig() {
        return new WandBalanceConfig(
                INITIAL_CHARGES,
                MAX_CHARGES,
                NORMAL_CAST_COST,
                FULL_POWER_CAST_COST,
                FULL_POWER_TICKS,
                RECHARGE_AMOUNT
        );
    }

    @Override
    public WandBalanceConfig getBalanceConfig(ModServerConfig config) {
        return getBalanceConfig();
    }
    //endregion

    @Override
    public int getNewWandXpCost() { return 0; }

    @Override
    public int getRechargeXpCost() { return 0; }

    @Override
    protected Component getNoChargesMessage() {
        return Component.translatable("message." + ArcaneRelics.MOD_ID + ".arcane_wand.cast.no_power");
    }

    @Override
    public boolean isFullyCharged(ItemStack stack) {
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
