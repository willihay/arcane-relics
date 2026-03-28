package org.bensam.arcanerelics.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.bensam.arcanerelics.ArcaneRelics;
import org.bensam.arcanerelics.blockentity.BlockEntityWandEnchantingTable;
import org.bensam.arcanerelics.menu.WandEnchantingMenu;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class BlockWandEnchantingTable extends BaseEntityBlock {
    public static final MapCodec<BlockWandEnchantingTable> CODEC = simpleCodec(BlockWandEnchantingTable::new);
    private static final Component CONTAINER_TITLE = Component.translatable("container." + ArcaneRelics.MOD_ID + ".wand_enchanting.title");

    public BlockWandEnchantingTable(Properties properties) {
        super(properties);
    }

    @Override
    protected @NonNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NonNull BlockPos blockPos, @NonNull BlockState blockState) {
        return new BlockEntityWandEnchantingTable(blockPos, blockState);
    }

    @Override
    protected @NonNull InteractionResult useWithoutItem(@NonNull BlockState blockState, Level level, @NonNull BlockPos blockPos, @NonNull Player player, @NonNull BlockHitResult blockHitResult) {
        if (!level.isClientSide()) {
            player.openMenu(blockState.getMenuProvider(level, blockPos));
            //player.awardStat(Stats.INTERACT_WITH_CRAFTING_TABLE); // TODO: create stat for wand enchanting table
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    protected @Nullable MenuProvider getMenuProvider(@NonNull BlockState blockState, Level level, @NonNull BlockPos blockPos) {
        if (level.getBlockEntity(blockPos) instanceof BlockEntityWandEnchantingTable blockEntityWandEnchantingTable) {
            return new SimpleMenuProvider(
                    (containerId, inventory, player) -> new WandEnchantingMenu(
                            containerId,
                            inventory,
                            blockEntityWandEnchantingTable,
                            blockEntityWandEnchantingTable.getMenuData(),
                            ContainerLevelAccess.create(level, blockPos)
                    ),
                    CONTAINER_TITLE
            );
        }

        return null;
    }
}
