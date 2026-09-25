# World Flags

World flags describe how the environment behaves inside a region: whether mobs spawn, whether fire spreads, whether water flows in from the wilderness, and so on. Unlike player flags, they are not aimed at a particular player. They shape the region itself, and every visitor is affected by them.

The defaults shown here match `default-world-flags` in `flags.yml`. Natural behavior (leaves decaying, crops growing, snow melting) is allowed by default, while destructive behavior (fire spread, explosions, wither damage) is denied. A few flags govern interactions originating **outside** the region, such as `wilderness-pistons` and `wilderness-minecarts`, which keep neighboring builds from interfering with your land.

The **Bit** column is the flag's individual bitwise value; use the [Homestead Flags Calculator](https://tayebyassine.github.io/HomesteadFlagsCalculator) when you need a combined value.

| Flag                        |   Bit   |  Default  | Description                                   |
|:----------------------------|:-------:|:---------:|:----------------------------------------------|
| `passive-entity-spawn`      |    1    | **Allow** | Passive mobs (animals) spawn naturally        |
| `hostile-entity-spawn`      |    2    | **Allow** | Hostile mobs (monsters) spawn naturally       |
| `entity-grief`              |    4    |   Deny    | Entities (except creepers) damage blocks      |
| `entity-damage`             |    8    |   Deny    | Entities attack each other                    |
| `leaves-decay`              |   16    | **Allow** | Leaves decay naturally                        |
| `fire-spread`               |   32    |   Deny    | Fire spreads to other blocks                  |
| `liquid-flow`               |   64    |   Deny    | Water/lava flows into the region from outside |
| `explosion-damage`          |   128   |   Deny    | Explosions damage blocks                      |
| `wither-damage`             |   256   |   Deny    | Wither boss destroys blocks                   |
| `wilderness-pistons`        |   512   |   Deny    | Pistons outside push blocks into region       |
| `wilderness-dispensers`     |  1024   |   Deny    | Dispensers outside dispense into region       |
| `wilderness-minecarts`      |  2048   |   Deny    | Minecarts from outside enter region           |
| `plant-growth`              |  4096   | **Allow** | Crops and saplings grow                       |
| `grass-growth`              |  8192   | **Allow** | Grass spreads                                 |
| `sculk-spread`              |  16384  | **Allow** | Sculk spreads from shriekers                  |
| `player-glowing`            |  32768  |   Deny    | Players have glowing effect                   |
| `snow-melting`              |  65536  | **Allow** | Snow melts                                    |
| `ice-melting`               | 131072  | **Allow** | Ice melts                                     |
| `snowman-trails`            | 262144  | **Allow** | Snow golems leave snow trails                 |
| `windcharge-burst`          | 524288  |   Deny    | Wind charges burst on impact                  |
| `copper-golems-interaction` | 1048576 |     —     | :material-close: **Deprecated**               |
| `wars`                      | 2097152 |   Deny    | Region can be targeted for war                |
| `projectiles`               | 4194304 |   Deny    | Projectiles (from non-player sources)         |
| `weather-snow`              | 8388608 | **Allow** | Snow forms during weather in cold biomes      |

## Spawner Exclusion

By default, mob spawners are exempt from the spawn flags, so spawner-based farms keep working even when `passive-entity-spawn` or `hostile-entity-spawn` is denied. This behavior is controlled in `flags.yml`:

```yaml
flags-configuration:
  exclude-spawners: true
```

- **true** (default): Mobs from spawners are **not** blocked by the passive/hostile spawn flags.
- **false**: Spawner mobs obey the spawn flags like any other mob.

## Changing World Flags

Set a flag with `/hs flags world [flag] [allow/deny]`, or edit `default-world-flags` in `flags.yml` to change the starting state for newly created regions. See [Flags Overview](Flags%20Overview.md) for details.

