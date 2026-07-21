# Wars

!!! warning "Experimental & Potentially Deprecated"

    The war system has been implemented but rarely used. It may become deprecated unless servers actively use it.

A war is an organized PvP conflict between regions with an economic stake.

## Declaring a War

```
/hs war declare [target-region] [prize] (war-name)
```

**Example:** `/hs war declare EnemyBase 50000 TheGreatWar`

### Prerequisites

- Your region must have the `wars` world flag set to Allow
- The target must also have `wars` enabled
- You must have enough money in your region bank for the prize

### What Happens During War

- Members of both regions can attack each other freely
- Ignored flags: Doors, trap doors, fence gates, PvP, Passthrough
- Players who are members of **both** regions are "regicides" — can attack either side

## Surrendering

```
/hs war surrender
```

The other region wins and receives the prize.

## Winning

A war ends when a player from either side dies. The opposing team wins and receives the prize.

### Configuration

```yaml
# In regions.yml
wars:
  enabled: false
  min-prize: 1000.0
  max-prize: 1000000000.0
  keep-inventory: false
  give-head: true
  broadcast-type: "regions"  # regions, server, or silent
```
