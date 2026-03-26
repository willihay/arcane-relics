package org.bensam.arcanerelics;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;
import org.bensam.arcanerelics.screen.WandEnchantingScreen;

public class ArcaneRelicsClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.

		// Register screens.
		MenuScreens.register(ModMenus.WAND_ENCHANTING_MENU.get(), WandEnchantingScreen::new);
	}
}