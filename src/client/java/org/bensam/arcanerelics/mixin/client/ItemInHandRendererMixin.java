package org.bensam.arcanerelics.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.bensam.arcanerelics.item.AbstractChargedWandItem;
import org.bensam.arcanerelics.renderer.WandClientState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {
    // Inject right before the call to this.renderItem(...) in ItemInHandRenderer::renderArmWithItem(...)
    @Inject(
            method = "renderArmWithItem(Lnet/minecraft/client/player/AbstractClientPlayer;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V",
                    shift = At.Shift.BEFORE
            )
    )
    private void arcanerelics$modifyHeldWandTransform(
            AbstractClientPlayer player,
            float pitch,
            float partialTick,
            InteractionHand hand,
            float swingProgress,
            ItemStack stack,
            float equipProgress,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            int packedLight,
            CallbackInfo ci
    ) {
        if (!(stack.getItem() instanceof AbstractChargedWandItem)) {
            return;
        }

        InteractionHand activeHand = WandClientState.CAST_ANIMATION.getActiveHand();

        // Only animate the hand that is actually casting.
        if (activeHand != null && activeHand != hand) {
            return;
        }

        // Release animation: tilt the wand forward while keeping it attached to the hand.
        float p = WandClientState.CAST_ANIMATION.getReleaseProgress();
        if (WandClientState.CAST_ANIMATION.isCasting() && p > 0.0f) {
            // Calculate release tilt angle with an easing function (-2p^3 + 3p^2) that:
            // - starts gently
            // - speeds up in the middle
            // - slows down towards the end
            float eased = p * p * (3.0f - 2.0f * p);

            // Interpolate the result between 0 and 65 degrees.
            float tilt = Mth.lerp(eased, 0.0f, 65.0f);

            // Tilt the wand forward (around X-axis), rotating near the grip point.
            poseStack.translate(0.0D, -0.02D, -0.08D);
            poseStack.mulPose(Axis.XP.rotationDegrees(-tilt));
            poseStack.translate(0.0D, 0.02D, 0.08D);
            return;
        }

        // Power-up animation: make the wand tip wobble in a small circle while the grip stays in hand.
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && minecraft.player.isUsingItem()) {
            long gameTick = minecraft.level == null ? 0L : minecraft.level.getGameTime();
            float framePartialTick = minecraft.getFrameTimeNs() / 50_000_000.0f;
            float time = gameTick + framePartialTick;
            float phase = time * 0.35f; // scale game time by spin speed to get the animation phase

            // Tiny wobble around the wand's anchored pose.
            float pitchWobble = (float) Math.sin(phase) * 4.5f;
            float yawWobble = (float) Math.cos(phase) * 4.5f;
            float rollWobble = (float) Math.sin(phase * 0.5f) * 2.0f;

            // Apply local rotations.
            poseStack.mulPose(Axis.YP.rotationDegrees(yawWobble));
            poseStack.mulPose(Axis.XP.rotationDegrees(pitchWobble));
            poseStack.mulPose(Axis.ZP.rotationDegrees(rollWobble));
        }
    }
}
