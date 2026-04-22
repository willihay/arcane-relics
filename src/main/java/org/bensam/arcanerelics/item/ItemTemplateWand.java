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
import org.bensam.arcanerelics.config.ModServerConfig;
import org.bensam.arcanerelics.config.ModServerConfigManager;
import org.bensam.arcanerelics.config.WandBalanceConfig;

import java.util.List;

public class ItemTemplateWand extends AbstractChargedWandItem implements WandEnchantingTableOutput {
    // Step 1:
    // Add any combination of EnchantedBookSource, FixedItemSource, or PotionSource to the list of enchanting sources
    // that can be used to produce this wand in a wand enchanting table.
    private static final List<WandEnchantingSource> ENCHANTING_SOURCES = List.of();

    public ItemTemplateWand(Properties properties, WandDefinition definition) {
        super(properties, definition);
    }

    @Override
    public List<WandEnchantingSource> getEnchantingSources() {
        return ENCHANTING_SOURCES;
    }

    //region Config Accessors
    // Step 2:
    // Set up configuration for this new wand by doing the following:
    // a) Create a new Config record in the .config package (you can use an existing config such as FangWandConfig as a template.
    // b) Add that new Config to ModServerConfig.
    // c) Add its defaults to ModServerConfigDefaults.
    // d) Uncomment the following 3 methods and update them to use this wand's accessor and config record
    //    in place of templateWand() and TemplateWandConfig.
    //@Override
    //protected WandBalanceConfig getBalanceConfig(Level level) {
    //    return ModServerConfigManager.getConfig(level).templateWand().balance();
    //}

    //private TemplateWandConfig getTemplateWandConfig(Level level) {
    //    return ModServerConfigManager.getConfig(level).templateWand();
    //}

    //@Override
    //public WandBalanceConfig getTooltipConfig(ModServerConfig config) {
    //    return config.templateWand().balance();
    //}
    //endregion

    //region Recharge Methods
    @Override
    protected RechargeContext tryRecharge(Level level, Player player, ItemStack wandStack) {
        // Step 3:
        // Define how this wand can be recharged from an alternate source, outside the wand enchanting table, typically
        // from a mob registered in EntityType, within a radius defined by a constant.
        // Typical pattern:
        // return this.rechargeFromSource(wandStack, () -> {
        //     BlockPos closestMob = findClosestMobOfType(level, player.blockPosition(), MOB_EXTRACTION_RADIUS, EntityType.MOB);
        //     return new RechargeContext(closestMob != null, 0, closestMob, (EntityType.MOB).getDescription());
        // });
        return new RechargeContext(false, 0, null, null);
    }

    // Step 4:
    // a) Choose an appropriate sound event to play when the recharge from an alternate source is successful.
    // b) Determine if there's a more appropriate particle effect to use in a trail between the alternate source and the player's wand.
    // c) Determine if you want to override the default playRechargeFailEffects() that's called when the recharge fails.
    // d) Determine if you want to override the default sendRechargeFeedback() that's called to inform the player of the
    //    result of the recharge attempt. Common uses for an override are when you have multiple recharge options or
    //    multiple failure cases, and want to send custom messages, perhaps based on recharge metadata that you place
    //    into the RechargeContext.
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
    // Step 5:
    // Implement the casting logic. Return true if successful so that the parent class lifecycle can consume charges.
    // See Javadoc for performCast() for more info.
    @Override
    protected boolean performCast(ServerLevel level, Player player, ItemStack stack, float powerUpPercentage, boolean isFullyPowered) {
        return false;
    }

    // Step 6:
    // Typically, pick a sound to play with a successful cast.
    // Optional: override default effects in playCastFailEffects().
    @Override
    protected void playCastSuccessEffects(ServerLevel level, Player player, ItemStack stack) {
        level.playSound(
                null,
                player.blockPosition(),
                SoundEvents.EVOKER_CAST_SPELL, // replace with appropriate sound event
                SoundSource.PLAYERS,
                1.0F,
                1.0F
        );
    }
    //endregion
}
