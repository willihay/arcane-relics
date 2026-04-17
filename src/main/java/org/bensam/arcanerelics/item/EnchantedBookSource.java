package org.bensam.arcanerelics.item;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.List;

/**
 * A wand enchanting source backed by enchanted books containing a specific enchantment.
 * <p>
 * The enchanting level is the level of the configured enchantment found on the matching book, and the
 * displayed inputs include one enchanted book for each supported level from the enchantment registry.
 */
public record EnchantedBookSource(ResourceKey<Enchantment> enchantmentKey) implements WandEnchantingSource {
    @Override
    public List<ItemStack> getEnchantingItems(RegistryAccess registryAccess) {
        return AbstractChargedWandItem.getAllEnchantedBooks(registryAccess, enchantmentKey);
    }

    @Override
    public int getEnchantingLevel(ItemStack stack) {
        return AbstractChargedWandItem.getEnchantmentLevel(stack, enchantmentKey);
    }

    @Override
    public boolean matches(ItemStack stack) {
        return stack.is(Items.ENCHANTED_BOOK) && AbstractChargedWandItem.hasEnchantment(stack, enchantmentKey);
    }
}
