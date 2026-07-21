# Flags Overview

Flags are permissions that control what players and the environment can do inside your region. Homestead includes **38 player flags**, **23 world flags**, and **18 control flags**.

!!! tip "Flags Calculator"

    Use the [Homestead Flags Calculator](https://tayebyassine.github.io/HomesteadFlagsCalculator) to compute bitwise flag values.

## Flag Categories

| Category | Scope                                | Configured By |
|:---------|:-------------------------------------|:--------------|
| **Player Flags** | What members and non-members can do  | Anyone with flag permissions |
| **World Flags** | Environmental effects, mobs, weather | Anyone with flag permissions |
| **Control Flags** | What trusted members can manage      | Region owners only (via GUI) |

## How Flags Work

Each flag has two states:

| State | Effect |
|:----:|:-------|
| **Allow** (`true`) | The action is permitted |
| **Deny** (`false`) | The action is blocked |

## Managing Flags

### For Non-Members (Global)

```
/hs flags global [flag] [allow/deny]
```

Example: `/hs flags global pvp deny`

### For Specific Members

```
/hs flags member [player] [flag] [allow/deny]
```

Example: `/hs flags member Steve break-blocks allow`

### For Environment (World)

```
/hs flags world [flag] [allow/deny]
```

Example: `/hs flags world fire-spread deny`

### Via GUI

```
/region flags
```

Opens a visual menu where you can browse and toggle all flags.

### Control Flags

Control flags for trusted members are managed through the GUI:

1. Open the Region Menu
2. Click **Players Management**
3. Click **Trusted Players**
4. Right-click a player head

## Default Values

Default flag values for **new** regions are set in `flags.yml`:

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

!!! info "Control Flags"

    Control flags are always in Deny state by default and cannot be changed globally.

## Flag Reference

- [Full Player Flags List](Player Flags.md)
- [Full World Flags List](World Flags.md)
- [Full Control Flags List](Control Flags.md)
- [Disabling Flags](Disabled Flags.md)
