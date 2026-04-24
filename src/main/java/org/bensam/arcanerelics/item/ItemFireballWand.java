package org.bensam.arcanerelics.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.bensam.arcanerelics.ArcaneRelics;
import org.bensam.arcanerelics.config.*;

import java.util.List;

public class ItemFireballWand extends AbstractChargedWandItem implements WandEnchantingTableOutput {
    private static final List<WandEnchantingSource> ENCHANTING_SOURCES = List.of(new EnchantedBookSource(Enchantments.FLAME));
    private static final int RECHARGE_METADATA_GHAST_EXTRACTION = 1;
    private static final int RECHARGE_METADATA_BLAZE_EXTRACTION = 2;
    private static final double TARGET_MOTION_RETENTION_FACTOR = 0.8;
    private static final float BASE_EXPLOSION_POWER = 0.5f;
    private static final float MAX_EXPLOSION_POWER = 2.0f;

    public ItemFireballWand(Properties properties, WandDefinition definition) {
        super(properties, definition);
    }

    @Override
    public List<WandEnchantingSource> getEnchantingSources() {
        return ENCHANTING_SOURCES;
    }

    //region Config Accessors
    @Override
    public WandBalanceConfig getBalanceConfig(ModServerConfig config) {
        return config.fireballWand().balance();
    }

    private FireballWandConfig getFireballConfig() {
        return ModServerConfigManager.getConfig().fireballWand();
    }
    //endregion

    //region Recharge Methods
    @Override
    protected RechargeContext tryRecharge(Level level, Player player, ItemStack wandStack) {
        FireballWandConfig fireballConfig = this.getFireballConfig();
        return this.rechargeFromSource(level, wandStack, () -> findNearbyMobFuel(
                level,
                player.blockPosition(),
                fireballConfig.blazeExtractionRadius(),
                fireballConfig.ghastExtractionRadius()
        ));
    }

    protected static RechargeContext findNearbyMobFuel(Level level, BlockPos center, int blazeExtractionRadius, int ghastExtractionRadius) {
        BlockPos closestMob = findClosestMobOfType(level, center, ghastExtractionRadius, EntityType.GHAST);
        if (closestMob != null) {
            return new RechargeContext(
                    true,
                    RECHARGE_METADATA_GHAST_EXTRACTION,
                    closestMob,
                    (EntityType.GHAST).getDescription());
        }

        closestMob = findClosestMobOfType(level, center, ghastExtractionRadius, EntityType.HAPPY_GHAST);
        if (closestMob != null) {
            return new RechargeContext(
                    true,
                    RECHARGE_METADATA_GHAST_EXTRACTION,
                    closestMob,
                    (EntityType.HAPPY_GHAST).getDescription());
        }

        closestMob = findClosestMobOfType(level, center, blazeExtractionRadius, EntityType.BLAZE);
        if (closestMob != null) {
            return new RechargeContext(
                    true,
                    RECHARGE_METADATA_BLAZE_EXTRACTION,
                    closestMob,
                    (EntityType.BLAZE).getDescription());
        }

        return new RechargeContext(
                false,
                0,
                null,
                null);
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
                rechargeContext.rechargeMetadata() == RECHARGE_METADATA_BLAZE_EXTRACTION ? SoundEvents.BLAZE_AMBIENT : SoundEvents.GHAST_SCREAM,
                SoundSource.PLAYERS,
                1.0f, // volume
                1.0f // pitch
        );

        // Create recharge particle effects.
        if (rechargeContext.sourcePos() != null) {
            // Create recharge particle trail from mob to player's wand.
            Vec3 mobStart = Vec3.atCenterOf(rechargeContext.sourcePos());
            Vec3 wandTip = getWandTipPosition(player, hand);
            spawnParticleTrail(level, ParticleTypes.SMALL_FLAME, mobStart, wandTip, 12, 5, 0.04);
        }
    }

    @Override
    protected void sendRechargeFeedback(Player player, RechargeContext rechargeContext) {
        if (rechargeContext.succeeded()) {
            super.sendRechargeFeedback(player, rechargeContext);
        } else {
            player.displayClientMessage(
                    Component.translatable("message." + ArcaneRelics.MOD_ID + ".fireball_wand.recharge.fail"),
                    true
            );
        }
    }
    //endregion

    //region Cast Methods
    @Override
    protected boolean performCast(ServerLevel level, Player player, ItemStack stack, float powerUpPercentage, boolean isFullyPowered) {
        int explosionPower = Math.round(Mth.lerp(powerUpPercentage, BASE_EXPLOSION_POWER, MAX_EXPLOSION_POWER));
        this.shootFireball(level, player, explosionPower, isFullyPowered);
        return true;
    }

    protected void shootFireball(ServerLevel level, Player player, int explosionPower, boolean isFullyPowered) {
        Vec3 fireballTargetVec = player.getLookAngle().normalize();
        Vec3 fireballPos = player.getEyePosition().add(fireballTargetVec);

        if (this.shouldUseAimAssist(player)) {
            LivingEntity targetEntity = getBestLivingTargetInArcAngle(level, player, 60, 9.0);
            if (targetEntity != null) {
                Vec3 targetEntityCenter = targetEntity.getBoundingBox().getCenter();
                Vec3 aimingLead = getAimingLeadToTarget(
                        fireballPos,
                        targetEntityCenter,
                        targetEntity.getKnownSpeed().scale(TARGET_MOTION_RETENTION_FACTOR),
                        0.1,
                        0.1,
                        0.95
                );
                fireballTargetVec = targetEntityCenter.add(aimingLead).subtract(fireballPos);
            }
        }

        if (isFullyPowered) {
            LargeFireball fireball = new LargeFireball(level, player, fireballTargetVec, explosionPower);
            fireball.setPos(fireballPos);
            level.addFreshEntity(fireball);
        }
        else {
            SmallFireball fireball = new SmallFireball(level, player, fireballTargetVec);
            fireball.setPos(fireballPos);
            level.addFreshEntity(fireball);
        }
    }

    private boolean shouldUseAimAssist(Player player) {
        if (!this.getFireballConfig().allowAimAssist()) {
            return false;
        }

        if (player instanceof ServerPlayer serverPlayer) {
            return SyncedClientConfig.isFireballAimAssistEnabled(serverPlayer);
        }

        return true;
    }

    @Override
    protected void playCastSuccessEffects(ServerLevel level, Player player, ItemStack stack) {
        level.playSound(
                null,
                player.blockPosition(),
                SoundEvents.GHAST_SHOOT,
                SoundSource.PLAYERS,
                1.0f, // volume
                1.0f // pitch
        );
    }
    //endregion
}
