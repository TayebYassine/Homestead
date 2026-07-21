# Managing a Region

Once you've created a region, you can customize it extensively. This guide covers the full range of management features.

## Targeting Regions

Most commands work on your **targeted** region — your active region.

```
/hs set [region]
```

| Who | Can Target |
|:----|:-----------|
| Owner | Any region they own |
| Trusted Member | Regions where they're a member |
| Operator | **Any** region on the server |

## Basic Settings

### Rename

```
/region rename [new-name]
```

### Display Name

Set a display name (supports colors) separately from the region name:

```
/region setdisplayname [display-name]
```

Example: `/region setdisplayname &6&lThe &2&lBase`

### Description

```
/region setdescription [description]
```

Example: `/region setdescription Our cozy survival base"

### Spawn Location

Set where players teleport when visiting:

```
/region setspawn
```

Stand where you want the spawn point and run the command.

### Map Appearance

Customize region display on web maps:

```
/region setmapcolor [color]
/region setmapicon [icon-name]
```

### Weather & Time

Control the environment within your region:

```
/region settime [time]
/region setweather [weather]
```

Requires the `set-weather-and-time` control flag.

## Managing Members

### Trust a Player

```
/region trust [player]
```

Trusted players can build and interact in your region. Their specific permissions depend on their [member flags](../Configuration/Player Flags.md).

!!! info "Acceptance System"

    If enabled (default), the invited player must run `/region accept [region]` to join.

### Deny an Invite

```
/region deny [region]
```

### Untrust a Player

```
/region untrust [player]
```

### Ban a Player

```
/region ban [player]
```

Banned players cannot enter the region, even if passthrough is allowed.

### Unban

```
/region unban [player]
```

### Kick

```
/region kick [player]
```

Temporarily removes the player from the region. They can re-enter immediately.

### View Members & Bans

```
/region members      # List trusted members
/region banlist      # List banned players
```

### Leave a Region

```
/hs leave confirm
```

Removes yourself as a trusted member from the targeted region.

## Managing Flags

See the [Flags Overview](../Configuration/Flags Overview.md) for a full explanation.

```
/region flags          # Open flags GUI
/region flags global [flag] [allow/deny]
/region flags member [player] [flag] [allow/deny]
/region flags world [flag] [allow/deny]
```

## Region Menu

Open the main region management GUI:

```
/region menu
```

This GUI provides access to:
- Region information
- Member management
- Flag configuration
- Bank operations
- Sub-area management
- Region settings

## Economy

```
/region deposit [amount]          # Add money to region bank
/region withdraw [amount]         # Take money from region bank
/region balance                   # Check bank balance
/region setmembertax [amount]     # Set member taxes (0 to disable)
```

## Deleting a Region

```
/region delete confirm
```

!!! danger "Permanent"

    This cannot be undone! All chunks are unclaimed, settings lost, members removed. **If FAWE chunk regeneration is enabled, builds are destroyed.**

