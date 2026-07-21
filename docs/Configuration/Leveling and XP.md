# Leveling & XP

Region leveling rewards players for killing mobs inside their claimed regions. As a region gains XP, it levels up and unlocks bonuses.

## How It Works

1. Players kill mobs inside their claimed region
2. Each kill grants XP based on mob type
3. XP accumulates — the region levels up
4. Each level unlocks rewards (chunks, members, upkeep reduction)

!!! info "Outside Kills"

    Only kills that happen **inside** your claimed region count — except the Ender Dragon, which counts regardless of location.

## Level Formula

```
XP Required = 5 × level² + 50 × level + 100
```

| Level | XP Required | Cumulative XP |
|:-----:|:-----------:|:-------------:|
| 1 | 155 | 155 |
| 2 | 220 | 375 |
| 5 | 475 | 1,625 |
| 10 | 1,100 | 6,200 |
| 20 | 3,100 | 28,700 |
| 50 | 15,100 | 384,200 |

## Configuration (`levels.yml`)

### Enable/Disable

```yaml
levels:
  enabled: true
```

### XP Timeout

Prevents XP farming by limiting how often XP can be gained:

```yaml
levels:
  timeout: 2  # Seconds between XP gains
```

### XP per Mob Type

Configure XP range (min, max) per entity:

```yaml
levels:
  on-kill-entity:
    ENDER_DRAGON: [ 5000, 10000 ]  # Boss tier
    WARDEN: [ 5000, 7500 ]
    WITHER: [ 2500, 5000 ]
    ELDER_GUARDIAN: [ 150, 300 ]   # Mini-boss tier
    CREEPER: [ 15, 30 ]            # Standard hostile
    ZOMBIE: [ 10, 20 ]
    COW: [ 10, 20 ]                # Passive
    BAT: [ 1, 3 ]                  # Ambient
```

### Level-Up Rewards

Rewards are cumulative — you get **all** rewards from previous levels too.

```yaml
levels:
  rewards:
    5:   # Level 5
      chunks: 1
      members: 0
      subareas: 0
      upkeep-reduction: 5   # Percent
    10:
      chunks: 2
      members: 1
      subareas: 0
      upkeep-reduction: 5
    15:
      chunks: 4
      members: 2
      subareas: 1
      upkeep-reduction: 10
    50:
      chunks: 8
      members: 6
      subareas: 4
      upkeep-reduction: 50
```

!!! warning "Ascending Order"

    Keep levels in ascending order in the config file. Rewards from all unlocked levels **stack**.

### Reward Types

| Reward | Effect |
|:-------|:-------|
| `chunks` | Bonus claimable chunks for the region |
| `members` | Bonus trusted member slots |
| `subareas` | Bonus sub-area slots |
| `upkeep-reduction` | Percentage reduction on upkeep costs |
