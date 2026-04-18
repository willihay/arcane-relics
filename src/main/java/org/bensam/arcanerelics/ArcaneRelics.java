package org.bensam.arcanerelics;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.ComponentTooltipAppenderRegistry;
import net.minecraft.core.component.DataComponents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArcaneRelics implements ModInitializer {
	public static final String MOD_ID = "arcane-relics";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		LOGGER.info("onInitialize start");

		ModStats.initialize();
		ModComponents.initialize();
		ModItems.initialize();
		ModBlocks.initialize();
		ModBlockEntities.initialize();
		ModMenus.initialize();
		ModNetworks.initialize();

		ComponentTooltipAppenderRegistry.addAfter(DataComponents.DAMAGE, ModComponents.WAND_CHARGES_COMPONENT);
		ComponentTooltipAppenderRegistry.addAfter(ModComponents.WAND_CHARGES_COMPONENT, ModComponents.WAND_TOOLTIP_COMPONENT);

		ModCreativeTab.initialize();
		LOGGER.info("onInitialize complete");
	}
}