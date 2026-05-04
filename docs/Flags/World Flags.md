# World Flags

World flags control the environment, mob spawning, and natural events within regions.

## Table of Flags

| Flag Name                     | Bitwise Value | Default State | Description                                               |
|-------------------------------|---------------|---------------|-----------------------------------------------------------|
| `passive-entity-spawn`        | `1`           | true          | Passive mobs (animals) spawn                              |
| `hostile-entity-spawn`        | `2`           | true          | Hostile mobs (monsters) spawn                             |
| `entity-grief`                | `4`           | false         | Entities damage blocks (except creepers)                  |
| `entity-damage`               | `8`           | false         | Entities attack each other                                |
| `leaves-decay`                | `16`          | true          | Leaves decay naturally                                    |
| `fire-spread`                 | `32`          | false         | Fire spreads to other blocks                              |
| `liquid-flow`                 | `64`          | false         | Water/lava flows into region from outside                 |
| `explosion-damage`            | `128`         | false         | Explosions damage blocks (except wither)                  |
| `wither-damage`               | `256`         | false         | Wither damage                                             |
| `wilderness-pistons`          | `512`         | false         | Pistons outside push blocks into region                   |
| `wilderness-dispensers`       | `1024`        | false         | Dispensers outside dispense into region                   |
| `wilderness-minecarts`        | `2048`        | false         | Minecarts from outside enter region                       |
| `plant-growth`                | `4096`        | true          | Crops, saplings grow                                      |
| `grass-growth`                | `8192`        | true          | Grass spreads                                             |
| `sculk-spread`                | `16384`       | true          | Sculk spreads from shriekers                              |
| `player-glowing`              | `32768`       | true          | Players have glowing effect                               |
| `snow-melting`                | `65536`       | true          | Snow melts                                                |
| `ice-melting`                 | `131072`      | true          | Ice melts                                                 |
| `snowman-trails`              | `262144`      | true          | Snow golems leave snow trails                             |
| `windcharge-burst`            | `524288`      | false         | Wind charges burst                                        |
| `copper-golems-interaction`\* | `1048576`     | ?             | Copper golems to open copper chests within region borders |
| `wars`                        | `2097152`     | false         | Region can be targeted for war                            |
| `projectiles`                 | `4194304`     | false         | Any kind of projectile from any source except players     |
| `weather-snow`                | `8388608`     | true          | Snow form during weather storm (in cold biomes)           |

\*: Deprecated, do not use.
