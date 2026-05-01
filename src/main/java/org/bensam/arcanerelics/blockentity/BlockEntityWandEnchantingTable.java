package org.bensam.arcanerelics.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.bensam.arcanerelics.ModBlockEntities;
import org.bensam.arcanerelics.ModItems;
import org.bensam.arcanerelics.block.BlockWandEnchantingTable;
import org.bensam.arcanerelics.config.ModServerConfigManager;
import org.bensam.arcanerelics.item.AbstractChargedWandItem;
import org.bensam.arcanerelics.item.WandEnchantingTableOutput;
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
        this.recomputeState(true);
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

    public @NonNull ItemStack getRenderedWand() {
        return this.items.get(WAND_INPUT_SLOT);
    }

    @Override
    public @NonNull CompoundTag getUpdateTag(HolderLookup.@NonNull Provider provider) {
        return this.saveCustomOnly(provider);
    }

    @Override
    public @NonNull Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
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
            this.recomputeState(true);
        }
        return result;
    }

    @Override
    public @NonNull ItemStack removeItemNoUpdate(int slotIndex) {
        ItemStack result = ContainerHelper.takeItem(this.items, slotIndex);
        this.recomputeState(true);
        return result;
    }

    @Override
    public void setChanged() {
        super.setChanged();

        if (this.level != null) {
            // Send update to neighbors AND clients (bitmask = 3) since this block's light level can change.
            this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    public void setItem(int slotIndex, @NonNull ItemStack itemStack) {
        this.items.set(slotIndex, itemStack);

        if (itemStack.getCount() > this.getMaxStackSize()) {
            itemStack.setCount(this.getMaxStackSize());
        }

        this.recomputeState(true);
    }

    @Override
    public boolean stillValid(Player player) {
        return player.isWithinBlockInteractionRange(this.getBlockPos(), 4.0);
    }
    //endregion

    public void recomputeState(boolean canMarkChanged) {
        if (!(this.level instanceof ServerLevel)) {
            return;
        }

        // Save current state.
        ItemStack previousOutput = this.items.get(WAND_OUTPUT_SLOT).copy();
        int previousXpCost = this.xpCost;
        boolean previousHasValidRecipe = this.hasValidRecipe;

        // Recompute based on current input slots.
        ItemStack wandStack = getItem(WAND_INPUT_SLOT);
        ItemStack arcaneStack = getItem(ARCANE_ITEM_SLOT);
        ItemStack lapisStack = getItem(LAPIS_INPUT_SLOT);

        this.hasValidRecipe = isArcaneWand(wandStack)
                && isArcaneEnchantmentItem(arcaneStack)
                && lapisStack.is(Items.LAPIS_LAZULI);

        this.recomputeWandOutput();
        this.recomputeContainerData();
        this.syncLapisBlockState(lapisStack.is(Items.LAPIS_LAZULI) && !lapisStack.isEmpty());

        // Compare previous state with recomputed state to see if anything has changed.
        boolean changed = !ItemStack.isSameItemSameComponents(previousOutput, this.items.get(WAND_OUTPUT_SLOT))
                || previousXpCost != this.xpCost
                || previousHasValidRecipe != this.hasValidRecipe;

        if (canMarkChanged && changed) {
            setChanged();
        }
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

        // Validate current input wand charges are within config-defined parameters.
        if (isArcaneWand(inputWand)) {
            ((AbstractChargedWandItem) inputWand.getItem()).normalizeCharges(inputWand);
        }

        if (this.hasValidRecipe) {
            ItemStack arcaneItem = getItem(ARCANE_ITEM_SLOT);
            ItemStack recipeWand = ModItems.getWandEnchantmentOutput(arcaneItem);

            // Get the wand item class instances so we can set the charges in the output wand and calculate XP cost.
            if (!recipeWand.isEmpty()
                    && recipeWand.getItem() instanceof AbstractChargedWandItem newOutputWandItem
                    && inputWand.getItem() instanceof AbstractChargedWandItem inputWandItem) {
                // Make the output wand and set charges.
                newOutputWand = recipeWand.copy();
                newOutputWand.setCount(1);

                // Determine if the arcane item has a higher enchantment level than 1 or is an extended potion and adjust charge bonus accordingly.
                int arcaneItemLevel = 1;
                if (recipeWand.getItem() instanceof WandEnchantingTableOutput outputItem && arcaneItem.is(Items.ENCHANTED_BOOK)) {
                    arcaneItemLevel = outputItem.getLevelOfEnchantmentItem(arcaneItem);
                }

                // If input and output are the same wand type, carry charges and any custom name over and recharge.
                if (inputWand.is(newOutputWand.getItem())) {
                    // Preserve the custom name if the input wand was named.
                    if (inputWand.has(DataComponents.CUSTOM_NAME)) {
                        newOutputWand.set(DataComponents.CUSTOM_NAME, inputWand.getCustomName());
                    }

                    newOutputWandItem.setCharges(newOutputWand, inputWandItem.getCharges(inputWand));
                    newOutputWandItem.addCharges(newOutputWand, newOutputWandItem.getRechargeChargeAmount(arcaneItemLevel));

                    this.xpCost = ModServerConfigManager.getConfig().wandEnchantingTable().enableRechargeWandXpCost() ? newOutputWandItem.getRechargeXpCost() : 0;
                }
                // Otherwise, calculate and set the initial number of charges for the new wand.
                else {
                    newOutputWandItem.setCharges(newOutputWand, newOutputWandItem.getNewWandCharges(arcaneItemLevel));
                    this.xpCost = ModServerConfigManager.getConfig().wandEnchantingTable().enableEnchantWandXpCost() ? newOutputWandItem.getNewWandXpCost() : 0;
                }
            }
        }

        // Only write if the stack actually changed.
        if (!ItemStack.isSameItemSameComponents(currentOutputWand, newOutputWand)) {
            this.items.set(WAND_OUTPUT_SLOT, newOutputWand);
        }
    }

    private void syncLapisBlockState(boolean hasLapis) {
        BlockState blockState = this.getBlockState();
        if (blockState.hasProperty(BlockWandEnchantingTable.HAS_LAPIS)
                && blockState.getValue(BlockWandEnchantingTable.HAS_LAPIS) != hasLapis) {
            this.level.setBlock(this.getBlockPos(), blockState.setValue(BlockWandEnchantingTable.HAS_LAPIS, hasLapis), 3);
        }
    }

    //region Persistence Methods
    @Override
    protected void loadAdditional(@NonNull ValueInput valueInput) {
        super.loadAdditional(valueInput);
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            this.items.set(i, ItemStack.EMPTY);
        }
        ContainerHelper.loadAllItems(valueInput, this.items);
    }

    @Override
    protected void saveAdditional(@NonNull ValueOutput valueOutput) {
        super.saveAdditional(valueOutput);

        // Only persist the input slots. The output slot is always computed.
        NonNullList<ItemStack> inputItems = NonNullList.withSize(WAND_OUTPUT_SLOT, ItemStack.EMPTY);
        for (int i = 0; i < WAND_OUTPUT_SLOT; i++) {
            inputItems.set(i, this.items.get(i));
        }
        ContainerHelper.saveAllItems(valueOutput, inputItems);
    }

    @Override
    protected void applyImplicitComponents(@NonNull DataComponentGetter dataComponentGetter) {
        super.applyImplicitComponents(dataComponentGetter);
        // Restore input slot items when BlockItem is placed as a Block/Block Entity.
        dataComponentGetter.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(this.items);
        if (this.level instanceof ServerLevel) {
            this.recomputeState(false);
        }
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.@NonNull Builder builder) {
        super.collectImplicitComponents(builder);
        // Collect items in input slots to save in components container in BlockItem when block breaks.
        builder.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(this.items.subList(0, WAND_OUTPUT_SLOT)));
    }

    @SuppressWarnings("deprecation")
    @Override
    public void removeComponentsFromTag(@NonNull ValueOutput valueOutput) {
        super.removeComponentsFromTag(valueOutput);
        valueOutput.discard("Items"); // no need for Items tag when representing this block entity as components
    }

    @Override
    public void preRemoveSideEffects(@NonNull BlockPos blockPos, @NonNull BlockState blockState) {
        // Overriding with an empty method prevents spilling contents on block break.
    }
    //endregion
}
