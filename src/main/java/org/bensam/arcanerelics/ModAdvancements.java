package org.bensam.arcanerelics;

import net.minecraft.advancements.CriteriaTriggers;
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
        enchantWandTrigger = CriteriaTriggers.register(ArcaneRelics.MOD_ID + ":enchant_wand", new WandEnchantTrigger());
        rechargeWandTrigger = CriteriaTriggers.register(ArcaneRelics.MOD_ID + ":recharge_wand", new WandRechargeTrigger());
    }
}
