package org.bensam.arcanerelics.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

public class ItemFangWand extends AbstractChargedWandItem implements WandEnchantingTableOutput {
    private static final int WAND_RANGE = 40;
    private static final int EVOKER_EXTRACTION_RADIUS = 8;

    public ItemFangWand(Properties properties, WandDefinition definition) {
        super(properties, definition);
    }

    @Override
    public boolean canBeProducedOrRechargedBy(ItemStack stack) {
        return stack.is(Items.TOTEM_OF_UNDYING);
    }

    @Override
    public List<ItemStack> getEnchantmentItems(Level level) {
        return List.of(new ItemStack(Items.TOTEM_OF_UNDYING));
    }

    @Override
    public int getLevelOfEnchantmentItem(ItemStack stack) {
        return this.canBeProducedOrRechargedBy(stack) ? 1 : 0;
    }

    //region Recharge Methods
    @Override
    protected RechargeContext tryRecharge(Level level, Player player, ItemStack wandStack) {
        if (this.isFullyCharged(wandStack)) {
            return new RechargeContext(RechargeResult.ALREADY_FULL, 0, null, this.getAlreadyFullMessagePath());
        }

        BlockPos closestMob = findClosestMobOfType(level, player.blockPosition(), EVOKER_EXTRACTION_RADIUS, EntityType.EVOKER);
        if (closestMob != null) {
            this.setCharges(wandStack, this.getMaxCharges());
            return new RechargeContext(RechargeResult.RECHARGE_SUCCESS, 0, closestMob, "fang_wand.recharge.success");
        }

        return new RechargeContext(RechargeResult.RECHARGE_FAIL, 0, null, "fang_wand.recharge.fail");
    }

    @Override
    protected void playRechargeEffects(
            ServerLevel level,
            Player player,
            InteractionHand hand,
            ItemStack stack,
            RechargeContext rechargeContext
    ) {
        if (rechargeContext.result() == RechargeResult.RECHARGE_SUCCESS) {
            // Play sound effects.
            level.playSound(
                    null,
                    player.blockPosition(),
                    SoundEvents.EVOKER_AMBIENT,
                    SoundSource.PLAYERS,
                    1.0f,
                    1.0f
            );

            // Create recharge particle effects.
            if (rechargeContext.sourcePos() != null) {
                // Create recharge particle trail from mob to player's wand.
                Vec3 mobStart = Vec3.atCenterOf(rechargeContext.sourcePos());
                Vec3 wandTip = getWandTipPosition(player, hand);
                spawnParticleTrail(level, ParticleTypes.ENCHANTED_HIT, mobStart, wandTip, 12, 5, 0.04);
            }
        }

        // Play default effects.
        super.playRechargeEffects(level, player, hand, stack, rechargeContext);
    }
    //endregion

    //region Cast Methods

    @Override
    protected boolean performCast(ServerLevel level, Player player, ItemStack stack, float powerUpPercentage, boolean isFullyPowered) {
        TargetResult target = getTarget(player, WAND_RANGE);
        if (target == null) {
            return false;
        }

        float fangDamage = 10.0F + (powerUpPercentage * 10.0F);

        if (target.blockPos().closerThan(player.blockPosition(), 3)) {
            // At close range, create a defensive ring of fangs around the player.
            createFangRing(level, player, target.blockPos(), isFullyPowered, fangDamage);
        }
        else {
            // Create a line of fangs between the player and the target.
            createFangLine(level, player, target.blockPos(), fangDamage);
        }

        return true;
    }

