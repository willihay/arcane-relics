package org.bensam.arcanerelics;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import org.bensam.arcanerelics.block.BlockWandEnchantingTable;

import java.util.function.Function;
import java.util.function.Supplier;

public class ModBlocks {
    private ModBlocks() {}

    private static BlockWandEnchantingTable wandEnchantingTableInternal;
    public static final Supplier<BlockWandEnchantingTable> WAND_ENCHANTING_TABLE = () -> wandEnchantingTableInternal;

    public static void initialize() {
        // Register mod blocks.
        wandEnchantingTableInternal = register(
                "wand_enchanting_table",
                BlockWandEnchantingTable::new,
                BlockBehaviour.Properties.of()
                        .mapColor(MapColor.COLOR_CYAN)
                        .instrument(NoteBlockInstrument.BASEDRUM)
                        .requiresCorrectToolForDrops()
                        .lightLevel(blockState -> 7)
                        .sound(SoundType.STONE)
                        .strength(5.0f, 1200.0f)
        );
    }

    public static <T extends Block> T register(
            String name,
            Function<BlockBehaviour.Properties, T> blockFactory,
            BlockBehaviour.Properties settings
    ) {
        // Create the block key.
        ResourceKey<Block> blockKey = ResourceKey.create(
                Registries.BLOCK,
                Identifier.fromNamespaceAndPath(ArcaneRelics.MOD_ID, name)
        );

        // Create the block instance.
        T block = blockFactory.apply(settings.setId(blockKey));

        // Create the block item key.
        ResourceKey<Item> blockItemKey = ResourceKey.create(
                Registries.ITEM,
                Identifier.fromNamespaceAndPath(ArcaneRelics.MOD_ID, name)
        );

        // Create the block item instance.
        BlockItem blockItem = new BlockItem(
                block,
                new Item.Properties().setId(blockItemKey).useBlockDescriptionPrefix()
        );

        // Register the block item.
        Registry.register(BuiltInRegistries.ITEM, blockItemKey, blockItem);

        // Register the block.
        return Registry.register(BuiltInRegistries.BLOCK, blockKey, block);
    }
}
