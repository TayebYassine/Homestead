# Flags

Flags are permissions that control what players and the environment can do inside your region. Homestead includes 35+ player flags and 20+ world flags, giving you complete control over your land.

## Understanding Flags

- **Player Flags** control what players (including non-members) can do in your region.
- **World Flags** control environmental effects and mob behavior.
- **Region Control Flags** control what trusted players can manage in your region.

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
  passive-entity-spawn: true   # Allow by default
  hostile-entity-spawn: true   # Allow by default
  entity-grief: false          # Deny by default
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

### Container Access

| Flag       | Default | Description                |
|------------|:-------:|----------------------------|
| containers | Deny    | Open chests, barrels, etc. |
| use-anvil  | Deny    | Use anvils                 |

### Doors & Gates

| Flag        | Default | Description         |
|-------------|:-------:|---------------------|
| doors       | Deny    | Open/close doors    |
| trap-doors  | Deny    | Open/close trapdoors|
| fence-gates | Deny    | Open/close gates    |

### Redstone & Mechanisms

| Flag            | Default | Description                     |
|-----------------|:-------:|---------------------------------|
| redstone        | Deny    | Use redstone components         |
| levers          | Deny    | Use levers                      |
| buttons         | Deny    | Press buttons                   |
| pressure-plates | Deny    | Trigger pressure plates         |
| trigger-tripwire| Deny    | Activate tripwires              |
| use-bells       | Deny    | Ring bells                      |

### Farming & Crops

| Flag             | Default | Description                             |
|------------------|:-------:|-----------------------------------------|
| harvest-crops    | Deny    | Harvest (break) crops                   |
| block-trampling  | Deny    | Trample farmland and turtle eggs        |

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

### Movement & Teleportation

| Flag            | Default | Description                                |
|-----------------|:-------:|--------------------------------------------|
| passthrough     | Allow   | Enter and move through the region          |
| teleport        | Deny    | Teleport using ender pearls or chorus fruit|
| elytra          | Allow   | Fly with elytra                            |
| teleport-spawn  | Deny    | Teleport to region's spawn point           |

### Combat

| Flag             | Default | Description                    |
|------------------|:-------:|--------------------------------|
| pvp              | Deny    | Fight other players            |
| take-fall-damage | Deny    | Take damage from falling       |
| throw-potions    | Deny    | Throw splash/lingering potions |

### Other Actions

| Flag                | Default | Description                           |
|---------------------|:-------:|---------------------------------------|
| general-interaction |  Deny   | General interactions (catch-all)      |
| pickup-items        |  Allow  | Pick up dropped items                 |
| sleep               |  Deny   | Use beds to sleep                     |
| trigger-raid        |  Deny   | Start raids by entering with Bad Omen |

## World Flags

World flags control the environment, mob spawning, and natural events within regions.

### Mob Spawning

| Flag                 | Default | Description                        |
|----------------------|:-------:|------------------------------------|
| passive-entity-spawn | Allow   | Spawn passive mobs (animals)       |
| hostile-entity-spawn | Allow   | Spawn hostile mobs (monsters)      |

### Entity Damage

| Flag             | Default | Description                              |
|------------------|:-------:|------------------------------------------|
| entity-grief     |  Deny   | Entities (except creepers) damage blocks |
| entity-damage    |  Deny   | Entities attack each other               |
| explosion-damage |  Deny   | Explosions (except wither) damage blocks |
| wither-damage    |  Deny   | Wither damages blocks                    |
| projectiles      |  Deny   | Any projectile shot by a non-player      |

### Environmental Effects

| Flag         | Default | Description            |
|--------------|:-------:|------------------------|
| fire-spread  |  Deny   | Fire spreads to blocks |
| leaves-decay |  Allow  | Leaves decay naturally |
| liquid-flow  |  Deny   | Water/lava flows in    |
| snow-melting |  Allow  | Snow melts naturally   |
| ice-melting  |  Allow  | Ice melts naturally    |
| weather-snow |  Allow  | Weather snow           |

### Plant Growth

| Flag         | Default | Description               |
|--------------|:-------:|---------------------------|
| plant-growth | Allow   | Crops and plants grow     |
| grass-growth | Allow   | Grass spreads             |
| sculk-spread | Allow   | Sculk spreads from shriekers |

### External Interactions

| Flag                        | Default | Description                                  |
|-----------------------------|:-------:|----------------------------------------------|
| wilderness-pistons          | Deny    | Pistons outside region push/pull blocks in   |
| wilderness-dispensers       | Deny    | Dispensers outside region dispense into region |
| wilderness-minecarts        | Deny    | Minecarts from outside enter region          |
| copper-golems-interaction\* | Deny    | Copper golems from outside enter region      |

\*: Removed since version **5.1.0.0**

### Special Features

| Flag             | Default | Description                         |
|------------------|:-------:|-------------------------------------|
| player-glowing   | Allow   | Players have glowing effect         |
| snowman-trails   | Allow   | Snow golems leave snow trails       |
| windcharge-burst | Deny    | Wind charges can burst in region    |
| wars             | Deny    | Region can be targeted for war      |
