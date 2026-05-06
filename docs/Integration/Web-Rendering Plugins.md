# Web-Rendering Plugins

These plugins create interactive web-based maps of your server, similar to Google Maps. Homestead automatically displays all claimed regions on these maps.

## Supported Plugins

| Plugin    | Default Port | URL                                              |
|-----------|--------------|--------------------------------------------------|
| BlueMap   | **8100**     | [http://localhost:8100/](http://localhost:8100/) |
| Squaremap | **8080**     | [http://localhost:8080/](http://localhost:8080/) |
| Pl3xMap   | **8080**     | [http://localhost:8080/](http://localhost:8080/) |
| dynmap    | **8123**     | [http://localhost:8123/](http://localhost:8123/) |

## Setup

1. Install one of the supported map plugins
2. Configure the map plugin following their documentation
3. Restart your server
4. Claimed regions will automatically appear on the web map!

No additional configuration needed in Homestead, it just works.

## Configuration

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

## Customizing Icons

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

Players can then select these icons for their regions using the command `/region setmapicon [name]`.

## Update Interval

`update-interval: 60` means the map updates every 60 seconds.

- **Lower values** (30, 45): More frequent updates, shows changes faster
- **Higher values** (120, 180): Less server load, updates less often

For busy servers, use 90-120 seconds. For small servers, 30-60 seconds works fine.
