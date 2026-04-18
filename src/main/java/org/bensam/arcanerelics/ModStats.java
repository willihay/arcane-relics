package org.bensam.arcanerelics;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;

public class ModStats {
    private ModStats() {}

    private static Identifier wandsEnchanted;
    private static Identifier wandsRecharged;

    public static void initialize() {
        wandsEnchanted = makeCustomStat("wands_enchanted", StatFormatter.DEFAULT);
        wandsRecharged = makeCustomStat("wands_recharged", StatFormatter.DEFAULT);
    }

    public static Stat<Identifier> getWandsEnchantedStat() {
        return Stats.CUSTOM.get(wandsEnchanted);
    }

    public static Stat<Identifier> getWandsRechargedStat() {
        return Stats.CUSTOM.get(wandsRecharged);
    }

    private static Identifier makeCustomStat(String name, StatFormatter statFormatter) {
        Identifier id = Identifier.fromNamespaceAndPath(ArcaneRelics.MOD_ID, name);
        Registry.register(BuiltInRegistries.CUSTOM_STAT, id.getPath(), id);
        Stats.CUSTOM.get(id, statFormatter);
        return id;
    }
}
