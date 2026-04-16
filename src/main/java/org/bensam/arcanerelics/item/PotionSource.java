package org.bensam.arcanerelics.item;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.Level;

import java.util.List;

public record PotionSource(Holder<Potion> potion) implements WandEnchantingSource {
    @Override
    public List<ItemStack> getEnchantingItems(Level level) {
        return AbstractChargedWandItem.getAllEffectItems(potion);
    }

    @Override
    public int getEnchantingLevel(ItemStack stack) {
        return matches(stack) ? 1 : 0;
    }

    @Override
    public boolean matches(ItemStack stack) {
        return AbstractChargedWandItem.hasPotionEffect(stack, potion);
    }
}
