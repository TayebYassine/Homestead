# Teleportation

## Region Home

Teleport to your region's spawn point:

```
/region home
```

The spawn must be set first with `/region setspawn`.

## Visiting Regions

### By Region Name

```
/region visit [region-name]
```

**Example:** `/region visit MyBase`

### By Player

```
/region visit [player-name]
```

**Example:** `/region visit Steve`

Visits the first welcome sign of that player's region.

### By Welcome Sign Index

If a player has multiple welcome signs:

```
/region visit [player] [index]
```

## Delayed Teleport

Teleports have a configurable delay to prevent combat-logging:

```yaml
# In regions.yml
delayed-teleport:
  enabled: true
  delay: 3                # Seconds
  price: 0.0              # Cost (0.0 = free)
  cancel-on-move: true    # Cancel if player moves
  ignore-operators: true  # OPs bypass delay
  boss-bar:
    enabled: true
    title: "&7Teleporting in &3{seconds}s&7..."
    color: "PURPLE"
    style: "SEGMENTED_10"
    countdown-mode: "DEPLETE"
```

Players see a boss bar countdown during the delay. Moving cancels the teleport (configurable).

## Fly Mode

If the server allows it, trusted members can fly within the region:

```
/region fly
```

Toggles flight mode within your region's boundaries.

!!! info "Permission Required"

    Requires `homestead.actions.regions.fly` permission.

## Teleport Flags

| Flag | Effect |
|:-----|:-------|
| `teleport-spawn` | Allow non-members to teleport to region spawn |
| `teleport` | Allow ender pearl / chorus fruit teleportation |

## Cooldowns

```yaml
# In regions.yml
cooldown:
  region-teleport:
    ignore-operators: true
    value: 15   # 15 seconds between teleports
```
