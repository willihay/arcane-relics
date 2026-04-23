package org.bensam.arcanerelics.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class ModServerConfig {
    public static final int CURRENT_VERSION = 1;

    public int version = CURRENT_VERSION;
    public WandEnchantingTableConfig wandEnchantingTable = new WandEnchantingTableConfig();
    public FangWandConfig fangWand = new FangWandConfig();
    public FireballWandConfig fireballWand = new FireballWandConfig();
    public IceWandConfig iceWand = new IceWandConfig();
    public LevitationWandConfig levitationWand = new LevitationWandConfig();
    public LightningWandConfig lightningWand = new LightningWandConfig();
    public RegenerationWandConfig regenerationWand = new RegenerationWandConfig();
    public WindWandConfig windWand = new WindWandConfig();

    public static final Codec<ModServerConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("version").forGetter(ModServerConfig::version),
            WandEnchantingTableConfig.CODEC.fieldOf("wand_enchanting_table").forGetter(ModServerConfig::wandEnchantingTable),
            FangWandConfig.CODEC.fieldOf("fang_wand").forGetter(ModServerConfig::fangWand),
            FireballWandConfig.CODEC.fieldOf("fireball_wand").forGetter(ModServerConfig::fireballWand),
            IceWandConfig.CODEC.fieldOf("ice_wand").forGetter(ModServerConfig::iceWand),
            LevitationWandConfig.CODEC.fieldOf("levitation_wand").forGetter(ModServerConfig::levitationWand),
            LightningWandConfig.CODEC.fieldOf("lightning_wand").forGetter(ModServerConfig::lightningWand),
            RegenerationWandConfig.CODEC.fieldOf("regeneration_wand").forGetter(ModServerConfig::regenerationWand),
            WindWandConfig.CODEC.fieldOf("wind_wand").forGetter(ModServerConfig::windWand)
    ).apply(instance, ModServerConfig::new));

    public ModServerConfig() {}

    public ModServerConfig(
            int version,
            WandEnchantingTableConfig wandEnchantingTable,
            FangWandConfig fangWand,
            FireballWandConfig fireballWand,
            IceWandConfig iceWand,
            LevitationWandConfig levitationWand,
            LightningWandConfig lightningWand,
            RegenerationWandConfig regenerationWand,
            WindWandConfig windWand
    ) {
        this.version = version;
        this.wandEnchantingTable = wandEnchantingTable;
        this.fangWand = fangWand;
        this.fireballWand = fireballWand;
        this.iceWand = iceWand;
        this.levitationWand = levitationWand;
        this.lightningWand = lightningWand;
        this.regenerationWand = regenerationWand;
        this.windWand = windWand;
    }

    public int version() {
        return this.version;
    }

    public WandEnchantingTableConfig wandEnchantingTable() {
        return this.wandEnchantingTable;
    }

    public FangWandConfig fangWand() {
        return this.fangWand;
    }

    public FireballWandConfig fireballWand() {
        return this.fireballWand;
    }

    public IceWandConfig iceWand() {
        return this.iceWand;
    }

    public LevitationWandConfig levitationWand() {
        return this.levitationWand;
    }

    public LightningWandConfig lightningWand() {
        return this.lightningWand;
    }

    public RegenerationWandConfig regenerationWand() {
        return this.regenerationWand;
    }

    public WindWandConfig windWand() {
        return this.windWand;
    }

    public static ModServerConfig defaults() {
        return ModServerConfigDefaults.create();
    }
}
