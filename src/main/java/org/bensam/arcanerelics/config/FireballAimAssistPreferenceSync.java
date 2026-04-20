package org.bensam.arcanerelics.config;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import org.bensam.arcanerelics.network.SyncFireballAimAssistPreferenceC2SPayload;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class FireballAimAssistPreferenceSync {
    private static final boolean DEFAULT_ENABLED = true;
    private static final Map<UUID, Boolean> PLAYER_AIM_ASSIST_ENABLED = new ConcurrentHashMap<>();

    private FireballAimAssistPreferenceSync() {}

    public static void initialize() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                PLAYER_AIM_ASSIST_ENABLED.put(handler.player.getUUID(), DEFAULT_ENABLED));

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                PLAYER_AIM_ASSIST_ENABLED.remove(handler.player.getUUID()));

        ServerPlayNetworking.registerGlobalReceiver(SyncFireballAimAssistPreferenceC2SPayload.TYPE, (payload, context) ->
                PLAYER_AIM_ASSIST_ENABLED.put(context.player().getUUID(), payload.aimAssistEnabled()));
    }

    public static boolean isAimAssistEnabled(ServerPlayer player) {
        return PLAYER_AIM_ASSIST_ENABLED.getOrDefault(player.getUUID(), DEFAULT_ENABLED);
    }
}
