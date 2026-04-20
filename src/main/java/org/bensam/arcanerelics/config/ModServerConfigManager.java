package org.bensam.arcanerelics.config;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public final class ModServerConfigManager {
    private ModServerConfigManager() {}

    public static ModServerConfig getConfig(ServerLevel level) {
        return ModServerConfigState.get(level).config();
    }

    public static ModServerConfig getConfig(Level level) {
        if (!(level instanceof ServerLevel serverLevel)) {
            throw new IllegalStateException("Server config requested from non-server level");
        }
        return getConfig(serverLevel);
    }
}
