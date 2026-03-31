package org.bensam.arcanerelics.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ItemLevitationWand extends AbstractChargedWandItem implements WandEnchantingTableOutput {
    private static final int WAND_RANGE = 60;
    private static final int SHULKER_EXTRACTION_RADIUS = 8;

    public ItemLevitationWand(Properties properties, WandDefinition definition) {
        super(properties, definition);
    }

    @Override
    public boolean canBeProducedOrRechargedBy(ItemStack stack) {
        return stack.is(Items.SHULKER_SHELL);
    }

    //region Recharge Methods
    @Override
    protected RechargeContext tryRecharge(Level level, Player player, ItemStack wandStack) {
        if (this.isFullyCharged(wandStack)) {
            return new RechargeContext(RechargeResult.ALREADY_FULL, 0, null, this.getAlreadyFullMessagePath());
        }

        BlockPos closestMob = findClosestMobOfType(level, player.blockPosition(), SHULKER_EXTRACTION_RADIUS, Shulker.class);
        if (closestMob != null) {
            this.setCharges(wandStack, this.getMaxCharges());
            return new RechargeContext(RechargeResult.RECHARGE_SUCCESS, 0, closestMob, "levitation_wand.recharge.success");
        }
        else {
            return new RechargeContext(RechargeResult.RECHARGE_FAIL, 0, null, "levitation_wand.recharge.fail");
        }
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
                    SoundEvents.SHULKER_AMBIENT,
                    SoundSource.PLAYERS,
                    1.0f, // volume
                    1.0f // pitch
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
    protected boolean performCast(Level level, Player player, ItemStack stack, float powerUpPercentage, boolean isFullyPowered) {
        TargetResult target = getTarget(player, WAND_RANGE);
        if (target == null || target.entity() == null || !(target.entity() instanceof LivingEntity targetEntity)) {
            return false;
        }

        // Fire shulker bullet.
        Vec3 look = player.getLookAngle().normalize();
        Vec3 projectilePos = player.getEyePosition(1.0f).add(look.scale(2));
        ShulkerBullet projectile = new ShulkerBullet(level, player, targetEntity, Direction.UP.getAxis());

        projectile.setPos(projectilePos);
        level.addFreshEntity(projectile);
        return true;
    }

    @Override
    protected void playCastSuccessEffects(ServerLevel level, Player player, ItemStack stack) {
        level.playSound(
                null,
                player.blockPosition(),
                SoundEvents.SHULKER_SHOOT,
                SoundSource.PLAYERS,
                1.0f, // volume
                1.0f // pitch
        );
    }
//endregion
}
