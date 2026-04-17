# Arcane Relics

## Build and test commands

- `.\gradlew.bat build --no-daemon` builds the mod and produces the remapped jars under `build/libs/`.
- `.\gradlew.bat test --no-daemon` runs the Gradle test task. The task exists, but the repository currently has no `src/test` sources.
- `.\gradlew.bat test --tests "org.bensam.arcanerelics.SomeTest"` runs a single test class or method when test sources are present.
- `.\gradlew.bat runClient` starts the Fabric development client.
- `.\gradlew.bat runDatagen` starts the Fabric data generation run configuration.

## High-level architecture

- This is a Fabric mod for Minecraft `1.21.11` with Loom's split environment source sets enabled. Shared gameplay code lives in `src/main`, while client-only screens, JEI integration, render helpers, and client mixins live in `src/client`.
- `src/main/resources/fabric.mod.json` wires four entrypoints: `ArcaneRelics` for common initialization, `ArcaneRelicsClient` for client setup, `ArcaneRelicsDataGenerator` for datagen, and `JEIPlugin` for JEI integration.
- `ArcaneRelics.onInitialize()` is the common bootstrap path. It initializes components, items, blocks, block entities, menus, network payload types, and the creative tab, then registers tooltip appenders so wand charge/tooltips render from data components.
- The wand system is centered on `AbstractChargedWandItem`. The base class owns charge storage, use/release lifecycle, power-up timing, recharge flow, targeting helpers, generic success/failure effects, and the server-to-client casting packets. Individual wands mostly supply balance via `WandDefinition` and override `performCast(...)`, `tryRecharge(...)`, and optional feedback/effect hooks.
- The Wand Enchanting Table is not driven by custom recipe JSON. `BlockEntityWandEnchantingTable` derives the output wand and XP cost from its four slots, and `WandEnchantingMenu` / `WandEnchantingScreen` only expose that derived state. The actual matching rules come from wand items that implement `WandEnchantingTableOutput` and declare `WandEnchantingSource` matchers such as `FixedItemSource`, `EnchantedBookSource`, and `PotionSource`.
- `ModItems.register(...)` automatically collects charged wands into the lists used by both the enchanting table and JEI. JEI recipes are assembled at runtime by `WandEnchantingRecipeBuilder` from those wand declarations, so recipe changes should start at the wand item, not the JEI layer.
- Vanilla crafting JSON under `src/main/resources/data/arcane-relics/recipe` only covers the base `arcane_wand` and `wand_enchanting_table`. Transforming one wand into another happens through the custom enchanting table flow.
- Client cast animation is split across networking and mixins: the server sends `WandBeginCastS2CPayload` / `WandSucceedCastS2CPayload`, `WandClientPackets` updates `WandClientState`, and client mixins adjust first-person wand rendering and show charge feedback.

## Key conventions

- Follow the central `Mod*` registration pattern (`ModItems`, `ModBlocks`, `ModMenus`, etc.) and build identifiers with `Identifier.fromNamespaceAndPath(ArcaneRelics.MOD_ID, ...)`. Registered instances are stored in private static fields and exposed through `Supplier`s because registration happens during initialization.
- Treat wand state as data components, not ad-hoc NBT. `WandDefinition.createProperties(...)` is the canonical way to attach `wand_charges`, `wand_max_charges`, and tooltip metadata to a wand item.
- When adding a wand, put the balance numbers in `ModItems`, subclass `AbstractChargedWandItem`, and implement `WandEnchantingTableOutput` if the wand should be producible/rechargeable in the table. `getEnchantingSources()` is the source of truth for both the table and JEI.
- Do not duplicate enchanting logic in `BlockEntityWandEnchantingTable`, `WandEnchantingMenu`, or JEI classes. Those classes assume wand items define their own catalyst rules and charge behavior.
- Keep client-only code in `src/client`. Common gameplay logic, registries, items, blocks, and server-safe networking declarations stay in `src/main`.
- Tooltip text for wands is driven by translation keys like `item.arcane-relics.<wand>.info.1`, `info.2`, etc. The number of lines must stay in sync with `WandDefinition.tooltipLineCount`.