    private void createFangRing(ServerLevel level, Player player, BlockPos blockPos, boolean isFullyPowered, float fangDamage) {
        double minY = Math.min(blockPos.getY(), player.getY());
        double maxY = Math.max(blockPos.getY(), player.getY()) + 1.0;
        float targetXZAngle = (float) Mth.atan2(blockPos.getZ() - player.getZ(), blockPos.getX() - player.getX());
        int numInnerFangs = 5;
        float innerRingInterval = (float) Math.PI * 2.0F / numInnerFangs;
        float innerRadius = 1.5F;
        int numOuterFangs = 8;
        float outerRingInterval = (float) Math.PI * 2.0F / numOuterFangs;
        float outerRadius = 2.5F;

        for (int i = 0; i < numInnerFangs; i++) {
            float fangAngle = targetXZAngle + (i * innerRingInterval);
            this.createFang(level, player, fangDamage, player.getX() + Mth.cos(fangAngle) * innerRadius, player.getZ() + Mth.sin(fangAngle) * innerRadius, minY, maxY, fangAngle, 0);
        }

        if (isFullyPowered) {
            for (int i = 0; i < numOuterFangs; i++) {
                float fangAngle = targetXZAngle + (i * outerRingInterval) + innerRingInterval;
                this.createFang(level, player, fangDamage, player.getX() + Mth.cos(fangAngle) * outerRadius, player.getZ() + Mth.sin(fangAngle) * outerRadius, minY, maxY, fangAngle, 3);
            }
        }
    }

    private void createFangLine(ServerLevel level, Player player, BlockPos blockPos, float fangDamage) {
        double minY = Math.min(blockPos.getY(), player.getY());
        double maxY = Math.max(blockPos.getY(), player.getY()) + 1.0;
        int fangDelay = 0;
        double distance = 2.5;
        double distanceToTarget = Math.sqrt(blockPos.distToCenterSqr(player.position())) + 2.0;
        float horizAngleToTarget = (float) Mth.atan2(blockPos.getZ() - player.getZ(), blockPos.getX() - player.getX());

        do {
            this.createFang(level, player, fangDamage, player.getX() + Mth.cos(horizAngleToTarget) * distance, player.getZ() + Mth.sin(horizAngleToTarget) * distance, minY, maxY, horizAngleToTarget, fangDelay);
            distance += 1.25;
            fangDelay++;
        } while (distance < distanceToTarget);
    }

    private void createFang(ServerLevel level, Player player, float fangDamage, double x, double z, double minY, double maxY, float horizAngle, int delay) {
        BlockPos targetPos = BlockPos.containing(x, maxY, z);
        int lowestY = Mth.floor(minY) - 1;
        boolean validPos = false;
        double yOffset = 0.0;

        do {
            BlockPos blockBelowPos = targetPos.below();
            BlockState blockBelowState = level.getBlockState(blockBelowPos);
            if (blockBelowState.isFaceSturdy(level, blockBelowPos, Direction.UP)) {
                if (!level.isEmptyBlock(targetPos)) {
                    BlockState targetBlockState = level.getBlockState(targetPos);
                    VoxelShape voxelShape = targetBlockState.getCollisionShape(level, targetPos);
                    if (!voxelShape.isEmpty()) {
                        yOffset = voxelShape.max(Direction.Axis.Y);
                    }
                }

                validPos = true;
                break;
            }

            targetPos = targetPos.below();
        } while (targetPos.getY() >= lowestY);

        if (validPos) {
            double spawnY = targetPos.getY() + yOffset;
            EvokerFangs fangs = new EvokerFangs(level, x, spawnY, z, horizAngle, delay, player);
            ((EvokerFangsAccess) fangs).arcanerelics$setFangWandDamage(fangDamage);
            level.addFreshEntity(fangs);
            level.gameEvent(GameEvent.ENTITY_PLACE, new Vec3(x, spawnY, z), GameEvent.Context.of(player));
        }
    }

    @Override
    protected void playCastSuccessEffects(ServerLevel level, Player player, ItemStack stack) {
        level.playSound(
                null,
                player.blockPosition(),
                SoundEvents.EVOKER_CAST_SPELL,
                SoundSource.PLAYERS,
                1.0F,
                1.0F
        );
    }
    //endregion
}
