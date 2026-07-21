# Other Settings

This page covers additional Homestead configuration options.

## Clean Startup

Automatically remove corrupted or invalid data on startup.

```yaml
# In regions.yml
clean-startup: true
```

!!! success "Recommended"

    Keep this enabled unless startup time is a concern.

## Adjacent Chunks

Force all claimed chunks to be connected (no scattered claims).

```yaml
# In regions.yml
adjacent-chunks: true
```

Disable for multi-location builds (separate bases and farms).

## Chunk Price

Charge players a fee to claim each chunk.

```yaml
# In regions.yml
chunk-price: 0.0  # 0.0 = free
```

Requires an economy plugin.

## Auto-Select Target Region

Automatically assign a random target region to new players.

```yaml
# In regions.yml
autoset-target-region: true
```

## Selection Tool

Configure the item used to select area corners (for sub-areas):

```yaml
# In regions.yml
selection-tool:
  item: GOLDEN_HOE
  messages:
    none: "&7Please select two corners..."
    firstCorner: "&7First corner: &a✔..."
    # ...
```

## Disabled Worlds

Prevent claiming in specific worlds:

```yaml
# In regions.yml
disabled-worlds-exact:
  - "world_the_end"
  - "factions"

disabled-worlds-pattern:
  - "em_*"           # Wildcard — matches em_world, em_dungeon, etc.
  - "minigame_*_end"
```

## Enter/Exit Messages

Show messages when players enter or leave a region:

```yaml
# In regions.yml
enter-exit-region-message:
  enabled: true
  type: 'actionbar'   # title, actionbar, or chat
  messages:
    enter:
      actionbar: "&7You enter the territory of &2{region-displayname}"
    exit:
      actionbar: "&7You left &2{region-displayname}"
```

Variables: `{region-displayname}`, `{region-description}`, `{region-owner}`

## Log Private Chat

Log region chat messages to console for moderation:

```yaml
# In regions.yml
log-private-chat: true
```

## Cooldown System

Configure cooldowns per action to prevent spam:

```yaml
# In regions.yml
cooldown:
  flag-change-state:
    ignore-operators: false
    value: 3                    # Seconds
  region-spawn-change:
    ignore-operators: true
    value: 600                  # 10 minutes
  region-rename-change:
    ignore-operators: true
    value: 600
  region-description-change:
    ignore-operators: true
    value: 300                  # 5 minutes
  region-transfer-ownership:
    ignore-operators: true
    value: 604800               # 7 days
  region-dynamic-map-settings-change:
    ignore-operators: true
    value: 43200                # 12 hours
  region-chunk-claim:
    ignore-operators: true
    value: 5                    # 5 seconds
  region-chunk-unclaim:
    ignore-operators: true
    value: 5
  region-teleport:
    ignore-operators: true
    value: 15
  war-flag-disabled:
    ignore-operators: true
    value: 129600               # 36 hours
```

## Delayed Teleport

Configure teleport delays and boss bar display:

```yaml
# In regions.yml
delayed-teleport:
  enabled: true
  delay: 3                     # Seconds
  price: 0.0                   # Cost (0.0 = free)
  cancel-on-move: true         # Cancel if player moves
  ignore-operators: true       # OPs bypass delay
  boss-bar:
    enabled: true
    title: "&7Teleporting in &3{seconds}s&7..."
    color: "PURPLE"
    style: "SEGMENTED_10"
    countdown-mode: "DEPLETE"
```

## TNT Below Sea Level

Allow TNT explosions only below Y=63 in unclaimed areas:

```yaml
# In regions.yml
special-feat:
  tnt-explodes-only-below-sea-level: false
```

## Trust Acceptance System

Control whether players must accept trust invitations:

```yaml
# In regions.yml
special-feat:
  ignore-trust-acceptance-system: false
```

- **false** (default): Trusted players must run `/region accept`
- **true**: Players are trusted immediately

## Force-Loaded Chunks

Keep chunks loaded even when no players are nearby:

!!! info "Limits"

    Max force-loaded chunks per region is set in [Ranks & Limits](Ranks and Limits.md).

## World Rules

Apply custom flags to wilderness (unclaimed) chunks per world:

```yaml
# In flags.yml
world-rules:
  enabled: false
  worlds:
    world_the_end:
      player_flags: 68719474687
      world_flags: 13631487
    pvp_arena:
      player_flags: 68719474687
      world_flags: 13631487
```

!!! info "Ignored Flags"

    These world rules flags are ignored: `liquid-flow`, `wilderness-dispensers`, `wilderness-pistons`, `wilderness-minecarts`

## Storage Feature

Allow trusted members to share a private chest:

```yaml
# In regions.yml
storage:
  enabled: false
  size: 27     # 9, 18, 27, 36, 45, or 54
```

## Welcome Message

Customize the join message for players:

```yaml
# In regions.yml
welcome-message:
  enabled: true
  message: "&eWelcome back! There are &6{unread-logs} &eunread mails..."
```

## War Configuration

```yaml
# In regions.yml
wars:
  enabled: false
  min-prize: 1000.0
  max-prize: 1000000000.0
  keep-inventory: false
  give-head: true
  broadcast-type: "regions"   # regions, server, or silent
```

## Ignore Region Protection in Disabled Worlds

```yaml
# In regions.yml
special-feat:
  ignore-region-protection-if-action-in-disabled-world: true
```

## End Portal Teleport

```yaml
# In regions.yml
special-feat:
  teleport-players-back-to-region-spawn-when-entering-end-exit-portal: true
```
