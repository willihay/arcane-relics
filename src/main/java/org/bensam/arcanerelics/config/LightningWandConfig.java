package org.bensam.arcanerelics.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class LightningWandConfig {
    public WandBalanceConfig balance = new WandBalanceConfig();
    public int lightningRodExtractionRadius;

    public static final Codec<LightningWandConfig>  CODEC = RecordCodecBuilder.create(instance -> instance.group(
            WandBalanceConfig.CODEC.fieldOf("balance").forGetter(LightningWandConfig::balance),
            Codec.INT.fieldOf("lightning_rod_extraction_radius").forGetter(LightningWandConfig::lightningRodExtractionRadius)
    ).apply(instance, LightningWandConfig::new));

    public LightningWandConfig() {}

    public LightningWandConfig(WandBalanceConfig balance, int lightningRodExtractionRadius) {
        this.balance = balance;
        this.lightningRodExtractionRadius = lightningRodExtractionRadius;
    }

    public WandBalanceConfig balance() {
        return this.balance;
    }

    public int lightningRodExtractionRadius() {
        return this.lightningRodExtractionRadius;
    }
}
