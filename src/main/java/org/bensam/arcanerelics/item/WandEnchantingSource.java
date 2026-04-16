package org.bensam.arcanerelics.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

public interface WandEnchantingSource {
    List<ItemStack> getEnchantingItems(Level level);
    int getEnchantingLevel(ItemStack stack);
    boolean matches(ItemStack stack);
}
