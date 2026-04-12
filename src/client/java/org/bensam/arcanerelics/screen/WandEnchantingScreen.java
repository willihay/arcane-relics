package org.bensam.arcanerelics.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.bensam.arcanerelics.ArcaneRelics;
import org.bensam.arcanerelics.menu.WandEnchantingMenu;
import org.jspecify.annotations.NonNull;

@Environment(EnvType.CLIENT)
public class WandEnchantingScreen extends AbstractContainerScreen<WandEnchantingMenu> {
    private static final Identifier CONTAINER_BG_TEXTURE = Identifier.fromNamespaceAndPath(ArcaneRelics.MOD_ID, "textures/gui/wand_enchanting.png");
    private static final Identifier EMPTY_SLOT_LAPIS_TEXTURE = Identifier.fromNamespaceAndPath(ArcaneRelics.MOD_ID, "textures/gui/wand_enchanting_empty_slot_lapis.png");
    private static final Identifier EMPTY_SLOT_WAND_TEXTURE = Identifier.fromNamespaceAndPath(ArcaneRelics.MOD_ID, "textures/gui/wand_enchanting_empty_slot_wand.png");
    private static final Identifier ERROR_SPRITE = Identifier.fromNamespaceAndPath(ArcaneRelics.MOD_ID, "textures/gui/wand_enchanting_error.png");
    private final Player player;

    public WandEnchantingScreen(WandEnchantingMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 178;
        this.inventoryLabelY = this.imageHeight - 94;
        player = inventory.player;
    }

    private boolean hasAnyInputItems() {
        return this.menu.hasAnyInputItems();
    }

    private boolean hasMissingLapis() {
        return !this.menu.hasLapis();
    }

    private boolean hasMissingWand() {
        return !this.menu.hasWand();
    }

    private boolean hasValidRecipe() {
        return this.menu.hasValidRecipe();
    }

    @Override
    protected void init() {
        super.init();

        // Position the title.
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
        this.titleLabelY = 22;
    }

    @Override
    public void render(@NonNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.render(guiGraphics, mouseX, mouseY, delta);

        // Render item tooltips.
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float delta, int mouseX, int mouseY) {
        int xo = (this.width - this.imageWidth) / 2;
        int yo = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED,
                CONTAINER_BG_TEXTURE,
                xo,
                yo,
                0.0F,
                0.0F,
                this.imageWidth,
                this.imageHeight,
                BACKGROUND_TEXTURE_WIDTH,
                BACKGROUND_TEXTURE_HEIGHT
        );

        this.renderEmptySlots(guiGraphics, this.leftPos, this.topPos);
        this.renderErrorIcon(guiGraphics, this.leftPos, this.topPos);
    }

    protected void renderEmptySlots(GuiGraphics guiGraphics, int i, int j) {
        // blit() takes: renderLayer, texture, x, y, u, v, width, height, textureWidth, textureHeight
        if (this.hasMissingWand()) {
            guiGraphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    EMPTY_SLOT_WAND_TEXTURE,
                    i + WandEnchantingMenu.WAND_INPUT_SLOT_X,
                    j + WandEnchantingMenu.RECIPE_ROW_Y,
                    0,0, 16, 16, 16, 16
            );
        }

        if (this.hasMissingLapis()) {
            guiGraphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    EMPTY_SLOT_LAPIS_TEXTURE,
                    i + WandEnchantingMenu.LAPIS_INPUT_SLOT_X,
                    j + WandEnchantingMenu.RECIPE_ROW_Y,
                    0,0, 16, 16, 16, 16
            );
        }
    }

    protected void renderErrorIcon(GuiGraphics guiGraphics, int i, int j) {
        if (!this.hasValidRecipe() && this.hasAnyInputItems()) {
            guiGraphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    ERROR_SPRITE,
                    i + 65,
                    j + 46,
                    0, 0, 28, 21, 28, 21
            );
        }
    }

    @Override
    protected void renderLabels(@NonNull GuiGraphics guiGraphics, int i, int j) {
        super.renderLabels(guiGraphics, i, j);

        int xpCost = this.menu.getXpCost();
        if (xpCost > 0) {
            int textColor = 0xFF80FF20; // Light green
            if (!this.menu.canPickupResult(this.player)) {
                textColor = 0xFFFF6060; // Light red
            }
            Component costComponent = Component.translatable(
                    "container." + ArcaneRelics.MOD_ID + ".wand_enchanting.cost",
                    xpCost);

            int rectangleTopY = 69;
            int rectangleRightX = this.imageWidth - 8;
            int rectangleLeftX = rectangleRightX - this.font.width(costComponent) - 4;
            int textX = rectangleLeftX + 2;
            int textY = rectangleTopY + 2;
            int rectangleHeight = this.font.lineHeight + 2;
            guiGraphics.fill(
                    rectangleLeftX,
                    rectangleTopY,
                    rectangleRightX,
                    rectangleTopY + rectangleHeight,
                    0x4F000000); // Dark gray transparent
            guiGraphics.drawString(this.font, costComponent, textX, textY, textColor);
        }
    }
}
