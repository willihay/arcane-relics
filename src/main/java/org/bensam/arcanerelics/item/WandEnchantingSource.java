package org.bensam.arcanerelics.item;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Describes one valid catalyst source for producing or recharging a wand in the Wand Enchanting Table.
 * <p>
 * Implementations should be small, reusable matchers that answer three questions for a single source type:
 * whether a stack matches, which item stacks should be shown in recipe displays, and what enchanting level the
 * source contributes when matched.
 * <p>
 * Contract notes:
 * <ul>
 *     <li>{@link #matches(ItemStack)} and {@link #getEnchantingLevel(ItemStack)} should stay consistent.</li>
 *     <li>{@link #getEnchantingLevel(ItemStack)} should return {@code 0} for non-matching stacks.</li>
 *     <li>{@link #getEnchantingItems(RegistryAccess)} should return representative display inputs for JEI and
 *     other recipe-like views, not mutate game state.</li>
 * </ul>
 */
public interface WandEnchantingSource {
    List<ItemStack> getEnchantingItems(RegistryAccess registryAccess);
    int getEnchantingLevel(ItemStack stack);
    boolean matches(ItemStack stack);
}
