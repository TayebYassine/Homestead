# Flags Overview

Flags are the fine-grained permissions that control what players and the environment can do inside a region. Homestead includes **38 player flags**, **23 world flags**, and **18 control flags**. Together they cover everything from whether strangers may break blocks, to whether fire spreads, to which management actions trusted members may perform.

!!! tip "Flags Calculator"

    Use the [Homestead Flags Calculator](https://tayebyassine.github.io/HomesteadFlagsCalculator) to compute bitwise flag values.

## Flag Categories

Flags fall into three categories:

- **Player flags** — What members and non-members can do inside the region, such as breaking blocks, opening containers, or fighting.
- **World flags** — Environmental behavior within the region: mob spawning, weather, fire, explosions, and similar effects.
- **Control flags** — What trusted members may manage, such as inviting players, changing flags, or claiming new chunks.

Each category has its own page with a full list of flag IDs, bit values, and defaults: [Player Flags](Player%20Flags.md), [World Flags](World%20Flags.md), and [Control Flags](Control%20Flags.md).

## How Flags Work

Every flag has exactly one of two states:

- **Allow** (`true`) — The action is permitted.
- **Deny** (`false`) — The action is blocked.

Internally, flags are stored as a single 64-bit integer. Each flag occupies its own bit position (so each flag's numeric value is a distinct power of two), and combining flags means OR-ing their values together. That is why combined values are easier to produce with the flags calculator than by hand.

## Managing Flags

Flags can be changed per region with commands or through the GUI:

- **Global (non-members)**: `/hs flags global [flag] [allow/deny]`
- **Per member**: `/hs flags member [player] [flag] [allow/deny]`
- **World**: `/hs flags world [flag] [allow/deny]`
- **GUI**: `/hs flags global`, `/hs flags world`, or `/hs members`

### Control Flags

Control flags for trusted members are managed through the Region Menu rather than commands:

1. Open the Region Menu.
2. Click **Players Management**.
3. Click **Trusted Players**.
4. Right-click a player head to toggle that member's control flags.

## Default Flag States

Default values for **new** regions are set in `flags.yml`. Changing a default affects regions created after the change; use the override command below to apply a value to every existing region at once:

```yaml
default-players-flags:
  break-blocks: false
  containers: false
  passthrough: true
  # ...

default-world-flags:
  passive-entity-spawn: true
  hostile-entity-spawn: true
  entity-grief: false
  # ...
```

!!! info "About Control Flags"

    Control flags are in **Deny** state by default and cannot be set globally; they are configured per trusted member instead.

## Override Flag State

To force a flag's value across all existing regions at once, use the override command:

```
/hsadmin overrideflag [global/world/member] [flag] (allow/deny)
/hsadmin overrideflag member [player] [flag] (allow/deny)
```

This is especially useful after editing defaults in `flags.yml` or after adding a flag to `disabled-flags`, since neither change rewrites regions that already exist.

## Flag Reference

- [Full Player Flags List](Player%20Flags.md)
- [Full World Flags List](World%20Flags.md)
- [Full Control Flags List](Control%20Flags.md)
- [Disabling Flags](Disabled%20Flags.md)

