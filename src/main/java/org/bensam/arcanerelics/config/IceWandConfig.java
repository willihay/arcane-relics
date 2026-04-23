package org.bensam.arcanerelics.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class IceWandConfig {
    public WandBalanceConfig balance = new WandBalanceConfig();
    public int strayExtractionRadius;

    public static final Codec<IceWandConfig>  CODEC = RecordCodecBuilder.create(instance -> instance.group(
            WandBalanceConfig.CODEC.fieldOf("balance").forGetter(IceWandConfig::balance),
            Codec.INT.fieldOf("stray_extraction_radius").forGetter(IceWandConfig::strayExtractionRadius)
    ).apply(instance, IceWandConfig::new));

    public IceWandConfig() {}

    public IceWandConfig(WandBalanceConfig balance, int strayExtractionRadius) {
        this.balance = balance;
        this.strayExtractionRadius = strayExtractionRadius;
    }

    public WandBalanceConfig balance() {
        return this.balance;
    }

    public int strayExtractionRadius() {
        return this.strayExtractionRadius;
    }
}
