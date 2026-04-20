package org.bensam.arcanerelics.config;

import com.mojang.serialization.Codec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.bensam.arcanerelics.ArcaneRelics;

public final class ModServerConfigState extends SavedData {
    public static final String ID = ArcaneRelics.CONFIG_ID + "_server_config";

    public static final Codec<ModServerConfigState> CODEC =
            ModServerConfig.CODEC.xmap(ModServerConfigState::new, ModServerConfigState::config);

    @SuppressWarnings("DataFlowIssue")
    public static final SavedDataType<ModServerConfigState> TYPE =
            new SavedDataType<>(ID, ModServerConfigState::new, CODEC, null);

    private ModServerConfig config;

    public ModServerConfigState() {
        this(ModServerConfig.defaults());
    }

    public ModServerConfigState(ModServerConfig config) {
        this.config = config;
    }

    public ModServerConfig config() {
        return this.config;
    }

    public void setConfig(ModServerConfig config) {
        this.config = config;
        this.setDirty();
    }

    public static ModServerConfigState get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(TYPE);
    }
}
