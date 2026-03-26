package org.bensam.arcanerelics.menu;

import net.minecraft.world.inventory.ContainerData;

public class WandEnchantingContainerData implements ContainerData {
    private static final int SIZE = 4;

    private int xpCost;
    private int hasLapis;
    private int hasRecipeError;
    private int hasWand;

    @Override
    public int get(int i) {
        return switch (i) {
            case 0 -> this.xpCost;
            case 1 -> this.hasLapis;
            case 2 -> this.hasRecipeError;
            case 3 -> this.hasWand;
            default -> throw new IndexOutOfBoundsException("Invalid container data index: " + i);
        };
    }

    @Override
    public void set(int i, int value) {
        switch (i) {
            case 0 -> this.xpCost = value;
            case 1 -> this.hasLapis = value;
            case 2 -> this.hasRecipeError = value;
            case 3 -> this.hasWand = value;
            default -> throw new IndexOutOfBoundsException("Invalid container data index: " + i);
        }
    }

    @Override
    public int getCount() {
        return SIZE;
    }

    public void setXpCost(int xpCost) {
        this.xpCost = xpCost;
    }

    public void setHasLapis(boolean hasLapis) {
        this.hasLapis = hasLapis ? 1 : 0;
    }

    public void setHasRecipeError(boolean hasRecipeError) {
        this.hasRecipeError = hasRecipeError ? 1 : 0;
    }

    public void setHasWand(boolean hasWand) { this.hasWand = hasWand ? 1 : 0; }
}
