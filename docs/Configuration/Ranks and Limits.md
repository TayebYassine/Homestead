# Ranks and Permissions
## By Groups
Each player has a group; by default, it's `default`. They may upgrade to another rank, for example, `vip` group, so they need special upgrades for their regions, like 6+ more claims, 4+ members... etc.

There are two options for limits:

- **groups**: This will make Homestead use LuckPerms (or any other permissions plugin) to fetch a player's group and then get the group's limit.
- **static**: This will make Homestead not use LuckPerms or any other permissions plugin, but all players will have the same limits (not depending on groups). Note that operators will have different limits.

```yaml
limits:
  # Two valid options:
  # - 'static': Static limits; No permission plugins required.
  # - 'groups': Group limits; Permissions and groups plugin required (LuckPerms...)
  method: 'static'
```

To change the limits for each group, edit the following settings:

!!! warning "Undefined Player Groups"

    Any group that was not defined in the Homestead configuration files will have their limits set to 0, which means they cannot claim chunks or create regions.

```yaml
limits:
  groups:
    default: # Group: default
      regions: 1
      chunks-per-region: 4
      members-per-region: 2
      subareas-per-region: 1
      max-subarea-volume: 400
      commands-cooldown: 2
    vip: # Group: vip
      ...
    admin: # Group: admin
      ...

  # If method is static
  static:
    non-op:
      regions: 1
      chunks-per-region: 4
      members-per-region: 2
      subareas-per-region: 1
      max-subarea-volume: 400
      commands-cooldown: 2
    op:
      ...
```

## By Player's name

To reduce the complexity of creating many groups for each specific player, you can put their name in the configuration file to avoid creating multiple groups!

```yaml
player-limits:
  Dead_Master_121: # <-- A player name (he's a cool guy btw)
    regions: 10
    chunks-per-region: 100
    members-per-region: 16
    subareas-per-region: 10
    max-subarea-volume: 1200
    commands-cooldown: 0
    
  TFA_Gaming: # <-- Another player name
    ...
```