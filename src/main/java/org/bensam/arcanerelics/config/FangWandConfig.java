package org.bensam.arcanerelics.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record FangWandConfig(
        WandBalanceConfig balance,
        int evokerExtractionRadius) {
    public static final Codec<FangWandConfig>  CODEC = RecordCodecBuilder.create(instance -> instance.group(
            WandBalanceConfig.CODEC.fieldOf("balance").forGetter(FangWandConfig::balance),
            Codec.INT.fieldOf("evoker_extraction_radius").forGetter(FangWandConfig::evokerExtractionRadius)
    ).apply(instance, FangWandConfig::new));
}
