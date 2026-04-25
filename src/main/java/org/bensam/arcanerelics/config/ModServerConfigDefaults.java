package org.bensam.arcanerelics.config;

public final class ModServerConfigDefaults {
    private ModServerConfigDefaults() {}

    public static ModServerConfig create() {
        return new ModServerConfig(
                ModServerConfig.CURRENT_VERSION,
                new WandEnchantingTableConfig(true, true),
                new FangWandConfig(new WandBalanceConfig(
                        20,
                        40,
                        1,
                        1,
                        20,
                        20),
                        8,
                        40),
                new FireballWandConfig(new WandBalanceConfig(
                        20,
                        40,
                        1,
                        2,
                        40,
                        20),
                        8,
                        20,
                        true),
                new IceWandConfig(new WandBalanceConfig(
                        30,
                        60,
                        1,
                        1,
                        40,
                        30),
                        8,
                        50),
                new LevitationWandConfig(new WandBalanceConfig(
                        20,
                        40,
                        1,
                        1,
                        60,
                        20),
                        8,
                        50),
                new LightningWandConfig(new WandBalanceConfig(
                        15,
                        30,
                        1,
                        2,
                        60,
                        15),
                        16,
                        true,
                        50),
                new RegenerationWandConfig(new WandBalanceConfig(
                        10,
                        30,
                        1,
                        1,
                        40,
                        10),
                        20,
                        50),
                new WindWandConfig(new WandBalanceConfig(
                        30,
                        60,
                        1,
                        1,
                        20,
                        30),
                        8,
                        24)
        );
    }
}
