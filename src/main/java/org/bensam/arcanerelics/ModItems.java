package org.bensam.arcanerelics;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.bensam.arcanerelics.item.ItemLightningWand;

import java.util.function.Function;
import java.util.function.Supplier;

public final class ModItems {
    private ModItems() {}

    private static ItemLightningWand lightningWandInternal;

    public static final Supplier<ItemLightningWand> LIGHTNING_WAND = () -> lightningWandInternal;

    public static void initialize() {
        // Register mod items.
        lightningWandInternal = register(
                "lightning_wand",
                ItemLightningWand::new,
                new Item.Properties()
                        .component(
                                ModComponents.WAND_CHARGES_COMPONENT,
                                new ModComponents.WandChargesComponent(ItemLightningWand.INITIAL_WAND_CHARGES)
                        )
                        .component(
                                ModComponents.WAND_TOOLTIP_COMPONENT,
                                new ModComponents.WandTooltipComponent(
                                        "item." + ArcaneRelics.MOD_ID + ".lightning_wand.info",
                                        3
                                )
                        )
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
        Registry.register(BuiltInRegistries.ITEM, itemKey, item);

        return item;
    }
}
