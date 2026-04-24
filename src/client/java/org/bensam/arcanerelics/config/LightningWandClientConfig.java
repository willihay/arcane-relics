package org.bensam.arcanerelics.config;

public class LightningWandClientConfig {
    public boolean blockBreakingExplosionEnabled = true;

    public LightningWandClientConfig() {}

    public LightningWandClientConfig(boolean blockBreakingExplosionEnabled) {
        this.blockBreakingExplosionEnabled = blockBreakingExplosionEnabled;
    }

    public boolean blockBreakingExplosionEnabled() {
        return this.blockBreakingExplosionEnabled;
    }
}
