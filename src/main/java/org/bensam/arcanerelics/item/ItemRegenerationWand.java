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

import java.util.List;

public class ItemRegenerationWand extends AbstractChargedWandItem implements WandEnchantingTableOutput {
    private static final int GHAST_EXTRACTION_RADIUS = 20;
    private static final int WAND_RANGE = 50;

    public ItemRegenerationWand(Properties properties, WandDefinition definition) {
        super(properties, definition);
    }

    @Override
    public boolean canBeProducedOrRechargedBy(ItemStack stack) {
        return stack.is(Items.GOLDEN_APPLE)
                || stack.is(Items.ENCHANTED_GOLDEN_APPLE)
                || hasPotionEffect(stack, Potions.REGENERATION)
                || hasPotionEffect(stack, Potions.STRONG_REGENERATION)
                || hasPotionEffect(stack, Potions.LONG_REGENERATION);
    }

    @Override
    public List<ItemStack> getEnchantmentItems(Level level) {
        List<ItemStack> items = getAllEffectItems(Potions.REGENERATION);
        items.add(new ItemStack(Items.GOLDEN_APPLE));
        items.add(new ItemStack(Items.ENCHANTED_GOLDEN_APPLE));
        return items;
    }

    @Override
    public int getLevelOfEnchantmentItem(ItemStack stack) {
        return this.canBeProducedOrRechargedBy(stack) ? 1 : 0; }

    //region Recharge Methods
    @Override
    protected RechargeContext tryRecharge(Level level, Player player, ItemStack wandStack) {
        if (this.isFullyCharged(wandStack)) {
            return new RechargeContext(RechargeResult.ALREADY_FULL, 0, null, this.getAlreadyFullMessagePath());
        }

        BlockPos closestMob = findClosestMobOfType(level, player.blockPosition(), GHAST_EXTRACTION_RADIUS, EntityType.HAPPY_GHAST);
        if (closestMob != null) {
            this.setCharges(wandStack, this.getMaxCharges());
            return new RechargeContext(RechargeResult.RECHARGE_SUCCESS, 0, closestMob, "regen_wand.recharge.success");
        }

        return new RechargeContext(RechargeResult.RECHARGE_FAIL, 0, null, "regen_wand.recharge.fail");
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

        // Play default effects.
        super.playRechargeEffects(level, player, hand, stack, rechargeContext);
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
