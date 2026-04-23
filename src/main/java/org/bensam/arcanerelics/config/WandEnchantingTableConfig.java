package org.bensam.arcanerelics.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class WandEnchantingTableConfig {
    public boolean enableEnchantWandXpCost;
    public boolean enableRechargeWandXpCost;

    public static final Codec<WandEnchantingTableConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("enable_enchant_wand_xp_cost").forGetter(WandEnchantingTableConfig::enableEnchantWandXpCost),
            Codec.BOOL.fieldOf("enable_recharge_wand_xp_cost").forGetter(WandEnchantingTableConfig::enableRechargeWandXpCost)
    ).apply(instance, WandEnchantingTableConfig::new));

    public WandEnchantingTableConfig() {}

    public WandEnchantingTableConfig(boolean enableEnchantWandXpCost, boolean enableRechargeWandXpCost) {
        this.enableEnchantWandXpCost = enableEnchantWandXpCost;
        this.enableRechargeWandXpCost = enableRechargeWandXpCost;
    }

    public boolean enableEnchantWandXpCost() {
        return this.enableEnchantWandXpCost;
    }

    public boolean enableRechargeWandXpCost() {
        return this.enableRechargeWandXpCost;
    }
}
