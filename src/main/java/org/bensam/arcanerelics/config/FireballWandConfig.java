package org.bensam.arcanerelics.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class FireballWandConfig {
    public WandBalanceConfig balance = new WandBalanceConfig();
    public int blazeExtractionRadius;
    public int ghastExtractionRadius;
    public boolean allowAimAssist;

    public static final Codec<FireballWandConfig>  CODEC = RecordCodecBuilder.create(instance -> instance.group(
            WandBalanceConfig.CODEC.fieldOf("balance").forGetter(FireballWandConfig::balance),
            Codec.INT.fieldOf("blaze_extraction_radius").forGetter(FireballWandConfig::blazeExtractionRadius),
            Codec.INT.fieldOf("ghast_extraction_radius").forGetter(FireballWandConfig::ghastExtractionRadius),
            Codec.BOOL.fieldOf("allow_aim_assist").forGetter(FireballWandConfig::allowAimAssist)
    ).apply(instance, FireballWandConfig::new));

    public FireballWandConfig() {}

    public FireballWandConfig(
            WandBalanceConfig balance,
            int blazeExtractionRadius,
            int ghastExtractionRadius,
            boolean allowAimAssist
    ) {
        this.balance = balance;
        this.blazeExtractionRadius = blazeExtractionRadius;
        this.ghastExtractionRadius = ghastExtractionRadius;
        this.allowAimAssist = allowAimAssist;
    }

    public WandBalanceConfig balance() {
        return this.balance;
    }

    public int blazeExtractionRadius() {
        return this.blazeExtractionRadius;
    }

    public int ghastExtractionRadius() {
        return this.ghastExtractionRadius;
    }

    public boolean allowAimAssist() {
        return this.allowAimAssist;
    }
}
