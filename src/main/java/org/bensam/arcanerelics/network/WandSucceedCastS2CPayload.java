package org.bensam.arcanerelics.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.bensam.arcanerelics.ArcaneRelics;
import org.jspecify.annotations.NonNull;

public record WandSucceedCastS2CPayload(long gameTime) implements CustomPacketPayload {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(ArcaneRelics.MOD_ID, "wand_succeed_cast");
    public static final Type<WandSucceedCastS2CPayload> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, WandSucceedCastS2CPayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.LONG, WandSucceedCastS2CPayload::gameTime,
                    WandSucceedCastS2CPayload::new
            );

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
