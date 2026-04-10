package org.bensam.arcanerelics.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.bensam.arcanerelics.ArcaneRelics;
import org.bensam.arcanerelics.item.AbstractChargedWandItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MinecraftClientMixin {
    @Inject(
            method = "startAttack",
            at = @At("HEAD")
    )
    private void arcanerelics$showWandChargesOnAttack(CallbackInfoReturnable<Boolean> cir) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }

        ItemStack stack = mc.player.getMainHandItem();
        if (!(stack.getItem() instanceof AbstractChargedWandItem wand)) {
            return;
        }

        mc.player.displayClientMessage(
                Component.translatable(
                        "message." + ArcaneRelics.MOD_ID + ".wand.charges.remaining",
                        stack.getHoverName(),
                        wand.getCharges(stack)
                ),
                true
        );
    }
}
