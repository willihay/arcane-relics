package org.bensam.arcanerelics;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public final class ModCreativeTab {
    private ModCreativeTab() {}

    public static final ResourceKey<CreativeModeTab> CUSTOM_CREATIVE_TAB_KEY =
            ResourceKey.create(
                    BuiltInRegistries.CREATIVE_MODE_TAB.key(),
                    Identifier.fromNamespaceAndPath(ArcaneRelics.MOD_ID, "creative_tab")
            );

    private static CreativeModeTab tabInternal;
    public static final Supplier<CreativeModeTab> CUSTOM_CREATIVE_TAB = () -> tabInternal;

    public static void initialize() {
        // Build the custom creative tab.
        tabInternal = FabricItemGroup.builder()
                .icon(() -> new ItemStack(ModItems.LIGHTNING_WAND.get()))
                .title(Component.translatable("itemGroup." + ArcaneRelics.MOD_ID))
                .displayItems((params, output) -> {
                    output.accept(ModItems.LIGHTNING_WAND.get());
                })
                .build();

        // Register the custom creative tab.
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, CUSTOM_CREATIVE_TAB_KEY, tabInternal);
    }
}
