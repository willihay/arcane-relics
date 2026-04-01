package org.bensam.arcanerelics.item;

import net.minecraft.world.item.ItemStack;

public interface WandEnchantingTableOutput {
    // Returns true if the item stack can produce the implementing class as output.
    boolean canBeProducedOrRechargedBy(ItemStack stack);

    // Returns the "enchantment level" of the item stack that produces the implementing class as output.
    // Enchanted books should return the level of the enchantment on the book.
    // Potions should return their level of potency.
    // Other items should return 1.
    int getLevelOfEnchantmentItem(ItemStack stack);
}
