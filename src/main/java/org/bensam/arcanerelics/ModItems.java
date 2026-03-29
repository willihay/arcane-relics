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
    private static ItemFireWand fireWandInternal;
    private static ItemLevitationWand levitationWandInternal;
    private static ItemLightningWand lightningWandInternal;

    public static final Supplier<ItemArcaneWand> ARCANE_WAND = () -> arcaneWandInternal;
    public static final Supplier<ItemFireWand> FIRE_WAND = () -> fireWandInternal;
    public static final Supplier<ItemLevitationWand> LEVITATION_WAND = () -> levitationWandInternal;
    public static final Supplier<ItemLightningWand> LIGHTNING_WAND = () -> lightningWandInternal;

    public static void initialize() {
        // Register mod items.
        arcaneWandInternal = register(
                "arcane_wand",
                ItemArcaneWand::new,
                new Item.Properties()
                        .component(
                                ModComponents.WAND_CHARGES_COMPONENT,
                                new ModComponents.WandChargesComponent(ItemArcaneWand.INITIAL_CHARGES)
                        )
                        .component(
                                ModComponents.WAND_MAX_CHARGES_COMPONENT,
                                ItemArcaneWand.MAX_CHARGES
                        )
                        .component(ModComponents.WAND_TOOLTIP_COMPONENT,
                                new ModComponents.WandTooltipComponent(
                                        "item." + ArcaneRelics.MOD_ID + ".arcane_wand.info",
                                        2
                                )
                        )
        );

        fireWandInternal = register(
                "fire_wand",
                ItemFireWand::new,
                new Item.Properties()
                        .component(
                                ModComponents.WAND_CHARGES_COMPONENT,
                                new ModComponents.WandChargesComponent(ItemFireWand.INITIAL_CHARGES)
                        )
                        .component(
                                ModComponents.WAND_MAX_CHARGES_COMPONENT,
                                ItemFireWand.MAX_CHARGES
                        )
                        .component(
                                ModComponents.WAND_TOOLTIP_COMPONENT,
                                new ModComponents.WandTooltipComponent(
                                        "item." + ArcaneRelics.MOD_ID + ".fire_wand.info",
                                        3
                                )
                        )
                        .stacksTo(1)
        );

        levitationWandInternal = register(
                "levitation_wand",
                ItemLevitationWand::new,
                new Item.Properties()
                        .component(
                                ModComponents.WAND_CHARGES_COMPONENT,
                                new ModComponents.WandChargesComponent(ItemLevitationWand.INITIAL_CHARGES)
                        )
                        .component(
                                ModComponents.WAND_MAX_CHARGES_COMPONENT,
                                ItemLevitationWand.MAX_CHARGES
                        )
                        .component(
                                ModComponents.WAND_TOOLTIP_COMPONENT,
                                new ModComponents.WandTooltipComponent(
                                        "item." + ArcaneRelics.MOD_ID + ".levitation_wand.info",
                                        2
                                )
                        )
                        .stacksTo(1)
        );

        lightningWandInternal = register(
                "lightning_wand",
                ItemLightningWand::new,
                new Item.Properties()
                        .component(
                                ModComponents.WAND_CHARGES_COMPONENT,
                                new ModComponents.WandChargesComponent(ItemLightningWand.INITIAL_CHARGES)
                        )
                        .component(
                                ModComponents.WAND_MAX_CHARGES_COMPONENT,
                                ItemLightningWand.MAX_CHARGES
                        )
                        .component(
                                ModComponents.WAND_TOOLTIP_COMPONENT,
                                new ModComponents.WandTooltipComponent(
                                        "item." + ArcaneRelics.MOD_ID + ".lightning_wand.info",
                                        4
                                )
                        )
                        .stacksTo(1)
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

    public static ItemStack getArcaneEnchantmentItem(@NonNull ItemStack stack) {
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
        return !getArcaneEnchantmentItem(stack).isEmpty();
    }
}
