package org.bensam.arcanerelics.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.bensam.arcanerelics.ModBlockEntities;
import org.bensam.arcanerelics.menu.WandEnchantingContainerData;

public class BlockEntityWandEnchantingTable extends BlockEntity implements Container {
    // --- Slot layout ---
    // Slot 0: wand input
    // Slot 1: arcane enchantment item
    // Slot 2: lapis input
    // Slot 3: result/output
    private static final int WAND_INPUT_SLOT = 0;
    private static final int ARCANE_ITEM_SLOT = 1;
    private static final int LAPIS_INPUT_SLOT = 2;
    private static final int WAND_OUTPUT_SLOT = 3;
    public static final int INVENTORY_SIZE = 4;

    // slot storage
    private final NonNullList<ItemStack> items = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);

    // state sync data
    public final WandEnchantingContainerData containerData = new WandEnchantingContainerData();
    private int xpCost;
    private boolean hasLapis;
    private boolean hasRecipeError;
    private boolean hasWand;

    public BlockEntityWandEnchantingTable(BlockPos blockPos, BlockState blockState) {
        super(ModBlockEntities.WAND_ENCHANTING_TABLE.get(), blockPos, blockState);
    }

    //region Helper Methods
    @Override
    public void clearContent() {
        this.items.clear();
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            this.items.add(ItemStack.EMPTY);
        }
        this.recomputeState();
    }

    @Override
    public int getContainerSize() {
        return INVENTORY_SIZE;
    }

    @Override
    public ItemStack getItem(int slotIndex) {
        return this.items.get(slotIndex);
    }

    @Override
    public boolean isEmpty() {
        return this.items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack removeItem(int slotIndex, int amount) {
        ItemStack result = ContainerHelper.removeItem(this.items, slotIndex, amount);
        if (!result.isEmpty()) {
            this.recomputeState();
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slotIndex) {
        ItemStack result = ContainerHelper.takeItem(this.items, slotIndex);
        this.recomputeState();
        return result;
    }

    @Override
    public void setItem(int slotIndex, ItemStack itemStack) {
        this.items.set(slotIndex, itemStack);

        if (itemStack.getCount() > this.getMaxStackSize()) {
            itemStack.setCount(this.getMaxStackSize());
        }

        this.recomputeState();
    }

    @Override
    public boolean stillValid(Player player) {
        return player.isWithinBlockInteractionRange(this.getBlockPos(), 4.0);
    }
    //endregion

    //region Container Data Helper Methods
    public ContainerData getMenuData() {
        return this.containerData;
    }

    public int getXpCost() {
        return this.xpCost;
    }

    public boolean hasLapis() {
        return this.hasLapis;
    }

    public boolean hasRecipeError() {
        return this.hasRecipeError;
    }

    public boolean hasWand() { return this.hasWand;
    }
    //endregion

    public void recomputeState() {
        ItemStack wandStack = getItem(WAND_INPUT_SLOT);
        ItemStack arcaneStack = getItem(ARCANE_ITEM_SLOT);
        ItemStack lapisStack = getItem(LAPIS_INPUT_SLOT);

        boolean validWand = !wandStack.isEmpty(); // TODO: replace with wand predicate
        boolean validArcaneItem = !arcaneStack.isEmpty(); // TODO: replace with arcane-item predicate
        this.hasWand = validWand;
        this.hasLapis = !lapisStack.isEmpty() && lapisStack.is(Items.LAPIS_LAZULI);
        this.hasRecipeError = !validWand || !validArcaneItem || !this.hasLapis;

        // TODO: compute real cost from wand + arcane item + lapis rules
        this.xpCost = this.hasRecipeError ? 0 : 1;

        this.recomputeContainerData();
        this.recomputeWandOutput();

        setChanged();
    }

    private void recomputeContainerData() {
        this.containerData.setXpCost(this.xpCost);
        this.containerData.setHasLapis(this.hasLapis);
        this.containerData.setHasRecipeError(this.hasRecipeError);
        this.containerData.setHasWand(this.hasWand);
    }

    private void recomputeWandOutput() {
        // TODO: compute wand output based on wand and arcane item
        // - inspect wand input
        // - inspect arcane item input
        // - inspect lapis availability
        // - compute the output ItemStack
        // - place the output into WAND_OUTPUT_SLOT

        ItemStack currentOutput = getItem(WAND_OUTPUT_SLOT);
        ItemStack newOutput = currentOutput.copy();

        if (this.hasRecipeError) {
            newOutput = ItemStack.EMPTY;
        } else {
            // TODO: create the actual enchanted/recharged wand result
            // newOutput = computedResultStack;
        }

        // Only write if the stack actually changed.
        if (!ItemStack.isSameItemSameComponents(currentOutput, newOutput)) {
            this.items.set(WAND_OUTPUT_SLOT, newOutput);
        }
    }

    //region Persistence Methods
    @Override
    protected void loadAdditional(ValueInput valueInput) {
        super.loadAdditional(valueInput);
        ContainerHelper.loadAllItems(valueInput, this.items);

        this.recomputeState();
    }

    @Override
    protected void saveAdditional(ValueOutput valueOutput) {
        super.saveAdditional(valueOutput);
        ContainerHelper.saveAllItems(valueOutput, this.items);
    }
    //endregion
}
