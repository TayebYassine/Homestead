# Other Settings

This page covers additional Homestead configuration options from `regions.yml`, `config.yml`, and `flags.yml` that are not documented elsewhere.

---

## Debug Mode

Show detailed console messages for troubleshooting.

```yaml
# In config.yml
debug: false
```

!!! warning "Console Flood"

    Debug mode can flood the console on busy servers. Only enable it when actively debugging, then turn it off.

## Metrics

Anonymous usage statistics via bStats and FastStats. No personal data is collected.

```yaml
# In config.yml
metrics: true
```

- [bStats Privacy Policy](https://bstats.org/privacy-policy)
- [FastStats Privacy Policy](https://faststats.dev/privacy)

## Player Input Display

Control how Homestead prompts players when it needs chat input (renaming a region, trusting a player, etc.).

```yaml
# In config.yml
player-input:
  type: "chat"   # chat, title, or actionbar
```

Messages shown during input sessions are configured in the [language file](../Advanced/Language.md).

---

## Clean Startup

Automatically remove corrupted or invalid data on startup (orphaned regions, broken coordinates, etc.).

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

Requires an economy plugin. See [Economy Setup](../Economy/Setup.md).

## Auto-Select Target Region

Automatically assign a random target region to new players.

```yaml
# In regions.yml
autoset-target-region: true
```

## Sub-Areas Toggle

Globally enable or disable the sub-areas feature.

```yaml
# In regions.yml
sub-areas:
  enabled: true
```

When `false`, players cannot create new sub-areas. Existing sub-areas are unaffected. See [Sub-Areas](../Usage/Sub-Areas.md).

## Rewards Toggle

Globally enable or disable the rewards system (bonus chunks for members and playtime).

```yaml
# In regions.yml
rewards:
  enabled: true
```

When `false`, no bonus chunks or sub-areas are awarded. See [Rewards](Rewards.md).

## Welcome Signs Toggle

Enable or disable welcome signs globally.

```yaml
# In regions.yml
welcome-signs:
  enabled: false
```

See [Welcome Signs](../Usage/Welcome%20Signs.md).

---

## Selection Tool

Configure the item used to select area corners, for sub-areas or claiming multiple chunks at once.

```yaml
# In regions.yml
selection-tool:
  item:
    name: "&eSelection Tool"
    lore:
      - "&7Use this tool to create sub-areas or"
      - "&7claim new chunks!"
    type: GOLDEN_HOE  # Any Bukkit material, or "NEXO-(id)" for NexoMC items

  messages:
    none: "&7Please select two corners/points inside or outside of the region."
    firstCorner: "&7First corner: &a✔ &7| Second corner: &c✘"
    secondCorner: "&7First corner: &c✘ &7| Second corner: &a✔"
    selectionDone: "&aYou can create a new sub-area or claim new chunks!"
```

## Disabled Worlds

Prevent claiming in specific worlds:

```yaml
# In regions.yml
disabled-worlds-exact:
  - "world_the_end"
  - "factions"
  - "pvp_arena"

disabled-worlds-pattern:
  - "em_*"            # Wildcard; matches em_world, em_dungeon, etc.
  - "minigame_*_end"
```

- **Exact** — case-sensitive full world names
- **Pattern** — `*` matches any characters

Use an empty array (`[]`) if no worlds should be disabled.

---

## Enter/Exit Messages

Show messages when players enter or leave a region:

```yaml
# In regions.yml
enter-exit-region-message:
  enabled: true
  type: 'title'   # title, actionbar, or chat
  messages:
    enter:
      title: [ "&2{region-displayname}", "&7{region-description}" ]  # [ TITLE, SUBTITLE ]
      actionbar: "&7You enter the territory of &2{region-displayname}&7: {region-description}"
      chat: "&7You enter the territory of &2{region-displayname}&7: {region-description}"
    exit:
      title: [ "&3Wilderness", "&7Roam the wild!" ]
      actionbar: "&7You left the territory of &2{region-displayname}&7."
      chat: "&7You left the territory of &2{region-displayname}&7."
```

When `type` is `title`, the `title` field is an array of `[main title, subtitle]`.

## Welcome Message

Customize the join message for players (shown on rejoin, not first join):

```yaml
# In regions.yml
welcome-message:
  enabled: true
  message: "&eWelcome back, {player}!\nThere are &6{unread-logs} &eunread mails, with &6{regions-invited} &eregions sent you an invite!"
```

Use `\n` for new lines.

## Log Private Chat

Log region chat messages to console for moderation:

```yaml
# In regions.yml
log-private-chat: true
```

Region chat can also be forwarded to Discord via webhook. See [Plugin Integrations](../Advanced/Integrations.md).

---

## Borders

Configure the region border visualization shown by `/region borders`.

```yaml
# In regions.yml
borders:
  enabled: true

  # Display type:
  # - particles: Dust particles following the player's Y position (recommended)
  # - blocks:    Fake blocks at the highest surface of the border
  # - glow:      Fake blocks with glowing effect following the player's Y position
  type: particles

  # Dust colors (type = 'particles'), RGB 0–255
  dust-colors:
    owner: [0, 255, 0]       # Green
    member: [255, 255, 0]    # Yellow
    visitor: [255, 0, 0]     # Red
    sub-area: [0, 179, 255]  # Light blue

  # Dust size (type = 'particles')
  dust-size: 3.0

  # Block type (type = 'blocks')
  block-type: GRAY_GLAZED_TERRACOTTA
```

Full usage guide: [Borders & Maps](../Usage/Borders%20and%20Maps.md).

---

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

Use the [Homestead Flags Calculator](https://tayebyassine.github.io/HomesteadFlagsCalculator) to compute bitwise flag values.

!!! info "Ignored Flags"

    These world rules flags are ignored: `liquid-flow`, `wilderness-dispensers`, `wilderness-pistons`, `wilderness-minecarts`

## Allow All Flags for Trusted Players

When a player joins a region as a trusted member, automatically allow all player flags for them.

```yaml
# In flags.yml
flags-configuration:
  allow-all-flags-for-trusted-players: true
```

- **true** (default): Trusted members get all player flags allowed on join
- **false**: Trusted members start with the region's default flag states

## Spawner Exclusion

Control whether mobs from spawners are ignored by spawn flags:

```yaml
# In flags.yml
flags-configuration:
  exclude-spawners: true
```

When `true`, spawner-spawned mobs are **not** blocked by the passive/hostile entity spawn flags.

---

## Cooldown System

Configure cooldowns per action to prevent spam. Restarting the server resets all cooldowns.

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
    value: 600                  # 10 minutes
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
    value: 5                    # 5 seconds
  region-teleport:
    ignore-operators: true
    value: 15                   # 15 seconds
  selection-tool:
    ignore-operators: false
    value: 604800               # 7 days
  war-flag-disabled:
    ignore-operators: true
    value: 129600               # 36 hours
```

| Cooldown Key                         | What It Limits                                | Default  |
|:-------------------------------------|:----------------------------------------------|:---------|
| `flag-change-state`                  | Changing any flag state                       | 3s       |
| `region-spawn-change`                | Updating the region spawn                     | 10 min   |
| `region-rename-change`               | Renaming the region                           | 10 min   |
| `region-description-change`          | Updating the description                      | 5 min    |
| `region-transfer-ownership`          | Transferring ownership (incl. sale signs)     | 7 days   |
| `region-dynamic-map-settings-change` | Map color / icon changes                      | 12 hours |
| `region-chunk-claim`                 | Claiming a chunk                              | 5s       |
| `region-chunk-unclaim`               | Unclaiming a chunk                            | 5s       |
| `region-teleport`                    | Teleporting (`/region home`, `/region visit`) | 15s      |
| `selection-tool`                     | Using the selection tool                      | 7 days   |
| `war-flag-disabled`                  | Changing the `wars` flag after a war          | 36 hours |

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

Full usage guide: [Teleportation](../Usage/Teleportation.md).

---

## Force-Loaded Chunks

Keep chunks loaded even when no players are nearby.

There is no global on/off toggle. This feature is controlled per-chunk by the player. The maximum number of force-loaded chunks per region is set in [Ranks & Limits](Ranks%20and%20Limits.md) via `max-force-loaded-chunks`.

---

## Storage Feature

Allow trusted members to share a private chest:

```yaml
# In regions.yml
storage:
  enabled: false
  size: 27     # 9, 18, 27, 36, 45, or 54
```

See [Region Storage](../Usage/Region%20Storage.md).

---

## Special Features

Miscellaneous behavior toggles grouped under `special-feat` in `regions.yml`.

### TNT Below Sea Level

Allow TNT explosions only below Y=63 in unclaimed areas (protects the surface):

```yaml
# In regions.yml
special-feat:
  tnt-explodes-only-below-sea-level: false
```

- **false** (default): TNT explodes normally everywhere
- **true**: TNT only explodes below Y=63

### Trust Acceptance System

Control whether players must accept trust invitations:

```yaml
# In regions.yml
special-feat:
  ignore-trust-acceptance-system: false
```

- **false** (default): Trusted players must run `/region accept`
- **true**: Players are trusted immediately when `/region trust` is run

### Ignore Region Protection in Disabled Worlds

Disable region protection entirely in worlds listed under `disabled-worlds-exact` / `disabled-worlds-pattern`:

```yaml
# In regions.yml
special-feat:
  ignore-region-protection-if-action-in-disabled-world: true
```

- **true** (default, recommended): Region protection does not apply in disabled worlds
- **false**: Regions in disabled worlds are still protected

### End Portal Teleport

Teleport players to their region spawn instead of world spawn when exiting the End:

```yaml
# In regions.yml
special-feat:
  teleport-players-back-to-region-spawn-when-entering-end-exit-portal: true
```

- **true** (default): Players exiting the End return to their region spawn
- **false**: Players exit to the world spawn as normal

---

## War Configuration

```yaml
# In regions.yml
wars:
  enabled: false

  # Prize bounds
  min-prize: 1000.0            # $1k
  max-prize: 1000000000.0      # $1B

  # Force keep inventory when a player dies during a war.
  # Ignores the vanilla game rule for any world.
  keep-inventory: false

  # Give the winning region owner's head to the winner
  give-head: true

  # Broadcast "A war has been declared":
  # - regions: Only sent to regions in the war
  # - server:  Sent to the entire server
  # - silent:  No message sent
  broadcast-type: "regions"

  # Flags forced to Allow during a war
  override-flags:
    - "pvp"
    - "doors"
    - "trap-doors"
    - "fence-gates"
    - "passthrough"
    - "elytra"
    - "teleport"
    - "pickup-items"
    - "take-fall-damage"
    - "containers"
    - "break-blocks"
    - "place-blocks"

  # Ownership wager wars: the loser's region transfers to the winner
  ownership-wars:
    enabled: true
    default-kills-to-win: 10        # Kills needed to win (overridable per war)
    default-timeout-minutes: 60     # Timeout in minutes (0 = no timeout)
```

Full usage guide: [Wars](../Usage/Wars.md).

---

## Economy Systems

These are configured in `regions.yml` but have dedicated documentation pages:

| System           | Config Key | Docs                                |
|:-----------------|:-----------|:------------------------------------|
| **Upkeep**       | `upkeep`   | [Upkeep](../Economy/Upkeep.md)      |
| **Member Taxes** | `taxes`    | [Member Taxes](../Economy/Taxes.md) |
| **Renting**      | `renting`  | [Rent](../Economy/Rent.md)          |
| **Selling**      | `selling`  | [Sell](../Economy/Sell.md)          |

!!! warning "Restart Required"

    Changes to `upkeep`, `taxes`, and `renting` require a **full server restart** to take effect.

