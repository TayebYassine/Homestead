# Integrations and Plugins

Homestead uses a lot of different APIs to communicate with other dependencies and implement as many features as possible for a Minecraft server.

## WorldGuard
**WorldGuard** is another grief prevention plugin mostly used to protect spawns, PVP arenas... etc.

To prevent players from claiming chunks inside a WorldGuard-protected area, enable this feature:

```yaml
worldguard:
  protect-existing-regions: true
```

## WorldEdit
Homestead uses a unique feature that only exists in **WorldEdit**, which is regenerating chunks.

!!! warning "Deprecated Feature"

    This feature is deprecated by WorldEdit developers, and it's not recommended to keep it enabled when your server has very active players. Regenerating chunks requires a significant amount of memory, especially when a player or operator deletes a region with more than 4 claimed chunks.

To enable the feature, you must install WorldEdit and have this configuration enabled in **config.yml**:

```yaml
worldedit:
  regenerate-chunks: false
```

## PlaceholderAPI
**PlaceholderAPI** is a plugin that allows server owners to display information from various plugins in a uniform format.

!!! success "Automatic Hook"

    There is no need to use `/papi ecloud download Homestead`, Homestead automatically registers the placeholders on startup.

Available placeholders:

- `%homestead_region_bank%`: Displays the region's bank balance.
- `%homestead_region_name%`: Shows the name of the region.
- `%homestead_region_current%`: Shows the name of the region that the player is currently inside.
- `%homestead_region_claimed_chunks%`: Indicates the total number of claimed chunks in the region.
- `%homestead_region_max_chunks%`: Displays the maximum number of chunks a player can claim.
- `%homestead_region_trusted_members%`: Indicates the total number of members in the region.
- `%homestead_region_max_members%`: Displays the maximum number of chunks a player can claim.
- `%homestead_upkeep_amount%`: Displays the required amount to pay for taxes.
- `%homestead_upkeep_at%`: Shows when the region's owners must pay their taxes.
- `%homestead_war_name%`: Shows the current war name.
- `%homestead_war_prize%`: Shows the current war prize.

## Web Map Rendering Plugins

**dynmap**, **Pl3xMap**, **Squaremap**, and **BlueMap** are Google Maps-like plugins that show all claimed chunks in marked boxes.

Install one of these plugins, and restart the server. You should be able to see all claimed chunks via these links:

- **BlueMap**: [http://localhost:8100/](http://localhost:8100/)
- **Squaremap**: [http://localhost:8080/](http://localhost:8080/)
- **Pl3xMap**: [http://localhost:8080/](http://localhost:8080/)
- **dynmap**: [http://localhost:8123/](http://localhost:8123/)

```yaml
dynamic-maps:
  # Enable this setting? true to enable, false to disable.
  enabled: true

  update-interval: 60 # seconds

  # Region icons
  icons:
    # Enable this setting? true to enable, false to disable.
    # If true, players can pick icons for their regions.
    enabled: true

    default: https://imgur.com/TUQzlCK.png
    size: 20 # px

    list:
      Hut: https://imgur.com/GFrfD0H.png
      ...

  chunks:
    operator-color: 0xFF0000
    operator-description: ...

    ...
```

