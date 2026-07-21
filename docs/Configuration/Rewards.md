# Rewards

The rewards system gives players bonus chunks and sub-areas based on community engagement and playtime. Rewards stack on top of base limits from [Ranks & Limits](Ranks and Limits.md).

## Example Calculation

| Source | Chunks |
|:-------|:------:|
| Base limit (default rank) | 4 |
| 3 trusted members (2 each) | +6 |
| 10 hours playtime | +4 |
| **Total** | **14** |

## Member Rewards

Region owners get bonus chunks for each trusted member they add.

```yaml
rewards:
  for-each-member:
    chunks: 2     # Bonus chunks per member
    subareas: 1   # Bonus sub-areas per member
```

**Examples:**

| Members | Chunks Bonus | Sub-Areas Bonus |
|:-------:|:------------:|:---------------:|
| 3 | +6 | +3 |
| 5 | +10 | +5 |
| 10 | +20 | +10 |

!!! warning "Dynamic"

    If you untrust a player, you lose those reward chunks. Rewards go to the region **owner**, not the members.

## Playtime Rewards

The longer a player is active, the more bonus chunks they earn. The highest qualifying tier is used — **tiers do not stack**.

```yaml
rewards:
  by-playtime:
    - minutes: 30
      chunks: 1
      subareas: 1
    - hours: 3
      chunks: 2
      subareas: 2
    - hours: 12
      chunks: 4
      subareas: 3
    - days: 1
      chunks: 6
      subareas: 4
    - days: 3
      chunks: 8
      subareas: 6
    - days: 7
      chunks: 10
      subareas: 8
```

### How Tiers Work

If a player has **7 hours** of playtime:

- :material-check: Qualifies for 30 min tier
- :material-check: Qualifies for 3 hour tier
- :material-close: Does NOT qualify for 12 hour tier yet
- **Gets**: 2 chunks (from the 3 hour tier — highest qualifying)

!!! tip "Tier Design"

    Make sure each tier gives **more** than the previous one. Rewards only increase as playtime grows.

## Best Practices

| Server Size | Recommended Max Chunks |
|:------------|:----------------------:|
| Small | 30–50 |
| Medium | 20–40 |
| Large | 15–30 |

Balance your base limits, member rewards, and playtime tiers to prevent players from claiming unreasonable amounts of the map.
