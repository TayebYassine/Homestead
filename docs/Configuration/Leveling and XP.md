# Leveling & XP

Region leveling rewards players for killing mobs inside their claimed regions. As a region earns XP, it rises through levels and unlocks bonuses for everyone who owns or builds there: extra chunks, member slots, sub-areas, and reduced upkeep. The system gives communities a long-term goal beyond simply claiming land.

## How It Works

1. A player kills a mob inside their claimed region.
2. The kill grants XP based on the mob's type — a random value between the configured minimum and maximum.
3. XP accumulates for the region; once enough is earned, the region levels up.
4. Each new level unlocks the rewards configured for it, and all rewards from lower levels still apply.

!!! tip "Outside Kills"

    Only kills that happen **inside** your claimed region count, with one exception: the Ender Dragon counts regardless of where it is defeated.

## Level Formula

The XP required to reach the next level follows:

```
XP Required = 5 × level² + 50 × level + 100
```

Early levels arrive quickly, while later levels take substantially longer, so most regions climb past the first few levels during normal play and only dedicated communities approach the cap.

| Level | XP Required | Cumulative XP |
|:-----:|:-----------:|:-------------:|
|   1   |     155     |      155      |
|   2   |     220     |      375      |
|   5   |     475     |     1,625     |
|  10   |    1,100    |     6,200     |
|  20   |    3,100    |    28,700     |
|  50   |   15,100    |    384,200    |

The maximum level is **50**, and it cannot be changed.

## Configuration

All leveling settings live in `levels.yml`.

### Enable or Disable

Toggle the region leveling system on or off:

```yaml
levels:
  enabled: true
```

- **true** (default): Kills award XP and regions level up.
- **false**: No XP is awarded and the leveling system stays dormant.

### Anti-Farm Timeout

XP gain is rate-limited so players cannot farm it by killing many entities in quick succession:

```yaml
levels:
  timeout: 2  # Seconds between XP gains
```

A single player therefore earns XP at most once every 2 seconds by default. Raise the value if players find ways to farm faster than intended.

### XP per Entity

Each entity type has its own XP range; the actual amount awarded is random between the two values:

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

Entity names use the Bukkit `EntityType` enum ([full list](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/entity/EntityType.html)). The shipped config covers bosses, hostile mobs, passive animals, and ambient creatures; extend it with more names if you want to reward additional mobs. Adjust the ranges to shape your progression. Generous passive-animal XP makes leveling accessible to everyone, while concentrating XP in bosses and hostile mobs favors combat-focused servers.

### Level Rewards

Rewards are cumulative: reaching level 10 grants everything configured for level 10 **plus** every reward from the levels below it.

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
    20:
      chunks: 4
      members: 2
      subareas: 1
      upkeep-reduction: 15
    30:
      chunks: 4
      members: 2
      subareas: 1
      upkeep-reduction: 30
    50:
      chunks: 8
      members: 6
      subareas: 4
      upkeep-reduction: 50
```

!!! warning "Ascending Order"

    Keep levels in ascending order in the config file. Rewards from all unlocked levels **stack**, and the maximum level is 50.

### Reward Types

- **`chunks`** — Bonus claimable chunks added to the region's limit.
- **`members`** — Bonus trusted-member slots.
- **`subareas`** — Bonus sub-area slots.
- **`upkeep-reduction`** — Percentage reduction applied to the region's [upkeep](../Economy/Upkeep.md) cost.

