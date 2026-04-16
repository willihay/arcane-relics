package org.bensam.arcanerelics.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

public record FixedItemSource(Item item) implements WandEnchantingSource {
    @Override
    public List<ItemStack> getEnchantingItems(Level level) {
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
