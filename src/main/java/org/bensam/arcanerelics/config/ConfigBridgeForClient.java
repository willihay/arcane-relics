package org.bensam.arcanerelics.config;

import org.bensam.arcanerelics.item.AbstractChargedWandItem;
import org.jspecify.annotations.NonNull;

import java.util.function.BooleanSupplier;
import java.util.function.Function;

public final class ConfigBridgeForClient {
    private static Function<AbstractChargedWandItem, WandBalanceConfig> wandBalanceResolver;
    private static BooleanSupplier verboseTooltipsSupplier = () -> true;

    private ConfigBridgeForClient() {}

    public static void initialize(
            Function<AbstractChargedWandItem, WandBalanceConfig> resolver,
            BooleanSupplier verboseTooltipsSupplier
    ) {
        ConfigBridgeForClient.wandBalanceResolver = resolver;
        ConfigBridgeForClient.verboseTooltipsSupplier = verboseTooltipsSupplier;
    }

    public static @NonNull WandBalanceConfig getWandBalanceConfig(AbstractChargedWandItem wandItem) {
        return wandBalanceResolver.apply(wandItem);
    }

    public static boolean showVerboseTooltips() {
        return verboseTooltipsSupplier.getAsBoolean();
    }
}
