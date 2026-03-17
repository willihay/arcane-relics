package org.bensam.arcanerelics.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import org.bensam.arcanerelics.ArcaneRelics;
import org.jspecify.annotations.Nullable;

public class ItemLightningWand extends AbstractChargedWandItem {
    public static final int WAND_RANGE = 100;
    public static final int INITIAL_CHARGES = 20;
    public static final int MAX_CHARGES = 40;
    private static final int FULL_CHARGE_TICKS = 60;

    private static final int NORMAL_CAST_COST = 1;
    private static final int FULL_POWER_CAST_COST = 2;

    public static final int LIGHTNING_ROD_RECHARGE_RADIUS = 12;
    private static final int BLAZE_ROD_RECHARGE_AMOUNT = 20;
    private static final int BLAZE_ROD_RECHARGE_THRESHOLD = MAX_CHARGES - (BLAZE_ROD_RECHARGE_AMOUNT / 2);

    public static final float BASE_EXPLOSION_POWER = 0.75f;
    public static final float MAX_EXPLOSION_POWER = 2.5f;

    public record TargetResult(BlockPos blockPos, @Nullable Entity entity) {}

    private enum RechargeResult {
        LIGHTNING_ROD_SUCCESS,
        BLAZE_ROD_SUCCESS,
        NO_THUNDER,
        TOO_CHARGED_FOR_BLAZE_ROD,
        NO_BLAZE_ROD,
        ALREADY_FULL
    }

    public ItemLightningWand(Properties properties) {
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
                RechargeResult result = this.tryRecharge(level, player, stack);
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
        boolean stormCastingIsFree = level.isThundering();

        // Calculate charge cost.
        int chargeCost = stormCastingIsFree
                ? 0
                : (fullyCharged ? FULL_POWER_CAST_COST : NORMAL_CAST_COST);

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

        // Scale lightning damage by charge.
        float explosionPower = Mth.lerp(charge, BASE_EXPLOSION_POWER, MAX_EXPLOSION_POWER);

        // Summon charged lightning and consume charges.
        if (summonChargedLightning(level, player, explosionPower, fullyCharged) && chargeCost > 0) {
            this.consumeCharges(stack, chargeCost);
        }

        return true;
    }

    private RechargeResult tryRecharge(Level level, Player player, ItemStack wandStack) {
        if (this.isFullyCharged(wandStack)) {
            return RechargeResult.ALREADY_FULL;
        }

        boolean isThundering = level.isThundering();

        BlockPos lightningRodPos = findNearbyLightningRod(level, player.blockPosition(), LIGHTNING_ROD_RECHARGE_RADIUS);
        boolean foundLightningRod = lightningRodPos != null;
        if (foundLightningRod && isThundering) {
            this.setCharges(wandStack, this.getMaxCharges());
            return RechargeResult.LIGHTNING_ROD_SUCCESS;
        }

        RechargeResult blazeRodResult = this.tryBlazeRodRecharge(player, wandStack);
        if (blazeRodResult == RechargeResult.BLAZE_ROD_SUCCESS || blazeRodResult == RechargeResult.NO_BLAZE_ROD) {
            return blazeRodResult;
        }

        if (foundLightningRod) {
            return RechargeResult.NO_THUNDER;
        }

        return blazeRodResult;
    }

    private static @Nullable BlockPos findNearbyLightningRod(Level level, BlockPos center, int radius) {
        BlockPos closestRod = null;
        double closestDistanceSq = Double.MAX_VALUE;

        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-radius, -radius, -radius),
                center.offset(radius, radius, radius)
        )) {
            if (!level.getBlockState(pos).is(Blocks.LIGHTNING_ROD)) {
                continue;
            }

            if (!level.canSeeSky(pos.above())) {
                continue;
            }

            double distanceSq = pos.distSqr(center);
            if (distanceSq < closestDistanceSq) {
                closestDistanceSq = distanceSq;
                closestRod = pos.immutable();
            }
        }

        return closestRod;
    }

    private RechargeResult tryBlazeRodRecharge(Player player, ItemStack stack) {
        if (this.getCharges(stack) > BLAZE_ROD_RECHARGE_THRESHOLD) {
            return RechargeResult.TOO_CHARGED_FOR_BLAZE_ROD;
        }

        if (!consumeOneBlazeRodFromInventory(player)) {
            return RechargeResult.NO_BLAZE_ROD;
        }

        this.addCharges(stack, BLAZE_ROD_RECHARGE_AMOUNT);
        return RechargeResult.BLAZE_ROD_SUCCESS;
    }

    private boolean consumeOneBlazeRodFromInventory(Player player) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack inventoryStack = player.getInventory().getItem(slot);
            if (inventoryStack.is(Items.BLAZE_ROD)) {
                inventoryStack.shrink(1);
                player.getInventory().setChanged();
                return true;
            }
        }

        return false;
    }

    private void sendRechargeFeedback(Player player, RechargeResult result) {
        switch (result) {
            case LIGHTNING_ROD_SUCCESS -> player.displayClientMessage(
                    Component.translatable("message." + ArcaneRelics.MOD_ID + ".lightning_wand.recharge.lightning_rod"),
                    true
            );
            case BLAZE_ROD_SUCCESS -> player.displayClientMessage(
                    Component.translatable("message." + ArcaneRelics.MOD_ID + ".lightning_wand.recharge.blaze_rod"),
                    true
            );
            case TOO_CHARGED_FOR_BLAZE_ROD -> player.displayClientMessage(
                    Component.translatable("message." + ArcaneRelics.MOD_ID + ".lightning_wand.recharge.too_charged"),
                    true
            );
            case NO_BLAZE_ROD -> player.displayClientMessage(
                    Component.translatable("message." + ArcaneRelics.MOD_ID + ".lightning_wand.recharge.no_blaze_rod"),
                    true
            );
            case NO_THUNDER -> player.displayClientMessage(
                    Component.translatable("message." + ArcaneRelics.MOD_ID + ".lightning_wand.recharge.no_thunder"),
                    true
            );
            case ALREADY_FULL -> player.displayClientMessage(
                    Component.translatable("message." + ArcaneRelics.MOD_ID + ".wand.recharge.fully_charged"),
                    true
            );
        }
    }

    private boolean summonChargedLightning(Level level, Player player, float explosionPower, boolean withBlockBreak) {
        TargetResult target = getTarget(player, WAND_RANGE);
        if (target == null) {
            return false;
        }
        Vec3 strikePos = Vec3.atBottomCenterOf(target.blockPos());

        // Create the lightning bolt.
        LightningBolt lightningBolt = new LightningBolt(EntityType.LIGHTNING_BOLT, level);
        lightningBolt.setPos(strikePos);
        level.addFreshEntity(lightningBolt);

        // Create explosion.
        Level.ExplosionInteraction interaction = withBlockBreak
                ? Level.ExplosionInteraction.BLOCK
                : Level.ExplosionInteraction.NONE;
        level.explode(player, strikePos.x, strikePos.y, strikePos.z, explosionPower, false, interaction);

        return true;
    }

    public static TargetResult getTarget(Player player, double distance) {
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
}
