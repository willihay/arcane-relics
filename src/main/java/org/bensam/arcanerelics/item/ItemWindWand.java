package org.bensam.arcanerelics.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ItemWindWand extends AbstractChargedWandItem implements WandEnchantingTableOutput {
    private static final int BREEZE_EXTRACTION_RADIUS = 8;
    private static final double WIND_RANGE = 24.0D;
    private static final double WIND_STEP = 0.55D;
    private static final double WIND_HALF_ANGLE_RADIANS = Math.toRadians(32.0D);

    private static final boolean DEBUG_CONE_LINES = false;
    private static final double DEBUG_LINE_RADIUS_SCALE = 0.015D;

    public ItemWindWand(Properties properties, WandDefinition definition) {
        super(properties, definition);
    }

    @Override
    public boolean canBeProducedOrRechargedBy(ItemStack stack) {
        if (stack.is(Items.ENCHANTED_BOOK) && hasEnchantment(stack, Enchantments.WIND_BURST)) {
            return true;
        }
        return hasPotionEffect(stack, Potions.WIND_CHARGED);
    }

    @Override
    public List<ItemStack> getEnchantmentItems(Level level) {
        List<ItemStack> items = getAllEnchantedBooks(level, Enchantments.WIND_BURST);
        items.addAll(getAllEffectItems(Potions.WIND_CHARGED));
        return items;
    }

    @Override
    public int getLevelOfEnchantmentItem(ItemStack stack) {
        if (stack.is(Items.ENCHANTED_BOOK)) {
            return getEnchantmentLevel(stack, Enchantments.WIND_BURST);
        }
        return hasPotionEffect(stack, Potions.WIND_CHARGED) ? 1 : 0;
    }

    //region Recharge Methods
    @Override
    protected RechargeContext tryRecharge(Level level, Player player, ItemStack wandStack) {
        return this.rechargeFromSource(wandStack, () -> {
            BlockPos closestMob = findClosestMobOfType(level, player.blockPosition(), BREEZE_EXTRACTION_RADIUS, EntityType.BREEZE);
            return new RechargeContext(closestMob != null, 0, closestMob, (EntityType.BREEZE).getDescription());
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
                SoundEvents.BREEZE_INHALE,
                SoundSource.PLAYERS,
                1.0f,
                1.0f
        );

        // Create recharge particle effects.
        if (rechargeContext.sourcePos() != null) {
            // Create recharge particle trail from mob to player's wand.
            Vec3 mobStart = Vec3.atCenterOf(rechargeContext.sourcePos());
            Vec3 wandTip = getWandTipPosition(player, hand);
            spawnParticleTrail(level, ParticleTypes.SMALL_GUST, mobStart, wandTip, 12, 5, 0.04);
        }
    }
    //endregion

    //region Cast Methods
    @Override
    protected boolean performCast(ServerLevel level, Player player, ItemStack stack, float powerUpPercentage, boolean isFullyPowered) {
        // Simulates a forward-moving wind cone by stepping through space, spawning visuals, checking obstruction,
        // finding entities and blocks inside the cone, and applying wind effects exactly once to each valid target.
        Vec3 start = getWandTipPosition(player, null);
        Vec3 forward = player.getLookAngle().normalize();

        Vec3 worldUp = Vec3.Y_AXIS;
        Vec3 right = forward.cross(worldUp);
        // If player is looking straight up or down, use a default right vector.
        if (right.lengthSqr() < 1.0E-6D) {
            right = Vec3.X_AXIS;
        } else {
            right = right.normalize();
        }
        Vec3 up = right.cross(forward).normalize();

        double power = Mth.clamp(powerUpPercentage, 0.0D, 1.0D);

        // Baseline strength knobs.
        double coneRadiusAtEnd = Math.tan(WIND_HALF_ANGLE_RADIANS) * WIND_RANGE;
        double baseForwardPush = isFullyPowered ? 6.0D : 3.0D + (power * 3.0D);
        double baseUpPush = isFullyPowered ? 0.60D : 0.38D + (power * 0.22D);

        Set<Entity> affectedEntities = new HashSet<>();
        Set<BlockPos> affectedBlocks = new HashSet<>();

        // Move along the player’s forward look direction in small increments,
        // treating each position as a cross-section of a cone.
        for (double traveled = 0.0D; traveled <= WIND_RANGE; traveled += WIND_STEP) {
            double t = traveled / WIND_RANGE;

            // Get the center of the cone at this point, and the current radius of the cone.
            Vec3 center = start.add(forward.scale(traveled));
            double currentRadius = coneRadiusAtEnd * t;

            if (t >= 0.12D) {
                // Start spawning wind particles.
                spawnWindParticles(level, center, forward, t, coneRadiusAtEnd);
            }

            if (DEBUG_CONE_LINES) {
                spawnDebugConeRing(level, center, right, up, currentRadius);
            }

            if (isBlocked(level, player, start, center)) {
                continue;
            }

            AABB searchBox = new AABB(
                    center.x - currentRadius - 1.5D, center.y - currentRadius - 1.5D, center.z - currentRadius - 1.5D,
                    center.x + currentRadius + 1.5D, center.y + currentRadius + 1.5D, center.z + currentRadius + 1.5D
            );

            // Find entities inside the cone of wind to affect, pushing them away from the player and slightly upward.
            for (Entity entity : level.getEntities(player, searchBox, e -> e.isAlive() && e != player)) {
                if (!isInsideCone(start, forward, entity.position(), WIND_HALF_ANGLE_RADIANS, WIND_RANGE)) {
                    continue;
                }

                // If entity hasn't already been affected by this cone of wind, apply an impulse.
                if (affectedEntities.add(entity)) {
                    Vec3 impulse = computeWindImpulse(
                            start,
                            forward,
                            entity,
                            power,
                            isFullyPowered,
                            baseForwardPush,
                            baseUpPush
                    );
                    entity.push(impulse.x, impulse.y, impulse.z);
                    entity.hurtMarked = true;
                }
            }

            // Now find blocks inside the cone of wind that might have wind-sensitive properties.
            AABB blockBox = new AABB(
                    center.x - currentRadius, center.y - currentRadius, center.z - currentRadius,
                    center.x + currentRadius, center.y + currentRadius, center.z + currentRadius
            );

            // Convert the bounding box to integer block coordinates and loop through all block positions in that volume.
            for (BlockPos pos : BlockPos.betweenClosed(
                    Mth.floor(blockBox.minX), Mth.floor(blockBox.minY), Mth.floor(blockBox.minZ),
                    Mth.floor(blockBox.maxX), Mth.floor(blockBox.maxY), Mth.floor(blockBox.maxZ)
            )) {
                if (affectedBlocks.contains(pos)) {
                    continue;
                }

                if (!isInsideCone(start, forward, Vec3.atCenterOf(pos), WIND_HALF_ANGLE_RADIANS, WIND_RANGE)) {
                    continue;
                }

                if (affectWindSensitiveBlock(level, pos)) {
                    affectedBlocks.add(pos.immutable());
                }
            }
        }

        return true;
    }

    private static Vec3 computeWindImpulse(
            Vec3 origin,
            Vec3 forward,
            Entity entity,
            double power,
            boolean isFullyPowered,
            double baseForwardPush,
            double baseUpPush
    ) {
        Vec3 entityPos = entity.position();
        Vec3 toEntity = entityPos.subtract(origin);

        double forwardDistance = Math.max(0.01D, toEntity.dot(forward));
        Vec3 lateral = toEntity.subtract(forward.scale(forwardDistance));

        double lateralRatio = lateral.length() / Math.max(0.01D, forwardDistance);
        double centerBonus = Mth.clamp(1.0D - lateralRatio, 0.40D, 1.0D);

        double forwardPush = baseForwardPush * (0.65D + 0.35D * centerBonus);
        double upPush = baseUpPush * (0.75D + 0.25D * centerBonus);

        if (!entity.onGround()) {
            forwardPush *= 1.10D;
            upPush *= 0.90D;
        } else {
            upPush *= 1.20D;
        }

        if (!isFullyPowered) {
            forwardPush *= 0.85D + (power * 0.30D);
            upPush *= 0.85D + (power * 0.20D);
        }

        Vec3 impulse = forward.scale(forwardPush).add(0.0D, upPush, 0.0D);

        if (lateral.lengthSqr() > 1.0E-6D) {
            impulse = impulse.add(lateral.normalize().scale(0.15D * centerBonus));
        }

        return impulse;
    }

    // Assumes forward is normalized.
    private static boolean isInsideCone(Vec3 origin, Vec3 forward, Vec3 point, double halfAngleRadians, double maxDistance) {
        // Get vector from origin to point.
        Vec3 delta = point.subtract(origin);
        // Project vector onto the forward vector to get distance of the point along the cone axis.
        double distance = delta.dot(forward);

        // Reject distances along its axis that are outside the cone.
        if (distance <= 0.0D || distance > maxDistance) {
            return false;
        }

        double coneRadiusAtPoint = Math.tan(halfAngleRadians) * distance;

        // Get the perpendicular component of the origin-to-point vector to the cone axis.
        Vec3 perpendicular = delta.subtract(forward.scale(distance));

        // Point is inside the cone if the perpendicular component is within the cone radius.
        return perpendicular.lengthSqr() <= (coneRadiusAtPoint * coneRadiusAtPoint);
    }

    private static boolean isBlocked(ServerLevel level, Player player, Vec3 from, Vec3 to) {
        BlockHitResult hit = level.clip(new ClipContext(
                from,
                to,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                player
        ));
        return hit.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK;
    }

    private static void spawnWindParticles(ServerLevel level, Vec3 center, Vec3 forward, double t, double coneRadiusAtEnd) {
        // Push the particles farther out so they visibly travel.
        Vec3 particleCenter = center.add(forward.scale(0.95D));

        int particles;
        double spread;
        double speed;

        if (t < 0.30D) {
            particles = 1;
            spread = 0.06D;
            speed = 0.015D;
        } else if (t < 0.70D) {
            particles = 2;
            spread = 0.12D;
            speed = 0.025D;
        } else {
            particles = 3;
            spread = 0.20D;
            speed = 0.035D;
        }

        // Make the cloud widen as the gust travels outward.
        double coneWidth = Math.max(0.12D, coneRadiusAtEnd * t * 0.30D);

        // Use spread as actual positional randomness around the cone-width.
        double xzSpread = coneWidth * spread;
        double ySpread = coneWidth * (spread * 0.65D);

        level.sendParticles(
                ParticleTypes.GUST_EMITTER_SMALL,
                particleCenter.x, particleCenter.y, particleCenter.z,
                particles,
                xzSpread, ySpread, xzSpread,
                speed
        );
    }

    private static boolean affectWindSensitiveBlock(ServerLevel level, BlockPos pos) {
        var blockState = level.getBlockState(pos);

        if (blockState.canBeReplaced())
        {
            level.destroyBlock(pos, true);
            return true;
        }

        // Check for tags explicitly, just in case it's something that was missed by canBeReplaced().
        if (blockState.is(BlockTags.LEAVES)
                || blockState.is(BlockTags.FLOWERS)
                || blockState.is(BlockTags.REPLACEABLE_BY_TREES)
                || blockState.is(BlockTags.SAPLINGS)) {
            level.destroyBlock(pos, true);
            return true;
        }

        return false;
    }

    private static void spawnDebugConeRing(ServerLevel level, Vec3 center, Vec3 right, Vec3 up, double radius) {
        if (radius <= 0.001D) {
            return;
        }

        // Create a ring of particles around the center point,
        // which is along the cone axis,
        // on the plane orthogonal to the cone axis as specified by the right and up vectors.
        int points = 16;
        for (int i = 0; i < points; i++) {
            double angle = (Math.PI * 2.0D * i) / points;
            Vec3 offset = right.scale(Math.cos(angle) * radius) // build the right offset
                    .add(up.scale(Math.sin(angle) * radius)); // add the up offset
            Vec3 p = center.add(offset); // then add the center to get the final position of this particle

            level.sendParticles(
                    ParticleTypes.END_ROD,
                    p.x, p.y, p.z,
                    1,
                    DEBUG_LINE_RADIUS_SCALE, DEBUG_LINE_RADIUS_SCALE, DEBUG_LINE_RADIUS_SCALE,
                    0.0D
            );
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
