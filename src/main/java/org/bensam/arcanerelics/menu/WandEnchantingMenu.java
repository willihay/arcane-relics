package org.bensam.arcanerelics.menu;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.bensam.arcanerelics.ModMenus;
import org.bensam.arcanerelics.blockentity.BlockEntityWandEnchantingTable;
import org.bensam.arcanerelics.item.AbstractChargedWandItem;

public class WandEnchantingMenu extends AbstractContainerMenu {
    // --- Slot layout ---
    private static final int WAND_INPUT_SLOT = 0;
    private static final int ARCANE_ITEM_SLOT = 1;
    private static final int LAPIS_INPUT_SLOT = 2;
    private static final int WAND_OUTPUT_SLOT = 3;
    private static final int SLOT_COUNT = 4;

    // --- Synced data layout ---
    private static final int DATA_XP_COST = 0;
    private static final int DATA_HAS_LAPIS = 1;
    private static final int DATA_CAN_ENCHANT = 2;
    private static final int DATA_COUNT = 3;

    private final Container blockInventory;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    // Client-side constructor:
    // Uses dummy containers so the menu can be constructed on the client
    // Real state is synced from the server through ContainerData and slot containers.
    public WandEnchantingMenu(int containerId, Inventory playerInventory) {
        this(
                containerId,
                playerInventory,
                new SimpleContainer(SLOT_COUNT),
                new SimpleContainerData(DATA_COUNT)
        );
    }

    // Server-side constructor:
    public WandEnchantingMenu(int containerId, Inventory playerInventory, Container blockInventory, ContainerData data) {
        super(ModMenus.WAND_ENCHANTING_MENU.get(), containerId);

        this.blockInventory = blockInventory;
        this.data = data;
        this.access = ContainerLevelAccess.NULL;
        //this.access = ContainerLevelAccess.create(playerInventory.player.level(), blockEntity.getBlockPos());

        // Add block entity slots.
        // Slot 0: Wand input
        this.addSlot(new Slot(blockInventory, WAND_INPUT_SLOT, 27, 47) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof AbstractChargedWandItem<?>;
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        // Slot 1: Arcane item input
        this.addSlot(new Slot(blockInventory, ARCANE_ITEM_SLOT, 49, 47) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return true; // TODO: implement helper function to check if item is a valid arcane item
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        // Slot 2: Lapis input
        this.addSlot(new Slot(blockInventory, LAPIS_INPUT_SLOT, 71, 47) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(Items.LAPIS_LAZULI);
            }

            @Override
            public int getMaxStackSize() {
                return 64;
            }
        });

        // Slot 3: Result wand
        this.addSlot(new Slot(blockInventory, WAND_OUTPUT_SLOT, 133, 47) {
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

        // Data sync.
        this.addDataSlots(data);

        // Initialize result state.
        // TODO: recomputeResult();
    }

    public int getXpCost() {
        return this.data.get(DATA_XP_COST);
    }

    public boolean hasLapis() {
        return this.data.get(DATA_HAS_LAPIS) != 0;
    }

    public boolean canEnchant() {
        return this.data.get(DATA_CAN_ENCHANT) != 0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        // TODO:
        // - handle shift-clicking between player inventory and table slots
        // - move wand to wand slot
        // - move lapis to lapis slot
        // - move arcane item to arcane item slot
        // - move result to player inventory
        Slot slot = this.slots.get(slotIndex);

        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stackInSlot = slot.getItem();
        ItemStack copy = stackInSlot.copy();

        // Result slot -> player inventory
        if (slotIndex == WAND_OUTPUT_SLOT) {
            if (!this.moveItemStackTo(stackInSlot, 4, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }

            slot.onQuickCraft(stackInSlot, copy);
        }
        // Block inventory -> player inventory

        // Player inventory -> block inventory

        return ItemStack.EMPTY;
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);

        // Only the real block inventory should drive recomputation.
        if (container == this.blockInventory && this.blockInventory instanceof BlockEntityWandEnchantingTable blockEntity) {
            // Recompute the derived state on the server side.
            // This should update:
            // - XP cost
            // - lapis present flag
            // - enchantability flag
            // - output slot contents
            //
            blockEntity.recomputeState();
        }
    }

    @Override
    public boolean stillValid(Player player) {
        // TODO: Use a proper access check: return blockEntity.stillValid(player);
        return this.blockInventory.stillValid(player);
    }
}
