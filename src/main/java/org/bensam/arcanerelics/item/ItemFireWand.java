package org.bensam.arcanerelics.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.bensam.arcanerelics.ArcaneRelics;
import org.jspecify.annotations.Nullable;

public class ItemFireWand extends AbstractChargedWandItem {
    public static final int INITIAL_CHARGES = 30;
    public static final int MAX_CHARGES = 50;
    private static final int FULL_CHARGE_TICKS = 20;

    private static final int NORMAL_CAST_COST = 1;
    private static final int FULL_POWER_CAST_COST = 2;

    public static final int GHAST_EXTRACTION_RECHARGE_RADIUS = 20;
    private static final int GHAST_TEAR_RECHARGE_AMOUNT = 30;
    private static final int GHAST_TEAR_RECHARGE_THRESHOLD = MAX_CHARGES - (GHAST_TEAR_RECHARGE_AMOUNT / 2);

    public static final float BASE_EXPLOSION_POWER = 0.5f;
    public static final float MAX_EXPLOSION_POWER = 2.0f;

    private enum RechargeResult {
        GHAST_EXTRACTION_SUCCESS,
        GHAST_TEAR_SUCCESS,
        NO_GHAST_TEAR,
        TOO_CHARGED_FOR_GHAST_TEAR,
        ALREADY_FULL
    }

