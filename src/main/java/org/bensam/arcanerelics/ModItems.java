package org.bensam.arcanerelics;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.bensam.arcanerelics.item.*;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public final class ModItems {
    private ModItems() {}

    private static final List<Item> WAND_ENCHANTING_TABLE_OUTPUTS = new ArrayList<>();

    private static ItemArcaneWand arcaneWandInternal;
    private static ItemFangWand fangWandInternal;
    private static ItemFireWand fireWandInternal;
    private static ItemIceWand iceWandInternal;
    private static ItemLevitationWand levitationWandInternal;
    private static ItemLightningWand lightningWandInternal;
    private static ItemWindWand windWandInternal;

    public static final Supplier<ItemArcaneWand> ARCANE_WAND = () -> arcaneWandInternal;
    public static final Supplier<ItemFangWand> FANG_WAND = () -> fangWandInternal;
    public static final Supplier<ItemFireWand> FIRE_WAND = () -> fireWandInternal;
    public static final Supplier<ItemIceWand> ICE_WAND = () -> iceWandInternal;
    public static final Supplier<ItemLevitationWand> LEVITATION_WAND = () -> levitationWandInternal;
    public static final Supplier<ItemLightningWand> LIGHTNING_WAND = () -> lightningWandInternal;
    public static final Supplier<ItemWindWand> WIND_WAND = () -> windWandInternal;

    private static final WandDefinition ARCANE_WAND_DEFINITION =
            new WandDefinition(0, 0, 1, 1, Integer.MAX_VALUE, 0, 2, false);

    private static final WandDefinition FANG_WAND_DEFINITION =
            new WandDefinition(20, 40, 1, 1, 20, 20, 4, true);

    private static final WandDefinition FIRE_WAND_DEFINITION =
            new WandDefinition(20, 40, 1, 2, 20, 20, 3, true);

    private static final WandDefinition ICE_WAND_DEFINITION =
            new WandDefinition(30, 60, 1, 1, 40, 30, 3, true);

    private static final WandDefinition LEVITATION_WAND_DEFINITION =
            new WandDefinition(20, 40, 1, 1, 60, 20, 3, true);

    private static final WandDefinition LIGHTNING_WAND_DEFINITION =
            new WandDefinition(15, 30, 1, 2, 60, 15, 4, true);

    private static final WandDefinition WIND_WAND_DEFINITION =
            new WandDefinition(30, 60, 1, 1, 20, 30, 3, true);

    public static void initialize() {
        // Register mod items.
        arcaneWandInternal = register(
                "arcane_wand",
                props -> new ItemArcaneWand(props, ARCANE_WAND_DEFINITION),
                ARCANE_WAND_DEFINITION.createProperties("item." + ArcaneRelics.MOD_ID + ".arcane_wand.info")
        );

        fangWandInternal = register(
                "fang_wand",
                props -> new ItemFangWand(props, FANG_WAND_DEFINITION),
                FANG_WAND_DEFINITION.createProperties("item." + ArcaneRelics.MOD_ID + ".fang_wand.info")
        );

        fireWandInternal = register(
                "fire_wand",
                props -> new ItemFireWand(props, FIRE_WAND_DEFINITION),
                FIRE_WAND_DEFINITION.createProperties("item." + ArcaneRelics.MOD_ID + ".fire_wand.info")
        );

        iceWandInternal = register(
                "ice_wand",
                props -> new ItemIceWand(props, ICE_WAND_DEFINITION),
                ICE_WAND_DEFINITION.createProperties("item." + ArcaneRelics.MOD_ID + ".ice_wand.info")
        );

        levitationWandInternal = register(
                "levitation_wand",
                props -> new ItemLevitationWand(props, LEVITATION_WAND_DEFINITION),
                LEVITATION_WAND_DEFINITION.createProperties("item." + ArcaneRelics.MOD_ID + ".levitation_wand.info")
        );

        lightningWandInternal = register(
                "lightning_wand",
                props -> new ItemLightningWand(props, LIGHTNING_WAND_DEFINITION),
                LIGHTNING_WAND_DEFINITION.createProperties("item." + ArcaneRelics.MOD_ID + ".lightning_wand.info")
        );

        windWandInternal = register(
                "wind_wand",
                props -> new ItemWindWand(props, WIND_WAND_DEFINITION),
                WIND_WAND_DEFINITION.createProperties("item." + ArcaneRelics.MOD_ID + ".wind_wand.info")
        );
    }

    public static <T extends Item> T register(
            String name,
            Function<Item.Properties, T> itemFactory,
            Item.Properties settings
    ) {
        // Create the item key.
        ResourceKey<Item> itemKey = ResourceKey.create(
                Registries.ITEM,
                Identifier.fromNamespaceAndPath(ArcaneRelics.MOD_ID, name)
        );

        // Create the item instance.
        T item = itemFactory.apply(settings.setId(itemKey));

        // Register the item.
        T registered = Registry.register(BuiltInRegistries.ITEM, itemKey, item);

        if (registered instanceof WandEnchantingTableOutput) {
            WAND_ENCHANTING_TABLE_OUTPUTS.add(registered);
        }

        return registered;
    }

    public static ItemStack getWandEnchantmentOutput(@NonNull ItemStack stack) {
        if (stack.isEmpty()) { return ItemStack.EMPTY; }

        for (Item item : WAND_ENCHANTING_TABLE_OUTPUTS) {
            if (item instanceof WandEnchantingTableOutput outputItem
                    && outputItem.canBeProducedOrRechargedBy(stack)) {
                return new ItemStack(item);
            }
        }

        return ItemStack.EMPTY;
    }

    public static boolean isArcaneEnchantmentItem(@NonNull ItemStack stack) {
        return !getWandEnchantmentOutput(stack).isEmpty();
    }
}
