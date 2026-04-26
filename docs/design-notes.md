# Arcane Relics Design Notes

This document is a design and architecture overview for Arcane Relics. It is written for future maintainers, especially future me, and for anyone who wants to understand the project before forking it or contributing changes.

## Project shape

Arcane Relics is a Fabric mod for Minecraft `1.21.11`.

The codebase is split by environment:

- `src/main` contains common gameplay code, registries, items, blocks, block entities, menus, networking payload definitions, config, stats, and advancement triggers.
- `src/client` contains client-only setup, screens, rendering and mixin code, JEI integration, Mod Menu integration, YACL integration, and datagen entrypoints/providers.

Entrypoints are declared in `src/main/resources/fabric.mod.json`:

- `ArcaneRelics` for common initialization
- `ArcaneRelicsClient` for client initialization
- `ArcaneRelicsDataGenerator` for Fabric datagen
- `JEIPlugin` for JEI integration
- `ModMenuIntegration` for Mod Menu

## High-level gameplay model

The mod revolves around a base Arcane Wand and a set of charged specialty wands.

- The **Arcane Wand** is the inert base item.
- The **Wand Enchanting Table** turns the Arcane Wand into a specialty wand.
- Repeating the same enchanting-table recipe with an already enchanted wand recharges it.
- Most specialty wands also support an alternate in-world recharge source, usually tied to a mob or block.

This leads to three core gameplay flows:

1. Create an Arcane Wand.
2. Enchant it into a specialty wand at the Wand Enchanting Table.
3. Spend and restore charges through either the table or an alternate recharge source.

## Bootstrap and registration layout

`ArcaneRelics.onInitialize()` is the main bootstrap path. It initializes:

- advancements
- stats
- data components
- items
- blocks
- block entities
- menus
- networking
- synced client config
- the creative tab
- commands

Server config is initialized when the overworld loads, then synced to players separately.

The code follows a `Mod*` registration pattern:

- `ModItems`
- `ModBlocks`
- `ModBlockEntities`
- `ModMenus`
- `ModAdvancements`
- `ModNetworks`
- `ModStats`
- and similar classes

Registered instances are stored in private static fields and exposed through `Supplier`s. This keeps initialization order explicit and avoids early static access problems.

## Wand architecture

`AbstractChargedWandItem` is the center of the wand system.

It owns the common behavior for:

- charge storage and normalization
- cast lifecycle
- power-up timing
- recharge lifecycle
- target helpers
- common success and failure handling
- advancement and stat hooks for recharge-related events
- server-to-client cast animation packets

Concrete wand item classes mostly provide:

- their runtime balance config selection
- `performCast(...)`
- `tryRecharge(...)`
- optional wand-specific feedback or effects

When adding or changing a wand, start by understanding the base class first.

## Wand state and metadata

Wand charge state is stored in data components, not ad-hoc NBT.

Important design choices:

- current charges use `ModComponents.WAND_CHARGES_COMPONENT`
- tooltip text comes from `WandDefinition`
- gameplay balance is not stored in `WandDefinition`

`WandDefinition` is intentionally small. It holds stable item-definition metadata such as:

- translation key prefix
- tooltip line count

Gameplay balance comes from config at runtime, not from baked registration metadata.

## Config architecture

The server gameplay config is world-local JSON5, managed through `ModServerConfigManager`.

Important consequences of that design:

- gameplay balance is server authoritative
- most gameplay code should read config at runtime
- clients receive a synced snapshot for display and client-side behavior gates
- config is centrally managed for the current server

Client preferences are separate from server gameplay config. The client can have local settings, while the server still owns gameplay-affecting values.

The Arcane Wand is intentionally treated as a special case and does not participate in the same gameplay-balance configuration as the specialty wands.

## Wand Enchanting Table design

The Wand Enchanting Table does not use custom recipe JSON for the wand transformation logic.

Instead:

- `BlockEntityWandEnchantingTable` computes the output stack and XP cost from the input slots
- `WandEnchantingMenu` exposes that derived state to the UI
- wand items define their own enchanting inputs by implementing `WandEnchantingTableOutput`

This means the source of truth for wand enchanting behavior is on the wand item side, not in the block entity or screen.

The shared pattern is:

1. The table inspects the ingredient item.
2. `ModItems.getWandEnchantmentOutput(...)` determines which wand item can be produced or recharged.
3. The block entity computes the output stack and XP cost.
4. The menu handles taking the result and awards stats and advancements.

## JEI and datagen implications

JEI recipes are built at runtime from wand declarations, not from static recipe JSON.

That means:

- if a wand enchanting rule changes, update the wand item and its enchanting sources
- do not duplicate enchanting logic in JEI code
- do not treat JEI as the source of truth

Vanilla crafting JSON under `src/main/resources/data/arcane-relics/recipe` is only for normal crafting such as:

- `arcane_wand`
- `wand_enchanting_table`

