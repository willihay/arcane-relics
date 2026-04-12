package org.bensam.arcanerelics.integration.jei.category;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.bensam.arcanerelics.ArcaneRelics;
import org.bensam.arcanerelics.ModBlocks;
import org.bensam.arcanerelics.ModItems;
import org.bensam.arcanerelics.integration.jei.recipe.WandEnchantingRecipe;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class WandEnchantingCategory implements IRecipeCategory<WandEnchantingRecipe> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            ArcaneRelics.MOD_ID,
            "textures/gui/jei/wand_enchanting.png"
    );

    private static final int WIDTH = 116;
    private static final int HEIGHT = 44;

    private final IDrawable background;
    private final IDrawable icon;

    public WandEnchantingCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(TEXTURE, 0, 0, WIDTH, HEIGHT);
        this.icon = guiHelper.createDrawableIngredient(
                VanillaTypes.ITEM_STACK,
                new ItemStack(ModBlocks.WAND_ENCHANTING_TABLE.get())
        );
    }

    @Override
    public @NonNull IRecipeType<WandEnchantingRecipe> getRecipeType() {
        return WandEnchantingRecipe.TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("container." + ArcaneRelics.MOD_ID + ".wand_enchanting.title");
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, WandEnchantingRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 5, 12)
                .addItemStacks(ModItems.getAllWands());

        builder.addSlot(RecipeIngredientRole.INPUT, 23, 12)
                .addItemStacks(recipe.enchantmentItems());

        builder.addSlot(RecipeIngredientRole.INPUT, 41, 12)
                .add(new ItemStack(Items.LAPIS_LAZULI));

        builder.addSlot(RecipeIngredientRole.OUTPUT, 95, 12)
                .add(recipe.outputWand());
    }

    @Override
    public void draw(
            WandEnchantingRecipe recipe,
            IRecipeSlotsView recipeSlotsView,
            GuiGraphics guiGraphics,
            double mouseX,
            double mouseY
    ) {
        background.draw(guiGraphics);

        guiGraphics.drawString(
                Minecraft.getInstance().font,
                Component.translatable("container." + ArcaneRelics.MOD_ID + ".wand_enchanting.cost", recipe.xpCost()),
                12,
                34,
                0xFF80FF20
        );
    }
}
