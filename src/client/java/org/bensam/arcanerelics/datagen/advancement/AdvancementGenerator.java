package org.bensam.arcanerelics.datagen.advancement;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.minecraft.advancements.*;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import org.bensam.arcanerelics.ArcaneRelics;
import org.bensam.arcanerelics.ModBlocks;
import org.bensam.arcanerelics.ModItems;
import org.bensam.arcanerelics.advancement.WandEnchantTrigger;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class AdvancementGenerator extends FabricAdvancementProvider {
    public AdvancementGenerator(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(output, registryLookup);
    }

    @Override
    public void generateAdvancement(HolderLookup.Provider registryLookup, Consumer<AdvancementHolder> consumer) {
        AdvancementHolder arcaneRelics = Advancement.Builder.advancement()
                .display(
                        ModItems.ARCANE_WAND.get(), // display icon
                        Component.translatable(advTranslationTitle("root")), // title
                        Component.translatable(advTranslationDesc("root")), // description
                        Identifier.parse("minecraft:block/obsidian"), // background for this tab's advancements page
                        AdvancementType.TASK,
                        true, // show toast on completion
                        true, // announce it to chat
                        false // hide until achieved
                )
                .addCriterion("has_ender_eye", InventoryChangeTrigger.TriggerInstance.hasItems(Items.ENDER_EYE))
                .rewards(new AdvancementRewards.Builder()
                        .addRecipe(recipeKey("arcane_wand"))
                        .addRecipe(recipeKey("wand_enchanting_table")))
                .save(consumer, advName("relics/root"));

        AdvancementHolder craftWandEnchantingTable = newChildAdvancement(
                arcaneRelics,
                ModBlocks.WAND_ENCHANTING_TABLE.get().asItem(),
                "wand_enchanting_table",
                AdvancementType.TASK,
                true, true, false)
                .addCriterion("has_wand_enchanting_table", InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.WAND_ENCHANTING_TABLE.get().asItem()))
                .save(consumer, advName("relics/wand_enchanting_table"));

        AdvancementHolder enchantFirstWand = newChildAdvancement(
                craftWandEnchantingTable,
                ModItems.ARCANE_WAND.get(),
                "first_wand",
                AdvancementType.CHALLENGE,
                true, false, false)
                .addCriterion("enchanted_any_wand", WandEnchantTrigger.TriggerInstance.enchantedAnyWand())
                .rewards(new AdvancementRewards.Builder().addExperience(50))
                .save(consumer, advName("relics/first_wand"));

//        AdvancementHolder rechargeFirstWand = newChildAdvancement(
//                craftWandEnchantingTable,
//                ModItems.ARCANE_WAND.get(),
//                "first_wand_recharge",
//                AdvancementType.TASK,
//                true, false, false)
//                .addCriterion("recharged_any_wand", WandRechargeTrigger.TriggerInstance.rechargedAnyWand())
//                .save(consumer, advName("relics/first_wand_recharge"));

        AdvancementHolder enchantFangWand = newChildAdvancement(
                enchantFirstWand,
                ModItems.FANG_WAND.get(),
                "fang_wand",
                AdvancementType.GOAL,
                true, true, false)
                .addCriterion("enchanted_fang_wand", WandEnchantTrigger.TriggerInstance.enchantedWand(ModItems.FANG_WAND.get()))
                .save(consumer, advName("relics/fang_wand"));

//        AdvancementHolder rechargeFangWandFromMob = newChildAdvancement(
//                enchantFangWand,
//                ModItems.FANG_WAND.get(),
//                "fang_wand_alt_recharge",
//                AdvancementType.CHALLENGE,
//                true, true, false)
//                .addCriterion("recharge_fang_wand_from_mob", WandRechargeTrigger.TriggerInstance.rechargedWandFromAlternateSource(ModItems.FANG_WAND.get()))
//                .rewards(new AdvancementRewards.Builder().addExperience(40))
//                .save(consumer, advName("relics/fang_wand_alt_recharge"));

        AdvancementHolder enchantFireballWand = newChildAdvancement(
                enchantFirstWand,
                ModItems.FIREBALL_WAND.get(),
                "fireball_wand",
                AdvancementType.GOAL,
                true, true, false)
                .addCriterion("enchanted_fireball_wand", WandEnchantTrigger.TriggerInstance.enchantedWand(ModItems.FIREBALL_WAND.get()))
                .save(consumer, advName("relics/fireball_wand"));

        AdvancementHolder allWands = newChildAdvancement(
                craftWandEnchantingTable,
                ModBlocks.WAND_ENCHANTING_TABLE.get().asItem(),
                "all_wands",
                AdvancementType.CHALLENGE,
                true, true, false)
                .addCriterion("enchanted_fang_wand", WandEnchantTrigger.TriggerInstance.enchantedWand(ModItems.FANG_WAND.get()))
                .addCriterion("enchanted_fireball_wand", WandEnchantTrigger.TriggerInstance.enchantedWand(ModItems.FIREBALL_WAND.get()))
                .rewards(new AdvancementRewards.Builder().addExperience(500))
                .save(consumer, advName("relics/all_wands"));
    }

    private static Advancement.Builder newChildAdvancement(
            AdvancementHolder parent,
            ItemLike displayItem,
            String advName,
            AdvancementType advType,
            boolean showToast,
            boolean announce,
            boolean hide
    ) {
        return Advancement.Builder.advancement().parent(parent)
                .display(
                        displayItem,
                        Component.translatable(advTranslationTitle(advName)),
                        Component.translatable(advTranslationDesc(advName)),
                        null,
                        advType,
                        showToast,
                        announce,
                        hide
                );
    }

    private static String advName(String path) {
        return ArcaneRelics.MOD_ID + ":" + path;
    }

    private static String advTranslationTitle(String advancement) {
        return "advancement." + ArcaneRelics.MOD_ID + "." + advancement + ".title";
    }

    private static String advTranslationDesc(String advancement) {
        return "advancement." + ArcaneRelics.MOD_ID + "." + advancement + ".description";
    }

    private static ResourceKey<Recipe<?>> recipeKey(String path) {
        return ResourceKey.create(
                Registries.RECIPE,
                Identifier.fromNamespaceAndPath(ArcaneRelics.MOD_ID, path)
        );
    }
}
