package org.bensam.arcanerelics.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import org.bensam.arcanerelics.config.ModClientConfigManager;
import org.bensam.arcanerelics.config.SyncedServerConfig;

public final class ConfigClientPackets {
    private ConfigClientPackets() {}

    public static void registerClientReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(SyncServerConfigS2CPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> SyncedServerConfig.set(payload.config()));
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> sendClientPreferences());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> SyncedServerConfig.clear());
    }

    public static void sendClientPreferences() {
        if (!ClientPlayNetworking.canSend(SyncClientConfigC2SPayload.TYPE)) {
            return;
        }

        ClientPlayNetworking.send(new SyncClientConfigC2SPayload(
                ModClientConfigManager.getConfig().fireballWand().aimAssistEnabled(),
                ModClientConfigManager.getConfig().lightningWand().blockBreakingExplosionEnabled()
        ));
    }
}
