package org.bensam.arcanerelics.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record WandBalanceConfig(
        int initialCharges,
        int maxCharges,
        int normalCastCost,
        int fullPowerCastCost,
        int fullPowerTicks,
        int rechargeAmount
) {
    public static final Codec<WandBalanceConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("initial_charges").forGetter(WandBalanceConfig::initialCharges),
            Codec.INT.fieldOf("max_charges").forGetter(WandBalanceConfig::maxCharges),
            Codec.INT.fieldOf("normal_cast_cost").forGetter(WandBalanceConfig::normalCastCost),
            Codec.INT.fieldOf("full_power_cast_cost").forGetter(WandBalanceConfig::fullPowerCastCost),
            Codec.INT.fieldOf("full_power_ticks").forGetter(WandBalanceConfig::fullPowerTicks),
            Codec.INT.fieldOf("recharge_amount").forGetter(WandBalanceConfig::rechargeAmount)
    ).apply(instance, WandBalanceConfig::new));
}
