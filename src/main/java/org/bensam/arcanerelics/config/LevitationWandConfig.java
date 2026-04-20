package org.bensam.arcanerelics.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record LevitationWandConfig(
        WandBalanceConfig balance,
        int shulkerExtractionRadius
) {
    public static final Codec<LevitationWandConfig>  CODEC = RecordCodecBuilder.create(instance -> instance.group(
            WandBalanceConfig.CODEC.fieldOf("balance").forGetter(LevitationWandConfig::balance),
            Codec.INT.fieldOf("shulker_extraction_radius").forGetter(LevitationWandConfig::shulkerExtractionRadius)
    ).apply(instance, LevitationWandConfig::new));
}
