package org.bensam.arcanerelics.config;

import org.jspecify.annotations.Nullable;

public final class SyncedServerConfig {
    private static @Nullable ModServerConfig current;

    private SyncedServerConfig() {}

    public static @Nullable ModServerConfig get() {
        return current;
    }

    public static void set(ModServerConfig config) {
        current = config;
    }

    public static void clear() {
        current = null;
    }
}
