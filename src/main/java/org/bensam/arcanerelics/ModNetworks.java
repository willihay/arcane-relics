package org.bensam.arcanerelics;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import org.bensam.arcanerelics.network.SyncClientConfigC2SPayload;
import org.bensam.arcanerelics.network.SyncServerConfigS2CPayload;
import org.bensam.arcanerelics.network.WandBeginCastS2CPayload;
import org.bensam.arcanerelics.network.WandSucceedCastS2CPayload;

public class ModNetworks {
    private ModNetworks() {}

    public static void initialize() {
        // Register packets.
        PayloadTypeRegistry.serverboundPlay().register(SyncClientConfigC2SPayload.TYPE, SyncClientConfigC2SPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SyncServerConfigS2CPayload.TYPE, SyncServerConfigS2CPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(WandBeginCastS2CPayload.TYPE, WandBeginCastS2CPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(WandSucceedCastS2CPayload.TYPE, WandSucceedCastS2CPayload.CODEC);
    }
}
