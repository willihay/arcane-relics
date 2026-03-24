package org.bensam.arcanerelics;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.bensam.arcanerelics.blockentity.BlockEntityWandEnchantingTable;

import java.util.function.Supplier;

public class ModBlockEntities {
    private ModBlockEntities() {}

    private static BlockEntityType<BlockEntityWandEnchantingTable> wandEnchantingTableInternal;
    public static final Supplier<BlockEntityType<BlockEntityWandEnchantingTable>> WAND_ENCHANTING_TABLE =
            () -> wandEnchantingTableInternal;

    public static void initialize() {
        wandEnchantingTableInternal = Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                Identifier.fromNamespaceAndPath(ArcaneRelics.MOD_ID, "wand_enchanting_table"),
                FabricBlockEntityTypeBuilder.create(
                        BlockEntityWandEnchantingTable::new,
                        ModBlocks.WAND_ENCHANTING_TABLE.get()
                ).build()
        );
    }
}
