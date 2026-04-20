package org.bensam.arcanerelics.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.bensam.arcanerelics.ArcaneRelics;
import org.jspecify.annotations.NonNull;

public record WandBeginCastS2CPayload(boolean isMainHand, long gameTime) implements CustomPacketPayload {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(ArcaneRelics.MOD_ID, "wand_begin_cast");
    public static final CustomPacketPayload.Type<WandBeginCastS2CPayload> TYPE = new CustomPacketPayload.Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, WandBeginCastS2CPayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, WandBeginCastS2CPayload::isMainHand,
                    ByteBufCodecs.LONG, WandBeginCastS2CPayload::gameTime,
                    WandBeginCastS2CPayload::new
            );

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
