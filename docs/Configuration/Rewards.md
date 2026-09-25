# Rewards

The rewards system hands players bonus chunks and sub-areas in exchange for community engagement and playtime. It runs on top of the base limits from [Ranks & Limits](Ranks%20and%20Limits.md), so an active region can grow well beyond what any rank allows on its own. Bonuses from [leveling](Leveling%20and%20XP.md) stack with these as well.

Rewards are configured in `regions.yml`. First, make sure the system is switched on:

```yaml
rewards:
  enabled: true
```

- **true** (default): Trusted members and playtime award bonus chunks and sub-areas.
- **false**: Rewards are off; players keep only their base limits.

Here is an example calculation for a default-rank player:

- Base limit (default rank): **4** chunks
- 3 trusted members × 2 chunks each: **+6** chunks
- 12 hours of playtime (12-hour tier): **+4** chunks
- **Total: 14 chunks**

## Member Rewards

Region owners get bonus chunks for each trusted member they add, which encourages inviting friends rather than hoarding land:

```yaml
rewards:
  for-each-member:
    chunks: 2     # Bonus chunks per member
    subareas: 1   # Bonus sub-areas per member
```

With the default values:

- **3 members**: +6 chunks, +3 sub-areas
- **5 members**: +10 chunks, +5 sub-areas
- **10 members**: +20 chunks, +10 sub-areas

Member bonuses grow linearly with no cap, so raise or lower `chunks` and `subareas` if the defaults scale too quickly for your server's economy.

## Playtime Rewards

The longer a player is active, the more bonus chunks they earn. The highest qualifying tier is used; the playtime tiers themselves do not stack with each other, though they do combine with member rewards.

```yaml
rewards:
  by-playtime:
    - minutes: 30
      hours: 0
      days: 0
      chunks: 1
      subareas: 1
    - minutes: 0
      hours: 3
      days: 0
      chunks: 2
      subareas: 2
    - minutes: 0
      hours: 12
      days: 0
      chunks: 4
      subareas: 3
    - minutes: 0
      hours: 0
      days: 1
      chunks: 6
      subareas: 4
    - minutes: 0
      hours: 0
      days: 3
      chunks: 8
      subareas: 6
    - minutes: 0
      hours: 0
      days: 7
      chunks: 10
      subareas: 8
```

The `minutes`, `hours`, and `days` fields are additive within a single tier, so a tier can mix units: `days: 1` together with `hours: 12` for a threshold of one and a half days.

### How Tiers Work

If a player has **7 hours** of playtime:

- :material-check: Qualifies for the 30-minute tier
- :material-check: Qualifies for the 3-hour tier
- :material-close: Does not yet qualify for the 12-hour tier
- **Gets**: 2 chunks, from the 3-hour tier (the highest qualifying tier)

!!! tip "Tier Design"

    Make sure each tier gives **more** than the previous one. Rewards only increase as playtime grows, so a smaller later tier would never be reached.

