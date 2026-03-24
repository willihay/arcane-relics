package org.bensam.arcanerelics.menu;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.bensam.arcanerelics.blockentity.BlockEntityWandEnchantingTable;
import org.bensam.arcanerelics.item.AbstractChargedWandItem;

public class WandEnchantingMenu extends AbstractContainerMenu {
    // --- Slot layout ---
    private static final int WAND_INPUT_SLOT = 0;
    private static final int ARCANE_ITEM_SLOT = 1;
    private static final int LAPIS_INPUT_SLOT = 2;
    private static final int WAND_OUTPUT_SLOT = 3;

    private final ContainerLevelAccess access;
    private final BlockEntityWandEnchantingTable blockEntity;

    public WandEnchantingMenu(int containerId, Inventory playerInventory, BlockEntityWandEnchantingTable blockEntity) {
        super(ModMenuType.WAND_ENCHANTING_MENU, containerId);
        this.access = ContainerLevelAccess.create(playerInventory.player.level(), blockEntity.getBlockPos());
        this.blockEntity = blockEntity;

        // Add block entity slots.
        // Slot 0: Wand input
        this.addSlot(new Slot(blockEntity, WAND_INPUT_SLOT, 27, 47) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof AbstractChargedWandItem<?>;
            }
        });

        // Slot 1: Arcane item input
        this.addSlot(new Slot(blockEntity, ARCANE_ITEM_SLOT, 49, 47) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return true; // TODO: implement helper function to check if item is a valid arcane item
            }
        });

        // Slot 2: Lapis input
        this.addSlot(new Slot(blockEntity, LAPIS_INPUT_SLOT, 71, 47) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() == Items.LAPIS_LAZULI;
            }

            @Override
            public int getMaxStackSize() {
                return 64;
            }
        });

        // Slot 3: Result wand
        this.addSlot(new ResultSlot(playerInventory.player, blockEntity, WAND_OUTPUT_SLOT, 133, 47) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                // TODO: consume XP
                // TODO: consume lapis and arcane item
                // TODO: update wand result state
                super.onTake(player, stack);
            }
        });

        // Add the player inventory slots.
        this.addStandardInventorySlots(playerInventory, 8, 84);

        // Initialize result state.
        // TODO: recomputeResult();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int i) {
        // TODO:
        // - handle shift-clicking between player inventory and table slots
        // - move wand to wand slot
        // - move lapis to lapis slot
        // - move arcane item to arcane item slot
        // - move result to player inventory

        return ItemStack.EMPTY;
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);

        // TODO: recompute the output whenever input slots change
        // recomputeResult();
    }

    @Override
    public boolean stillValid(Player player) {
        // TODO: return stillValid(this.access, player, blockType);
        return true;
    }
}
