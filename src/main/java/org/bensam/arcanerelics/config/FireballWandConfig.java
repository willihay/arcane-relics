package org.bensam.arcanerelics.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record FireballWandConfig(
        WandBalanceConfig balance,
        int blazeExtractionRadius,
        int ghastExtractionRadius,
        boolean allowAimAssist
) {
    public static final Codec<FireballWandConfig>  CODEC = RecordCodecBuilder.create(instance -> instance.group(
            WandBalanceConfig.CODEC.fieldOf("balance").forGetter(FireballWandConfig::balance),
            Codec.INT.fieldOf("blaze_extraction_radius").forGetter(FireballWandConfig::blazeExtractionRadius),
            Codec.INT.fieldOf("ghast_extraction_radius").forGetter(FireballWandConfig::ghastExtractionRadius),
            Codec.BOOL.fieldOf("allow_aim_assist").forGetter(FireballWandConfig::allowAimAssist)
    ).apply(instance, FireballWandConfig::new));
}
