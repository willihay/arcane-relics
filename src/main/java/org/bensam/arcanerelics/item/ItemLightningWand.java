package org.bensam.arcanerelics.item;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import org.bensam.arcanerelics.ArcaneRelics;
import org.jspecify.annotations.Nullable;

public class ItemLightningWand extends AbstractChargedWandItem<ItemLightningWand.LightningRechargeResult> {
    public static final int INITIAL_CHARGES = 20;
    public static final int MAX_CHARGES = 40;

    private static final int NORMAL_CAST_COST = 1;
    private static final int FULL_POWER_TICKS = 60;
    private static final int FULL_POWER_CAST_COST = 2;
    private static final int WAND_RANGE = 60;

    private static final int LIGHTNING_ROD_RECHARGE_RADIUS = 12;
    private static final int BLAZE_ROD_RECHARGE_AMOUNT = 20;
    private static final int BLAZE_ROD_RECHARGE_THRESHOLD = MAX_CHARGES - (BLAZE_ROD_RECHARGE_AMOUNT / 2);

    private static final float BASE_EXPLOSION_POWER = 0.75f;
    private static final float MAX_EXPLOSION_POWER = 2.5f;

    public record TargetResult(BlockPos blockPos, @Nullable Entity entity) {}

    public ItemLightningWand(Properties properties) {
        super(properties, INITIAL_CHARGES, MAX_CHARGES);
    }

    //region Helper Functions
    @Override
    protected int getFullPowerCastCost() {
        return FULL_POWER_CAST_COST;
    }

    @Override
    protected int getFullPowerTicks() {
        return FULL_POWER_TICKS;
    }

    @Override
    protected int getNormalCastCost() {
        return NORMAL_CAST_COST;
    }

    @Override
    protected int getPowerUpCost(Level level, Player player, ItemStack stack, int chargeTicks, boolean fullyCharged) {
        return level.isThundering() ? 0 : super.getPowerUpCost(level, player, stack, chargeTicks, fullyCharged);
    }
    //endregion

    //region Recharge Functions
    public enum LightningRechargeResult implements RechargeResult {
        LIGHTNING_ROD_SUCCESS,
        BLAZE_ROD_SUCCESS,
        NO_THUNDER,
        TOO_CHARGED_FOR_BLAZE_ROD,
        NO_BLAZE_ROD,
        ALREADY_FULL
    }

    @Override
    protected LightningRechargeResult tryRecharge(Level level, Player player, ItemStack wandStack) {
        if (this.isFullyCharged(wandStack)) {
            return LightningRechargeResult.ALREADY_FULL;
        }

        boolean isThundering = level.isThundering();

        BlockPos lightningRodPos = findNearbyLightningRod(level, player.blockPosition(), LIGHTNING_ROD_RECHARGE_RADIUS);
        boolean foundLightningRod = lightningRodPos != null;
        if (foundLightningRod && isThundering) {
            this.setCharges(wandStack, this.getMaxCharges());
            return LightningRechargeResult.LIGHTNING_ROD_SUCCESS;
        }

        LightningRechargeResult blazeRodResult = this.tryBlazeRodRecharge(player, wandStack);
        if (blazeRodResult == LightningRechargeResult.BLAZE_ROD_SUCCESS || blazeRodResult == LightningRechargeResult.NO_BLAZE_ROD) {
            return blazeRodResult;
        }

        if (foundLightningRod) {
            return LightningRechargeResult.NO_THUNDER;
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

    private LightningRechargeResult tryBlazeRodRecharge(Player player, ItemStack stack) {
        if (this.getCharges(stack) > BLAZE_ROD_RECHARGE_THRESHOLD) {
            return LightningRechargeResult.TOO_CHARGED_FOR_BLAZE_ROD;
        }

        if (!consumeArcaneFuelFromInventory(player, Items.BLAZE_ROD)) {
            return LightningRechargeResult.NO_BLAZE_ROD;
        }

        this.addCharges(stack, BLAZE_ROD_RECHARGE_AMOUNT);
        return LightningRechargeResult.BLAZE_ROD_SUCCESS;
    }

    @Override
    protected void sendRechargeFeedback(Player player, LightningRechargeResult result) {
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
                    this.getFullyChargedMessage(),
                    true
            );
        }
    }
    //endregion

    //region Cast Methods
    @Override
    protected boolean performCast(Level level, Player player, ItemStack stack, float powerUpPercentage, boolean isFullyPowered) {
        float explosionPower = Mth.lerp(powerUpPercentage, BASE_EXPLOSION_POWER, MAX_EXPLOSION_POWER);
        return this.summonChargedLightning(level, player, explosionPower, isFullyPowered);
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
    //endregion
}
