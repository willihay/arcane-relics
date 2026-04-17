package org.bensam.arcanerelics.item;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;

import java.util.List;

/**
 * A wand enchanting source backed by potion-bearing items with one specific potion type.
 * <p>
 * The displayed inputs include all standard item forms produced by
 * {@link AbstractChargedWandItem#getAllEffectItems(Holder)}, and the enchanting level is {@code 1} for any
 * matching potion-bearing stack.
 */
public record PotionSource(Holder<Potion> potion) implements WandEnchantingSource {
    @Override
    public List<ItemStack> getEnchantingItems(RegistryAccess registryAccess) {
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
