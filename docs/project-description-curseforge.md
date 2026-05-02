![Arcane Relics mod project image](https://raw.githubusercontent.com/willihay/arcane-relics/mc-1.21.11/docs/images/ProjectBanner.png)

## ABOUT

**Arcane Relics** is a magic-focused Fabric mod built around charged wands, a custom **Wand Enchanting Table**, and a progression loop that rewards experimentation.

Start with an inert **Arcane Wand**, learn how to enchant it, and gradually build a collection of specialty wands with distinct strengths, recharge rules, and play styles. Some wands reward careful aim, some reward charging up power, and some open up creative mobility or utility options alongside combat.

For players, Arcane Relics adds magical tools that feel earned rather than handed out. For server admins, it adds a compact but configurable gameplay system that can fit comfortably into survival progression.

## FEATURES

- **Arcane Wand progression**  
  Craft a base Arcane Wand, then transform it into magical specialty wands at the **Wand Enchanting Table**.

  ![Wand Enchanting Table UI](https://raw.githubusercontent.com/willihay/arcane-relics/mc-1.21.11/docs/images/enchanting_UI.png)

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

**Crafting Recipes**

<div class="spoiler">
   <p><strong>Arcane Wand</strong></p>
   <p>
     <img src="https://raw.githubusercontent.com/willihay/arcane-relics/mc-1.21.11/docs/images/recipe_arcane_wand.png" 
alt="Arcane Wand crafting recipe" />
   </p>

   <p><strong>Wand Enchanting Table</strong></p>
   <p>
     <img 
src="https://raw.githubusercontent.com/willihay/arcane-relics/mc-1.21.11/docs/images/recipe_wand_enchanting_table.png" 
alt="Wand Enchanting Table crafting recipe" />
   </p>
</div>

**Wand Enchanting Example**

<div class="spoiler">
<p><strong>Arcane Wand</strong> + <strong>Book of Flame</strong> + <strong>Lapis Lazuli</strong> -> <strong>Fireball 
Wand</strong></p>

 <p><img src="https://raw.githubusercontent.com/willihay/arcane-relics/mc-1.21.11/docs/images/enchanting_fireball_wand.png" 
alt="Enchanting an arcane wand into a fireball wand" /></p>
</div>

**Table of Wands**

<div class="spoiler">
   <table>
     <thead>
       <tr>
         <th>Wand</th>
         <th>Main theme</th>
         <th>Key ingredients</th>
         <th>Alternate recharge source</th>
       </tr>
     </thead>
     <tbody>
       <tr>
         <td>Fang Wand</td>
         <td>Evoker-style fangs</td>
         <td>Totem of Undying</td>
         <td>Evoker</td>
       </tr>
       <tr>
         <td>Fireball Wand</td>
         <td>Fireball projectile</td>
         <td>Book of Flame</td>
         <td>Blaze or any type of Ghast</td>
       </tr>
       <tr>
         <td>Ice Wand</td>
         <td>Ice imprisonment</td>
         <td>Book of Frost Walker</td>
         <td>Stray</td>
       </tr>
       <tr>
         <td>Levitation Wand</td>
         <td>Shulker projectile and self-lift</td>
         <td>Shulker Shell</td>
         <td>Shulker</td>
       </tr>
       <tr>
         <td>Lightning Wand</td>
         <td>Lightning strike</td>
         <td>Book of Channeling</td>
         <td>Lightning Rod during a thunderstorm</td>
       </tr>
       <tr>
         <td>Regeneration Wand</td>
         <td>Regeneration effect</td>
         <td>Golden Apple; Enchanted Golden Apple; Any Potion of Regeneration; Tipped Arrow of Regeneration</td>
         <td>Happy Ghast</td>
       </tr>
       <tr>
         <td>Wind Wand</td>
         <td>Gust attack</td>
         <td>Book of Wind Burst; Any Potion of Wind Charging; Tipped Arrow of Wind Charging</td>
         <td>Breeze</td>
       </tr>
     </tbody>
   </table>
</div>

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
