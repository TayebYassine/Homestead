# Flags

Flags are permissions that control what players and the environment can do inside your region. Homestead includes 35+ player flags and 20+ world flags, giving you complete control over your land.

## Understanding Flags

**Player Flags** control what players (including non-members) can do in your region.  
**World Flags** control environmental effects and mob behavior.

Each flag can be set to:

- **Allow** (`true`): The action is permitted
- **Deny** (`false`): The action is blocked

## Setting Default Flag Values

Default flags apply to all newly created regions. Players can change these later for their own regions.

In `config.yml`:

```yaml
default-players-flags:
  break-blocks: false      # Deny by default
  place-blocks: false      # Deny by default
  containers: false        # Deny by default
  passthrough: true        # Allow by default
  # ... more flags
  
default-world-flags:
  passive-entities-spawn: true   # Allow by default
  hostile-entities-spawn: true   # Allow by default
  entities-grief: false          # Deny by default
  # ... more flags
```

## Player Flags

Player flags control what non-members can do inside regions. Members and owners typically have all permissions by default.

### Building & Destruction

| Flag           | Default | Description                                    |
|----------------|:-------:|------------------------------------------------|
| break-blocks   | Deny    | Break or destroy blocks                        |
| place-blocks   | Deny    | Place blocks                                   |
| frost-walker   | Deny    | Use Frost Walker enchanted boots (creates ice) |
| ignite         | Deny    | Start fires with flint and steel               |

**Use case:** Keep these denied to prevent griefing. Only trust specific players if you want them to build.

### Container Access

| Flag       | Default | Description                |
|------------|:-------:|----------------------------|
| containers | Deny    | Open chests, barrels, etc. |
| use-anvil  | Deny    | Use anvils                 |

**Use case:** Deny container access to protect valuable items. Enable for shops or community storage areas.

### Doors & Gates

| Flag        | Default | Description         |
|-------------|:-------:|---------------------|
| doors       | Deny    | Open/close doors    |
| trap-doors  | Deny    | Open/close trapdoors|
| fence-gates | Deny    | Open/close gates    |

**Use case:** Deny to keep your base secure. Allow if you want visitors to enter buildings.

### Redstone & Mechanisms

| Flag            | Default | Description                     |
|-----------------|:-------:|---------------------------------|
| redstone        | Deny    | Use redstone components         |
| levers          | Deny    | Use levers                      |
| buttons         | Deny    | Press buttons                   |
| pressure-plates | Deny    | Trigger pressure plates         |
| trigger-tripwire| Deny    | Activate tripwires              |
| use-bells       | Deny    | Ring bells                      |

**Use case:** Deny to prevent outsiders from using your redstone contraptions. Allow for public farms or minigames.

### Farming & Crops

| Flag             | Default | Description                             |
|------------------|:-------:|-----------------------------------------|
| harvest-crops    | Deny    | Harvest (break) crops                   |
| block-trampling  | Deny    | Trample farmland and turtle eggs        |

**Use case:** Deny to protect your farms. Allow for public/community farms.

### Entity Interaction

| Flag                     | Default | Description                               |
|--------------------------|:-------:|-------------------------------------------|
| interact-entities        | Deny    | Right-click entities (general)            |
| armor-stands             | Deny    | Remove armor from armor stands            |
| item-frame-interaction   | Deny    | Add/remove items from item frames         |
| damage-passive-entities  | Deny    | Kill passive mobs (cows, sheep, etc.)     |
| damage-hostile-entities  | Deny    | Kill hostile mobs (zombies, skeletons)    |
| trade-villagers          | Deny    | Trade with villagers                      |
| vehicles                 | Deny    | Ride vehicles (minecarts, horses, boats)  |
| spawn-entities           | Allow   | Use spawn eggs                            |

**Use case:** Control who can interact with your animals, villagers, and decorations.

### Movement & Teleportation

| Flag            | Default | Description                                |
|-----------------|:-------:|--------------------------------------------|
| passthrough     | Allow   | Enter and move through the region          |
| teleport        | Deny    | Teleport using ender pearls or chorus fruit|
| elytra          | Allow   | Fly with elytra                            |
| teleport-spawn  | Deny    | Teleport to region's spawn point           |

**Use case:**

- Set `passthrough: false` for completely private regions
- Allow `elytra` for aerial access but deny `teleport` to control entry points

### Combat

| Flag             | Default | Description                    |
|------------------|:-------:|--------------------------------|
| pvp              | Deny    | Fight other players            |
| take-fall-damage | Deny    | Take damage from falling       |
| throw-potions    | Deny    | Throw splash/lingering potions |

**Use case:** Enable PvP for arena regions. Deny fall damage in parkour areas.

### Other Actions

| Flag              | Default | Description                        |
|-------------------|:-------:|------------------------------------|
| general-interaction| Deny   | General interactions (catch-all)   |
| pickup-items      | Allow   | Pick up dropped items              |
| sleep             | Deny    | Use beds to sleep                  |
| trigger-raid      | Deny    | Start raids by entering with Bad Omen |

