# Borders & Maps

## Region Borders

Visualize your claimed chunks with colored borders.

### Toggle Display

```
/region borders
```

Shows the borders of all claimed chunks. Run again to hide.

### Configuration

```yaml
# In regions.yml
borders:
  enabled: true
  type: particles       # particles or blocks
  dust-colors:
    owner: [0, 255, 0]      # Green — owner
    member: [255, 255, 0]   # Yellow — members
    visitor: [255, 0, 0]    # Red — visitors
    sub-area: [0, 179, 255] # Light blue — sub-areas
  dust-size: 3.0
  block-type: GRAY_GLAZED_TERRACOTTA
```

### Display Types

| Type | Pros | Cons |
|:-----|:-----|:-----|
| **Particles** | Low performance impact, doesn't interfere with building | May be invisible with some resource packs |
| **Blocks** | Very visible | May cause visual conflicts with builds |

!!! warning "Particle Visibility"

    If players can't see borders:
    1. Check if resource packs disable particles
    2. Check Minecraft accessibility settings
    3. Try switching to `type: blocks`

## Web Map Integration

Homestead automatically displays claimed regions on supported web map plugins. No extra configuration needed!

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

### Custom Icons

Players can select icons for their regions:

```
/region setmapicon [icon]
```

### Map Colors

Players can change their region's display color:

```
/region setmapcolor [color]
```

Example: `/region setmapcolor RED` or `/region setmapcolor #FF5733`

### BlueMap 2D Mode

```yaml
dynamic-maps:
  bluemap:
    use-2d-markers: false  # Set true to always use 2D markers
```
