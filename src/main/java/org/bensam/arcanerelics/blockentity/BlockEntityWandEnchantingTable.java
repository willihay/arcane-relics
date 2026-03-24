package org.bensam.arcanerelics.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.bensam.arcanerelics.ModBlockEntities;

public class BlockEntityWandEnchantingTable extends BlockEntity implements Container {
    // --- Slot layout ---
    // Slot 0: wand input
    // Slot 1: arcane enchantment item
    // Slot 2: lapis input
    // Slot 3: result/output
    private static final int INVENTORY_SIZE = 4;

    // slot storage
    private final NonNullList<ItemStack> items = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);

    // state sync data
    private int xpCost;
    private boolean hasLapis;
    private boolean canEnchant;

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

        setChanged();
    }

    @Override
    public int getContainerSize() {
        return INVENTORY_SIZE;
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.items.get(slot);
    }

    @Override
    public boolean isEmpty() {
        return this.items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(this.items, slot, amount);
        if (!result.isEmpty()) {
            setChanged();
        }

        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(this.items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack itemStack) {
        this.items.set(slot, itemStack); // TODO: menu should enforce specific items in specific slots and max stack size of 1 for all items except lapis, which is 64

        if (itemStack.getCount() > this.getMaxStackSize()) {
            itemStack.setCount(this.getMaxStackSize());
        }

        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return true; // TODO: replace with a real distance/block-state check to prevent "remote access shenanigans"
    }
    //endregion

    //region Persistence Methods

    @Override
    protected void loadAdditional(ValueInput valueInput) {
        super.loadAdditional(valueInput);
        ContainerHelper.loadAllItems(valueInput, this.items);
    }

    @Override
    protected void saveAdditional(ValueOutput valueOutput) {
        super.saveAdditional(valueOutput);
        ContainerHelper.saveAllItems(valueOutput, this.items);
    }
//endregion
}
