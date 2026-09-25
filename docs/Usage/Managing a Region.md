# Managing a Region

Once you've created a region, you can customize it extensively. This guide covers the full range of management features.

---

## Targeting Regions

Most commands work on your **targeted** region, your active region.

```
/hs set [region]
```

Who can target what:

- **Owners** can target any region they own.
- **Trusted members** can target regions where they are a member.
- **Operators** can target **any** region on the server.

If you own only one region, it is selected automatically. See [Other Settings](../Configuration/Other%20Settings.md#auto-select-target-region) to control automatic selection for new players.

---

## Basic Settings

### Rename

```
/region rename [new-name]
```

Renaming is blocked if the name is already in use. A rename cooldown applies (10 minutes by default).

### Display Name

Set a display name (supports color codes) separately from the region name:

```
/region setdisplayname [display-name]
```

**Example:** `/region setdisplayname &6&lThe &2&lBase`

### Description

```
/region setdescription [description]
```

**Example:** `/region setdescription Our cozy survival base`

### Spawn Location

Set where players teleport when visiting:

```
/region setspawn
```

Stand where you want the spawn point and run the command. The spawn must be inside a chunk claimed by the region. A spawn-change cooldown applies (10 minutes by default).

### Map Appearance

Customize how the region appears on supported web maps:

```
/region setmapcolor [color]
/region setmapicon [icon-name]
```

Run either command without an argument to open the color or icon menu instead. Map changes share a 12-hour cooldown. See [Borders & Maps](Borders%20and%20Maps.md#web-map-integration).

### Weather & Time

Control the environment within your region:

```
/region settime [time]
/region setweather [weather]
```

Both commands require the `set-weather-and-time` control flag (see [Control Flags](../Configuration/Control%20Flags.md)). Time and weather settings are client-side and logged to the region's activity log.

---

## Managing Members

### Trust a Player

```
/region trust [player]
```

Trusted players can build and interact in your region. Their specific permissions depend on their [member flags](../Configuration/Player%20Flags.md).

!!! info "Acceptance System"

    By default, the invited player must run `/region accept [region]` to join. Set `special-feat.ignore-trust-acceptance-system: true` in `regions.yml` to trust players immediately. See [Other Settings](../Configuration/Other%20Settings.md#trust-acceptance-system).

### Deny an Invite

```
/region deny [region]
```

Rejects a pending invitation without joining the region.

### Untrust a Player

```
/region untrust [player]
```

Removes the player (or revokes a pending invitation). The player loses member permissions immediately.

### Ban a Player

```
/region ban [player] (reason)
```

Banned players cannot enter the region, even if passthrough is allowed. An optional reason can be attached.

### Unban

```
/region unban [player]
```

Lifts a region ban so the player can enter again.

### Kick

```
/region kick [player]
```

Removes the player from the region while they are inside the borders. They are not banned and can be trusted again later.

### View Members & Bans

```
/region members
/region banlist
```

Both commands open a GUI for the targeted region. Add `gui` (`/region banlist gui`) to open the ban list GUI directly from chat.

### Leave a Region

```
/hs leave confirm
```

Removes yourself as a trusted member from the targeted region. You must confirm with `confirm`; without it, Homestead asks you to confirm first.

---

## Managing Flags

See the [Flags Overview](../Configuration/Flags%20Overview.md) for a full explanation of flag types and how they resolve.

```
/region flags
/region flags global [flag] (allow/deny)
/region flags member [player] [flag] (allow/deny)
/region flags world [flag] (allow/deny)
```

- `/region flags` with no further arguments opens the flags GUI.
- `global` sets a player flag for every non-member in the region.
- `member` sets a flag for one trusted member.
- `world` sets an environment flag for the region.

Leave off `allow`/`deny` to toggle the flag's current state.

---

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

If no region is targeted, the menu opens a list of all regions instead.

---

## Economy

```
/region balance (region)
/region deposit [amount/all]
/region withdraw [amount/all]
/region setmembertax [amount]
```

- `balance` shows the bank balance of the targeted region (or a named region).
- `deposit` moves money from your wallet into the region bank; use `all` to deposit everything.
- `withdraw` moves money from the region bank to your wallet; use `all` to withdraw everything.
- `setmembertax` sets what each trusted member owes per tax period; set to `0` to disable.

Deposits and withdrawals require an economy plugin. See [Economy Setup](../Economy/Setup.md). Bank behavior is covered in [Regional Bank](../Economy/Bank.md) and taxes in [Member Taxes](../Economy/Taxes.md).

---

## Deleting a Region

```
/region delete confirm
```

!!! danger "Permanent"

    This cannot be undone! All chunks are unclaimed, settings lost, members removed. **If FAWE chunk regeneration is enabled, builds are destroyed.**

