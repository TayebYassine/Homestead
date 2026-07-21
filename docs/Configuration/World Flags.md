# World Flags

World flags control the environment, mob spawning, and natural events within regions.

| Flag | Bit | Default | Description |
|:-----|:---:|:-------:|:------------|
| `passive-entity-spawn` | 1 | **Allow** | Passive mobs (animals) spawn naturally |
| `hostile-entity-spawn` | 2 | **Allow** | Hostile mobs (monsters) spawn naturally |
| `entity-grief` | 4 | Deny | Entities (except creepers) damage blocks |
| `entity-damage` | 8 | Deny | Entities attack each other |
| `leaves-decay` | 16 | **Allow** | Leaves decay naturally |
| `fire-spread` | 32 | Deny | Fire spreads to other blocks |
| `liquid-flow` | 64 | Deny | Water/lava flows into the region from outside |
| `explosion-damage` | 128 | Deny | Explosions damage blocks |
| `wither-damage` | 256 | Deny | Wither boss destroys blocks |
| `wilderness-pistons` | 512 | Deny | Pistons outside push blocks into region |
| `wilderness-dispensers` | 1024 | Deny | Dispensers outside dispense into region |
| `wilderness-minecarts` | 2048 | Deny | Minecarts from outside enter region |
| `plant-growth` | 4096 | **Allow** | Crops and saplings grow |
| `grass-growth` | 8192 | **Allow** | Grass spreads |
| `sculk-spread` | 16384 | **Allow** | Sculk spreads from shriekers |
| `player-glowing` | 32768 | **Allow** | Players have glowing effect |
| `snow-melting` | 65536 | **Allow** | Snow melts |
| `ice-melting` | 131072 | **Allow** | Ice melts |
| `snowman-trails` | 262144 | **Allow** | Snow golems leave snow trails |
| `windcharge-burst` | 524288 | Deny | Wind charges burst on impact |
| `copper-golems-interaction` | 1048576 | — | :material-close: **Deprecated** — do not use |
| `wars` | 2097152 | Deny | Region can be targeted for war |
| `projectiles` | 4194304 | Deny | Projectiles (from non-player sources) |
| `weather-snow` | 8388608 | **Allow** | Snow forms during weather in cold biomes |

## Spawner Exclusion

In `flags.yml`, you can configure whether mobs from spawners are ignored by spawn flags:

```yaml
flags-configuration:
  exclude-spawners: true
```

When `true`, spawner-spawned mobs are **not** blocked by the passive/hostile spawn flags.
