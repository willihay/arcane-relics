package org.bensam.arcanerelics.item;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface WandEnchantingTableOutput {
    /**
     * Returns the complete set of catalyst sources that can produce or recharge this wand in the Wand Enchanting Table.
     * <p>
     * Implementations should treat this as a declarative list of valid sources. The default methods in this interface
     * derive matching, display items, and enchanting level from this list, so the returned sources should contain the
     * full supported behavior for the wand.
     */
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
    // Potions and other items should return 1.
    default int getLevelOfEnchantmentItem(ItemStack stack) {
        return getEnchantingSources().stream()
                .filter(source -> source.matches(stack))
                .mapToInt(source -> source.getEnchantingLevel(stack))
                .findFirst()
                .orElse(0);
    }
}
