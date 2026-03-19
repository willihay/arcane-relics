package org.bensam.arcanerelics.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import org.bensam.arcanerelics.ArcaneRelics;
import org.bensam.arcanerelics.ModComponents;

public abstract class AbstractChargedWandItem<R extends Enum<R> & RechargeResult> extends Item {
    private final int initialCharges;
    private final int maxCharges;

    public AbstractChargedWandItem(Properties properties, int initialCharges, int maxCharges) {
        super(properties);
        this.initialCharges = initialCharges;
        this.maxCharges = maxCharges;
    }

    //region Helper Functions
    public int addCharges(ItemStack stack, int amount) {
        int currentCharges = this.getCharges(stack);
        int newCharges = Math.min(currentCharges + amount, this.getMaxCharges());
        this.setCharges(stack, newCharges);
        return newCharges;
    }

    public void consumeCharges(ItemStack stack, int amount) {
        this.setCharges(stack, this.getCharges(stack) - amount);
    }

    public Component getChargeDisplayMessage(ItemStack stack) {
        int charges = this.getCharges(stack);
        int maxCharges = this.getMaxCharges();
        return Component.translatable(
                "message." + ArcaneRelics.MOD_ID + ".wand.charges",
                charges,
                maxCharges
        ).withStyle(ChatFormatting.GOLD);
    }

    public int getCharges(ItemStack stack) {
        return stack.getOrDefault(
                ModComponents.WAND_CHARGES_COMPONENT,
                new ModComponents.WandChargesComponent(this.initialCharges)
        ).charges();
    }

    protected int getElapsedTicks(ItemStack stack, LivingEntity entity, int remainingUseDuration) {
        return this.getUseDuration(stack, entity) - remainingUseDuration;
    }

    public int getMaxCharges() {
        return this.maxCharges;
    }

    public Component getFullyChargedMessage() {
        return Component.translatable("message." + ArcaneRelics.MOD_ID + ".wand.recharge.fully_charged");
    }

    public Component getNoChargesMessage() {
        return Component.translatable("message." + ArcaneRelics.MOD_ID + ".wand.cast.no_charges");
    }

    protected int getPowerUpCost(Level level, Player player, ItemStack stack, int chargeTicks, boolean fullyCharged) {
        return fullyCharged ? this.getFullPowerCastCost() : this.getNormalCastCost();
    }

    protected float getPowerUpPercentage(int elapsedTicks) {
        return Mth.clamp((float) elapsedTicks / this.getFullPowerTicks(), 0.0f, 1.0f);
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000; // same as bow; effectively as long as you want
    }

    public boolean hasAtLeastCharges(ItemStack stack, int amount) {
        return this.getCharges(stack) >= amount;
    }

    public boolean hasCharges(ItemStack stack) {
        return this.getCharges(stack) > 0;
    }

    public boolean isFullyCharged(ItemStack stack) {
        return this.getCharges(stack) >= this.getMaxCharges();
    }

    protected boolean isFullyPoweredUp(int elapsedTicks) {
        return elapsedTicks >= this.getFullPowerTicks();
    }

    protected void setCharges(ItemStack stack, int charges) {
        stack.set(
                ModComponents.WAND_CHARGES_COMPONENT,
                new ModComponents.WandChargesComponent(Math.max(0, Math.min(charges, this.maxCharges)))
        );
    }
    //endregion

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // If sneaking, try to recharge.
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide()) {
                R result = this.tryRecharge(level, player, stack);
                this.sendRechargeFeedback(player, result);
            }
            return InteractionResult.SUCCESS;
        }

        // If not sneaking, start normal power-up behavior.
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        super.onUseTick(level, entity, stack, remainingUseDuration);

        int elapsedTicks = this.getElapsedTicks(stack, entity, remainingUseDuration);
        if (this.isFullyPoweredUp(elapsedTicks)) {
            stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        } else {
            stack.remove(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
        }
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        stack.remove(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);

        // The remaining logic is for server-side only.
        if (level.isClientSide()) {
            return true;
        }

        // Players are the only entities who will use a lightning wand.
        if (!(entity instanceof Player player)) {
            return false;
        }

        // Determine power-up status.
        int elapsedTicks = this.getElapsedTicks(stack, entity, timeLeft);
        float powerUpPercentage = this.getPowerUpPercentage(elapsedTicks);
        boolean isFullyPowered = elapsedTicks >= this.getFullPowerTicks();

        // Calculate charge cost.
        int chargeCost = this.getPowerUpCost(level, player, stack, elapsedTicks, isFullyPowered);

        // Check if wand has enough charges remaining to complete the cast.
        if (chargeCost > 0 && !this.hasAtLeastCharges(stack, chargeCost)) {
            player.displayClientMessage(this.getNoChargesMessage(), true);
            return true;
        }

        // Perform cast.
        boolean castSucceeded = this.performCast(level, player, stack, powerUpPercentage, isFullyPowered);

        // Consume charges.
        if (castSucceeded && chargeCost > 0) {
            this.consumeCharges(stack, chargeCost);
        }

        return true;
    }

    protected boolean consumeArcaneFuelFromInventory(Player player, Item item) {
        var inventory = player.getInventory();
        int inventorySize = inventory.getContainerSize();
        for (int slot = 0; slot < inventorySize; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.is(item)) {
                stack.shrink(1);
                inventory.setChanged();
                return true;
            }
        }
        return false;
    }

    //region Abstract Functions
    protected abstract int getFullPowerTicks();
    protected abstract int getNormalCastCost();
    protected abstract int getFullPowerCastCost();

    protected abstract R tryRecharge(Level level, Player player, ItemStack wandStack);
    protected abstract void sendRechargeFeedback(Player player, R result);

    protected abstract boolean performCast(
            Level level,
            Player player,
            ItemStack stack,
            float powerUpPercentage,
            boolean isFullyPowered
    );
    //endregion
}
