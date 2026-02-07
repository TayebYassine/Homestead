# Ranks and Limits

Ranks and limits control how many regions, chunks, and members each player can have. Homestead offers flexible limit systems based on permission groups or static values.

## Limit System Methods

Choose between two methods for managing player limits:

### Groups Method

Uses your permissions plugin (like LuckPerms) to assign different limits based on player groups.

**Best for:**

- Servers with VIP/donor ranks
- Tiered gameplay progression
- Rewarding players with rank upgrades
- Varied player bases

### Static Method

All players get the same limits, regardless of rank.

**Best for:**

- Small friend servers
- Servers without ranks
- Simple, equal gameplay
- No permission plugin installed

## Configuring the Method

Set your preferred method in `config.yml`:

```yaml
limits:
  # Choose one:
  # - 'groups': Use permission groups (requires LuckPerms, etc.)
  # - 'static': Everyone gets the same limits
  method: 'groups'
```

## Understanding Limits

Each limit type controls a different aspect of claiming:

| Limit                | What It Controls                                    |
|----------------------|-----------------------------------------------------|
| `regions`            | Maximum regions a player can create                 |
| `chunks-per-region`  | Maximum chunks claimable per region                 |
| `members-per-region` | Maximum trusted members per region                  |
| `subareas-per-region`| Maximum sub-areas per region                        |
| `max-subarea-volume` | Maximum size of a sub-area (in blocks³)             |
| `commands-cooldown`  | Cooldown between commands (in seconds)              |

## Groups Method Configuration

### Defining Group Limits

In `config.yml`, under `limits.groups`, define limits for each permission group:

```yaml
limits:
  method: 'groups'
  
  groups:
    default:  # Basic players
      regions: 1
      chunks-per-region: 4
      members-per-region: 2
      subareas-per-region: 1
      max-subarea-volume: 400
      commands-cooldown: 2
      
    vip:  # VIP rank
      regions: 2
      chunks-per-region: 8
      members-per-region: 5
      subareas-per-region: 3
      max-subarea-volume: 800
      commands-cooldown: 1
      
    mvp:  # Premium rank
      regions: 3
      chunks-per-region: 16
      members-per-region: 10
      subareas-per-region: 5
      max-subarea-volume: 1600
      commands-cooldown: 0
      
    admin:  # Staff
      regions: 10
      chunks-per-region: 100
      members-per-region: 50
      subareas-per-region: 20
      max-subarea-volume: 5000
      commands-cooldown: 0
```

### How Groups Are Detected

Homestead uses your permissions plugin to determine a player's group:

1. Player joins the server
2. Homestead checks their primary permission group (via Vault/LuckPerms)
3. Applies the limits defined for that group
4. If the group isn't defined in config, player gets 0 limits (can't claim)

!!! warning "Undefined Groups"

    Any group **not** listed in the config will have **all limits set to 0**. This means players in that group cannot claim chunks or create regions. Always define all your server's groups!

### Adding New Groups

When you add a new rank/group to your server:

1. Add it to `config.yml` under `limits.groups`
2. Set appropriate limits for that group
3. Reload Homestead: `/hsadmin reload`

**Example - Adding a "Helper" group:**

```yaml
limits:
  groups:
    default:
      regions: 1
      chunks-per-region: 4
      # ...
      
    helper:  # New group
      regions: 2
      chunks-per-region: 10
      members-per-region: 5
      subareas-per-region: 2
      max-subarea-volume: 600
      commands-cooldown: 1
```

## Static Method Configuration

### Defining Static Limits

In `config.yml`, under `limits.static`, define limits for operators and non-operators:

```yaml
limits:
  method: 'static'
  
  static:
    non-op:  # Regular players
      regions: 1
      chunks-per-region: 4
      members-per-region: 2
      subareas-per-region: 1
      max-subarea-volume: 400
      commands-cooldown: 2
      
    op:  # Server operators
      regions: 10
      chunks-per-region: 100
      members-per-region: 50
      subareas-per-region: 20
      max-subarea-volume: 5000
      commands-cooldown: 0
```

### How Static Limits Work

- All regular players get `non-op` limits
- All operators get `op` limits
- No permission groups are checked
- Simple and straightforward

## Per-Player Limits

Override limits for specific players without creating new groups!

### Configuration

In `config.yml`, under `player-limits`, define custom limits for individual players:

```yaml
player-limits:
  Steve:  # Player's exact username
    regions: 5
    chunks-per-region: 25
    members-per-region: 15
    subareas-per-region: 10
    max-subarea-volume: 2000
    commands-cooldown: 0
    
  Alex:  # Another player
    regions: 3
    chunks-per-region: 12
    members-per-region: 8
    subareas-per-region: 4
    max-subarea-volume: 1000
    commands-cooldown: 1
```
