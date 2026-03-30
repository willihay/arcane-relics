package org.bensam.arcanerelics.item;

import net.minecraft.world.item.Item;
import org.bensam.arcanerelics.ModComponents;

public record WandDefinition(
        int initialCharges,
        int maxCharges,
        int normalCastCost,
        int fullPowerCastCost,
        int fullPowerTicks,
        int rechargeAmount,
        int tooltipLineCount,
        boolean stackToOne
) {
    public Item.Properties createProperties(String tooltipKeyPrefix) {
        Item.Properties properties = new Item.Properties()
                .component(
                        ModComponents.WAND_CHARGES_COMPONENT,
                        new ModComponents.WandChargesComponent(this.initialCharges)
                )
                .component(
                        ModComponents.WAND_MAX_CHARGES_COMPONENT,
                        this.maxCharges
                )
                .component(
                        ModComponents.WAND_TOOLTIP_COMPONENT,
                        new ModComponents.WandTooltipComponent(tooltipKeyPrefix, this.tooltipLineCount)
                );

        if (this.stackToOne) {
            properties = properties.stacksTo(1);
        }

        return properties;
    }
}
