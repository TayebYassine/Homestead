# Other Configuration

## Welcome Signs

Replace the command from `/region visit [region]` to `/region visit [player]`.

```yaml
welcome-signs:
  # Enable this feature? "true" to enable, "false" to disable.
  enabled: false
```

For full guide on Welcome signs usage, [click here](../Usage/Welcome%20Signs.md).

## Sub-Areas

Allow sub-areas to be created on the server.

```yaml
sub-areas:
  # Enable this feature? "true" to enable, "false" to disable.
  enabled: true
```

## Region Borders

Customize region borders by particles or client-side blocks.

```yaml
borders:
  enabled: true

  type: particles # particles, blocks

  block-type: GRAY_GLAZED_TERRACOTTA
```

!!! warning "No Particles Issue"

    Please check the resource packs you are using; some of them could have disabled particles, or in the accessibility settings of your Minecraft client, you could have disabled particles.

## Clean Startup

Clears corrupted data in regions and wars data.

```yaml
clean-startup: true
```

## Disabled Worlds

List of worlds where players cannot claim chunks.

```yaml
# Worlds where claiming is disabled. World name must be exact.
disabled-worlds-exact:
  - "world_the_end"
  - "factions"
  - "pvp_arena"

# Worlds where claiming is disabled. World name must be validated under the pattern.
# Use this for temporary worlds like EliteMobs plugin.
disabled-worlds-pattern:
  - "em_*"
  - "minigame_*_end"
```

## Disabled Flags

Flags that players cannot modify. You can find the list of flags [here](./Flags.md).

```yaml
disabled-flags:
  - "use-bells"
  - "no-fall-damage"
  - ...
```

## TNT Exploding Outside Regions + Below Sea Level

Allow TNT to explode only below the sea level (Y = 63) and outside a region?

```yaml
special-feat:
  tnt-explodes-only-below-sea-level: false
```

## No Acceptance System

Instantly trust players without waiting requested players to accept or deny the invites.

```yaml
special-feat:
  ignore-trust-acceptance-system: false
```
