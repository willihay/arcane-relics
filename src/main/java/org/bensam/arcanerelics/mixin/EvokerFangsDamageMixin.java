package org.bensam.arcanerelics.mixin;

import net.minecraft.world.entity.projectile.EvokerFangs;
import org.bensam.arcanerelics.item.EvokerFangsAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(EvokerFangs.class)
public class EvokerFangsDamageMixin {
    @ModifyArg(
            method = "dealDamageTo",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z"
            ),
            index = 2
    )
    private float arcanerelics$modifyFangDamage(float originalDamage) {
        EvokerFangs fangs = (EvokerFangs) (Object) this;
        if (!(fangs instanceof EvokerFangsAccess access)) {
            return originalDamage;
        }

        return access.arcanerelics$getFangWandDamage();
    }
}
