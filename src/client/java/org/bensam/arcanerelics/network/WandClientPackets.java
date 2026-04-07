package org.bensam.arcanerelics.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import org.bensam.arcanerelics.renderer.WandClientState;

public final class WandClientPackets {
    private WandClientPackets() {}

    public static void registerClientReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(WandBeginCastS2CPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                WandClientState.CAST_ANIMATION.beginCast(payload.isMainHand(), payload.gameTime());
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(WandSucceedCastS2CPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                WandClientState.CAST_ANIMATION.beginRelease(payload.gameTime());
            });
        });
    }
}
