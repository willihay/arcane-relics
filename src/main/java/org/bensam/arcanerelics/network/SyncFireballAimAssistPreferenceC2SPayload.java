package org.bensam.arcanerelics.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.bensam.arcanerelics.ArcaneRelics;
import org.jspecify.annotations.NonNull;

public record SyncFireballAimAssistPreferenceC2SPayload(boolean aimAssistEnabled) implements CustomPacketPayload {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(ArcaneRelics.MOD_ID, "sync_fireball_aim_assist_preference");
    public static final Type<SyncFireballAimAssistPreferenceC2SPayload> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncFireballAimAssistPreferenceC2SPayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, SyncFireballAimAssistPreferenceC2SPayload::aimAssistEnabled,
                    SyncFireballAimAssistPreferenceC2SPayload::new
            );

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() { return TYPE; }
}
