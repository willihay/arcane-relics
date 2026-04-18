package org.bensam.arcanerelics.item;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import org.bensam.arcanerelics.ArcaneRelics;
import org.bensam.arcanerelics.ModComponents;
import org.bensam.arcanerelics.ModStats;
import org.bensam.arcanerelics.network.WandBeginCastS2CPayload;
import org.bensam.arcanerelics.network.WandSucceedCastS2CPayload;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractChargedWandItem extends Item {
    private final WandDefinition definition;

    /**
     * Describes the outcome of a recharge attempt for the base wand lifecycle.
     * <p>
     * Subclasses return this from {@link #tryRecharge(Level, Player, ItemStack)} and the base class uses it to:
     * play success or failure effects, send the default recharge feedback message, and optionally carry
     * wand-specific metadata for subclasses that need to branch on the recharge outcome.
     * <p>
     * Contract notes:
     * <ul>
     *     <li>{@code succeeded} controls whether the recharge attempt is treated as a success.</li>
     *     <li>{@code rechargeMetadata} is optional wand-specific metadata for subclass use.</li>
     *     <li>{@code sourcePos} should be the world position of the recharge source when one exists and is useful for effects.</li>
     *     <li>{@code messageContext} is an optional translated message argument used by the default feedback path.</li>
     * </ul>
     */
    public record RechargeContext(boolean succeeded, int rechargeMetadata, @Nullable BlockPos sourcePos, @Nullable Component messageContext) {}

    public record TargetResult(BlockPos blockPos, @Nullable Entity entity) {}

    public AbstractChargedWandItem(Properties properties, WandDefinition definition) {
        super(properties);
        this.definition = definition;
    }

    //region Enchanting Methods
    protected static @NonNull List<ItemStack> getAllEnchantedBooks(RegistryAccess registryAccess, ResourceKey<Enchantment> enchantmentKey) {
        List<ItemStack> books = new ArrayList<>();

        var holder = registryAccess.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(enchantmentKey);
        Enchantment enchantment = holder.value();

        int maxEnchantmentLevel = enchantment.getMaxLevel();
        for (int enchantmentlevel = enchantment.getMinLevel(); enchantmentlevel <= maxEnchantmentLevel; enchantmentlevel++) {
            books.add(EnchantmentHelper.createBook(new EnchantmentInstance(holder, enchantmentlevel)));
        }

        return books;
    }

    protected static @NonNull List<ItemStack> getAllEffectItems(Holder<Potion> potionType) {
        List<ItemStack> items = new ArrayList<>();
        items.add(PotionContents.createItemStack(Items.POTION, potionType));
        items.add(PotionContents.createItemStack(Items.SPLASH_POTION, potionType));
        items.add(PotionContents.createItemStack(Items.LINGERING_POTION, potionType));
        items.add(PotionContents.createItemStack(Items.TIPPED_ARROW, potionType));
        return items;
    }

    public int getNewWandCharges(int enchantmentLevel) {
        int rechargeMultiplier = enchantmentLevel > 0 ? enchantmentLevel - 1 : 0;
        return Math.min(
                this.definition.initialCharges() + (this.getRechargeChargeAmount(enchantmentLevel) * rechargeMultiplier),
                this.getMaxCharges());
    }

    public int getNewWandXpCost() { return 2; }

    public int getRechargeChargeAmount(int enchantmentLevel) {
        int baseRechargeAmount = this.definition.rechargeAmount();
        return Math.min(baseRechargeAmount * enchantmentLevel, this.getMaxCharges());
    }

    public int getRechargeXpCost() { return 1; }

    public static int getEnchantmentLevel(ItemStack stack, ResourceKey<Enchantment> enchantmentKey) {
        var enchantments = EnchantmentHelper.getEnchantmentsForCrafting(stack);

        for (var entry : enchantments.entrySet()) {
            var key = entry.getKey().unwrapKey();
            if (key.isPresent() && key.get().equals(enchantmentKey)) {
                return entry.getIntValue();
            }
        }

        return 0;
    }

    public static boolean hasEnchantment(ItemStack stack, ResourceKey<Enchantment> enchantmentKey) {
        return getEnchantmentLevel(stack, enchantmentKey) > 0;
    }

    public static boolean hasPotionEffect(ItemStack stack, Holder<Potion> potionType) {
        PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        return contents.potion().isPresent() && contents.potion().get().equals(potionType);
    }
    //endregion

    //region Charge Helper Methods
    public final void addCharges(ItemStack stack, int amount) {
        int newCharges = this.getCharges(stack) + amount;
        this.setCharges(stack, newCharges);
    }

    public final void consumeCharges(ItemStack stack, int amount) {
        this.setCharges(stack, this.getCharges(stack) - amount);
    }

    public final int getCharges(ItemStack stack) {
        return stack.getOrDefault(
                ModComponents.WAND_CHARGES_COMPONENT,
                new ModComponents.WandChargesComponent(this.definition.initialCharges())
        ).charges();
    }

    public final int getMaxCharges() {
        return this.definition.maxCharges();
    }

    protected final boolean hasAtLeastCharges(ItemStack stack, int amount) {
        return this.getCharges(stack) >= amount;
    }

    protected final boolean hasEnoughChargesForCast(ItemStack stack, int chargeCost) {
        return chargeCost <= 0 || this.hasAtLeastCharges(stack, chargeCost);
    }

    public boolean isFullyCharged(ItemStack stack) {
        return this.getCharges(stack) >= this.getMaxCharges();
    }

    public void setCharges(ItemStack stack, int charges) {
        stack.set(
                ModComponents.WAND_CHARGES_COMPONENT,
                new ModComponents.WandChargesComponent(Math.clamp(charges, 0, this.getMaxCharges()))
        );
    }
    //endregion

    //region Targeting Methods
    protected static <T extends Entity> BlockPos findClosestMobOfType(
            Level level,
            BlockPos sourcePos,
            int radius,
            EntityType<T> mobType
    ) {
        BlockPos closestMob = null;
        double closestDistanceSq = Double.MAX_VALUE;

        AABB searchBox = new AABB(
                sourcePos.getX() - radius, sourcePos.getY() - radius, sourcePos.getZ() - radius,
                sourcePos.getX() + radius + 1, sourcePos.getY() + radius + 1, sourcePos.getZ() + radius + 1
        );

        for (T mob : level.getEntities(mobType, searchBox, Entity::isAlive)) {
            double distanceSq = mob.blockPosition().distSqr(sourcePos);
            if (distanceSq < closestDistanceSq) {
                closestDistanceSq = distanceSq;
                closestMob = mob.blockPosition().immutable();
            }
        }

        return closestMob;
    }

    protected static Vec3 getAimingLeadToTarget(
            Vec3 startPos,
            Vec3 targetPos,
            Vec3 targetVel,
            double projectileInitialSpeed,
            double acceleration,
            double drag
    ) {
        double projectileSpeed = projectileInitialSpeed;
        double distance = targetPos.distanceTo(startPos);
        double traveled = 0.0;
        int ticks = 0;

        while (traveled < distance && ticks < 200) {
            projectileSpeed = (projectileSpeed + acceleration) * drag;
            traveled += projectileSpeed;
            ticks++;
        }

        return targetVel.scale(ticks);
    }

    protected static @Nullable LivingEntity getBestLivingTargetInArcAngle(
            Level level,
            Player player,
            double range,
            double angleDegrees
    ) {
        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        Vec3 end = start.add(look.scale(range));
        double halfAngleRadians = Math.toRadians(angleDegrees / 2.0);
        double maxLateralDistance = Math.tan(halfAngleRadians) * range;
        double minDotProduct = Math.cos(halfAngleRadians);
        AABB searchBox = new AABB(start, end).inflate(maxLateralDistance);

        Entity bestEntity = null;
        double bestAngle = Double.MAX_VALUE;
        double bestDistanceToRaySq = Double.MAX_VALUE;
        double bestDistanceAlongLook = Double.MAX_VALUE;

        for (Entity entity : level.getEntities(
                player,
                searchBox,
                candidate -> candidate instanceof LivingEntity livingEntity
                        && livingEntity.isAlive()
                        && !candidate.isSpectator()
                        && candidate.isPickable()
        )) {
            Vec3 entityCenter = entity.getBoundingBox().getCenter();
            Vec3 toEntity = entityCenter.subtract(start);
            double distanceAlongLook = toEntity.dot(look);
            if (distanceAlongLook <= 0.0 || distanceAlongLook > range) {
                continue;
            }

            double distanceToEntitySq = toEntity.lengthSqr();
            if (distanceToEntitySq <= 0.0) {
                continue;
            }

            double dotProduct = distanceAlongLook / Math.sqrt(distanceToEntitySq);
            if (dotProduct < minDotProduct) {
                continue;
            }

            double maxDistanceToRay = Math.tan(halfAngleRadians) * distanceAlongLook;
            double distanceToRaySq = toEntity.subtract(look.scale(distanceAlongLook)).lengthSqr();
            if (distanceToRaySq > maxDistanceToRay * maxDistanceToRay) {
                continue;
            }

            BlockHitResult colliderHit = level.clip(new ClipContext(
                    start,
                    entityCenter,
                    ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE,
                    player
            ));
            if (colliderHit.getType() == HitResult.Type.BLOCK) {
                continue;
            }

            double angle = Math.acos(Mth.clamp(dotProduct, -1.0, 1.0));
            if (angle < bestAngle
                    || (angle == bestAngle && distanceToRaySq < bestDistanceToRaySq)
                    || (angle == bestAngle
                    && distanceToRaySq == bestDistanceToRaySq
                    && distanceAlongLook < bestDistanceAlongLook)) {
                bestEntity = entity;
                bestAngle = angle;
                bestDistanceToRaySq = distanceToRaySq;
                bestDistanceAlongLook = distanceAlongLook;
            }
        }

        return bestEntity == null ? null : (LivingEntity) bestEntity;
    }

    protected static TargetResult getTarget(Player player, double range) {
        // 1. Raycast for blocks.
        HitResult blockHit = player.pick(range, 0.0f, true);

        // 2. Raycast for entities.
        EntityHitResult entityHit = raycastLivingEntities(player, range, 1.0);

        // 3. If no entity hit, just return the block hit (if any).
        if (entityHit == null) {
            if (blockHit.getType() == HitResult.Type.BLOCK) {
                return new TargetResult(((BlockHitResult) blockHit).getBlockPos(), null);
            }
            return null;
        }

        // 4. Compare distances: entity vs. block.
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

    protected static boolean hasSkyAccess(Level level, BlockPos pos, boolean checkAdjacent) {
        // Check trivial case.
        if (level.canSeeSky(pos)) {
            return true;
        }

        // Check if every block up to the level height limit is sky-transparent.
        int startY = pos.getY();
        int delta = 0;
        for (int y = pos.getY(); level.isInsideBuildHeight(y); y++) {
            delta = y - startY;
            BlockState blockState = level.getBlockState(pos.above(delta));
            if (blockState.canBeReplaced() || blockState.is(BlockTags.LEAVES)) {
                continue;
            }
            // Found a non-transparent block above pos.

            if (checkAdjacent) {
                for (Direction direction : Direction.Plane.HORIZONTAL) {
                    if (hasSkyAccess(level, pos.relative(direction), false)) {
                        return true;
                    }
                }
            }
            return false;
        }

        return true;
    }

    protected static EntityHitResult raycastLivingEntities(Player player, double range, double inflate) {
        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = start.add(look.scale(range));

        AABB box = player.getBoundingBox().expandTowards(look.scale(range)).inflate(inflate);

        // Note: This Minecraft utility expects the square of the distance to be passed in as distance.
        return ProjectileUtil.getEntityHitResult(
                player,
                start,
                end,
                box,
                entity -> entity instanceof LivingEntity && !entity.isSpectator() && entity.isPickable(),
                range * range
        );
    }
    //endregion

    //region Wand Usage Lifecycle
    protected final int getElapsedTicks(ItemStack stack, LivingEntity entity, int remainingUseDuration) {
        return this.getUseDuration(stack, entity) - remainingUseDuration;
    }

    protected Component getNoChargesMessage() {
        return Component.translatable("message." + ArcaneRelics.MOD_ID + ".wand.cast.no_charges");
    }

    protected final int getFullPowerCastCost() { return this.definition.fullPowerCastCost(); }

    protected final int getFullPowerTicks() { return this.definition.fullPowerTicks(); }

    protected final int getNormalCastCost() { return this.definition.normalCastCost(); }

    protected int getPowerUpCost(Level level, Player player, ItemStack stack, int chargeTicks, boolean fullyPowered) {
        return fullyPowered ? this.getFullPowerCastCost() : this.getNormalCastCost();
    }

    protected final float getPowerUpPercentage(int elapsedTicks) {
        return Mth.clamp((float) elapsedTicks / this.getFullPowerTicks(), 0.0f, 1.0f);
    }

    @Override
    public @NonNull ItemUseAnimation getUseAnimation(@NonNull ItemStack stack) {
        return ItemUseAnimation.NONE;
    }

    @Override
    public int getUseDuration(@NonNull ItemStack stack, @NonNull LivingEntity entity) {
        return 72000; // same as bow; effectively as long as you want
    }

    protected static Vec3 getWandTipPosition(LivingEntity entity, @Nullable InteractionHand hand) {
        Vec3 eyePos = entity.getEyePosition();
        Vec3 look = entity.getLookAngle();

        // Build a stable horizontal right vector from yaw so it doesn't become unreliable when looking up/down.
        float yRotRad = entity.getYRot() * ((float) Math.PI / 180.0f);
        Vec3 right = new Vec3(-Mth.cos(yRotRad), 0.0, Mth.sin(yRotRad));

        double sideOffset = hand != null
                ? (hand == InteractionHand.MAIN_HAND ? 0.3 : -0.3)
                : 0.0;

        return eyePos
                .add(look.scale(0.65))      // forward: toward the wand tip
                .add(0.0, -0.22, 0.0) // slightly below the eye line
                .add(right.scale(sideOffset)); // left/right hand offset
    }

    protected final boolean isFullyPoweredUp(int elapsedTicks) {
        return elapsedTicks >= this.getFullPowerTicks();
    }

    @Override
    public final @NonNull InteractionResult use(@NonNull Level level, Player player, @NonNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // If sneaking, try to recharge.
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide()) {
                if (this.isFullyCharged(stack)) {
                    this.playRechargeFailEffects((ServerLevel) level, player, hand);
                    player.displayClientMessage(
                            Component.translatable("message." + ArcaneRelics.MOD_ID + ".wand.recharge.already_full"),
                            true
                    );
                } else {
                    RechargeContext rechargeContext = this.tryRecharge(level, player, stack);
                    if (rechargeContext.succeeded()) {
                        this.playRechargeSuccessEffects((ServerLevel) level, player, hand, stack, rechargeContext);
                        player.awardStat(ModStats.getWandsRechargedStat());
                    } else {
                        this.playRechargeFailEffects((ServerLevel) level, player, hand);
                    }
                    this.sendRechargeFeedback(player, rechargeContext);
                }
            }
            return InteractionResult.SUCCESS;
        }

        // If not sneaking, start powering up the wand to cast its power.

        // Begin the wand cast animation.
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            ServerPlayNetworking.send(
                    serverPlayer,
                    new WandBeginCastS2CPayload(hand == InteractionHand.MAIN_HAND, level.getGameTime())
            );
        }

        // Start officially using the wand.
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public void onUseTick(@NonNull Level level, @NonNull LivingEntity entity, @NonNull ItemStack stack, int remainingUseDuration) {
        super.onUseTick(level, entity, stack, remainingUseDuration);

        int elapsedTicks = this.getElapsedTicks(stack, entity, remainingUseDuration);
        if (this.isFullyPoweredUp(elapsedTicks)) {
            stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        } else {
            stack.remove(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
        }
    }

    @Override
    public final boolean releaseUsing(ItemStack stack, @NonNull Level level, @NonNull LivingEntity entity, int timeLeft) {
        stack.remove(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);

        // The remaining logic is for server-side only.
        if (!(level instanceof ServerLevel serverLevel)) {
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
        if (!this.hasEnoughChargesForCast(stack, chargeCost)) {
            this.playCastFailEffects(serverLevel, player);
            player.displayClientMessage(this.getNoChargesMessage(), true);
            return true;
        }

        // Perform cast.
        boolean castSucceeded = this.performCast(serverLevel, player, stack, powerUpPercentage, isFullyPowered);

        if (castSucceeded) {
            this.playCastSuccessEffects(serverLevel, player, stack);

            if (player instanceof ServerPlayer serverPlayer) {
                ServerPlayNetworking.send(
                        serverPlayer,
                        new WandSucceedCastS2CPayload(level.getGameTime())
                );
            }

            if (chargeCost > 0) {
                this.consumeCharges(stack, chargeCost);
            }

            player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
        } else {
            this.playCastFailEffects(serverLevel, player);
        }

        return true;
    }
    //endregion

    //region Recharge Methods
    /**
     * Attempts to recharge the wand while the player is sneak-using it.
     * <p>
     * Implementations should decide whether a valid recharge source is present and return a
     * {@link RechargeContext} describing the outcome. Subclasses should not directly send player feedback from
     * this method; the base lifecycle calls {@link #sendRechargeFeedback(Player, RechargeContext)} after the
     * attempt completes.
     * <p>
     * Implementations also should not mutate the wand's charges directly when using
     * {@link #rechargeFromSource(ItemStack, RechargeAttempt)}. That helper applies the successful recharge to the
     * wand stack after the subclass reports success. If a subclass intentionally bypasses that helper, it should
     * still preserve the same contract and avoid duplicating charge mutation unless there is a wand-specific reason.
     */
    protected abstract RechargeContext tryRecharge(Level level, Player player, ItemStack wandStack);

    @FunctionalInterface
    protected interface RechargeAttempt {
        RechargeContext run();
    }

    protected final RechargeContext rechargeFromSource(ItemStack wandStack, RechargeAttempt rechargeAttempt) {
        RechargeContext rechargeContext = rechargeAttempt.run();

        if (rechargeContext.succeeded()) {
            this.setCharges(wandStack, this.getMaxCharges());
        }

        return rechargeContext;
    }

    protected void playRechargeSuccessEffects(
            ServerLevel level,
            Player player,
            InteractionHand hand,
            ItemStack stack,
            RechargeContext rechargeContext
    ) {}

    private void playDefaultRechargeFailEffects(ServerLevel level, Player player, InteractionHand hand) {
        level.playSound(
                null,
                player.blockPosition(),
                SoundEvents.CAMPFIRE_CRACKLE,
                SoundSource.PLAYERS,
                1.0f,
                1.0f
        );

        // Create recharge fizzle particles.
        Vec3 wandTip = getWandTipPosition(player, hand);
        level.sendParticles(
                ParticleTypes.ELECTRIC_SPARK,
                wandTip.x, wandTip.y, wandTip.z, // position
                5, // # of particles
                0.03,0.03,0.03, // particle spread
                0.02 // particle speed
        );
    }

    protected void playRechargeFailEffects(ServerLevel level, Player player, InteractionHand hand) {
        this.playDefaultRechargeFailEffects(level, player, hand);
    }

    private void sendDefaultRechargeFeedback(Player player, String messagePath, @Nullable Component messageParameter) {
        if (messageParameter == null) {
            player.displayClientMessage(
                    Component.translatable("message." + ArcaneRelics.MOD_ID + "." + messagePath),
                    true
            );
        } else {
            player.displayClientMessage(
                    Component.translatable("message." + ArcaneRelics.MOD_ID + "." + messagePath, messageParameter),
                    true
            );
        }
    }

    protected void sendRechargeFeedback(Player player, RechargeContext rechargeContext) {
        this.sendDefaultRechargeFeedback(
                player,
                rechargeContext.succeeded() ? "wand.recharge.success" : "wand.recharge.fail",
                rechargeContext.messageContext());
    }

    protected static void spawnParticleTrail(
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

    //region Cast Methods
    /**
     * Performs the wand's cast effect after the base lifecycle has validated charge availability and computed the
     * current power-up state.
     * <p>
     * Implementations should perform only the wand-specific gameplay action and return whether the cast succeeded.
     * They should not consume charges, play the generic cast success or failure effects, or send the animation packet;
     * the base lifecycle handles those steps after this method returns.
     * <p>
     * Implementations may send additional wand-specific player feedback when the cast fails for a specific reason
     * beyond the standard "not enough charges" case, such as lightning requiring sky access.
     */
    protected abstract boolean performCast(
            ServerLevel level,
            Player player,
            ItemStack stack,
            float powerUpPercentage,
            boolean isFullyPowered
    );

    protected void playCastSuccessEffects(ServerLevel level, Player player, ItemStack stack) {}

    private void playDefaultCastFailEffects(ServerLevel level, Player player) {
        Vec3 frontOfPlayer = getWandTipPosition(player, null);
        level.sendParticles(
                ParticleTypes.WHITE_SMOKE,
                frontOfPlayer.x, frontOfPlayer.y, frontOfPlayer.z, // position
                5, // # of particles
                0.05, 0.05, 0.05, // particle spread
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

    protected void playCastFailEffects(ServerLevel level, Player player) {
        this.playDefaultCastFailEffects(level, player);
    }
    //endregion
}
