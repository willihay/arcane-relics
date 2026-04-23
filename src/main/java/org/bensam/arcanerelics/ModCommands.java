package org.bensam.arcanerelics;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.bensam.arcanerelics.command.ConfigCommand;

public class ModCommands {
    private ModCommands() {}

    public static void initialize() {
        CommandRegistrationCallback.EVENT.register(ConfigCommand::new);
    }
}
