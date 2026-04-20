package org.bensam.arcanerelics.config;

public class FireballWandClientConfig {
    public boolean aimAssistEnabled = true;

    public FireballWandClientConfig() {}

    public FireballWandClientConfig(boolean aimAssistEnabled) {
        this.aimAssistEnabled = aimAssistEnabled;
    }

    public boolean aimAssistEnabled() {
        return this.aimAssistEnabled;
    }
}
