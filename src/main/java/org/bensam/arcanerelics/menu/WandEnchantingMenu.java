package org.bensam.arcanerelics.menu;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import org.bensam.arcanerelics.ModBlocks;
import org.bensam.arcanerelics.ModItems;
import org.bensam.arcanerelics.ModMenus;
import org.bensam.arcanerelics.ModStats;
import org.bensam.arcanerelics.blockentity.BlockEntityWandEnchantingTable;
import org.jspecify.annotations.NonNull;

public class WandEnchantingMenu extends AbstractContainerMenu {
    // --- Recipe slot layout ---
    private static final int WAND_INPUT_SLOT = 0;
    public static final int WAND_INPUT_SLOT_X = 8;
    private static final int ARCANE_ITEM_SLOT = 1;
    private static final int ARCANE_ITEM_SLOT_X = 26;
    private static final int LAPIS_INPUT_SLOT = 2;
    public static final int LAPIS_INPUT_SLOT_X = 44;
    private static final int WAND_OUTPUT_SLOT = 3;
    private static final int WAND_OUTPUT_SLOT_X = 98;
    public static final int RECIPE_ROW_Y = 48;
    private static final int BLOCK_SLOT_COUNT = 4;

    // --- Other slot layout ---
    private static final int PLAYER_INVENTORY_ROW_X = 8;
    private static final int PLAYER_INVENTORY_ROW_Y = 96;
    private static final int FIRST_PLAYER_SLOT = BLOCK_SLOT_COUNT;
    private static final int FIRST_HOTBAR_SLOT = FIRST_PLAYER_SLOT + 27;

