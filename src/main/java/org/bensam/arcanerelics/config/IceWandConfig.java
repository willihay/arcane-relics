package org.bensam.arcanerelics.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class IceWandConfig {
    public WandBalanceConfig balance = new WandBalanceConfig();
    public int strayExtractionRadius;
    public int range;

    public static final Codec<IceWandConfig>  CODEC = RecordCodecBuilder.create(instance -> instance.group(
            WandBalanceConfig.CODEC.fieldOf("balance").forGetter(IceWandConfig::balance),
            Codec.INT.fieldOf("stray_extraction_radius").forGetter(IceWandConfig::strayExtractionRadius),
            Codec.INT.fieldOf("range").forGetter(IceWandConfig::range)
    ).apply(instance, IceWandConfig::new));

    public IceWandConfig() {}

    public IceWandConfig(WandBalanceConfig balance, int strayExtractionRadius, int range) {
        this.balance = balance;
        this.strayExtractionRadius = strayExtractionRadius;
        this.range = range;
    }

    public WandBalanceConfig balance() {
        return this.balance;
    }

    public int strayExtractionRadius() {
        return this.strayExtractionRadius;
    }

    public int range() {
        return this.range;
    }
}
