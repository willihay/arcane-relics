package org.bensam.arcanerelics.integration.yacl;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.bensam.arcanerelics.config.*;
import org.bensam.arcanerelics.network.ConfigClientPackets;

public class YACLConfigScreen {
    private YACLConfigScreen() {}

    public static Screen create(Screen parentScreen) {
        return YetAnotherConfigLib.createBuilder()
                .title(Component.literal("Arcane Relics config"))
                .save(YACLConfigScreen::saveConfig)
                .category(ConfigCategory.createBuilder()
                        .name(Component.literal("Client Configuration"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.literal("Verbose wand tooltips"))
                                .description(OptionDescription.of(Component.literal("Show help text in the tooltip for each wand. Turn off to see just the current charge information.")))
                                .binding(
                                        true, // default value
                                        () -> ModClientConfigManager.getConfig().verboseTooltips(), // getter to current value
                                        newValue -> ModClientConfigManager.getConfig().verboseTooltips = newValue
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build()
                        )
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.literal("Fireball wand aim assist"))
                                .description(OptionDescription.of(Component.literal("Use aim assist on fireballs from a fireball wand (if server permits).")))
                                .binding(
                                        true,
                                        () -> ModClientConfigManager.getConfig().fireballWand().aimAssistEnabled(),
                                        newValue -> ModClientConfigManager.getConfig().fireballWand().aimAssistEnabled = newValue
                                )
                                .controller(BooleanControllerBuilder::create)
                                .build()
                        )
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.literal("Lightning wand can break blocks"))
                                .description(OptionDescription.of(Component.literal("Lightning from a lightning wand breaks blocks at full power (if server permits).")))
                                .binding(
                                        true,
                                        () -> ModClientConfigManager.getConfig().lightningWand().blockBreakingExplosionEnabled(),
                                        newValue -> ModClientConfigManager.getConfig().lightningWand().blockBreakingExplosionEnabled = newValue
                                )
                                .controller(BooleanControllerBuilder::create)
                                .build()
                        )
                        .build()
                )
                .build()
                .generateScreen(parentScreen);
    }

    protected static void saveConfig() {
        ModClientConfigManager.save();
        ConfigClientPackets.sendClientPreferences();
    }
}
