package org.bensam.arcanerelics.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ModServerConfig(
        WandEnchantingTableConfig wandEnchantingTable,
        FangWandConfig fangWand,
        FireballWandConfig fireballWand,
        IceWandConfig iceWand,
        LevitationWandConfig levitationWand,
        LightningWandConfig lightningWand,
        RegenerationWandConfig regenerationWand,
        WindWandConfig windWand
) {
    public static final Codec<ModServerConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            WandEnchantingTableConfig.CODEC.fieldOf("wand_enchanting_table").forGetter(ModServerConfig::wandEnchantingTable),
            FangWandConfig.CODEC.fieldOf("fang_wand").forGetter(ModServerConfig::fangWand),
            FireballWandConfig.CODEC.fieldOf("fireball_wand").forGetter(ModServerConfig::fireballWand),
            IceWandConfig.CODEC.fieldOf("ice_wand").forGetter(ModServerConfig::iceWand),
            LevitationWandConfig.CODEC.fieldOf("levitation_wand").forGetter(ModServerConfig::levitationWand),
            LightningWandConfig.CODEC.fieldOf("lightning_wand").forGetter(ModServerConfig::lightningWand),
            RegenerationWandConfig.CODEC.fieldOf("regeneration_wand").forGetter(ModServerConfig::regenerationWand),
            WindWandConfig.CODEC.fieldOf("wind_wand").forGetter(ModServerConfig::windWand)
    ).apply(instance, ModServerConfig::new));

    public static ModServerConfig defaults() {
        return ModServerConfigDefaults.create();
    }
}
