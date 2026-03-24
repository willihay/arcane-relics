package org.bensam.arcanerelics.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.bensam.arcanerelics.ArcaneRelics;
import org.bensam.arcanerelics.blockentity.BlockEntityWandEnchantingTable;
import org.jspecify.annotations.Nullable;

public class BlockWandEnchantingTable extends BaseEntityBlock {
    public static final MapCodec<BlockWandEnchantingTable> CODEC = simpleCodec(BlockWandEnchantingTable::new);
    private static final Component CONTAINER_TITLE = Component.translatable("container." + ArcaneRelics.MOD_ID + ".wand_enchanting");

    public BlockWandEnchantingTable(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new BlockEntityWandEnchantingTable(blockPos, blockState);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        if (!level.isClientSide()) {
            player.openMenu(blockState.getMenuProvider(level, blockPos));
            //player.awardStat(Stats.INTERACT_WITH_CRAFTING_TABLE); // TODO: create stat for wand enchanting table
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    protected @Nullable MenuProvider getMenuProvider(BlockState blockState, Level level, BlockPos blockPos) {
        if (level.getBlockEntity(blockPos) instanceof BlockEntityWandEnchantingTable blockEntityWandEnchantingTable) {
            return new SimpleMenuProvider(
                    (i, inventory, player) -> new AnvilMenu(
                            i,
                            inventory,
                            ContainerLevelAccess.create(level, blockPos)),
                    CONTAINER_TITLE
            );
        }

        return null;
    }
}
