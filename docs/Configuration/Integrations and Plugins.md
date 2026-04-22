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

!!! danger "Data Loss"

    Regenerating chunks **permanently deletes** everything in those chunks. This cannot be undone. Make sure players understand this before enabling the feature.

### Configuration

```yaml
fastasyncworldedit:
  regenerate-chunks: false
```

### How It Works

1. Player deletes their region or unclaims a chunk
2. Homestead marks all claimed chunks
3. FAWE regenerates each chunk to its original terrain
4. All blocks, entities, and items in those chunks are removed
5. The chunks look like they were never claimed

## PlaceholderAPI

**PlaceholderAPI** (often called PAPI) is a plugin that displays information from other plugins in chat, tab lists, scoreboards, and more.

!!! success "Automatic Registration"

    Homestead automatically registers its placeholders when it starts. You don't need to download any expansion packs or run commands.

### Available Placeholders

**Region Information:**

| Placeholder                           | Description                                  |
|---------------------------------------|----------------------------------------------|
| `%homestead_region_name%`             | Name of player's primary region              |
| `%homestead_region_current%`          | Name of region player is standing in         |
| `%homestead_region_claimed_chunks%`   | Number of chunks claimed in current region   |
| `%homestead_region_max_chunks%`       | Maximum chunks player can claim              |
| `%homestead_region_trusted_members%`  | Number of members in current region          |
| `%homestead_region_max_members%`      | Maximum members allowed in current region    |

**Economy Information:**

| Placeholder                  | Description                          |
|------------------------------|--------------------------------------|
| `%homestead_region_bank%`    | Balance in region's bank             |
| `%homestead_upkeep_amount%`  | Amount due for next upkeep payment   |
| `%homestead_upkeep_at%`      | When next upkeep payment is due      |

**War Information:**

| Placeholder              | Description               |
|--------------------------|---------------------------|
| `%homestead_war_name%`   | Current war name          |
| `%homestead_war_prize%`  | Prize for winning the war |

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

| Plugin    | Default Port | URL                                              |
|-----------|--------------|--------------------------------------------------|
| BlueMap   | **8100**     | [http://localhost:8100/](http://localhost:8100/) |
| Squaremap | **8080**     | [http://localhost:8080/](http://localhost:8080/) |
| Pl3xMap   | **8080**     | [http://localhost:8080/](http://localhost:8080/) |
| dynmap    | **8123**     | [http://localhost:8123/](http://localhost:8123/) |

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
    ...
  
  # Claimed chunk appearance
  chunks:
    ...
```

### Customizing Icons

**Finding Icon URLs:**

1. Search for an icon you like (Imgur, image hosting sites)
2. Get the **direct image URL** (must end in **.png**)
3. Add it to the `list` section

**Example:**
```yaml
list:
  House: https://i.imgur.com/house123.png
  Mine: https://i.imgur.com/mine456.png
  Portal: https://i.imgur.com/portal789.png
```

Players can then select these icons for their regions using the command `/region set icon [name]`.

### Update Interval

`update-interval: 60` means the map updates every 60 seconds.

- **Lower values** (30, 45): More frequent updates, shows changes faster
- **Higher values** (120, 180): Less server load, updates less often

For busy servers, use 90-120 seconds. For small servers, 30-60 seconds works fine.
