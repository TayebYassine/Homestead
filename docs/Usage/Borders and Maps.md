# Borders & Maps

---

## Region Borders

Visualize your claimed chunks with colored borders while you explore.

### Toggle Display

```
/region borders
```

Shows the borders of all claimed chunks around you for **3 minutes**, then turns off automatically. Run the command again to restart the timer, or stop it immediately with:

```
/region borders stop
```

!!! info "Feature Toggle"

    If borders are disabled in `regions.yml` (`borders.enabled: false`), the command reports that the feature is off.

### Configuration

```yaml
# In regions.yml
borders:
  enabled: true
  type: particles       # particles, blocks, or glow
  dust-colors:
    owner: [0, 255, 0]      # Green: owner
    member: [255, 255, 0]   # Yellow: members
    visitor: [255, 0, 0]    # Red: visitors
    sub-area: [0, 179, 255] # Light blue: sub-areas
  dust-size: 3.0
  block-type: GRAY_GLAZED_TERRACOTTA
```

- `type` — how borders are rendered (see below).
- `dust-colors` — RGB values (0–255) used for particle borders, per relationship to the region.
- `dust-size` — particle size when `type` is `particles`.
- `block-type` — any Bukkit material used when `type` is `blocks`.

### Display Types

Three display types are available:

- **particles** — dust particles along the chunk edges that follow your Y position. Low performance impact and doesn't interfere with building, but may be invisible with some resource packs.
- **blocks** — fake block changes sent to your client at the highest surface of the border. Very visible, but may conflict visually with builds.
- **glow** — glowing fake blocks that follow your Y position. More visible than plain particles without placing real blocks.

Full reference: [Borders configuration](../Configuration/Other%20Settings.md#borders).

!!! warning "Particle Visibility"

    If players can't see borders:
    1. Check if resource packs disable particles.
    2. Check Minecraft accessibility settings.
    3. Try switching to `type: blocks` or `type: glow`.

---

## Web Map Integration

Homestead automatically displays claimed regions on supported web map plugins when dynamic maps are enabled in `config.yml`. No extra configuration is needed beyond installing the map plugin.

### Supported Maps

| Plugin | Default Port | URL |
|:-------|:------------:|:----|
| **BlueMap** | 8100 | [http://localhost:8100/](http://localhost:8100/) |
| **Squaremap** | 8080 | [http://localhost:8080/](http://localhost:8080/) |
| **Pl3xMap** | 8080 | [http://localhost:8080/](http://localhost:8080/) |
| **dynmap** | 8123 | [http://localhost:8123/](http://localhost:8123/) |

### Customizing Map Appearance

```yaml
# In config.yml
dynamic-maps:
  enabled: true
  update-interval: 60   # Seconds between map updates
  icons:
    enabled: false
    default: https://imgur.com/TUQzlCK.png
    size: 20
    list:
      Hut: https://imgur.com/GFrfD0H.png
      Mansion: https://imgur.com/62ofr2V.png
      # ... add your own icons
  chunks:
    color: 0x00FF00                # Default region color
    operator-color: 0xFF0000       # Operator-claimed region color
    transparency-fill: 40
    transparency-outline: 125
    description: "<div>...</div>"  # HTML hover description
```

- `update-interval` — how often map markers refresh, in seconds. Raise this on large servers to reduce CPU load.
- `icons.enabled` — allow players to pick custom icons for their regions.
- `chunks.color` / `operator-color` — default marker colors, as hex integers.
- `transparency-fill` / `transparency-outline` — alpha values (0–255) for filled areas and borders.

### Custom Icons

Players can select icons for their regions (when icons are enabled):

```
/region setmapicon [icon]
```

Run the command with no argument to open the icon menu. Use `Default` to reset to the default icon. Icon changes share a 12-hour cooldown with map color changes.

### Map Colors

Players can change their region's display color:

```
/region setmapcolor [color]
```

Run with no argument to open the color menu. Colors are named values (e.g. `RED`) rather than raw hex codes. The same 12-hour cooldown applies.

**Example:** `/region setmapcolor RED`

### BlueMap 2D Mode

```yaml
dynamic-maps:
  bluemap:
    use-2d-markers: false  # Set true to always use 2D markers
```

When `true`, BlueMap always renders region markers in 2D even if users select 3D in the BlueMap interface.

