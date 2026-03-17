package org.bensam.arcanerelics.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.bensam.arcanerelics.ModComponents;

public abstract class AbstractChargedWandItem extends Item {
    private final int initialCharges;
    private final int maxCharges;

    public AbstractChargedWandItem(Properties properties, int initialCharges, int maxCharges) {
        super(properties);
        this.initialCharges = initialCharges;
        this.maxCharges = maxCharges;
    }

    protected int addCharges(ItemStack stack, int amount) {
        int currentCharges = this.getCharges(stack);
        int newCharges = Math.min(currentCharges + amount, this.getMaxCharges());
        this.setCharges(stack, newCharges);
        return newCharges;
    }

    protected void consumeCharges(ItemStack stack, int amount) {
        this.setCharges(stack, this.getCharges(stack) - amount);
    }

    protected int getCharges(ItemStack stack) {
        return stack.getOrDefault(
                ModComponents.WAND_CHARGES_COMPONENT,
                new ModComponents.WandChargesComponent(this.initialCharges)
        ).charges();
    }

    protected int getInitialCharges() {
        return this.initialCharges;
    }

    protected int getMaxCharges() {
        return this.maxCharges;
    }

    protected boolean hasAtLeastCharges(ItemStack stack, int amount) {
        return this.getCharges(stack) >= amount;
    }

    protected boolean hasCharges(ItemStack stack) {
        return this.getCharges(stack) > 0;
    }

    protected boolean isFullyCharged(ItemStack stack) {
        return this.getCharges(stack) >= this.getMaxCharges();
    }

    protected void setCharges(ItemStack stack, int charges) {
        stack.set(
                ModComponents.WAND_CHARGES_COMPONENT,
                new ModComponents.WandChargesComponent(Math.max(0, Math.min(charges, this.maxCharges)))
        );
    }
}
