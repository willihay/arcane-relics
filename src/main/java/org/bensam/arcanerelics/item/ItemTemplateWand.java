package org.bensam.arcanerelics.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

public class ItemTemplateWand extends AbstractChargedWandItem implements WandEnchantingTableOutput {

    public ItemTemplateWand(Properties properties, WandDefinition definition) {
        super(properties, definition);
    }

    @Override
    public boolean canBeProducedOrRechargedBy(ItemStack stack) {
        return false;
    }

    @Override
    public List<ItemStack> getEnchantmentItems(Level level) {
        return List.of();
    }

    @Override
    public int getLevelOfEnchantmentItem(ItemStack stack) {
        return this.canBeProducedOrRechargedBy(stack) ? 1 : 0; // change this to check for stack item's level of enchantment or potency if applicable
    }

    //region Recharge Methods
    @Override
    protected RechargeContext tryRecharge(Level level, Player player, ItemStack wandStack) {
        if (this.isFullyCharged(wandStack)) {
            return new RechargeContext(RechargeResult.ALREADY_FULL, 0, null, this.getAlreadyFullMessagePath());
        }

        return new RechargeContext(RechargeResult.RECHARGE_FAIL, 0, null, "template_wand.recharge.fail");
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

            // Create recharge particle effects.
            if (rechargeContext.sourcePos() != null) {

            }
        }

        // Play default effects.
        super.playRechargeEffects(level, player, hand, stack, rechargeContext);
    }
    //endregion

    //region Cast Methods

    @Override
    protected boolean performCast(ServerLevel level, Player player, ItemStack stack, float powerUpPercentage, boolean isFullyPowered) {
        return false;
    }

    @Override
    protected void playCastSuccessEffects(ServerLevel level, Player player, ItemStack stack) {

    }
    //endregion
}
