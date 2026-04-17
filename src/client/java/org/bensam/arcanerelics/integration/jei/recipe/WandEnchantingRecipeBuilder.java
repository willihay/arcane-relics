package org.bensam.arcanerelics.integration.jei.recipe;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import org.bensam.arcanerelics.ModItems;
import org.bensam.arcanerelics.item.AbstractChargedWandItem;
import org.bensam.arcanerelics.item.WandEnchantingTableOutput;

import java.util.ArrayList;
import java.util.List;

public final class WandEnchantingRecipeBuilder {
    private WandEnchantingRecipeBuilder() {}

    public static List<WandEnchantingRecipe> buildAll(RegistryAccess registryAccess) {
        List<WandEnchantingRecipe> recipes = new ArrayList<>();

        for (AbstractChargedWandItem wand : ModItems.getAllOutputWands()) {
            if (wand instanceof WandEnchantingTableOutput wandOutput) {
                addRecipe(
                        recipes,
                        wandOutput.getEnchantmentItems(registryAccess),
                        new ItemStack(wand),
                        wand.getNewWandXpCost()
                );
            }
        }

        return recipes;
    }

    private static void addRecipe(
            List<WandEnchantingRecipe> recipes,
            List<ItemStack> enchantmentItems,
            ItemStack outputWand,
            int xpCost
    ) {
        recipes.add(new WandEnchantingRecipe(
                enchantmentItems,
                outputWand,
                xpCost
        ));
    }
}
