package org.bensam.arcanerelics.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class RegenerationWandConfig {
    public WandBalanceConfig balance = new WandBalanceConfig();
    public int ghastExtractionRadius;
    public int range;

    public static final Codec<RegenerationWandConfig>  CODEC = RecordCodecBuilder.create(instance -> instance.group(
            WandBalanceConfig.CODEC.fieldOf("balance").forGetter(RegenerationWandConfig::balance),
            Codec.INT.fieldOf("ghast_extraction_radius").forGetter(RegenerationWandConfig::ghastExtractionRadius),
            Codec.INT.fieldOf("range").forGetter(RegenerationWandConfig::range)
    ).apply(instance, RegenerationWandConfig::new));

    public RegenerationWandConfig() {}

    public RegenerationWandConfig(WandBalanceConfig balance, int ghastExtractionRadius, int range) {
        this.balance = balance;
        this.ghastExtractionRadius = ghastExtractionRadius;
        this.range = range;
    }

    public WandBalanceConfig balance() {
        return this.balance;
    }

    public int ghastExtractionRadius() {
        return this.ghastExtractionRadius;
    }

    public int range() {
        return this.range;
    }
}
