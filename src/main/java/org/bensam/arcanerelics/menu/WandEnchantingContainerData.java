package org.bensam.arcanerelics.menu;

import net.minecraft.world.inventory.ContainerData;

public class WandEnchantingContainerData implements ContainerData {
    private static final int SIZE = 2;

    private int xpCost;
    private int hasValidRecipe;

    @Override
    public int get(int i) {
        return switch (i) {
            case 0 -> this.xpCost;
            case 1 -> this.hasValidRecipe;
            default -> throw new IndexOutOfBoundsException("Invalid container data index: " + i);
        };
    }

    @Override
    public void set(int i, int value) {
        switch (i) {
            case 0 -> this.xpCost = value;
            case 1 -> this.hasValidRecipe = value;
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

    public void setHasValidRecipe(boolean hasValidRecipe) {
        this.hasValidRecipe = hasValidRecipe ? 1 : 0;
    }
}
