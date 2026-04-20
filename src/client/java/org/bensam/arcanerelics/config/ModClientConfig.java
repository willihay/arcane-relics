package org.bensam.arcanerelics.config;

public record ModClientConfig(
        int version,
        boolean verboseTooltips,
        FireballWandClientConfig fireballWand
) {
    public static final int CURRENT_VERSION = 1;

    public static ModClientConfig defaults() {
        return new ModClientConfig(
                CURRENT_VERSION,
                true,
                new FireballWandClientConfig(true)
        );
    }
}
