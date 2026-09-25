# Teleportation

---

## Region Home

Teleport to your region's spawn point:

```
/region home
```

The spawn must be set first with `/region setspawn`. If no spawn is set, the command reports that the region has no spawn location.

Access rules:

- The region owner and operators can always teleport to spawn.
- Non-members need **both** the `teleport-spawn` and `passthrough` player flags set to **Allow** for the region.

A teleport cooldown applies (15 seconds by default).

---

## Visiting Regions

### By Region Name (Welcome Signs Disabled)

When welcome signs are turned off, `/region visit` takes a region name and teleports you to that region's spawn:

```
/region visit [region-name]
```

**Example:** `/region visit MyBase`

The same access rules as `/region home` apply: owner/operator always, or `teleport-spawn` + `passthrough` for everyone else. The region must have a spawn set.

### By Player (Welcome Signs Enabled)

When welcome signs are enabled, visits target a player's welcome sign instead of a region spawn:

```
/region visit [player] (index)
```

**Examples:**

```
/region visit Steve
/region visit Alex 2
```

- With no arguments, `/region visit` opens a browse menu of every region that has a welcome sign; click an entry to teleport.
- The optional `index` is **0-based** and selects which of that player's welcome-sign regions to use when they own several (e.g. `0` for the first, `1` for the second). Indices outside the valid range are rejected.

Visiting by player always teleports you to the **welcome sign's location**, not the region spawn.

---

## Delayed Teleport

Teleports (home, visit, menu selections, and welcome-sign interactions) have a configurable delay to prevent combat-logging:

```yaml
# In regions.yml
delayed-teleport:
  enabled: true
  delay: 3                # Seconds
  price: 0.0              # Cost (0.0 = free)
  cancel-on-move: true    # Cancel if player moves
  ignore-operators: true  # OPs bypass delay
  boss-bar:
    enabled: true
    title: "&7Teleporting in &3{seconds}s&7..."
    color: "PURPLE"
    style: "SEGMENTED_10"
    countdown-mode: "DEPLETE"
```

Players see a boss bar countdown during the delay. Moving cancels the teleport (configurable via `cancel-on-move`). If a price is set, it is charged from the player's balance when the teleport completes; operators can bypass the delay entirely when `ignore-operators` is `true`.

Placeholders available in the boss bar title: `{seconds}` and `{location}`.

Full reference: [Delayed Teleport](../Configuration/Other%20Settings.md#delayed-teleport).

---

## Fly Mode

If the server allows it, trusted members can fly within the region:

```
/region fly
```

Toggles flight mode while you are inside a region's borders. Flight only works in a region where you are the owner or a trusted member (operators can fly anywhere).

!!! info "Permission Required"

    Requires the `homestead.actions.regions.fly` permission.

Running the command again (or leaving the region) disables flight.

---

## Teleport Flags

Two player flags control teleportation into and inside a region:

- **`teleport-spawn`** — allows non-members to teleport to the region spawn (via `/region home` or `/region visit` when welcome signs are off). Non-members also need `passthrough` for these commands to succeed.
- **`teleport`** — allows ender pearl and chorus fruit teleportation inside the region.

Both flags default to **Deny**. Set them with `/region flags global [flag] allow` or per member with `/region flags member [player] [flag] allow`. See [Flags Overview](../Configuration/Flags%20Overview.md).

---

## Cooldowns

Teleport cooldowns are configured in `regions.yml` under `cooldown`:

```yaml
# In regions.yml
cooldown:
  region-teleport:
    ignore-operators: true
    value: 15   # 15 seconds between teleports
```

- `value` — cooldown duration in seconds.
- `ignore-operators` — when `true`, operators bypass the cooldown.

Restarting the server resets all cooldowns. See the full [cooldown reference](../Configuration/Other%20Settings.md#cooldown-system) for other action cooldowns (claim, unclaim, spawn change, and so on).

---

## End Portal Return

When players exit the End, Homestead can send them to their region spawn instead of the world spawn:

```yaml
# In regions.yml
special-feat:
  teleport-players-back-to-region-spawn-when-entering-end-exit-portal: true
```

- **true** (default): Players exiting the End return to their region spawn.
- **false**: Players exit to the world spawn as normal.

