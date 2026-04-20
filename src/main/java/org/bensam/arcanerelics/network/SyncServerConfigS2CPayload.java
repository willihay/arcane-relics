package org.bensam.arcanerelics.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.bensam.arcanerelics.ArcaneRelics;
import org.bensam.arcanerelics.config.ModServerConfig;
import org.jspecify.annotations.NonNull;

public record SyncServerConfigS2CPayload(ModServerConfig config) implements CustomPacketPayload {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(ArcaneRelics.MOD_ID, "sync_server_config");
    public static final Type<SyncServerConfigS2CPayload> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncServerConfigS2CPayload> CODEC =
            ByteBufCodecs.fromCodecWithRegistries(ModServerConfig.CODEC)
                    .map(SyncServerConfigS2CPayload::new, SyncServerConfigS2CPayload::config);

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() { return TYPE; }
}
