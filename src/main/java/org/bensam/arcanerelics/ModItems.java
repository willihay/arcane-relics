package org.bensam.arcanerelics;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.bensam.arcanerelics.config.ModServerConfig;
import org.bensam.arcanerelics.item.*;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public final class ModItems {
    private ModItems() {}

    private static final List<ItemStack> WAND_ENCHANTING_TABLE_INPUTS = new ArrayList<>();
    private static final List<AbstractChargedWandItem> WAND_ENCHANTING_TABLE_OUTPUTS = new ArrayList<>();

    private static ItemArcaneWand arcaneWandInternal;
    private static ItemFangWand fangWandInternal;
    private static ItemFireballWand fireballWandInternal;
    private static ItemIceWand iceWandInternal;
    private static ItemLevitationWand levitationWandInternal;
    private static ItemLightningWand lightningWandInternal;
    private static ItemRegenerationWand regenerationWandInternal;
    private static ItemWindWand windWandInternal;

    public static final Supplier<ItemArcaneWand> ARCANE_WAND = () -> arcaneWandInternal;
    public static final Supplier<ItemFangWand> FANG_WAND = () -> fangWandInternal;
    public static final Supplier<ItemFireballWand> FIREBALL_WAND = () -> fireballWandInternal;
    public static final Supplier<ItemIceWand> ICE_WAND = () -> iceWandInternal;
    public static final Supplier<ItemLevitationWand> LEVITATION_WAND = () -> levitationWandInternal;
    public static final Supplier<ItemLightningWand> LIGHTNING_WAND = () -> lightningWandInternal;
    public static final Supplier<ItemRegenerationWand> REGENERATION_WAND = () -> regenerationWandInternal;
    public static final Supplier<ItemWindWand> WIND_WAND = () -> windWandInternal;

    private static final WandDefinition ARCANE_WAND_DEFINITION =
            new WandDefinition(
                    "item." + ArcaneRelics.MOD_ID + ".arcane_wand.info",
                    2);

    private static final WandDefinition FANG_WAND_DEFINITION =
            new WandDefinition(
                    "item." + ArcaneRelics.MOD_ID + ".fang_wand.info",
                    4);

    private static final WandDefinition FIREBALL_WAND_DEFINITION =
            new WandDefinition(
                    "item." + ArcaneRelics.MOD_ID + ".fireball_wand.info",
                    3);

    private static final WandDefinition ICE_WAND_DEFINITION =
            new WandDefinition(
                    "item." + ArcaneRelics.MOD_ID + ".ice_wand.info",
                    3);

    private static final WandDefinition LEVITATION_WAND_DEFINITION =
            new WandDefinition(
                    "item." + ArcaneRelics.MOD_ID + ".levitation_wand.info",
                    3);

    private static final WandDefinition LIGHTNING_WAND_DEFINITION =
            new WandDefinition(
                    "item." + ArcaneRelics.MOD_ID + ".lightning_wand.info",
                    4);

    private static final WandDefinition REGENERATION_WAND_DEFINITION =
            new WandDefinition(
                    "item." + ArcaneRelics.MOD_ID + ".regen_wand.info",
                    3);

    private static final WandDefinition WIND_WAND_DEFINITION =
            new WandDefinition(
                    "item." + ArcaneRelics.MOD_ID + ".wind_wand.info",
                    3);

    public static void initialize() {
        ModServerConfig defaults = ModServerConfig.defaults();

        // Register mod items.
        arcaneWandInternal = register(
                "arcane_wand",
                props -> new ItemArcaneWand(props, ARCANE_WAND_DEFINITION),
                ARCANE_WAND_DEFINITION.createProperties(ItemArcaneWand.INITIAL_CHARGES, false)
        );

        fangWandInternal = register(
                "fang_wand",
                props -> new ItemFangWand(props, FANG_WAND_DEFINITION),
                FANG_WAND_DEFINITION.createProperties(defaults.fangWand().balance().initialCharges(), true)
        );

        fireballWandInternal = register(
                "fireball_wand",
                props -> new ItemFireballWand(props, FIREBALL_WAND_DEFINITION),
                FIREBALL_WAND_DEFINITION.createProperties(defaults.fireballWand().balance().initialCharges(), true)
        );

        iceWandInternal = register(
                "ice_wand",
                props -> new ItemIceWand(props, ICE_WAND_DEFINITION),
                ICE_WAND_DEFINITION.createProperties(defaults.iceWand().balance().initialCharges(), true)
        );

        levitationWandInternal = register(
                "levitation_wand",
                props -> new ItemLevitationWand(props, LEVITATION_WAND_DEFINITION),
                LEVITATION_WAND_DEFINITION.createProperties(defaults.levitationWand().balance().initialCharges(), true)
        );

        lightningWandInternal = register(
                "lightning_wand",
                props -> new ItemLightningWand(props, LIGHTNING_WAND_DEFINITION),
                LIGHTNING_WAND_DEFINITION.createProperties(defaults.lightningWand().balance().initialCharges(), true)
        );

        regenerationWandInternal = register(
                "regen_wand",
                props -> new ItemRegenerationWand(props, REGENERATION_WAND_DEFINITION),
                REGENERATION_WAND_DEFINITION.createProperties(defaults.regenerationWand().balance().initialCharges(), true)
        );

        windWandInternal = register(
                "wind_wand",
                props -> new ItemWindWand(props, WIND_WAND_DEFINITION),
                WIND_WAND_DEFINITION.createProperties(defaults.windWand().balance().initialCharges(), true)
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

        if (registered instanceof AbstractChargedWandItem wandItem) {
            WAND_ENCHANTING_TABLE_INPUTS.add(new ItemStack(registered));

            if (registered instanceof WandEnchantingTableOutput) {
                WAND_ENCHANTING_TABLE_OUTPUTS.add(wandItem);
            }
        }

        return registered;
    }

    public static List<ItemStack> getAllWands() {
        return WAND_ENCHANTING_TABLE_INPUTS.stream()
                .map(ItemStack::copy)
                .toList();
    }

    public static List<AbstractChargedWandItem> getAllOutputWands() {
        return WAND_ENCHANTING_TABLE_OUTPUTS;
    }

    public static ItemStack getWandEnchantmentOutput(@NonNull ItemStack stack) {
        if (stack.isEmpty()) { return ItemStack.EMPTY; }

        for (AbstractChargedWandItem wandItem : WAND_ENCHANTING_TABLE_OUTPUTS) {
            if (wandItem instanceof WandEnchantingTableOutput outputItem
                    && outputItem.canBeProducedOrRechargedBy(stack)) {
                return new ItemStack(wandItem);
            }
        }

        return ItemStack.EMPTY;
    }

    public static boolean isArcaneEnchantmentItem(@NonNull ItemStack stack) {
        return !getWandEnchantmentOutput(stack).isEmpty();
    }
}
