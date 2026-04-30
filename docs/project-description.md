![Arcane Relics mod project image](https://cdn.modrinth.com/data/cached_images/cc126ec7089e78ef5a13eb45a03a47074c5a783b_0.webp)

## ABOUT

**Arcane Relics** is a magic-focused Fabric mod built around charged wands, a custom **Wand Enchanting Table**, and a progression loop that rewards experimentation.

Start with an inert **Arcane Wand**, learn how to enchant it, and gradually build a collection of specialty wands with distinct strengths, recharge rules, and play styles. Some wands reward careful aim, some reward charging up power, and some open up creative mobility or utility options alongside combat.

For players, Arcane Relics adds magical tools that feel earned rather than handed out. For server admins, it adds a compact but configurable gameplay system that can fit comfortably into survival progression.

## FEATURES

- **Arcane Wand progression**  
  Craft a base Arcane Wand, then transform it into magical specialty wands at the **Wand Enchanting Table**.

  ![Wand Enchanting Table UI](https://cdn.modrinth.com/data/cached_images/fdc7d43c498141f84570e20ede2c3053d55cf494.png)

- **Seven specialty wands**  
  Build a collection of wands with different behaviors and themes:
  - Fang Wand
  - Fireball Wand
  - Ice Wand
  - Levitation Wand
  - Lightning Wand
  - Regeneration Wand
  - Wind Wand

- **Charge-based magic**  
  Wands use limited charges, so casting decisions matter. Many wands also become stronger when you hold use before releasing.

- **Two recharge paths**  
  Recharge wands in the **Wand Enchanting Table**, or use alternate in-world recharge sources tied to specific mobs or blocks.

- **Custom enchanting flow**  
  Wand transformation does not just copy vanilla enchanting. The Wand Enchanting Table uses its own ingredient-based system for creating and restoring magical wands.

- **Advancement-driven milestones**  
  Track progress as you enchant and recharge different wands.

- **Optional client-side quality-of-life support**  
  Works nicely with optional companion mods such as **JEI**, **Mod Menu**, and **YACL**.

### SPOILERS

<details>
<summary>SPOILERS: Crafting Recipes</summary>

**Arcane Wand**

![Arcane Wand crafting recipe](https://cdn.modrinth.com/data/cached_images/a78e2f71ba5695fa4efdfcef8aa7f846bc9253aa.png)

**Wand Enchanting Table**

![Wand Enchanting Table crafting recipe](https://cdn.modrinth.com/data/cached_images/bdfc11db7a4f19e55c307f297ef89dbf347e3ad7.png)

</details>

<details>
<summary>SPOILERS: Wand Enchanting Example</summary>

**Arcane Wand** + **Book of Flame** + **Lapis Lazuli** -> **Fireball Wand**

![Enchanting an arcane wand into a fireball wand](https://cdn.modrinth.com/data/cached_images/82665ffbd9d37ac64e5455ef60a2f44dcc86b610.png)

</details>

<details>
<summary>SPOILERS: Table of Wands</summary>

| Wand | Main theme | Key ingredients                                                                                | Alternate recharge source |
| --- | --- |------------------------------------------------------------------------------------------------| --- |
| Fang Wand | Evoker-style fangs | Totem of Undying                                                                               | Evoker |
| Fireball Wand | Fireball projectile | Book of Flame                                                                                  | Blaze or any type of Ghast |
| Ice Wand | Ice imprisonment | Book of Frost Walker                                                                           | Stray |
| Levitation Wand | Shulker projectile and self-lift | Shulker Shell                                                                                  | Shulker |
| Lightning Wand | Lightning strike | Book of Channeling                                                                             | Lightning Rod during a thunderstorm |
| Regeneration Wand | Regeneration effect | Golden Apple; Enchanted Golden Apple; Any Potion of Regeneration; Tipped Arrow of Regeneration | Happy Ghast |
| Wind Wand | Gust attack | Book of Wind Burst; Potion of Wind Charged                                                     | Breeze |

</details>

## INSTALLATION

### Players

Required:

1. Fabric Loader
2. Fabric API
3. Arcane Relics

Optional client-side companions:

- **JEI** for recipe browsing
- **Mod Menu** for cleaner mod list integration
- **YACL** for the client config screen used by this mod

Install by placing Arcane Relics and Fabric API into your `mods` folder, then launch the game with Fabric Loader.

### Server admins

For a dedicated server, install:

1. Fabric Loader
2. Fabric API
3. Arcane Relics

JEI, Mod Menu, and YACL are client-side conveniences and are not required on the server.

## CONFIGURATION

### Client configuration

Arcane Relics supports client-side preferences for presentation and usability. If **YACL** is installed, the mod can expose a client config screen, and **Mod Menu** helps surface that cleanly in the mod list.

Client configuration is intended for local preferences such as how much wand tooltip detail you want to see. These settings do not replace server-controlled gameplay balance.

### Server configuration

Arcane Relics stores its gameplay config per world in:

```text
<world save>\data\arcane-relics\server-config.json5
```

This server-side config controls gameplay settings such as wand balance and **Wand Enchanting Table** behavior, including things like enchanting and recharge costs.

Admins can also manage the config in game with:

```mcfunction
/arcrel config reload
/arcrel config reset
```

- `reload` reloads the config from disk and syncs it to connected players
- `reset` restores the default config and writes it back to disk

This makes Arcane Relics practical for both single-player worlds and multiplayer servers that want a magical progression system with adjustable balance.
