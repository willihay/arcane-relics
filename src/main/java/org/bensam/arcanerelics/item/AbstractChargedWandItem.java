package org.bensam.arcanerelics.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.bensam.arcanerelics.ModComponents;

public abstract class AbstractChargedWandItem extends Item {
    private final int defaultCharges;

    public AbstractChargedWandItem(Properties properties, int defaultCharges) {
        super(properties);
        this.defaultCharges = defaultCharges;
    }

    protected int getCharges(ItemStack stack) {
        return stack.getOrDefault(
                ModComponents.WAND_CHARGES_COMPONENT,
                new ModComponents.WandChargesComponent(this.defaultCharges)
        ).charges();
    }

    protected void setCharges(ItemStack stack, int charges) {
        stack.set(
                ModComponents.WAND_CHARGES_COMPONENT,
                new ModComponents.WandChargesComponent(Math.max(0, charges))
        );
    }

    protected boolean hasCharges(ItemStack stack) {
        return this.getCharges(stack) > 0;
    }

    protected void consumeCharge(ItemStack stack, int amount) {
        this.setCharges(stack, this.getCharges(stack) - amount);
    }

    protected int getDefaultCharges() {
        return this.defaultCharges;
    }
}
