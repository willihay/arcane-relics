package org.bensam.arcanerelics.integration.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.bensam.arcanerelics.ArcaneRelics;
import org.bensam.arcanerelics.ModBlocks;
import org.bensam.arcanerelics.integration.jei.category.WandEnchantingCategory;
import org.bensam.arcanerelics.integration.jei.recipe.WandEnchantingRecipe;
import org.bensam.arcanerelics.integration.jei.recipe.WandEnchantingRecipeBuilder;
import org.jspecify.annotations.NonNull;

import java.util.List;

@JeiPlugin
public class JEIPlugin implements IModPlugin {
    private static final Identifier PLUGIN_UID = Identifier.fromNamespaceAndPath(ArcaneRelics.MOD_ID, "jei_plugin");

    @Override
    public @NonNull Identifier getPluginUid() {
        return PLUGIN_UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new WandEnchantingCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<WandEnchantingRecipe> recipes = WandEnchantingRecipeBuilder.buildAll();
        registration.addRecipes(WandEnchantingRecipe.TYPE, recipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(WandEnchantingRecipe.TYPE, new ItemStack(ModBlocks.WAND_ENCHANTING_TABLE.get()));
    }

    // TODO: Create and register a transfer handler for the Wand Enchanting Table.
    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        //registration.addRecipeTransferHandler();
    }
}
