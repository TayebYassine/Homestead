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

## Clean startup

Clears corrupted data in regions and wars data.

```yaml
clean-startup: true
```

## Disabled worlds

List of worlds where players cannot claim chunks.

```yaml
disabled-worlds:
  - "world_the_end"
  - "creative"
  - "factions"
  - ...
```

## Disabled flags

Flags that players cannot modify. You can find the list of flags [here](./Flags.md).

```yaml
disabled-flags:
  - "use-bells"
  - "no-fall-damage"
  - ...
```
