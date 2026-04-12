package org.bensam.arcanerelics.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

public interface WandEnchantingTableOutput {
    // Returns true if the item stack can produce the implementing class as output.
    boolean canBeProducedOrRechargedBy(ItemStack stack);

    // Get all enchantment items that can produce the implementing class in a wand enchanting table.
    List<ItemStack> getEnchantmentItems(Level level);

    // Returns the "enchantment level" of the item stack if it can produce an item of the implementing class as output,
    // or 0 if it cannot.
    // Enchanted books should return the level of the enchantment on the book.
    // Potions should return their level of potency.
    // Other items should return 1.
    int getLevelOfEnchantmentItem(ItemStack stack);
}
