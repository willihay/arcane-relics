package org.bensam.arcanerelics.item;

import net.minecraft.world.item.Item;
import org.bensam.arcanerelics.ModComponents;

public record WandDefinition(
        String tooltipTranslationKeyPrefix,
        int tooltipLineCount
) {
    public Item.Properties createProperties(int initialCharges, boolean stackToOne) {
        Item.Properties properties = new Item.Properties()
                .component(
                        ModComponents.WAND_CHARGES_COMPONENT,
                        initialCharges
                );

        if (stackToOne) {
            properties = properties.stacksTo(1);
        }

        return properties;
    }
}
