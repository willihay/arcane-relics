package org.bensam.arcanerelics.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.bensam.arcanerelics.config.LevitationWandConfig;
import org.bensam.arcanerelics.config.ModServerConfig;
import org.bensam.arcanerelics.config.ModServerConfigManager;
import org.bensam.arcanerelics.config.WandBalanceConfig;

import java.util.List;

public class ItemLevitationWand extends AbstractChargedWandItem implements WandEnchantingTableOutput {
    private static final List<WandEnchantingSource> ENCHANTING_SOURCES = List.of(new FixedItemSource(Items.SHULKER_SHELL));
    private static final int WAND_RANGE = 50;

    public ItemLevitationWand(Properties properties, WandDefinition definition) {
        super(properties, definition);
    }

    @Override
    public List<WandEnchantingSource> getEnchantingSources() {
        return ENCHANTING_SOURCES;
    }

    //region Config Accessors
    @Override
    protected WandBalanceConfig getBalanceConfig(Level level) {
        return ModServerConfigManager.getConfig(level).levitationWand().balance();
    }

    private LevitationWandConfig getLevitationWandConfig(Level level) {
        return ModServerConfigManager.getConfig(level).levitationWand();
    }

    @Override
    public WandBalanceConfig getTooltipConfig(ModServerConfig config) {
        return config.levitationWand().balance();
    }
    //endregion

    //region Recharge Methods
    @Override
    protected RechargeContext tryRecharge(Level level, Player player, ItemStack wandStack) {
        return this.rechargeFromSource(level, wandStack, () -> {
            BlockPos closestMob = findClosestMobOfType(level, player.blockPosition(), this.getLevitationWandConfig(level).shulkerExtractionRadius(), EntityType.SHULKER);
            return new RechargeContext(closestMob != null, 0, closestMob, (EntityType.SHULKER).getDescription());
        });
    }

    @Override
    protected void playRechargeSuccessEffects(
            ServerLevel level,
            Player player,
            InteractionHand hand,
            ItemStack stack,
            RechargeContext rechargeContext
    ) {
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
    //endregion

    //region Cast Methods
    @Override
    protected boolean performCast(ServerLevel level, Player player, ItemStack stack, float powerUpPercentage, boolean isFullyPowered) {
        TargetResult target = getTarget(player, WAND_RANGE);
        if (target == null) {
            return false;
        }

        boolean isTargetLivingEntity = target.entity() != null && target.entity() instanceof LivingEntity;
        if (!isTargetLivingEntity) {
            // If target position is within about 1 block of player, apply levitation effect directly to the player.
            if (target.blockPos().closerThan(player.blockPosition(), 2)) {
                int durationTicks = 60 + (int) (powerUpPercentage * 140);
                player.addEffect(new MobEffectInstance(MobEffects.LEVITATION, durationTicks));
                return true;
            }
            return false;
        }

        // Fire shulker bullet.
        Vec3 look = player.getLookAngle().normalize();
        Vec3 projectilePos = player.getEyePosition(1.0f).add(look.scale(2));
        ShulkerBullet projectile = new ShulkerBullet(level, player, target.entity(), Direction.UP.getAxis());

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
