# Ranks and Limits

Ranks and limits control how many regions, chunks, and members each player can have. Homestead offers flexible limit systems based on permission groups or static values.

## Limit System Methods

Choose between two methods for managing player limits:

- **groups:** Uses your permissions plugin (like LuckPerms) to assign different limits based on player groups.
- **static:** All players get the same limits, regardless of rank.
- **permissions:** Same as groups but using permissions instead. Use `homestead.group.(GROUP NAME)`.

## Configuring the Method

Set your preferred method in `limits.yml`:

```yaml
limits:
  method: 'static'
```

## Understanding Limits

Each limit type controls a different aspect of claiming:

| Limit                     | What It Controls                        |
|---------------------------|-----------------------------------------|
| `regions`                 | Maximum regions a player can create     |
| `chunks-per-region`       | Maximum chunks claimable per region     |
| `members-per-region`      | Maximum trusted members per region      |
| `subareas-per-region`     | Maximum sub-areas per region            |
| `max-subarea-volume`      | Maximum size of a sub-area (in blocks³) |
| `max-bank-deposit`        | Maximum bank deposit per region         |
| `max-force-loaded-chunks` | Maximum force loaded chunks per region  |
| `commands-cooldown`       | Cooldown between commands (in seconds)  |

## Groups Configuration

In `limits.yml`, under `limits.groups`, define limits for each permission group:

```yaml
limits:
  method: 'groups'
  
  groups:
    default: # Basic players
      regions: 1
      chunks-per-region: 4
      members-per-region: 2
      subareas-per-region: 1
      max-subarea-volume: 400
      max-bank-deposit: 100000000
      max-force-loaded-chunks: 2
      commands-cooldown: 2
      
    vip:  # VIP rank
      ...
      
    ...
```

!!! warning "Undefined Groups"

    Any group **not** listed in the config will have **all limits set to 0**. This means players in that group cannot claim chunks or create regions. Always define all your server's groups!

## Static Configuration

In `limits.yml`, under `limits.static`, define limits for operators and non-operators:

```yaml
limits:
  method: 'static'
  
  static:
    non-op:  # Regular players
      ...
      
    op:  # Server operators
      ...
```

## Permissions Configuration

Same logic as [groups configuration](#groups-configuration), but the players must have the permission `homestead.group.(GROUP NAME)` instead of
using LuckPerms groups system.

Examples: `homestead.group.default`, `homestead.group.vip`...

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
      ...
      
    ...
```

## Per-Player Limits

Override limits for specific players without creating new groups!

### Configuration

In `limits.yml`, under `player-limits`, define custom limits for individual players:

```yaml
player-limits:
  Steve:  # Player's exact username
    ...
    
  Alex:  # Another player
    ...
```
