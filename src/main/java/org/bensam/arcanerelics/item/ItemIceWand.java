package org.bensam.arcanerelics.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class ItemIceWand extends AbstractChargedWandItem implements WandEnchantingTableOutput {
    private static final int WAND_RANGE = 50;
    private static final int STRAY_EXTRACTION_RADIUS = 8;
    private static final float POWER_LEVEL_1 = 0.30f;
    private static final float POWER_LEVEL_2 = 0.50f;
    private static final float POWER_LEVEL_3 = 0.80f;

    public ItemIceWand(Properties properties, WandDefinition definition) {
        super(properties, definition);
    }

    @Override
    public boolean canBeProducedOrRechargedBy(ItemStack stack) {
        return stack.is(Items.ENCHANTED_BOOK) && hasEnchantment(stack, Enchantments.FROST_WALKER);
    }

    @Override
    public List<ItemStack> getEnchantmentItems(Level level) {
        return getAllEnchantedBooks(level, Enchantments.FROST_WALKER);
    }

    @Override
    public int getLevelOfEnchantmentItem(ItemStack stack) {
        return getEnchantmentLevel(stack, Enchantments.FROST_WALKER);
    }

    //region Recharge Methods
    @Override
    protected RechargeContext tryRecharge(Level level, Player player, ItemStack wandStack) {
        return this.rechargeFromSource(wandStack, () -> {
            BlockPos closestMob = findClosestMobOfType(level, player.blockPosition(), STRAY_EXTRACTION_RADIUS, EntityType.STRAY);
            return new RechargeContext(closestMob != null, 0, closestMob, (EntityType.STRAY).getDescription());
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
                SoundEvents.STRAY_AMBIENT,
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

        Vec3 look = player.getLookAngle().normalize();
        Vec3 particlePosStart = player.getEyePosition(1.0f).add(look.scale(2));
        Vec3 particlePosEnd = target.blockPos().getCenter();
        spawnParticleTrail(level, ParticleTypes.SNOWFLAKE, particlePosStart, particlePosEnd, 12, 5, 0.04);

        BlockPos center = target.entity() != null || level.getBlockState(target.blockPos()).canBeReplaced()
                ? target.blockPos()
                : target.blockPos().above();

        placeFrostCage(level, center, powerUpPercentage);

        return true;
    }

    private static void placeFrostCage(ServerLevel level, BlockPos center, float powerUpPercentage) {
        // Core cage: center + vertical stack.
        placeIceIfReplaceable(level, center);
        if (powerUpPercentage > POWER_LEVEL_1) {
            placeIceIfReplaceable(level, center.above());
        }
        if (powerUpPercentage > POWER_LEVEL_2) {
            placeIceIfReplaceable(level, center.above(2));
        }

        // 4-sided encasement around the center.
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            placeIceIfReplaceable(level, center.relative(direction));
            if (powerUpPercentage > POWER_LEVEL_3) {
                placeIceIfReplaceable(level, center.relative(direction).above());
            }
        }

        // 8-sided encasement is guaranteed above power level 3.
        if (powerUpPercentage > POWER_LEVEL_3) {
            placeIceIfReplaceable(level, center.north().east());
            placeIceIfReplaceable(level, center.north().west());
            placeIceIfReplaceable(level, center.south().east());
            placeIceIfReplaceable(level, center.south().west());
        }

        int extraBlocks = getExtraIceBlockCount(level.random, powerUpPercentage);
        if (extraBlocks <= 0) {
            return;
        }

        // Extra accents: corners and nearby shell positions.
        BlockPos[] accentPositions = new BlockPos[] {
                center.north().east(),
                center.north().west(),
                center.south().east(),
                center.south().west(),
                center.above().north(),
                center.above().south(),
                center.above().east(),
                center.above().west(),
                center.above(2).north(),
                center.above(2).south(),
                center.above(2).east(),
                center.above(2).west(),
                center.above(3)
        };

        boolean[] used = new boolean[accentPositions.length];
        int placed = 0;
        int tries = 0;

        while (placed < extraBlocks && tries++ < 50) {
            int maxIndex = powerUpPercentage <= POWER_LEVEL_2 ? 8 : accentPositions.length;
            int index = level.random.nextInt(maxIndex);
            if (used[index]) {
                continue;
            }

            used[index] = true;
            placeIceIfReplaceable(level, accentPositions[index]);
            placed++;
        }
    }

    private static int getExtraIceBlockCount(RandomSource random, float powerUpPercentage) {
        if (powerUpPercentage <= POWER_LEVEL_1) {
            return 0;
        }

        if (powerUpPercentage <= POWER_LEVEL_2) {
            return random.nextInt(3) + 2; // 2-4
        }

        if (powerUpPercentage <= POWER_LEVEL_3) {
            return random.nextInt(4) + 3; // 3-6
        }

        return random.nextInt(7) + 4; // 4-10
    }

    private static void placeIceIfReplaceable(ServerLevel level, BlockPos pos) {
        if (level.getBlockState(pos).canBeReplaced()) {
            level.setBlock(pos, Blocks.PACKED_ICE.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    protected void playCastSuccessEffects(ServerLevel level, Player player, ItemStack stack) {
        level.playSound(
                null,
                player.blockPosition(),
                SoundEvents.SNOWBALL_THROW,
                SoundSource.PLAYERS,
                1.0f, // volume
                1.0f // pitch
        );
    }
    //endregion
}
