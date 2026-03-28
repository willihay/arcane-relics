package org.bensam.arcanerelics.item;

import net.minecraft.world.item.ItemStack;

public interface WandEnchantingTableOutput {
    boolean canBeProducedOrRechargedBy(ItemStack stack);
}
