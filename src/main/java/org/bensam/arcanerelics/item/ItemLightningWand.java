package org.bensam.arcanerelics.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import org.bensam.arcanerelics.ModComponents;
import org.jspecify.annotations.Nullable;

public class ItemLightningWand extends AbstractChargedWandItem {
    public static final int WAND_RANGE = 100;
    public static final int INITIAL_WAND_CHARGES = 50;
    private static final int CHARGE_COST = 1;
    private static final int FULL_CHARGE_COST = 2;
    private static final int FULL_CHARGE_TICKS = 60;
    private static final int LIGHTNING_ROD_ATTRACTION_RADIUS = 12;
    public static final float BASE_EXPLOSION_POWER = 0.75f;
    public static final float MAX_EXPLOSION_POWER = 2.5f;

    public ItemLightningWand(Properties properties) {
        super(properties, INITIAL_WAND_CHARGES);
    }

    public record TargetResult(BlockPos blockPos, @Nullable Entity entity) {}

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

        // Check if there are any charges remaining on the wand.
        if (!this.hasCharges(stack)) {
            return true;
        }

        int useDuration = this.getUseDuration(stack, entity);
        int chargeTicks = useDuration - timeLeft; // how long they held right-click
        boolean fullyCharged = chargeTicks >= FULL_CHARGE_TICKS;

        // Clamp charge between 0.0 and 1.0.
        float charge = Mth.clamp((float) chargeTicks / FULL_CHARGE_TICKS, 0.0f, 1.0f);
        // 0–3 seconds → 0.0–1.0 charge

        // Now scale lightning damage by charge.
        float explosionPower = Mth.lerp(charge, BASE_EXPLOSION_POWER, MAX_EXPLOSION_POWER);

        if (summonChargedLightning(level, player, explosionPower, fullyCharged)) {
            // Use up a charge.
            int chargeCost = fullyCharged ? FULL_CHARGE_COST : CHARGE_COST;
            this.consumeCharge(stack, chargeCost);
        }

        return true;
    }

    private boolean summonChargedLightning(Level level, Player player, float explosionPower, boolean withBlockBreak) {
        TargetResult target = getTarget(player, WAND_RANGE);
        if (target == null) return false;
        BlockPos strikeBlockPos = target.blockPos();

        // Check for nearby lightning rods.
        BlockPos lightningRodPos = findNearbyLightningRod(level, strikeBlockPos, LIGHTNING_ROD_ATTRACTION_RADIUS);
        boolean hasLightningRod = lightningRodPos != null;
        if (hasLightningRod) {
            strikeBlockPos = lightningRodPos.above();
        }

        Vec3 strikePos = Vec3.atBottomCenterOf(strikeBlockPos);

        // Create the lightning bolt.
        LightningBolt lightningBolt = new LightningBolt(EntityType.LIGHTNING_BOLT, level);
        lightningBolt.setPos(strikePos);
        level.addFreshEntity(lightningBolt);

        // Create explosion.
        float finalExplosionPower = hasLightningRod ? 0.0f : explosionPower;
        Level.ExplosionInteraction interaction = withBlockBreak && !hasLightningRod
                ? Level.ExplosionInteraction.BLOCK
                : Level.ExplosionInteraction.NONE;
        level.explode(player, strikePos.x, strikePos.y, strikePos.z, finalExplosionPower, false, interaction);

        return true;
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
}
