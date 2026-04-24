package org.bensam.arcanerelics.config;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import org.bensam.arcanerelics.network.SyncClientConfigC2SPayload;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SyncedClientConfig {
    private static final boolean DEFAULT_ENABLED = true;
    private static final Map<UUID, Boolean> PLAYER_FIREBALL_AIM_ASSIST_ENABLED = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> PLAYER_LIGHTNING_BLOCK_BREAK_ENABLED = new ConcurrentHashMap<>();

    private SyncedClientConfig() {}

    public static void initialize() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            PLAYER_FIREBALL_AIM_ASSIST_ENABLED.put(handler.player.getUUID(), DEFAULT_ENABLED);
            PLAYER_LIGHTNING_BLOCK_BREAK_ENABLED.put(handler.player.getUUID(), DEFAULT_ENABLED);
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            PLAYER_FIREBALL_AIM_ASSIST_ENABLED.remove(handler.player.getUUID());
            PLAYER_LIGHTNING_BLOCK_BREAK_ENABLED.remove(handler.player.getUUID());
        });

        ServerPlayNetworking.registerGlobalReceiver(SyncClientConfigC2SPayload.TYPE, (payload, context) -> {
            PLAYER_FIREBALL_AIM_ASSIST_ENABLED.put(context.player().getUUID(), payload.fireballAimAssist());
            PLAYER_LIGHTNING_BLOCK_BREAK_ENABLED.put(context.player().getUUID(), payload.lightningBlockBreak());
        });
    }

    public static boolean isFireballAimAssistEnabled(ServerPlayer player) {
        return PLAYER_FIREBALL_AIM_ASSIST_ENABLED.getOrDefault(player.getUUID(), DEFAULT_ENABLED);
    }

    public static boolean isLightningBlockBreakEnabled(ServerPlayer player) {
        return PLAYER_LIGHTNING_BLOCK_BREAK_ENABLED.getOrDefault(player.getUUID(), DEFAULT_ENABLED);
    }
}
