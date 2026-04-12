package org.bensam.arcanerelics.integration.jei.recipe;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.bensam.arcanerelics.ArcaneRelics;
import org.bensam.arcanerelics.ModItems;
import org.bensam.arcanerelics.item.AbstractChargedWandItem;
import org.bensam.arcanerelics.item.WandEnchantingTableOutput;

import java.util.ArrayList;
import java.util.List;

public final class WandEnchantingRecipeBuilder {
    private WandEnchantingRecipeBuilder() {}

    public static List<WandEnchantingRecipe> buildAll() {
        List<WandEnchantingRecipe> recipes = new ArrayList<>();

        Level level = Minecraft.getInstance().level;
        if (level == null) {
            ArcaneRelics.LOGGER.error("Minecraft level is null, couldn't build wand enchanting recipes");
            return recipes;
        }

        for (AbstractChargedWandItem wand : ModItems.getAllOutputWands()) {
            if (wand instanceof WandEnchantingTableOutput wandOutput) {
                addRecipe(recipes, wandOutput.getEnchantmentItems(level), new ItemStack(wand), wand.getNewWandXpCost());
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
