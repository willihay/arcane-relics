package org.bensam.arcanerelics.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class LevitationWandConfig {
    public WandBalanceConfig balance = new WandBalanceConfig();
    public int shulkerExtractionRadius;
    public int range;

    public static final Codec<LevitationWandConfig>  CODEC = RecordCodecBuilder.create(instance -> instance.group(
            WandBalanceConfig.CODEC.fieldOf("balance").forGetter(LevitationWandConfig::balance),
            Codec.INT.fieldOf("shulker_extraction_radius").forGetter(LevitationWandConfig::shulkerExtractionRadius),
            Codec.INT.fieldOf("range").forGetter(LevitationWandConfig::range)
    ).apply(instance, LevitationWandConfig::new));

    public LevitationWandConfig() {}

    public LevitationWandConfig(WandBalanceConfig balance, int shulkerExtractionRadius, int range) {
        this.balance = balance;
        this.shulkerExtractionRadius = shulkerExtractionRadius;
        this.range = range;
    }

    public WandBalanceConfig balance() {
        return this.balance;
    }

    public int shulkerExtractionRadius() {
        return this.shulkerExtractionRadius;
    }

    public int range() {
        return this.range;
    }
}
