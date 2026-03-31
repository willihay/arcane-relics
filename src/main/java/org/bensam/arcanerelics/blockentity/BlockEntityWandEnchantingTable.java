package org.bensam.arcanerelics.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
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
import org.bensam.arcanerelics.ModItems;
import org.bensam.arcanerelics.item.AbstractChargedWandItem;
import org.bensam.arcanerelics.menu.WandEnchantingContainerData;
import org.jspecify.annotations.NonNull;

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
    private boolean hasValidRecipe;

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
    public @NonNull ItemStack getItem(int slotIndex) {
        return this.items.get(slotIndex);
    }

    public ContainerData getMenuData() {
        return this.containerData;
    }

    public static boolean isArcaneWand(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof AbstractChargedWandItem;
    }

    public static boolean isArcaneEnchantmentItem(ItemStack stack) {
        return ModItems.isArcaneEnchantmentItem(stack);
    }

    @Override
    public boolean isEmpty() {
        return this.items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public @NonNull ItemStack removeItem(int slotIndex, int amount) {
        ItemStack result = ContainerHelper.removeItem(this.items, slotIndex, amount);
        if (!result.isEmpty()) {
            this.recomputeState();
        }
        return result;
    }

    @Override
    public @NonNull ItemStack removeItemNoUpdate(int slotIndex) {
        ItemStack result = ContainerHelper.takeItem(this.items, slotIndex);
        this.recomputeState();
        return result;
    }

    @Override
    public void setItem(int slotIndex, @NonNull ItemStack itemStack) {
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

    public void recomputeState() {
        ItemStack wandStack = getItem(WAND_INPUT_SLOT);
        ItemStack arcaneStack = getItem(ARCANE_ITEM_SLOT);
        ItemStack lapisStack = getItem(LAPIS_INPUT_SLOT);

        this.hasValidRecipe = isArcaneWand(wandStack)
                && isArcaneEnchantmentItem(arcaneStack)
                && lapisStack.is(Items.LAPIS_LAZULI);

        this.recomputeWandOutput();
        this.recomputeContainerData();

        setChanged();
    }

    private void recomputeContainerData() {
        this.containerData.setXpCost(this.xpCost);
        this.containerData.setHasValidRecipe(this.hasValidRecipe);
    }

    // Assumes hasValidRecipe is up to date.
    private void recomputeWandOutput() {
        ItemStack inputWand = getItem(WAND_INPUT_SLOT);
        ItemStack currentOutputWand = getItem(WAND_OUTPUT_SLOT);
        ItemStack newOutputWand = ItemStack.EMPTY;
        this.xpCost = 0;

        if (this.hasValidRecipe) {
            ItemStack recipeWand = ModItems.getArcaneEnchantmentItem(getItem(ARCANE_ITEM_SLOT));

            // Verify we're dealing with wands.
            if (!recipeWand.isEmpty()
                    && recipeWand.getItem() instanceof AbstractChargedWandItem newOutputWandItem
                    && inputWand.getItem() instanceof AbstractChargedWandItem inputWandItem) {
                // Make the output wand and set charges.
                newOutputWand = recipeWand.copy();
                newOutputWand.setCount(1);

                // If input and output are the same wand type, carry charges and any custom name over and recharge.
                if (inputWand.is(newOutputWand.getItem())) {
                    // Preserve the custom name if the input wand was named.
                    if (inputWand.has(DataComponents.CUSTOM_NAME)) {
                        newOutputWand.set(DataComponents.CUSTOM_NAME, inputWand.getCustomName());
                    }

                    newOutputWandItem.setCharges(newOutputWand, inputWandItem.getCharges(inputWand));
                    newOutputWandItem.addCharges(newOutputWand, newOutputWandItem.getRechargeChargeAmount());

                    this.xpCost = newOutputWandItem.getRechargeXpCost();
                }
                // (Otherwise, leave the default number of charges in the new wand.)
                else {
                    this.xpCost = newOutputWandItem.getNewWandXpCost();
                }
            }
        }

        // Only write if the stack actually changed.
        if (!ItemStack.isSameItemSameComponents(currentOutputWand, newOutputWand)) {
            this.items.set(WAND_OUTPUT_SLOT, newOutputWand);
        }
    }

    //region Persistence Methods
    @Override
    protected void loadAdditional(@NonNull ValueInput valueInput) {
        super.loadAdditional(valueInput);
        ContainerHelper.loadAllItems(valueInput, this.items);

        this.recomputeState();
    }

    @Override
    protected void saveAdditional(@NonNull ValueOutput valueOutput) {
        super.saveAdditional(valueOutput);
        ContainerHelper.saveAllItems(valueOutput, this.items);
    }
    //endregion
}
