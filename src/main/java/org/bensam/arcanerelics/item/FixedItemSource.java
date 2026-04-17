package org.bensam.arcanerelics.item;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * A wand enchanting source backed by one specific item stack type.
 * <p>
 * Matching is a simple item check, the displayed inputs contain a single stack of that item, and the
 * enchanting level is {@code 1} when matched.
 */
public record FixedItemSource(Item item) implements WandEnchantingSource {
    @Override
    public List<ItemStack> getEnchantingItems(RegistryAccess registryAccess) {
        return List.of(new ItemStack(item));
    }

    @Override
    public int getEnchantingLevel(ItemStack stack) {
        return matches(stack) ? 1 : 0;
    }

    @Override
    public boolean matches(ItemStack stack) {
        return stack.is(item);
    }
}
