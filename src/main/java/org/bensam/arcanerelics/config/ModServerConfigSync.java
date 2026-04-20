package org.bensam.arcanerelics.config;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import org.bensam.arcanerelics.network.SyncServerConfigS2CPayload;

public final class ModServerConfigSync {
    private ModServerConfigSync() {}

    public static void initialize() {
        ServerPlayConnectionEvents.JOIN.register(
                (handler, sender, server) ->
                        syncToPlayer(handler.player));
    }

    private static void syncToPlayer(ServerPlayer player) {
        ModServerConfig config = ModServerConfigManager.getConfig(player.level());
        ServerPlayNetworking.send(player, new SyncServerConfigS2CPayload(config));
    }
}