    public ItemFireWand(Properties properties) {
        super(properties, INITIAL_CHARGES, MAX_CHARGES);
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000; // same as bow; effectively as long as you want
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // If sneaking, try to recharge.
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide()) {
                ItemFireWand.RechargeResult result = this.tryRecharge(level, player, stack);
                this.sendRechargeFeedback(player, result);
            }
            return InteractionResult.SUCCESS;
        }

        // If not sneaking, start normal charge-up behavior.
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        super.onUseTick(level, livingEntity, stack, remainingUseDuration);

        int elapsed = this.getUseDuration(stack, livingEntity) - remainingUseDuration;
        boolean fullyCharged = elapsed >= FULL_CHARGE_TICKS;

        if (fullyCharged) {
            stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        } else {
            stack.remove(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
        }
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        stack.remove(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);

        // The remaining logic is for server-side only.
        if (level.isClientSide())
            return true;

        // Players are the only entities who will use a lightning wand.
        if (!(entity instanceof Player player))
            return false;

        // Determine charge status.
        int useDuration = this.getUseDuration(stack, entity);
        int chargeTicks = useDuration - timeLeft; // how long they held right-click
        boolean fullyCharged = chargeTicks >= FULL_CHARGE_TICKS;

        // Calculate charge cost.
        int chargeCost = fullyCharged ? FULL_POWER_CAST_COST : NORMAL_CAST_COST;

        // Check if wand has enough charges remaining to complete the cast.
        if (chargeCost > 0 && !this.hasAtLeastCharges(stack, chargeCost)) {
            player.displayClientMessage(
                    Component.translatable("message." + ArcaneRelics.MOD_ID + ".wand.cast.no_charges"),
                    true
            );
            return true;
        }

        // Clamp charge between 0.0 and 1.0.
        float charge = Mth.clamp((float) chargeTicks / FULL_CHARGE_TICKS, 0.0f, 1.0f);

        // Scale fireball damage by charge.
        int explosionPower = Math.round(Mth.lerp(charge, BASE_EXPLOSION_POWER, MAX_EXPLOSION_POWER));

        // Shoot fireball and consume charges.
        if (shootFireball(level, player, explosionPower, fullyCharged) && chargeCost > 0) {
            this.consumeCharges(stack, chargeCost);
        }

        return true;
    }

    private RechargeResult tryRecharge(Level level, Player player, ItemStack wandStack) {
        if (this.isFullyCharged(wandStack)) {
            return RechargeResult.ALREADY_FULL;
        }

        BlockPos ghastPos = findNearbyGhast(level, player.blockPosition(), GHAST_EXTRACTION_RECHARGE_RADIUS);
        boolean foundGhast = ghastPos != null;
        if (foundGhast) {
            this.setCharges(wandStack, this.getMaxCharges());
            return RechargeResult.GHAST_EXTRACTION_SUCCESS;
        }

        return this.tryGhastTearRecharge(player, wandStack);
    }

    private static @Nullable BlockPos findNearbyGhast(Level level, BlockPos center, int radius) {
        BlockPos closestGhast = null;
        double closestDistanceSq = Double.MAX_VALUE;

        AABB searchBox = new AABB(
                center.getX() - radius, center.getY() - radius, center.getZ() - radius,
                center.getX() + radius + 1, center.getY() + radius + 1, center.getZ() + radius + 1
        );

        for (Ghast ghast : level.getEntitiesOfClass(Ghast.class, searchBox)) {
            if (!ghast.isAlive()) {
                continue;
            }

            double distanceSq = ghast.blockPosition().distSqr(center);
            if (distanceSq < closestDistanceSq) {
                closestDistanceSq = distanceSq;
                closestGhast = ghast.blockPosition().immutable();
            }
        }

        for (HappyGhast ghast : level.getEntitiesOfClass(HappyGhast.class, searchBox)) {
            if (!ghast.isAlive()) {
                continue;
            }

            double distanceSq = ghast.blockPosition().distSqr(center);
            if (distanceSq < closestDistanceSq) {
                closestDistanceSq = distanceSq;
                closestGhast = ghast.blockPosition().immutable();
            }
        }

        return closestGhast;
    }

    private RechargeResult tryGhastTearRecharge(Player player, ItemStack stack) {
        if (this.getCharges(stack) > GHAST_TEAR_RECHARGE_THRESHOLD) {
            return RechargeResult.TOO_CHARGED_FOR_GHAST_TEAR;
        }

        if (!consumeOneGhastTearFromInventory(player)) {
            return RechargeResult.NO_GHAST_TEAR;
        }

        this.addCharges(stack, GHAST_TEAR_RECHARGE_AMOUNT);
        return RechargeResult.GHAST_TEAR_SUCCESS;
    }

    private boolean consumeOneGhastTearFromInventory(Player player) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack inventoryStack = player.getInventory().getItem(slot);
            if (inventoryStack.is(Items.GHAST_TEAR)) {
                inventoryStack.shrink(1);
                player.getInventory().setChanged();
                return true;
            }
        }

        return false;
    }

    private void sendRechargeFeedback(Player player, ItemFireWand.RechargeResult result) {
        switch (result) {
            case GHAST_EXTRACTION_SUCCESS -> player.displayClientMessage(
                    Component.translatable("message." + ArcaneRelics.MOD_ID + ".fire_wand.recharge.ghast"),
                    true
            );
            case GHAST_TEAR_SUCCESS -> player.displayClientMessage(
                    Component.translatable("message." + ArcaneRelics.MOD_ID + ".fire_wand.recharge.ghast_tear"),
                    true
            );
            case TOO_CHARGED_FOR_GHAST_TEAR -> player.displayClientMessage(
                    Component.translatable("message." + ArcaneRelics.MOD_ID + ".fire_wand.recharge.too_charged"),
                    true
            );
            case NO_GHAST_TEAR -> player.displayClientMessage(
                    Component.translatable("message." + ArcaneRelics.MOD_ID + ".fire_wand.recharge.no_ghast_tear"),
                    true
            );
            case ALREADY_FULL -> player.displayClientMessage(
                    Component.translatable("message." + ArcaneRelics.MOD_ID + ".wand.recharge.fully_charged"),
                    true
            );
        }
    }

    private boolean shootFireball(Level level, Player player, int explosionPower, boolean withBlockBreak) {
        Vec3 look = player.getLookAngle();
        Vec3 fireballPos = player.getRopeHoldPosition(1);
        fireballPos = fireballPos.add(look.normalize().scale(2));

        LargeFireball fireball = new LargeFireball(level, player, look, explosionPower);
        fireball.setPos(fireballPos);
        level.addFreshEntity(fireball);

        return true;
    }
}
