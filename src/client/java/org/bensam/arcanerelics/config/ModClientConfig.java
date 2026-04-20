package org.bensam.arcanerelics.config;

public class ModClientConfig {
    public static final int CURRENT_VERSION = 1;

    public int version = CURRENT_VERSION;
    public boolean verboseTooltips = true;
    public FireballWandClientConfig fireballWand = new FireballWandClientConfig();

    public ModClientConfig() {}

    public ModClientConfig(int version, boolean verboseTooltips, FireballWandClientConfig fireballWand) {
        this.version = version;
        this.verboseTooltips = verboseTooltips;
        this.fireballWand = fireballWand;
    }

    public static ModClientConfig defaults() {
        return new ModClientConfig(
                CURRENT_VERSION,
                true,
                new FireballWandClientConfig(true)
        );
    }

    public int version() {
        return this.version;
    }

    public boolean verboseTooltips() {
        return this.verboseTooltips;
    }

    public FireballWandClientConfig fireballWand() {
        return this.fireballWand;
    }
}
