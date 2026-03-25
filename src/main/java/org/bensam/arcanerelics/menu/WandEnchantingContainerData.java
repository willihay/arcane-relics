package org.bensam.arcanerelics.menu;

import net.minecraft.world.inventory.ContainerData;

public class WandEnchantingContainerData implements ContainerData {
    private static final int SIZE = 3;

    private int xpCost;
    private int hasLapis;
    private int canEnchant;

    @Override
    public int get(int i) {
        return switch (i) {
            case 0 -> this.xpCost;
            case 1 -> this.hasLapis;
            case 2 -> this.canEnchant;
            default -> throw new IndexOutOfBoundsException("Invalid container data index: " + i);
        };
    }

    @Override
    public void set(int i, int value) {
        switch (i) {
            case 0 -> this.xpCost = value;
            case 1 -> this.hasLapis = value;
            case 2 -> this.canEnchant = value;
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

    public void setCanEnchant(boolean canEnchant) {
        this.canEnchant = canEnchant ? 1 : 0;
    }
}
