# Ranks & Limits

Homestead controls how many regions a player can create, how large each region can grow, and how many members and sub-areas it can hold. Limits are assigned by one of three methods, and individual players can be overridden on top of whichever method you choose. Getting these numbers right matters: too generous and a few players monopolize the map; too strict and newcomers feel boxed in from the start.

## Limit Methods

The right method depends on whether your server already runs a permissions plugin:

- **`static`** — The simplest option. Every non-operator player shares one set of limits, and operators get a second, more generous set. No permissions plugin required.
- **`groups`** — Limits follow permission groups from [LuckPerms](https://luckperms.net/) or a similar plugin. Best for servers that already tier perks by rank.
- **`permissions`** — The same idea as groups, but keyed to permission nodes (`homestead.group.NAME`) instead of group names. Useful when you want rank-based limits without restructuring groups.

Change the method in `limits.yml`:

```yaml
limits:
  method: 'static'
```

## Configurable Limits

The same keys appear in every limit block: static, groups, permissions, and per-player:

- **`regions`** — Maximum regions a player can create.
- **`chunks-per-region`** — Maximum chunks a single region may claim.
- **`members-per-region`** — Maximum trusted members per region.
- **`subareas-per-region`** — Maximum sub-areas per region.
- **`max-subarea-volume`** — Maximum block volume of a single sub-area.
- **`max-bank-deposit`** — Maximum amount that can be deposited into a region bank.
- **`max-force-loaded-chunks`** — Maximum force-loaded (always loaded) chunks per region.
- **`commands-cooldown`** — Global cooldown between Homestead commands, in seconds (`0` disables it).

## Static Limits

With `method: 'static'`, the config holds exactly two profiles: one for operators and one for everyone else:

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

Raising `chunks-per-region` lets early players expand faster; lowering `regions` keeps the map from filling up. Because there are only two tiers, static mode is easy to reason about but leaves no room for donator ranks.

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
      chunks-per-region: 6
      members-per-region: 8
      # ...

    admin:
      regions: 10
      chunks-per-region: 100
      # ...
```

!!! danger "Undefined Groups"

    Any group **not listed** in the config gets **all limits set to 0**, meaning those players cannot claim anything.

    Every permission group on your server must be defined here. Homestead's YAML validator re-adds missing groups on restart or reload, so do not delete groups you want to ignore. Leave them in place and set their limits to `0` instead.

## Permission-Based Limits

The same limit format as groups, but players qualify through permission nodes:

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

Give each rank in your permissions plugin its matching node. For example, add `homestead.group.vip` to the `vip` group in LuckPerms. The `permissions-priority` list decides which entry wins when a player holds more than one such node: entries higher in the list take priority.

## Per-Player Overrides

To give specific players different limits without creating a new group (content creators, event winners, or a builder who needs extra room), add them under `player-limits`:

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

Usernames are **case-sensitive** and must match exactly. Per-player limits take priority over both group and static settings.

## Rewards

Bonus chunks from [rewards](Rewards.md) and [leveling](Leveling%20and%20XP.md) are added on top of these base limits, so an active region's effective ceiling grows well beyond its rank's starting numbers.

