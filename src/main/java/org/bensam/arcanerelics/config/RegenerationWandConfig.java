package org.bensam.arcanerelics.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record RegenerationWandConfig(
        WandBalanceConfig balance,
        int ghastExtractionRadius
) {
    public static final Codec<RegenerationWandConfig>  CODEC = RecordCodecBuilder.create(instance -> instance.group(
            WandBalanceConfig.CODEC.fieldOf("balance").forGetter(RegenerationWandConfig::balance),
            Codec.INT.fieldOf("ghast_extraction_radius").forGetter(RegenerationWandConfig::ghastExtractionRadius)
    ).apply(instance, RegenerationWandConfig::new));
}
