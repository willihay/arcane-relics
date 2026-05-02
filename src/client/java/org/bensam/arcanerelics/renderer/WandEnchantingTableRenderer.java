package org.bensam.arcanerelics.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.bensam.arcanerelics.blockentity.BlockEntityWandEnchantingTable;
import org.jspecify.annotations.NonNull;

public class WandEnchantingTableRenderer implements BlockEntityRenderer<BlockEntityWandEnchantingTable, WandEnchantingTableRenderState> {
    private static final float WAND_RENDER_SCALE = 1.4F;
    private final ItemModelResolver itemModelResolver;

    public WandEnchantingTableRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public @NonNull WandEnchantingTableRenderState createRenderState() {
        return new WandEnchantingTableRenderState();
    }

    @Override
    public void extractRenderState(
            @NonNull BlockEntityWandEnchantingTable blockEntity,
            @NonNull WandEnchantingTableRenderState renderState,
            float partialTick,
            @NonNull Vec3 cameraPos,
            ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, renderState, partialTick, cameraPos, crumblingOverlay);
        if (blockEntity.getLevel() != null) {
            renderState.lightCoords = LevelRenderer.getLightCoords(blockEntity.getLevel(), blockEntity.getBlockPos().above());
        }
        this.itemModelResolver.updateForTopItem(
                renderState.wand,
                blockEntity.getRenderedWand(),
                ItemDisplayContext.GROUND,
                blockEntity.getLevel(),
                null,
                (int) blockEntity.getBlockPos().asLong()
        );
    }

    @Override
    public void submit(
            WandEnchantingTableRenderState renderState,
            @NonNull PoseStack poseStack,
            @NonNull SubmitNodeCollector submitNodeCollector,
            @NonNull CameraRenderState cameraRenderState
    ) {
        if (renderState.wand.isEmpty()) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.35F, 1.02F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.scale(WAND_RENDER_SCALE, WAND_RENDER_SCALE, WAND_RENDER_SCALE);
        renderState.wand.submit(poseStack, submitNodeCollector, renderState.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }
}
