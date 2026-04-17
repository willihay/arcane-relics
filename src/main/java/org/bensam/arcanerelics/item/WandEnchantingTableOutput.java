package org.bensam.arcanerelics.item;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface WandEnchantingTableOutput {
    List<WandEnchantingSource> getEnchantingSources();

    // Returns true if the item stack can produce the implementing class as output.
    default boolean canBeProducedOrRechargedBy(ItemStack stack) {
        return getEnchantingSources().stream().anyMatch(source -> source.matches(stack));
    }

    // Get all enchantment items that can produce the implementing class in a wand enchanting table.
    default List<ItemStack> getEnchantmentItems(RegistryAccess registryAccess) {
        return getEnchantingSources().stream()
                .flatMap(source -> source.getEnchantingItems(registryAccess).stream())
                .toList();
    }

    // Returns the "enchantment level" of the item stack if it can produce an item of the implementing class as output,
    // or 0 if it cannot.
    // Enchanted books should return the level of the enchantment on the book.
    // Potions should return their level of potency.
    // Other items should return 1.
    default int getLevelOfEnchantmentItem(ItemStack stack) {
        return getEnchantingSources().stream()
                .filter(source -> source.matches(stack))
                .mapToInt(source -> source.getEnchantingLevel(stack))
                .findFirst()
                .orElse(0);
    }
}
