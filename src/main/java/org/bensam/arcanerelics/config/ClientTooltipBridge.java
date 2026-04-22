package org.bensam.arcanerelics.config;

import org.bensam.arcanerelics.item.AbstractChargedWandItem;
import org.jspecify.annotations.Nullable;

import java.util.function.BooleanSupplier;
import java.util.function.Function;

public final class ClientTooltipBridge {
    private static @Nullable Function<AbstractChargedWandItem, WandBalanceConfig> resolver;
    private static BooleanSupplier verboseTooltipsSupplier = () -> true;

    private ClientTooltipBridge() {}

    public static void initialize(
            Function<AbstractChargedWandItem, WandBalanceConfig> resolver,
            BooleanSupplier verboseTooltipsSupplier
    ) {
        ClientTooltipBridge.resolver = resolver;
        ClientTooltipBridge.verboseTooltipsSupplier = verboseTooltipsSupplier;
    }

    public static @Nullable WandBalanceConfig getWandBalanceConfig(AbstractChargedWandItem wandItem) {
        return resolver != null ? resolver.apply(wandItem) : null;
    }

    public static boolean verboseTooltips() {
        return verboseTooltipsSupplier.getAsBoolean();
    }
}
