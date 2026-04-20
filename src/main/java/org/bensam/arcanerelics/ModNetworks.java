package org.bensam.arcanerelics;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import org.bensam.arcanerelics.network.SyncFireballAimAssistPreferenceC2SPayload;
import org.bensam.arcanerelics.network.SyncServerConfigS2CPayload;
import org.bensam.arcanerelics.network.WandBeginCastS2CPayload;
import org.bensam.arcanerelics.network.WandSucceedCastS2CPayload;

public class ModNetworks {
    private ModNetworks() {}

    public static void initialize() {
        // Register packets.
        PayloadTypeRegistry.playC2S().register(SyncFireballAimAssistPreferenceC2SPayload.TYPE, SyncFireballAimAssistPreferenceC2SPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SyncServerConfigS2CPayload.TYPE, SyncServerConfigS2CPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(WandBeginCastS2CPayload.TYPE, WandBeginCastS2CPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(WandSucceedCastS2CPayload.TYPE, WandSucceedCastS2CPayload.CODEC);
    }
}
