# Sub-Areas

Sub-areas are specific zones inside a region with their own permissions and flags. Think of them as "regions within your region."

---

## Use Cases

- **Shop** area where visitors can trade
- **PvP arena** where PvP is allowed
- **Public farm** where anyone can harvest
- **Private room** only accessible to certain members

---

## Creating a Sub-Area

### Step 1: Get the Selection Tool

The selection tool is a **Golden Hoe** by default (configurable in `regions.yml` under `selection-tool`). If you don't have one, run:

```
/hs subareas tool
```

A selection-tool cooldown applies (7 days by default, though operators can adjust this).

### Step 2: Select Two Corners

1. **Left-click** a block for the first corner.
2. **Right-click** a block for the second corner.

This defines the cuboid area. You'll see status messages after each selection.

!!! warning "Selection Rules"

    Both corners must be **inside your region**. The sub-area cannot overlap another sub-area, and its volume must stay within the configured limit.

### Step 3: Create the Sub-Area

```
/hs subareas create [name]
```

**Example:** `/hs subareas create Shop`

Creating, editing, and deleting sub-areas requires the `manage-subareas` control flag (owners always pass; see [Control Flags](../Configuration/Control%20Flags.md)).

Running `/hs subareas` with no arguments opens the sub-areas GUI for the targeted region.

---

## Managing Sub-Areas

All management actions use the `conf` subcommand:

```
/hs subareas conf [name] [action] (args)
```

The available actions are:

- **`delete`** — `/hs subareas conf [name] delete` deletes the sub-area permanently.
- **`rename`** — `/hs subareas conf [name] rename [new-name]` renames it (names must be unique within the region).
- **`resize`** — `/hs subareas conf [name] resize` resizes it from your current selection-tool selection.
- **`flags`** — `/hs subareas conf [name] flags [flag] (allow/deny)` sets a global player flag for the sub-area. Omit `allow`/`deny` to toggle.
- **`players add`** — `/hs subareas conf [name] players add [player]` adds a trusted region member to the sub-area (the player must already be trusted in the parent region).
- **`players remove`** — `/hs subareas conf [name] players remove [player]` removes them from the sub-area.
- **`players flags`** — `/hs subareas conf [name] players flags [player] [flag] (allow/deny)` sets a player-specific flag for that member inside the sub-area.

Sub-area flags and member flags override the parent region's settings while a player is inside the sub-area.

---

## Renting Sub-Areas

Sub-areas can have rental signs too. Place a `[Rent]` sign inside the sub-area after setting a rent price and duration with the rent commands. See [Rent](../Economy/Rent.md).

---

## Limits

How many sub-areas you can create and how large they can be come from `limits.yml`:

```yaml
# In limits.yml
limits:
  groups:
    default:
      subareas-per-region: 1
      max-subarea-volume: 400
```

- `subareas-per-region` — maximum number of sub-areas per region.
- `max-subarea-volume` — maximum volume (in blocks) of a single sub-area.

See [Ranks & Limits](../Configuration/Ranks%20and%20Limits.md) for group and permission-based limits.

!!! info "Global Toggle"

    The whole sub-areas feature can be disabled in `regions.yml` with `sub-areas.enabled: false`. Existing sub-areas are unaffected. See [Other Settings](../Configuration/Other%20Settings.md#sub-areas-toggle).

