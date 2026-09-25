# Wars

!!! warning "Experimental & Potentially Deprecated"

    The war system has been implemented but rarely used. It may become deprecated unless servers actively use it.

A war is an organized PvP conflict between regions with an economic stake.

---

## Prerequisites

Before declaring a war:

- Wars must be enabled in `regions.yml` (`wars.enabled: true`).
- Your region must have the `wars` world flag set to **Allow**.
- The target region must also have `wars` set to **Allow**.
- Both region banks must hold at least the prize amount.
- An economy plugin must be installed (Vault).
- Only the region owner (or an operator) can declare.

---

## Declaring a Money War

```
/hs war declare [target-region] [prize] (war-name)
```

**Example:** `/hs war declare EnemyBase 50000 TheGreatWar`

The prize must fall within the configured `min-prize` and `max-prize` range. The war name is optional; if omitted, it defaults to `War`.

### What Happens During War

- Members of both regions can attack each other freely.
- Flags listed under `wars.override-flags` are forced to **Allow** for the duration (by default: PvP, doors, trap doors, fence gates, passthrough, elytra, teleport, pickup items, fall damage, containers, break blocks, place blocks).
- Players who are members of **both** regions are "regicides" — can attack either side.

---

## Ownership Wars

Ownership wars wager the losing region itself rather than money. The loser's region transfers to the winning team.

```
/hs war declare-ownership [target-region] [kills-to-win] [timeout-minutes] (war-name)
```

**Example:** `/hs war declare-ownership EnemyBase 10 60 BorderClash`

- `kills-to-win` — kills needed before the war ends (must be a positive integer; defaults to `10` if omitted from config).
- `timeout-minutes` — time limit in minutes; `0` means no timeout (defaults to `60` from config).
- Ownership wars must be enabled with `wars.ownership-wars.enabled: true`.

When the war ends, the losing region's ownership is transferred to the winner.

---

## During a War

```
/hs war info
```

Shows the regions involved and the prize (for money wars).

---

## Surrendering

```
/hs war surrender
```

The other region wins. In a money war, the prize is transferred from your region bank to the winner's bank. In an ownership war, your region transfers to the winner.

---

## Winning

A war ends when a player from either side dies. The opposing team wins and receives the prize (or the region, for ownership wars).

After a war ends, both regions enter a cooldown during which the `wars` flag cannot be changed (36 hours by default; see `cooldown.war-flag-disabled`).

---

## Configuration

```yaml
# In regions.yml
wars:
  enabled: false
  min-prize: 1000.0
  max-prize: 1000000000.0
  keep-inventory: false
  give-head: true
  broadcast-type: "regions"  # regions, server, or silent
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
  ownership-wars:
    enabled: true
    default-kills-to-win: 10
    default-timeout-minutes: 60
```

- `keep-inventory` — force winners and losers to keep inventory when they die during a war (overrides the world game rule).
- `give-head` — give the winning region owner a head of the losing region owner.
- `broadcast-type` — who sees the war declaration message: `regions` (only participants), `server` (everyone), or `silent` (no broadcast).
- `ownership-wars.default-kills-to-win` / `default-timeout-minutes` — defaults for ownership wars; both can be overridden per declaration.

Full configuration reference: [War Configuration](../Configuration/Other%20Settings.md#war-configuration).