## World Flags

World flags control the environment, mob spawning, and natural events within regions.

### Mob Spawning

| Flag                   | Default | Description                        |
|------------------------|:-------:|------------------------------------|
| passive-entities-spawn | Allow   | Spawn passive mobs (animals)       |
| hostile-entities-spawn | Allow   | Spawn hostile mobs (monsters)      |

**Use case:**

- Deny hostile spawning for safe zones
- Deny passive spawning to prevent lag from animals
- Allow both for normal gameplay

### Entity Damage

| Flag                      | Default | Description                              |
|---------------------------|:-------:|------------------------------------------|
| entities-grief            | Deny    | Entities (except creepers) damage blocks |
| entities-damage-entities  | Deny    | Entities attack each other               |
| explosions-damage         | Deny    | Explosions (except wither) damage blocks |
| wither-damage             | Deny    | Wither damages blocks                    |

**Use case:** Keep these denied to prevent environmental damage to your builds.

### Environmental Effects

| Flag          | Default | Description                |
|---------------|:-------:|----------------------------|
| fire-spread   | Deny    | Fire spreads to blocks     |
| leaves-decay  | Allow   | Leaves decay naturally     |
| liquid-flow   | Deny    | Water/lava flows in        |
| snow-melting  | Allow   | Snow melts naturally       |
| ice-melting   | Allow   | Ice melts naturally        |

**Use case:** Deny `fire-spread` to prevent accidental fires. Deny `liquid-flow` to stop grief via water/lava.

### Plant Growth

| Flag         | Default | Description               |
|--------------|:-------:|---------------------------|
| plant-growth | Allow   | Crops and plants grow     |
| grass-growth | Allow   | Grass spreads             |
| sculk-spread | Allow   | Sculk spreads from shriekers |

**Use case:** Allow for normal farming. Deny if you want static decorative plants.

### External Interactions

| Flag                     | Default | Description                                  |
|--------------------------|:-------:|----------------------------------------------|
| wilderness-pistons       | Deny    | Pistons outside region push/pull blocks in   |
| wilderness-dispensers    | Deny    | Dispensers outside region dispense into region |
| wilderness-minecarts     | Deny    | Minecarts from outside enter region          |
| wilderness-copper-golems | Deny    | Copper golems from outside enter region      |

**Use case:** Keep denied to prevent griefing from the outside.

### Special Features

| Flag             | Default | Description                         |
|------------------|:-------:|-------------------------------------|
| player-glowing   | Allow   | Players have glowing effect         |
| snowman-trails   | Allow   | Snow golems leave snow trails       |
| windcharge-burst | Deny    | Wind charges can burst in region    |
| wars             | Deny    | Region can be targeted for war      |

## Configuring Flags

### In-Game

Players can change flags for their own regions using:

- The `/region flags` command
- The region menu GUI (press the configured item)

### Default Values

Server admins configure default flag values in `config.yml`:

```yaml
default-players-flags:
  break-blocks: false       # Non-members can't break blocks
  place-blocks: false       # Non-members can't place blocks
  containers: false         # Non-members can't open chests
  doors: false              # Non-members can't open doors
  passthrough: true         # Anyone can enter the region
  pvp: false                # No PvP by default
  pickup-items: true        # Anyone can pick up items
  elytra: true              # Anyone can fly with elytra
  
default-world-flags:
  passive-entities-spawn: true    # Animals spawn normally
  hostile-entities-spawn: true    # Monsters spawn normally
  entities-grief: false           # Prevent mob griefing
  fire-spread: false              # Prevent fire spreading
  explosions-damage: false        # Prevent explosion damage
  liquid-flow: false              # Prevent water/lava flow
  plant-growth: true              # Allow farming
  leaves-decay: true              # Leaves decay naturally
```

## Common Flag Combinations

### Safe Spawn/Town

```yaml
passthrough: true              # Anyone can enter
break-blocks: false            # No building
place-blocks: false            # No building  
pvp: false                     # No fighting
hostile-entities-spawn: false  # No monsters
take-fall-damage: false        # Safe landing
```

### PvP Arena

```yaml
passthrough: true          # Anyone can enter
pvp: true                  # Enable PvP
break-blocks: false        # Prevent terrain damage
place-blocks: false        # Prevent terrain damage
explosions-damage: false   # Protect arena structure
```

### Public Farm

```yaml
passthrough: true       # Anyone can enter
harvest-crops: true     # Anyone can harvest
place-blocks: true      # Anyone can replant
break-blocks: true      # Anyone can till soil
plant-growth: true      # Crops grow normally
```

### Private Base

```yaml
passthrough: false        # Members only
break-blocks: false       # Extra protection
place-blocks: false       # Extra protection
containers: false         # Protect storage
doors: false              # Locked doors
hostile-entities-spawn: false  # Safe environment
```

### Shop Region

```yaml
passthrough: true          # Customers can enter
containers: true           # Can buy from chests
buttons: true              # Can use item sorters
break-blocks: false        # Can't grief
place-blocks: false        # Can't grief
```
