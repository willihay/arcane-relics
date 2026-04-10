package org.bensam.arcanerelics.mixin;

import net.minecraft.world.entity.projectile.EvokerFangs;
import org.bensam.arcanerelics.item.EvokerFangsAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EvokerFangs.class)
public class EvokerFangsMixin implements EvokerFangsAccess {
    @Unique
    private float arcanerelics$fangWandDamage = 0.0F;

    @Override
    public float arcanerelics$getFangWandDamage() {
        return arcanerelics$fangWandDamage;
    }

    @Override
    public void arcanerelics$setFangWandDamage(float damage) {
        arcanerelics$fangWandDamage = damage;
    }
}
