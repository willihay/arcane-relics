package org.bensam.arcanerelics;

import net.minecraft.advancements.CriteriaTriggers;
import org.bensam.arcanerelics.advancement.WandEnchantTrigger;

import java.util.function.Supplier;

public class ModAdvancements {
    private ModAdvancements() {}

    private static WandEnchantTrigger enchantWandTrigger;
    public static final Supplier<WandEnchantTrigger> ENCHANT_WAND_TRIGGER = () -> enchantWandTrigger;

    public static void initialize() {
        enchantWandTrigger = CriteriaTriggers.register(ArcaneRelics.MOD_ID + ":enchant_wand", new WandEnchantTrigger());
    }
}
