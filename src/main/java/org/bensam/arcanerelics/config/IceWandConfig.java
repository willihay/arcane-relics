package org.bensam.arcanerelics.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record IceWandConfig(
        WandBalanceConfig balance,
        int strayExtractionRadius
) {
    public static final Codec<IceWandConfig>  CODEC = RecordCodecBuilder.create(instance -> instance.group(
            WandBalanceConfig.CODEC.fieldOf("balance").forGetter(IceWandConfig::balance),
            Codec.INT.fieldOf("stray_extraction_radius").forGetter(IceWandConfig::strayExtractionRadius)
    ).apply(instance, IceWandConfig::new));
}
