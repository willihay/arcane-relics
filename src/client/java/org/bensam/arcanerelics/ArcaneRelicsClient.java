package org.bensam.arcanerelics;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.gui.screens.MenuScreens;
import org.bensam.arcanerelics.config.ClientTooltipBridge;
import org.bensam.arcanerelics.config.ModClientConfigManager;
import org.bensam.arcanerelics.config.ModServerConfig;
import org.bensam.arcanerelics.config.SyncedServerConfig;
import org.bensam.arcanerelics.network.ConfigClientPackets;
import org.bensam.arcanerelics.network.WandClientPackets;
import org.bensam.arcanerelics.renderer.WandClientState;
import org.bensam.arcanerelics.screen.WandEnchantingScreen;

public class ArcaneRelicsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Register screens.
        MenuScreens.register(ModMenus.WAND_ENCHANTING_MENU.get(), WandEnchantingScreen::new);

        // Initialize client config manager.
        ModClientConfigManager.initialize();

        // Initialize bridge that provides access to tooltip-related config in wand tooltip (hover text) append method.
        ClientTooltipBridge.initialize(
                wandItem -> {
                    ModServerConfig config = SyncedServerConfig.get();
                    return config != null ? wandItem.getTooltipConfig(config) : null;
                },
                () -> ModClientConfigManager.getConfig().verboseTooltips()
        );

        // Register packet receivers.
        ConfigClientPackets.registerClientReceivers();
        WandClientPackets.registerClientReceivers();

        // Register renderer tick events.
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.level == null) {
                WandClientState.CAST_ANIMATION.clear();
                return;
            }

            if (!WandClientState.CAST_ANIMATION.isCasting()) {
                return;
            }

            long releaseTick = WandClientState.CAST_ANIMATION.getCastReleaseTick();
            if (releaseTick < 0L) {
                return;
            }

            long now = client.level.getGameTime();
            float durationTicks = 6.0f;
            float progress = Math.min(1.0f, (now - releaseTick) / durationTicks);
            WandClientState.CAST_ANIMATION.tickRelease(progress);

            if (progress >= 1.0f) {
                WandClientState.CAST_ANIMATION.clear();
            }
        });
    }
}
