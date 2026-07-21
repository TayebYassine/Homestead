# Leveling & XP

Regions gain experience when members kill mobs inside claimed territory. Leveling up unlocks bonus chunks, member slots, and upkeep reductions.

## Checking Your Region's Level

```
/region levels
```

Shows:

- Current level
- Current XP
- XP needed for next level
- Unlocked rewards

## How XP Works

1. Kill mobs **inside your claimed region**
2. Different mobs give different XP amounts
3. XP accumulates toward the next level
4. Level up and claim your rewards

### XP Per Mob

| Mob | XP Range | Tier |
|:----|:--------:|:----:|
| Ender Dragon | 5,000–10,000 | Boss |
| Warden | 5,000–7,500 | Boss |
| Wither | 2,500–5,000 | Boss |
| Elder Guardian | 150–300 | Mini-boss |
| Ravager | 100–200 | Elite |
| Breeze | 40–80 | Elite |
| Creeper | 15–30 | Standard |
| Zombie | 10–20 | Standard |
| Cow | 10–20 | Passive |
| Bat | 1–3 | Ambient |

!!! tip "XP Timeout"

    There's a 2-second cooldown between XP gains to prevent farming.

## Level Rewards

Level-up rewards are **cumulative** — you keep all rewards from previous levels.

| Level | Chunks | Members | Sub-Areas | Upkeep Reduction |
|:-----:|:------:|:-------:|:---------:|:----------------:|
| 5 | +1 | — | — | 5% |
| 10 | +2 | +1 | — | 5% |
| 15 | +4 | +2 | +1 | 10% |
| 20 | +4 | +2 | +1 | 15% |
| 30 | +4 | +2 | +1 | 30% |
| 50 | +8 | +6 | +4 | 50% |

## Formula

```
XP for next level = 5 × level² + 50 × level + 100
```

## Configuration

See [Leveling & XP Configuration](../Configuration/Leveling and XP.md) for full configuration details.
