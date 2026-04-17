package org.bensam.arcanerelics.item;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface WandEnchantingSource {
    List<ItemStack> getEnchantingItems(RegistryAccess registryAccess);
    int getEnchantingLevel(ItemStack stack);
    boolean matches(ItemStack stack);
}
