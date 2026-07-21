# Sub-Areas

Sub-areas are specific zones inside a region with their own permissions and flags. Think of them as "regions within your region."

## Use Cases

- **Shop** area where visitors can trade
- **PvP arena** where PvP is allowed
- **Public farm** where anyone can harvest
- **Private room** only accessible to certain members

## Creating a Sub-Area

### Step 1: Get the Selection Tool

The selection tool is a **Golden Hoe** (configurable in `regions.yml`).

### Step 2: Select Two Corners

1. **Left-click** a block for the first corner
2. **Right-click** a block for the second corner

This defines the cuboid area. You'll see status messages.

!!! warning "Selection Rules"

    Both corners must be **inside your region**. The sub-area cannot intersect another sub-area.

### Step 3: Create the Sub-Area

```
/hs subareas create [name]
```

**Example:** `/hs subareas create Shop`

## Managing Sub-Areas

All management actions use the `conf` subcommand:

```
/hs subareas conf [name] [action] [args]
```

| Action | Command | Description |
|:-------|:--------|:------------|
| Delete | `/hs subareas conf [name] delete` | Delete a sub-area |
| Rename | `/hs subareas conf [name] rename [new-name]` | Rename a sub-area |
| Resize | `/hs subareas conf [name] resize` | Resize using current selection |
| Flags | `/hs subareas conf [name] flags [flag] (allow/deny)` | Set sub-area flags (global) |
| Players | `/hs subareas conf [name] players add [player]` | Add a player to the sub-area |
| Players | `/hs subareas conf [name] players remove [player]` | Remove a player from the sub-area |
| Player Flags | `/hs subareas conf [name] players flags [player] [flag] (allow/deny)` | Set player-specific flags |

## Renting Sub-Areas

Sub-areas can have rental signs too. Place a `[Rent]` sign inside the sub-area.

## Limits

```yaml
# In limits.yml
limits:
  groups:
    default:
      subareas-per-region: 1
      max-subarea-volume: 400
```
