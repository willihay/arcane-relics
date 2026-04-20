package org.bensam.arcanerelics.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import org.bensam.arcanerelics.config.SyncedServerConfig;

public final class ConfigClientPackets {
    private ConfigClientPackets() {}

    public static void registerClientReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(SyncServerConfigS2CPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> SyncedServerConfig.set(payload.config()));
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> SyncedServerConfig.clear());
    }
}