    // --- Synced data layout ---
    private static final int DATA_XP_COST = 0;
    private static final int DATA_HAS_VALID_RECIPE = 1;
    private static final int DATA_COUNT = 2;

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
                new SimpleContainer(BLOCK_SLOT_COUNT),
                new SimpleContainerData(DATA_COUNT),
                ContainerLevelAccess.NULL
        );
    }

    // Server-side constructor:
    public WandEnchantingMenu(int containerId, Inventory playerInventory, Container blockInventory, ContainerData data, ContainerLevelAccess access) {
        super(ModMenus.WAND_ENCHANTING_MENU.get(), containerId);

        this.blockInventory = blockInventory;
        this.data = data;
        this.access = access;

        // Add block entity slots.
        // Slot 0: Wand input
        this.addSlot(new Slot(blockInventory, WAND_INPUT_SLOT, WAND_INPUT_SLOT_X, RECIPE_ROW_Y) {
            @Override
            public boolean mayPlace(@NonNull ItemStack stack) {
                return BlockEntityWandEnchantingTable.isArcaneWand(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 64;
            }
        });

        // Slot 1: Arcane item input
        this.addSlot(new Slot(blockInventory, ARCANE_ITEM_SLOT, ARCANE_ITEM_SLOT_X, RECIPE_ROW_Y) {
            @Override
            public boolean mayPlace(@NonNull ItemStack stack) {
                return ModItems.isArcaneEnchantmentItem(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 64;
            }
        });

        // Slot 2: Lapis input
        this.addSlot(new Slot(blockInventory, LAPIS_INPUT_SLOT, LAPIS_INPUT_SLOT_X, RECIPE_ROW_Y) {
            @Override
            public boolean mayPlace(@NonNull ItemStack stack) {
                return stack.is(Items.LAPIS_LAZULI);
            }

            @Override
            public int getMaxStackSize() {
                return 64;
            }
        });

        // Slot 3: Result wand
        this.addSlot(new Slot(blockInventory, WAND_OUTPUT_SLOT, WAND_OUTPUT_SLOT_X, RECIPE_ROW_Y) {
            @Override
            public boolean mayPlace(@NonNull ItemStack stack) {
                return false;
            }

            @Override
            public boolean mayPickup(@NonNull Player player) {
                return WandEnchantingMenu.this.canPickupResult(player);
            }

            @Override
            public void onTake(@NonNull Player player, @NonNull ItemStack stack) {
                WandEnchantingMenu.this.onResultTake(player, stack);
                super.onTake(player, stack);
            }
        });

        // Add the player inventory slots.
        this.addStandardInventorySlots(playerInventory, PLAYER_INVENTORY_ROW_X, PLAYER_INVENTORY_ROW_Y);

        // Data sync.
        this.addDataSlots(data);

        // Initialize the result state on the server.
        if (this.blockInventory instanceof BlockEntityWandEnchantingTable blockEntity) {
            blockEntity.recomputeState();
        }
    }

    protected boolean canMoveIntoInputSlots(ItemStack itemStack) {
        if (BlockEntityWandEnchantingTable.isArcaneWand(itemStack))
        {
            if (itemStack.isStackable()) {
                return this.getSlot(WAND_INPUT_SLOT).getItem().getCount() < this.getSlot(WAND_INPUT_SLOT).getMaxStackSize();
            }
            else {
                return !this.getSlot(WAND_INPUT_SLOT).hasItem();
            }
        }
        else if (itemStack.is(Items.LAPIS_LAZULI))
        {
            return this.getSlot(LAPIS_INPUT_SLOT).getItem().getCount() < this.getSlot(LAPIS_INPUT_SLOT).getMaxStackSize();
        }

        return ModItems.isArcaneEnchantmentItem(itemStack) && this.getSlot(ARCANE_ITEM_SLOT).getItem().getCount() < this.getSlot(ARCANE_ITEM_SLOT).getMaxStackSize();
    }

    public boolean canPickupResult(Player player) {
        return player.hasInfiniteMaterials() || player.experienceLevel >= this.getXpCost();
    }

    public int getXpCost() {
        return this.data.get(DATA_XP_COST);
    }

    public boolean hasAnyInputItems() {
        return this.getSlot(WAND_INPUT_SLOT).hasItem() || this.getSlot(ARCANE_ITEM_SLOT).hasItem() || this.getSlot(LAPIS_INPUT_SLOT).hasItem();
    }

    public boolean hasLapis() {
        return this.getSlot(LAPIS_INPUT_SLOT).hasItem();
    }

    public boolean hasWand() {
        return this.getSlot(WAND_INPUT_SLOT).hasItem();
    }

    public boolean hasValidRecipe() {
        return this.data.get(DATA_HAS_VALID_RECIPE) != 0;
    }

    protected boolean isValidBlock(BlockState blockState) {
        return blockState.is(ModBlocks.WAND_ENCHANTING_TABLE.get());
    }

    protected void onResultTake(Player player, ItemStack stack) {
        stack.onCraftedBy(player, stack.getCount());

        if (ItemStack.isSameItem(stack, this.getSlot(WAND_INPUT_SLOT).getItem())) {
            player.awardStat(ModStats.getWandsRechargedStat());
        } else {
            player.awardStat(ModStats.getWandsEnchantedStat());
        }

        // Consume player XP.
        if (!player.hasInfiniteMaterials()) {
            player.giveExperienceLevels(-this.getXpCost());

            // Consume input wand, enchantment item, and lapis.
            this.shrinkStackInSlot(WAND_INPUT_SLOT);
            this.shrinkStackInSlot(ARCANE_ITEM_SLOT);
            this.shrinkStackInSlot(LAPIS_INPUT_SLOT);
        }

        // Refresh result and synced flags immediately.
        if (this.blockInventory instanceof BlockEntityWandEnchantingTable blockEntity) {
            blockEntity.recomputeState();
        }
    }

    @Override
    public @NonNull ItemStack quickMoveStack(@NonNull Player player, int slotIndex) {
        Slot slot = this.slots.get(slotIndex);

        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        int inventorySize = this.slots.size();
        ItemStack sourceStack = slot.getItem();
        ItemStack returnStack = sourceStack.copy();

        // Result slot -> player inventory
        if (slotIndex == WAND_OUTPUT_SLOT) {
            if (!this.moveItemStackTo(sourceStack, FIRST_PLAYER_SLOT, inventorySize, true)) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, returnStack);
        }
        // Input slot inventory -> player inventory
        else if (slotIndex < WAND_OUTPUT_SLOT) {
            if (!this.moveItemStackTo(sourceStack, FIRST_PLAYER_SLOT, inventorySize, false)) {
                return ItemStack.EMPTY;
            }
        }
        // Player inventory -> input slot
        else if (canMoveIntoInputSlots(sourceStack)) {
            if (!this.moveItemStackTo(sourceStack, 0, BLOCK_SLOT_COUNT - 1, false)) {
                return ItemStack.EMPTY;
            }
        }
        // Standard player inventory -> hotbar
        else if (slotIndex < FIRST_HOTBAR_SLOT) {
            if (!this.moveItemStackTo(sourceStack, FIRST_HOTBAR_SLOT, inventorySize, false)) {
                return ItemStack.EMPTY;
            }
        }
        // Hotbar -> standard player inventory
        else {
            if (!this.moveItemStackTo(sourceStack, FIRST_PLAYER_SLOT, FIRST_HOTBAR_SLOT, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (sourceStack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (sourceStack.getCount() == returnStack.getCount()) {
            return ItemStack.EMPTY;
        }

        return returnStack;
    }

    private void shrinkStackInSlot(int i) {
        Slot slot = this.slots.get(i);
        ItemStack itemStack = slot.getItem();
        if (!itemStack.isEmpty()) {
            itemStack.shrink(1);
            slot.setChanged();
        }
    }

    @Override
    public void slotsChanged(@NonNull Container container) {
        super.slotsChanged(container);

        // Only the real block inventory should drive recomputation.
        if (container == this.blockInventory && this.blockInventory instanceof BlockEntityWandEnchantingTable blockEntity) {
            // Recompute the derived state on the server side.
            blockEntity.recomputeState();
        }
    }

    @Override
    public boolean stillValid(@NonNull Player player) {
        return this.access
                .evaluate((level, blockPos) ->
                        this.isValidBlock(level.getBlockState(blockPos))
                        && player.isWithinBlockInteractionRange(blockPos, 4.0),
                        true
                );
    }
}
