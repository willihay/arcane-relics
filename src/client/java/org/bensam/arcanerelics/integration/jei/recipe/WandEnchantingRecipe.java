package org.bensam.arcanerelics.integration.jei.recipe;

import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.world.item.ItemStack;
import org.bensam.arcanerelics.ArcaneRelics;

import java.util.List;

public record WandEnchantingRecipe(
        List<ItemStack> enchantmentItems,
        ItemStack outputWand,
        int xpCost
) {
    public static final IRecipeType<WandEnchantingRecipe> TYPE =
            IRecipeType.create(ArcaneRelics.MOD_ID, "wand_enchanting", WandEnchantingRecipe.class);
}