Advancements are generated in code from `src/client/.../datagen/advancement/AdvancementGenerator.java` and emitted to `src/main/generated/data/...`.

## Advancement trigger design

Custom advancement triggers are used for wand-specific events:

- `WandEnchantTrigger`
- `WandRechargeTrigger`

The project uses separate trigger classes for separate gameplay events instead of one overly generic trigger type.

This keeps the API readable:

- enchanting triggers talk about enchanted wands
- recharge triggers talk about recharged wands and alternate-source behavior

For recharge, the trigger can distinguish between:

- recharge in the Wand Enchanting Table
- recharge from an alternate source

That distinction is intentional and is part of advancement design.

## Source layout by responsibility

These files are good entry points when exploring the mod:

| Area | File                                                                                    |
| --- |-----------------------------------------------------------------------------------------|
| Common bootstrap | `src/main/java/org/bensam/arcanerelics/ArcaneRelics.java`                               |
| Item registration | `src/main/java/org/bensam/arcanerelics/ModItems.java`                                   |
| Wand base behavior | `src/main/java/org/bensam/arcanerelics/item/AbstractChargedWandItem.java`               |
| Wand enchanting logic | `src/main/java/org/bensam/arcanerelics/blockentity/BlockEntityWandEnchantingTable.java` |
| Menu result handling | `src/main/java/org/bensam/arcanerelics/menu/WandEnchantingMenu.java`                    |
| Client config | `src/client/java/org/bensam/arcanerelics/config/ModClientConfigManager.java`            |  
| Server config | `src/main/java/org/bensam/arcanerelics/config/ModServerConfigManager.java`              |
| Client synced config bridge | `src/main/java/org/bensam/arcanerelics/config/ConfigBridgeForClient.java`               |
| Datagen entrypoint | `src/client/java/org/bensam/arcanerelics/ArcaneRelicsDataGenerator.java`                |
| Advancement datagen | `src/client/java/org/bensam/arcanerelics/datagen/advancement/AdvancementGenerator.java` |

## Practical design rules

These rules match the current architecture and are worth preserving unless there is a good reason to change them.

### 1. Keep client-only code in `src/client`

Screens, JEI, render helpers, Mod Menu hooks, YACL hooks, and client mixins belong in `src/client`.

### 2. Keep gameplay logic in `src/main`

Wand behavior, registries, items, blocks, menus, stats, triggers, and server-safe networking declarations belong in `src/main`.

### 3. Put enchanting behavior on wand items

Do not duplicate wand enchanting logic in:

- the block entity
- the menu
- the screen
- JEI

Those layers should consume wand-defined rules, not reimplement them.

### 4. Use runtime config for gameplay balance

Do not introduce baked balance values into `WandDefinition` or other static registration metadata.

### 5. Use data components for wand state

Avoid custom NBT for wand charge state unless there is a very strong reason.

### 6. Preserve identifier and registration patterns

Use `Identifier.fromNamespaceAndPath(ArcaneRelics.MOD_ID, ...)` and the existing `Mod*` registration style for new registered content.

### 7. Keep tooltip metadata and translations in sync

Wand tooltip lines are driven by translation keys like:

- `item.arcane-relics.<wand>.info.1`
- `item.arcane-relics.<wand>.info.2`

The number of lines must stay aligned with `WandDefinition.tooltipLineCount`.

## Build, test, and datagen notes

Useful commands from the repo root:

```powershell
.\gradlew.bat build --no-daemon
.\gradlew.bat runClient
.\gradlew.bat runDatagen
```

Notes:

- `test` currently exists but the repository has no `src/test` sources.
- `runDatagen` writes generated data under `src/main/generated/data`.
- generated datagen cache files under `src/main/generated/.cache` are artifacts from the datagen process and should not be treated as source.

## When adding a new wand

Use this checklist:

1. Add the new item class under `src/main/.../item`. You may find it useful to copy-paste `ItemTemplateWand` when starting a new wand. This template file also contains step-by-step instructions for completing common wand implementation patterns.
2. Register it in `ModItems`.
3. Add a `WandDefinition` entry with tooltip metadata.
4. Wire its runtime balance config.
5. Implement casting behavior in the subclass.
6. Implement recharge behavior in `tryRecharge(...)`.
7. If it can be produced or recharged at the table, implement `WandEnchantingTableOutput`.
8. Add translation keys and tooltip lines.
9. Add or update advancement datagen if the wand needs progression coverage.
10. Verify JEI output if the wand participates in enchanting-table transformations.

## Why this structure exists

The mod has grown around a few goals:

- keep gameplay behavior centralized and understandable
- avoid duplicated rule definitions across UI, JEI, and gameplay layers
- let server owners control balance through config
- keep client-only integration isolated
- make new wands follow a repeatable pattern

If future changes preserve those goals, the project will stay easier to maintain.
