# Integrations and Plugins

Homestead integrates with popular Minecraft plugins to add features and compatibility. These integrations work automatically when the required plugins are installed.

## WorldGuard

**WorldGuard** is a region protection plugin commonly used to protect spawn areas, PvP arenas, and other special zones.

### Compatibility

Homestead can prevent players from claiming chunks inside WorldGuard-protected regions. This is useful for:

- Protecting spawn areas from claims
- Reserving event areas
- Keeping PvP arenas claim-free
- Protecting admin buildings

### Configuration

```yaml
worldguard:
  # Prevent claiming in WorldGuard regions?
  protect-existing-regions: true
```

When enabled:

- Players **cannot** claim chunks inside any WorldGuard region
- Existing Homestead regions inside WorldGuard areas remain unaffected
- Players will see an error message when trying to claim protected chunks

**Recommended:** Keep this enabled to prevent conflicts between the two plugins.

## WorldEdit

**WorldEdit** is a world editing plugin that includes chunk regeneration features.

### Chunk Regeneration

When a player deletes a region, Homestead can automatically regenerate all claimed chunks back to their original state. This removes all buildings, chests, and changes made to the land.

!!! warning "Performance Warning"

    Chunk regeneration uses significant server resources, especially for large regions. The WorldEdit developers consider this feature deprecated. Only enable this on servers with good hardware and few active players.

!!! danger "Data Loss"

    Regenerating chunks **permanently deletes** everything in those chunks. This cannot be undone. Make sure players understand this before enabling the feature.

### Configuration

```yaml
worldedit:
  # Regenerate chunks when regions are deleted?
  regenerate-chunks: false
```

### When to Use

- You have a powerful dedicated server
- Player activity is low to moderate
- You want to keep the world clean
- You don't mind the performance impact

### How It Works

1. Player deletes their region using `/region delete`
2. Homestead marks all claimed chunks
3. WorldEdit regenerates each chunk to its original terrain
4. All blocks, entities, and items in those chunks are removed
5. The chunks look like they were never claimed

## PlaceholderAPI

**PlaceholderAPI** (often called PAPI) is a plugin that displays information from other plugins in chat, tab lists, scoreboards, and more.

!!! success "Automatic Registration"

    Homestead automatically registers its placeholders when it starts. You don't need to download any expansion packs or run commands.

### Available Placeholders

**Region Information:**

| Placeholder                           | Description                                  | Example Output        |
|---------------------------------------|----------------------------------------------|-----------------------|
| `%homestead_region_name%`             | Name of player's primary region              | "MyBase"              |
| `%homestead_region_current%`          | Name of region player is standing in         | "ShopDistrict" or "-" |
| `%homestead_region_claimed_chunks%`   | Number of chunks claimed in current region   | "8"                   |
| `%homestead_region_max_chunks%`       | Maximum chunks player can claim              | "16"                  |
| `%homestead_region_trusted_members%`  | Number of members in current region          | "3"                   |
| `%homestead_region_max_members%`      | Maximum members allowed in current region    | "5"                   |

**Economy Information:**

| Placeholder                  | Description                          | Example Output |
|------------------------------|--------------------------------------|----------------|
| `%homestead_region_bank%`    | Balance in region's bank             | "$5,000"       |
| `%homestead_upkeep_amount%`  | Amount due for next upkeep payment   | "$800"         |
| `%homestead_upkeep_at%`      | When next upkeep payment is due      | "2d 5h 30m"    |

**War Information:**

| Placeholder              | Description               | Example Output     |
|--------------------------|---------------------------|--------------------|
| `%homestead_war_name%`   | Current war name          | "North vs South"   |
| `%homestead_war_prize%`  | Prize for winning the war | "$10,000"          |

### Usage Examples

**In Chat Format:**
```
[%homestead_region_current%] PlayerName: Hello!
```
Shows as: `[MyBase] PlayerName: Hello!`

**In Tab List:**
```
%player_name% [%homestead_region_claimed_chunks%/%homestead_region_max_chunks%]
```
Shows as: `Steve [8/16]`

**In Scoreboard:**
```
Region: %homestead_region_name%
Bank: %homestead_region_bank%
Members: %homestead_region_trusted_members%/%homestead_region_max_members%
```

**For Featherboard, DeluxeMenus, TAB, etc:**

Just insert the placeholders into your configuration files. They'll be replaced automatically.

## Web Map Rendering Plugins

These plugins create interactive web-based maps of your server, similar to Google Maps. Homestead automatically displays all claimed regions on these maps.

### Supported Plugins

| Plugin    | Default Port | URL                           |
|-----------|--------------|-------------------------------|
| BlueMap   | 8100         | http://localhost:8100/        |
| Squaremap | 8080         | http://localhost:8080/        |
| Pl3xMap   | 8080         | http://localhost:8080/        |
| dynmap    | 8123         | http://localhost:8123/        |

### Setup

1. Install one of the supported map plugins
2. Configure the map plugin following their documentation
3. Restart your server
4. Claimed regions will automatically appear on the web map!

No additional configuration needed in Homestead, it just works.

### Configuration Options

Customize how regions appear on web maps:

```yaml
dynamic-maps:
  # Enable web map integration?
  enabled: true

  # How often to update the map (in seconds)
  update-interval: 60

  # Region icon settings
  icons:
    # Allow players to choose custom icons?
    enabled: true

    # Default icon for regions
    default: https://imgur.com/TUQzlCK.png
    
    # Icon size in pixels
    size: 20

    # Available icons players can choose from
    list:
      Hut: https://imgur.com/GFrfD0H.png
      Castle: https://imgur.com/ABC1234.png
      Farm: https://imgur.com/XYZ5678.png
      Shop: https://imgur.com/DEF9012.png
      # Add more custom icons here

  # Claimed chunk appearance
  chunks:
    # Color for operator-claimed regions (hex color)
    operator-color: 0xFF0000
    
    # Description shown on operator regions
    operator-description: "Admin Region"
    
    # You can add more custom colors/descriptions for different groups
```

### Customizing Icons

**Finding Icon URLs:**

1. Search for an icon you like (Imgur, image hosting sites)
2. Get the **direct image URL** (must end in .png, .jpg, etc.)
3. Add it to the `list` section

**Example:**
```yaml
list:
  House: https://i.imgur.com/house123.png
  Mine: https://i.imgur.com/mine456.png
  Portal: https://i.imgur.com/portal789.png
```

Players can then select these icons for their regions using the region menu.

### Customizing Colors

Colors use hexadecimal format:

- Red: `0xFF0000`
- Green: `0x00FF00`
- Blue: `0x0000FF`
- Yellow: `0xFFFF00`
- Purple: `0x800080`
- Orange: `0xFFA500`

**Example for VIP players:**
```yaml
chunks:
  operator-color: 0xFF0000
  operator-description: "Admin Land"
  
  vip-color: 0xFFD700
  vip-description: "VIP Territory"
  
  default-color: 0x00FF00
  default-description: "Claimed Land"
```

### Update Interval

`update-interval: 60` means the map updates every 60 seconds.

- **Lower values** (30, 45): More frequent updates, shows changes faster
- **Higher values** (120, 180): Less server load, updates less often

For busy servers, use 90-120 seconds. For small servers, 30-60 seconds works fine.

## Compatibility Notes

**Port Conflicts:**  
If the default port is in use, change it in the map plugin's configuration (not in Homestead).

**Performance:**  
Map rendering uses server resources. If you experience lag:

- Increase the `update-interval`
- Limit the map render distance
- Use a lighter map plugin
