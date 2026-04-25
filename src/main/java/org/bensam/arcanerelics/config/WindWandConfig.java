package org.bensam.arcanerelics.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class WindWandConfig {
    public WandBalanceConfig balance = new WandBalanceConfig();
    public int breezeExtractionRadius;
    public int windRange;

    public static final Codec<WindWandConfig>  CODEC = RecordCodecBuilder.create(instance -> instance.group(
            WandBalanceConfig.CODEC.fieldOf("balance").forGetter(WindWandConfig::balance),
            Codec.INT.fieldOf("breeze_extraction_radius").forGetter(WindWandConfig::breezeExtractionRadius),
            Codec.INT.fieldOf("range").forGetter(WindWandConfig::windRange)
    ).apply(instance, WindWandConfig::new));

    public WindWandConfig() {}

    public WindWandConfig(WandBalanceConfig balance, int breezeExtractionRadius, int windRange) {
        this.balance = balance;
        this.breezeExtractionRadius = breezeExtractionRadius;
        this.windRange = windRange;
    }

    public WandBalanceConfig balance() {
        return this.balance;
    }

    public int breezeExtractionRadius() {
        return this.breezeExtractionRadius;
    }

    public int windRange() {
        return this.windRange;
    }
}
