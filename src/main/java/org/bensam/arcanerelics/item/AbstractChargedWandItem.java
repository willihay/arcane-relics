package org.bensam.arcanerelics.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import org.bensam.arcanerelics.ArcaneRelics;
import org.bensam.arcanerelics.ModComponents;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public abstract class AbstractChargedWandItem<R extends Enum<R> & RechargeResult> extends Item {
    private final int initialCharges;
    private final int maxCharges;

    public record RechargeContext<R extends Enum<R> & RechargeResult> (R result, @Nullable BlockPos sourcePos) {}
    public record TargetResult(BlockPos blockPos, @Nullable Entity entity) {}

    public AbstractChargedWandItem(Properties properties, int initialCharges, int maxCharges) {
        super(properties);
        this.initialCharges = initialCharges;
        this.maxCharges = maxCharges;
    }

    //region Helper Methods
    public int addCharges(ItemStack stack, int amount) {
        int currentCharges = this.getCharges(stack);
        int newCharges = Math.min(currentCharges + amount, this.getMaxCharges());
        this.setCharges(stack, newCharges);
        return newCharges;
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

    protected boolean consumeArcaneFuelFromInventory(Player player, Item item, ResourceKey<Enchantment> enchantmentKey) {
        var inventory = player.getInventory();
        int inventorySize = inventory.getContainerSize();

        for (int slot = 0; slot < inventorySize; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.is(item)) {
                var enchantments = EnchantmentHelper.getEnchantmentsForCrafting(stack);

                for (var entry : enchantments.entrySet()) {
                    var key = entry.getKey().unwrapKey();

                    if (key.isPresent() && key.get() == enchantmentKey) {
                        stack.shrink(1);
                        inventory.setChanged();
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public void consumeCharges(ItemStack stack, int amount) {
        this.setCharges(stack, this.getCharges(stack) - amount);
    }

    protected static <T extends Entity> BlockPos findClosestMobOfType(Level level, BlockPos center, int radius, Class<T> mobType) {
        BlockPos closestMob = null;
        double closestDistanceSq = Double.MAX_VALUE;

        AABB searchBox = new AABB(
                center.getX() - radius, center.getY() - radius, center.getZ() - radius,
                center.getX() + radius + 1, center.getY() + radius + 1, center.getZ() + radius + 1
        );

        for (T mob : level.getEntitiesOfClass(mobType, searchBox)) {
            if (!mob.isAlive()) {
                continue;
            }

            double distanceSq = mob.blockPosition().distSqr(center);
            if (distanceSq < closestDistanceSq) {
                closestDistanceSq = distanceSq;
                closestMob = mob.blockPosition().immutable();
            }
        }

        return closestMob;
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

    protected int getPowerUpCost(Level level, Player player, ItemStack stack, int chargeTicks, boolean fullyPowered) {
        return fullyPowered ? this.getFullPowerCastCost() : this.getNormalCastCost();
    }

    protected float getPowerUpPercentage(int elapsedTicks) {
        return Mth.clamp((float) elapsedTicks / this.getFullPowerTicks(), 0.0f, 1.0f);
    }

    public int getNewWandXpCost() { return 2; }

    public int getRechargeXpCost() { return 1; }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000; // same as bow; effectively as long as you want
    }

    protected static Vec3 getWandTipPosition(Player player, @Nullable InteractionHand hand) {
        Vec3 look = player.getLookAngle().normalize();
        Vec3 right = look.cross(Vec3.Y_AXIS).normalize();
        double sideOffset = hand != null
                ? (hand == InteractionHand.MAIN_HAND) ? 0.25 : -0.25
                : 0.0;

        return player.getEyePosition(1.0f)
                .add(look) // forward offset towards tip
                .add(right.scale(sideOffset)) // side offset towards interaction hand
                .add(0.0, -0.20, 0.0); // downwards offset towards hand level
    }

    public boolean hasAtLeastCharges(ItemStack stack, int amount) {
        return this.getCharges(stack) >= amount;
    }

    public static boolean hasEnchantment(ItemStack stack, ResourceKey<Enchantment> enchantmentKey) {
        var enchantments = EnchantmentHelper.getEnchantmentsForCrafting(stack);

        for (var entry : enchantments.entrySet()) {
            var key = entry.getKey().unwrapKey();

            if (key.isPresent() && key.get() == enchantmentKey) {
                return true;
            }
        }

        return false;
    }

    public boolean isFullyCharged(ItemStack stack) {
        return this.getCharges(stack) >= this.getMaxCharges();
    }

    protected boolean isFullyPoweredUp(int elapsedTicks) {
        return elapsedTicks >= this.getFullPowerTicks();
    }

    public void setCharges(ItemStack stack, int charges) {
        stack.set(
                ModComponents.WAND_CHARGES_COMPONENT,
                new ModComponents.WandChargesComponent(Math.max(0, Math.min(charges, this.maxCharges)))
        );
    }

    protected void spawnParticleTrail(
            ServerLevel level,
            ParticleOptions particle,
            Vec3 startPos,
            Vec3 endPos,
            int steps,
            int particlesPerStep,
            double spread
    ) {
        if (steps <= 0 || particlesPerStep <= 0) {
            return;
        }

        for (int i = 0; i < steps; i++) {
            double t = (double) i / steps;
            Vec3 pos = startPos.lerp(endPos, t);

            level.sendParticles(
                    particle,
                    pos.x, pos.y, pos.z,
                    particlesPerStep,
                    spread, spread, spread,
                    0.0
            );
        }
    }
    //endregion

    //region Targeting Methods
    protected static TargetResult getTarget(Player player, double distance) {
        // 1. Raycast for blocks.
        HitResult blockHit = player.pick(distance, 0.0f, true);

        // 2. Raycast for entities.
        EntityHitResult entityHit = raycastEntities(player, distance);

        // 3. If no entity hit, just return the block hit (if any).
        if (entityHit == null) {
            if (blockHit.getType() == HitResult.Type.BLOCK) {
                return new TargetResult(((BlockHitResult) blockHit).getBlockPos(), null);
            }
            return null;
        }

        // 4. Compare distances: entity vs block.
        double blockDist = blockHit.getLocation().distanceTo(player.getEyePosition());
        double entityDist = entityHit.getLocation().distanceTo(player.getEyePosition());

        if (entityDist < blockDist) {
            // Player is looking at an entity → return block under entity.
            Entity e = entityHit.getEntity();
            return new TargetResult(e.blockPosition(), e); // block the entity is standing on
        }

        // Otherwise return the block hit.
        if (blockHit.getType() == HitResult.Type.BLOCK) {
            return new TargetResult(((BlockHitResult) blockHit).getBlockPos(), null);
        }

        return null;
    }

    private static EntityHitResult raycastEntities(Player player, double distance) {
        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = start.add(look.scale(distance));

        AABB box = player.getBoundingBox().expandTowards(look.scale(distance)).inflate(1.0);

        // Note: This Minecraft utility expects the square of the distance to be passed in as distance.
        return ProjectileUtil.getEntityHitResult(
                player,
                start,
                end,
                box,
                entity -> !entity.isSpectator() && entity.isPickable(),
                distance * distance
        );
    }
    //endregion

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // If sneaking, try to recharge.
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide()) {
                RechargeContext<R> rechargeContext = this.tryRecharge(level, player, stack);
                this.playRechargeContextEffects((ServerLevel) level, player, hand, stack, rechargeContext);
                this.sendRechargeFeedback(player, rechargeContext.result());
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

        // Players are the only entities who will use an arcane wand.
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
            this.playCastFailEffects((ServerLevel) level, player, stack);
            player.displayClientMessage(this.getNoChargesMessage(), true);
            return true;
        }

        // Perform cast.
        boolean castSucceeded = this.performCast(level, player, stack, powerUpPercentage, isFullyPowered);

        if (castSucceeded) {
            this.playCastSuccessEffects((ServerLevel) level, player, stack);
            if (chargeCost > 0) {
                this.consumeCharges(stack, chargeCost);
            }
        }
        else {
            this.playCastFailEffects((ServerLevel) level, player, stack);
        }

        return true;
    }

    protected void playRechargeContextEffects(
            ServerLevel level,
            Player player,
            InteractionHand hand,
            ItemStack stack,
            @NonNull RechargeContext<R> rechargeContext
    ) {
        // Default sound effect. Can be combined with other effects.
        level.playSound(
                null,
                player.blockPosition(),
                SoundEvents.CAMPFIRE_CRACKLE,
                SoundSource.PLAYERS,
                1.0f, // volume
                1.0f // pitch
        );
    }

    protected void playCastSuccessEffects(ServerLevel level, Player player, ItemStack stack) {}

    protected void playCastFailEffects(ServerLevel level, Player player, ItemStack stack) {
        Vec3 frontOfPlayer = getWandTipPosition(player, null);
        level.sendParticles(
                ParticleTypes.WHITE_SMOKE,
                frontOfPlayer.x, frontOfPlayer.y, frontOfPlayer.z, // position
                5, // # of particles
                0.05,0.05,0.05, // particle spread
                0.02 // particle speed
        );
        level.playSound(
                null,
                player.blockPosition(),
                SoundEvents.FIRE_EXTINGUISH,
                SoundSource.PLAYERS,
                1.0f, // volume
                1.0f // pitch
        );
    }

    //region Abstract Methods
    protected abstract int getFullPowerTicks();
    protected abstract int getNormalCastCost();
    protected abstract int getFullPowerCastCost();
    public abstract int getRechargeChargeAmount();

    protected abstract RechargeContext<R> tryRecharge(Level level, Player player, ItemStack wandStack);
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
