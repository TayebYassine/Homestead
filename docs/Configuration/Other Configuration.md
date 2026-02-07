# Other Configuration

This page covers additional Homestead settings that don't fit into other categories but are still important for customizing your server.

## Welcome Signs

Welcome signs provide an alternative way for players to visit regions. Instead of typing `/region visit [region-name]`, players can use signs.

### How It Works

With welcome signs enabled:

- Players create a sign with specific text format
- The sign becomes functional
- Use format: `/region visit [player] (sign-index)` instead of `/region visit [region]`

### Configuration

```yaml
welcome-signs:
  # Enable welcome signs feature?
  enabled: false
```

## Sub-Areas

Sub-areas are smaller protected zones within a larger region. They allow region owners to create specific areas with different permissions.

### What Are Sub-Areas?

Think of sub-areas as "regions within regions":

- **Main region**: Your entire claimed land
- **Sub-area**: A smaller section with custom rules (like a shop, farm, or PvP arena within your base)

### Configuration

```yaml
sub-areas:
  # Enable sub-areas feature?
  enabled: true
```

### Use Cases

**Example 1: Public Shop in Private Base**

- Main region: Private base (only you can build)
- Sub-area: Shop area (public can enter and use chests)

**Example 2: PvP Arena in Town**

- Main region: Safe town (no PvP)
- Sub-area: Arena (PvP enabled)

**Example 3: Farm in Wilderness Region**

- Main region: General land
- Sub-area: Community farm (public can harvest)

For detailed sub-area management, see the [Sub-Areas Guide](../Usage/Sub-Areas.md).

### Limits

Sub-area limits are set per group in the [Ranks and Limits](./Ranks%20and%20Limits.md) configuration:

```yaml
limits:
  groups:
    default:
      subareas-per-region: 1      # Max sub-areas per region
      max-subarea-volume: 400     # Max sub-area size (blocks³)
```

## Region Borders

Visualize region boundaries using particles or client-side blocks.

### Configuration

```yaml
borders:
  # Enable region borders?
  enabled: true

  # Display type: "particles" or "blocks"
  type: particles

  # Block type (only used if type is "blocks")
  block-type: GRAY_GLAZED_TERRACOTTA
```

### Border Types

**Particles:**

- Visible as floating particle effects
- Don't interfere with building
- Lower performance impact
- Best for most servers

**Blocks:**

- Appear as actual blocks (client-side only)
- More visible than particles
- May cause visual conflicts
- Useful for clearly defined boundaries

### Block Type Options

If using `type: blocks`, choose from any Minecraft block:

```yaml
block-type: GLASS               # Transparent
block-type: GLOWSTONE           # Bright and visible
block-type: REDSTONE_BLOCK      # Red color
block-type: BARRIER             # Invisible but present
block-type: SEA_LANTERN         # Glowing
```

### Performance Considerations

**For servers with many players:**

- Use `particles` for better performance
- Consider disabling if experiencing lag

**For smaller servers:**

- Either option works fine
- `blocks` can be more visible

!!! warning "Particle Visibility Issues"

    Some resource packs disable particles. If players can't see borders:

    1. Check their resource pack settings
    2. Verify particles aren't disabled in Minecraft accessibility settings
    3. Try switching to `type: blocks`

## Clean Startup

Automatically remove corrupted or invalid data when Homestead starts.

### Configuration

```yaml
clean-startup: true
```

!!! info "Recommended Setting"

    Keep this **enabled** (`true`) unless startup time is a significant concern. It helps prevent issues before they cause problems.

## Disabled Worlds

Prevent players from claiming chunks in specific worlds.

### Configuration

There are two ways to disable worlds:

**Exact World Names:**
```yaml
# World names must match exactly
disabled-worlds-exact:
  - "world_the_end"
  - "world_nether"
  - "factions"
  - "pvp_arena"
  - "minigames"
```

**Pattern Matching:**
```yaml
# World names matching these patterns
disabled-worlds-pattern:
  - "em_*"           # Any world starting with "em_"
  - "minigame_*"     # Any world starting with "minigame_"
  - "*_temp"         # Any world ending with "_temp"
  - "dungeon_*_end"  # Pattern with middle wildcard
```

### Using Patterns

Patterns use `*` as a wildcard:

- `*` matches any characters
- `em_*` matches: `em_world`, `em_dungeon`, `em_123`
- `*_end` matches: `world_end`, `special_end`, `temp_end`
- `minigame_*_pvp` matches: `minigame_1_pvp`, `minigame_arena_pvp`

### Common Use Cases

**Resource Worlds (That Reset):**
```yaml
disabled-worlds-exact:
  - "resource_world"
  - "mining_dimension"
```

**Temporary Worlds:**
```yaml
disabled-worlds-pattern:
  - "temp_*"
  - "*_temporary"
```

**PvP/Minigame Worlds:**
```yaml
disabled-worlds-exact:
  - "pvp_arena"
  - "kitpvp"
  - "bedwars"
  - "skywars"
```

**Plugin-Generated Worlds (like EliteMobs):**
```yaml
disabled-worlds-pattern:
  - "em_*"  # EliteMobs dungeons
```

### Finding World Names

Not sure what your world is called?

1. Use `/worlds` or similar command (if available)
2. Check server files in the main directory (each folder is a world)
3. Check your world management plugin's config

## Disabled Flags

Prevent players from modifying specific flags, forcing them to use default values.

### Configuration

```yaml
disabled-flags:
  - "use-bells"
  - "trigger-raid"
  - "wither-damage"
  - "explosions-damage"
```

### When to Use

**Prevent players from:**

- Enabling dangerous features (explosions, wither damage)
- Changing important server-wide settings
- Creating exploits with specific flag combinations
- Overriding gameplay balance decisions

### Finding Flag Names

All available flags are listed in the [Flags Documentation](./Flags.md).

### Default Behavior

When a flag is disabled:

- It still does appear in the flags menu
- Players can't change it with commands
- The flag uses the default value from `config.yml`
- Only server operators can change it manually in the database (not recommended)

## TNT Exploding Below Sea Level

Allow TNT to explode **only** below sea level (Y = 63) **and** outside any claimed regions.

### Configuration

```yaml
special-feat:
  tnt-explodes-only-below-sea-level: false
```

### How It Works

When enabled:

- TNT explodes normally below Y = 63 in unclaimed areas
- TNT above Y = 63 doesn't explode (in unclaimed areas)
- TNT in claimed regions follows the region's explosion flags

!!! info "Sea Level"

    Sea level is Y = 63 in Minecraft. This setting uses that as the threshold, but it's really about "underground" vs "surface" rather than actual water.

## Trust Acceptance System

Control whether players must accept trust invitations or are trusted immediately.

### Configuration

```yaml
special-feat:
  ignore-trust-acceptance-system: false
```

### How It Works

**Default (false) - Requires Acceptance:**

1. Owner trusts a player: `/region trust PlayerName`
2. Player receives a notification
3. Player accepts: `/region accept [region]`
4. Player is now trusted in the region

**When Enabled (true) - Instant Trust:**

1. Owner trusts a player: `/region trust PlayerName`
2. Player is immediately trusted (no acceptance needed)

## Testing Your Configuration

After making changes:

1. **Reload Homestead:**
   ```
   /hsadmin reload
   ```

2. **Test each setting:**
    - Try claiming in disabled worlds
    - Attempt to change disabled flags
    - Check border visibility
    - Test welcome signs (if enabled)

3. **Check console** for any configuration errors

4. **Restart server** if reload doesn't apply changes
