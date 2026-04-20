package org.bensam.arcanerelics.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record LightningWandConfig(
        WandBalanceConfig balance,
        int lightningRodExtractionRadius
) {
    public static final Codec<LightningWandConfig>  CODEC = RecordCodecBuilder.create(instance -> instance.group(
            WandBalanceConfig.CODEC.fieldOf("balance").forGetter(LightningWandConfig::balance),
            Codec.INT.fieldOf("lightning_rod_extraction_radius").forGetter(LightningWandConfig::lightningRodExtractionRadius)
    ).apply(instance, LightningWandConfig::new));
}
