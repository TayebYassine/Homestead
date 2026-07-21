# Ranks & Limits

Control how many regions, chunks, members, and more each player can have. Homestead supports three limit assignment methods.

## Limit Methods

| Method | Best For |
|:-------|:---------|
| `static` | Simple servers. All non-OP players share the same limits; OPs get different limits. |
| `groups` | Servers with LuckPerms or similar. Limits are tied to permission groups. |
| `permissions` | Same as groups, but uses permission nodes (`homestead.group.NAME`) instead. |

```yaml
limits:
  method: 'static'
```

## Configurable Limits

| Setting | What It Controls |
|:--------|:-----------------|
| `regions` | Max regions a player can create |
| `chunks-per-region` | Max chunks per region |
| `members-per-region` | Max trusted members per region |
| `subareas-per-region` | Max sub-areas per region |
| `max-subarea-volume` | Max block volume of a sub-area |
| `max-bank-deposit` | Max deposit limit per region bank |
| `max-force-loaded-chunks` | Max force-loaded (always loaded) chunks per region |
| `commands-cooldown` | Global cooldown between commands (seconds, 0 = disabled) |

## Static Limits

```yaml
limits:
  method: 'static'
  static:
    non-op:
      regions: 1
      chunks-per-region: 4
      members-per-region: 2
      subareas-per-region: 1
      max-subarea-volume: 400
      max-bank-deposit: 100000000
      max-force-loaded-chunks: 2
      commands-cooldown: 2

    op:
      regions: 10
      chunks-per-region: 100
      members-per-region: 50
      subareas-per-region: 20
      max-subarea-volume: 5000
      max-bank-deposit: 100000000
      max-force-loaded-chunks: 100
      commands-cooldown: 0
```

## Group-Based Limits

```yaml
limits:
  method: 'groups'
  groups:
    default:
      regions: 1
      chunks-per-region: 4
      members-per-region: 2
      subareas-per-region: 1
      max-subarea-volume: 400
      max-bank-deposit: 100000000
      max-force-loaded-chunks: 2
      commands-cooldown: 2

    vip:
      regions: 2
      chunks-per-region: 8
      members-per-region: 6
      # ...

    admin:
      regions: 10
      chunks-per-region: 100
      # ...
```

!!! danger "Undefined Groups"

    Any group **not listed** in the config gets **all limits set to 0**. Every permission group on your server MUST be defined here.

## Permission-Based Limits

Same as groups, but players need the permission `homestead.group.NAME`:

```yaml
limits:
  method: 'permissions'
  permissions-priority:
    - admin
    - vip
    - mvp
    - default
  permissions:
    default:
      # ... same format as groups
    vip:
      # ...
```

## Per-Player Overrides

Override limits for specific players without creating new groups:

```yaml
player-limits:
  Steve:
    regions: 5
    chunks-per-region: 25
    members-per-region: 15
    subareas-per-region: 10
    max-subarea-volume: 2000
    max-bank-deposit: 100000000
    max-force-loaded-chunks: 100
    commands-cooldown: 0
```

Per-player limits take priority over groups and static settings.

## Rewards

Bonus chunks from [rewards](Rewards.md) and [leveling](Leveling and XP.md) are added on top of these base limits.
