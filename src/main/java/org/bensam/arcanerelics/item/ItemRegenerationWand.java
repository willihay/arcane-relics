package org.bensam.arcanerelics.item;

import net.minecraft.core.BlockPos;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.bensam.arcanerelics.config.*;

import java.util.List;

public class ItemRegenerationWand extends AbstractChargedWandItem implements WandEnchantingTableOutput {
    private static final List<WandEnchantingSource> ENCHANTING_SOURCES = List.of(
            new FixedItemSource(Items.GOLDEN_APPLE),
            new FixedItemSource(Items.ENCHANTED_GOLDEN_APPLE),
            new PotionSource(Potions.REGENERATION),
            new PotionSource(Potions.STRONG_REGENERATION),
            new PotionSource(Potions.LONG_REGENERATION)
    );
    private static final int WAND_RANGE = 50;

    public ItemRegenerationWand(Properties properties, WandDefinition definition) {
        super(properties, definition);
    }

    @Override
    public List<WandEnchantingSource> getEnchantingSources() {
        return ENCHANTING_SOURCES;
    }

    //region Config Accessors
    @Override
    public WandBalanceConfig getBalanceConfig(ModServerConfig config) {
        return config.regenerationWand().balance();
    }

    private RegenerationWandConfig getRegenerationWandConfig() {
        return ModServerConfigManager.getConfig().regenerationWand();
    }
    //endregion

    //region Recharge Methods
    @Override
    protected RechargeContext tryRecharge(Level level, Player player, ItemStack wandStack) {
        return this.rechargeFromSource(level, wandStack, () -> {
            BlockPos closestMob = findClosestMobOfType(level, player.blockPosition(), this.getRegenerationWandConfig().ghastExtractionRadius(), EntityType.HAPPY_GHAST);
            return new RechargeContext(closestMob != null, 0, closestMob, (EntityType.HAPPY_GHAST).getDescription());
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
                SoundEvents.GHAST_SCREAM,
                SoundSource.PLAYERS,
                1.0f, // volume
                1.0f // pitch
        );

        // Create recharge particle effects.
        if (rechargeContext.sourcePos() != null) {
            // Create recharge particle trail from mob to player's wand.
            Vec3 mobStart = Vec3.atCenterOf(rechargeContext.sourcePos());
            Vec3 wandTip = getWandTipPosition(player, hand);
            spawnParticleTrail(level, ParticleTypes.HEART, mobStart, wandTip, 12, 5, 0.04);
        }
    }
    //endregion

    //region Cast Methods
    @Override
    protected boolean performCast(ServerLevel level, Player player, ItemStack stack, float powerUpPercentage, boolean isFullyPowered) {
        int durationTicks = 900 + (int) (powerUpPercentage * 900);

        TargetResult target = getTarget(player, WAND_RANGE);
        if (target != null && target.entity() instanceof LivingEntity livingEntity) {
            livingEntity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, durationTicks));
        } else {
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, durationTicks));
        }

        return true;
    }

    @Override
    protected void playCastSuccessEffects(ServerLevel level, Player player, ItemStack stack) {
        level.playSound(
                null,
                player.blockPosition(),
                SoundEvents.ENCHANTMENT_TABLE_USE,
                SoundSource.PLAYERS,
                1.0f, //volume
                1.0f // pitch
        );
    }
    //endregion
}
