package org.bensam.arcanerelics.item;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class ItemTemplateWand extends AbstractChargedWandItem implements WandEnchantingTableOutput {

    public ItemTemplateWand(Properties properties, WandDefinition definition) {
        super(properties, definition);
    }

    @Override
    public boolean canBeProducedOrRechargedBy(ItemStack stack) {
        return false;
    }

    @Override
    public List<ItemStack> getEnchantmentItems(Level level) {
        return List.of();
    }

    @Override
    public int getLevelOfEnchantmentItem(ItemStack stack) {
        return this.canBeProducedOrRechargedBy(stack) ? 1 : 0; // change this to check for stack item's level of enchantment or potency if applicable
    }

    //region Recharge Methods
    @Override
    protected RechargeContext tryRecharge(Level level, Player player, ItemStack wandStack) {
        // Replace this with appropriate recharge attempt logic for this wand:
        return new RechargeContext(false, 0, null, null);
        // Typical pattern:
//        return this.rechargeFromSource(wandStack, () -> {
//            BlockPos closestMob = findClosestMobOfType(level, player.blockPosition(), MOB_EXTRACTION_RADIUS, EntityType.MOB);
//            return new RechargeContext(closestMob != null, 0, closestMob, (EntityType.MOB).getDescription());
//        });
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
                SoundEvents.EVOKER_AMBIENT, // replace with appropriate sound event
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
    //endregion

    //region Cast Methods

    @Override
    protected boolean performCast(ServerLevel level, Player player, ItemStack stack, float powerUpPercentage, boolean isFullyPowered) {
        return false;
    }

    @Override
    protected void playCastSuccessEffects(ServerLevel level, Player player, ItemStack stack) {

    }
    //endregion
}
