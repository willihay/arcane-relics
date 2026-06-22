package org.bensam.arcanerelics;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import org.bensam.arcanerelics.advancement.WandEnchantTrigger;
import org.bensam.arcanerelics.advancement.WandRechargeTrigger;

import java.util.function.Supplier;

public class ModAdvancements {
    private ModAdvancements() {}

    private static WandEnchantTrigger enchantWandTrigger;
    private static WandRechargeTrigger rechargeWandTrigger;

    public static final Supplier<WandEnchantTrigger> ENCHANT_WAND_TRIGGER = () -> enchantWandTrigger;
    public static final Supplier<WandRechargeTrigger> RECHARGE_WAND_TRIGGER = () -> rechargeWandTrigger;

    public static void initialize() {
        enchantWandTrigger = Registry.register(
                BuiltInRegistries.TRIGGER_TYPES,
                Identifier.fromNamespaceAndPath(ArcaneRelics.MOD_ID, "enchant_wand").toString(),
                new WandEnchantTrigger());

        rechargeWandTrigger = Registry.register(
                BuiltInRegistries.TRIGGER_TYPES,
                Identifier.fromNamespaceAndPath(ArcaneRelics.MOD_ID, "recharge_wand").toString(),
                new WandRechargeTrigger());
    }
}
