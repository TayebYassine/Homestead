# World Flags

World flags control the environment, mob spawning, and natural events within regions.

## Table of Flags

| Flag Name                     | Bitwise Value  |  Default State  | Description                                               |
|-------------------------------|:--------------:|:---------------:|-----------------------------------------------------------|
| `passive-entity-spawn`        |      `1`       |      Allow      | Passive mobs (animals) spawn                              |
| `hostile-entity-spawn`        |      `2`       |      Allow      | Hostile mobs (monsters) spawn                             |
| `entity-grief`                |      `4`       |      Deny       | Entities damage blocks (except creepers)                  |
| `entity-damage`               |      `8`       |      Deny       | Entities attack each other                                |
| `leaves-decay`                |      `16`      |      Allow      | Leaves decay naturally                                    |
| `fire-spread`                 |      `32`      |      Deny       | Fire spreads to other blocks                              |
| `liquid-flow`                 |      `64`      |      Deny       | Water/lava flows into region from outside                 |
| `explosion-damage`            |     `128`      |      Deny       | Explosions damage blocks (except wither)                  |
| `wither-damage`               |     `256`      |      Deny       | Wither damage                                             |
| `wilderness-pistons`          |     `512`      |      Deny       | Pistons outside push blocks into region                   |
| `wilderness-dispensers`       |     `1024`     |      Deny       | Dispensers outside dispense into region                   |
| `wilderness-minecarts`        |     `2048`     |      Deny       | Minecarts from outside enter region                       |
| `plant-growth`                |     `4096`     |      Allow      | Crops, saplings grow                                      |
| `grass-growth`                |     `8192`     |      Allow      | Grass spreads                                             |
| `sculk-spread`                |    `16384`     |      Allow      | Sculk spreads from shriekers                              |
| `player-glowing`              |    `32768`     |      Allow      | Players have glowing effect                               |
| `snow-melting`                |    `65536`     |      Allow      | Snow melts                                                |
| `ice-melting`                 |    `131072`    |      Allow      | Ice melts                                                 |
| `snowman-trails`              |    `262144`    |      Allow      | Snow golems leave snow trails                             |
| `windcharge-burst`            |    `524288`    |      Deny       | Wind charges burst                                        |
| `copper-golems-interaction`\* |   `1048576`    |        ?        | Copper golems to open copper chests within region borders |
| `wars`                        |   `2097152`    |      Deny       | Region can be targeted for war                            |
| `projectiles`                 |   `4194304`    |      Deny       | Any kind of projectile from any source except players     |
| `weather-snow`                |   `8388608`    |      Allow      | Snow form during weather storm (in cold biomes)           |

\*: Deprecated, do not use.
