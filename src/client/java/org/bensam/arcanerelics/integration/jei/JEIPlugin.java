package org.bensam.arcanerelics.integration.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.bensam.arcanerelics.ArcaneRelics;
import org.bensam.arcanerelics.ModBlocks;
import org.bensam.arcanerelics.ModMenus;
import org.bensam.arcanerelics.integration.jei.category.WandEnchantingCategory;
import org.bensam.arcanerelics.integration.jei.recipe.WandEnchantingRecipe;
import org.bensam.arcanerelics.integration.jei.recipe.WandEnchantingRecipeBuilder;
import org.bensam.arcanerelics.menu.WandEnchantingMenu;
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
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            ArcaneRelics.LOGGER.error("Minecraft level is null, couldn't build wand enchanting recipes");
            return;
        }

        List<WandEnchantingRecipe> recipes = WandEnchantingRecipeBuilder.buildAll(minecraft.level.registryAccess());
        registration.addRecipes(WandEnchantingRecipe.TYPE, recipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(WandEnchantingRecipe.TYPE, new ItemStack(ModBlocks.WAND_ENCHANTING_TABLE.get()));
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(
                WandEnchantingMenu.class,
                ModMenus.WAND_ENCHANTING_MENU.get(),
                WandEnchantingRecipe.TYPE,
                0,
                3,
                4,
                36);
    }
}
