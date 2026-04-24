package org.bensam.arcanerelics;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public final class ModComponents {
    private ModComponents() {}

    public static final DataComponentType<Integer> WAND_CHARGES_COMPONENT = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Identifier.fromNamespaceAndPath(ArcaneRelics.MOD_ID, "wand_charges"),
            DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .build()
    );

    public static void initialize() {
        ArcaneRelics.LOGGER.debug("Registering components");
    }
}
