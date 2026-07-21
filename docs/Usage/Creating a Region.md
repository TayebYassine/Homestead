# Creating a Region

This guide walks through creating a region, claiming chunks, and verifying protection.

## What Is a Region?

A **region** is your protected territory made up of claimed chunks. Each chunk is a 16×16 block area from bedrock to sky limit.

Inside your region:

- Only you and trusted members can build
- You control interactions (containers, doors, PvP, etc.)
- Your builds are safe from griefing
- You can customize permissions with flags

## Step 1: Create a Region

```
/region create [name]
```

**Example:** `/region create MyBase`

Creates an empty region. You start with zero chunks claimed — claim them in the next step.

!!! success "Auto-Target"

    Your first region is automatically targeted. See [Managing a Region](Managing a Region.md) for multi-region targeting.

## Step 2: Claim Chunks

Stand in a chunk and run:

```
/claim
```

The chunk is now protected. Walk to adjacent chunks and claim them to expand.

### Viewing Chunk Borders

Press **F3 + G** to see vanilla chunk borders, or use:

```
/region borders
```

This shows claimed chunk borders with colored particles (owners see green, members yellow, visitors red).

### Claiming Multiple Chunks at Once

```
/claim [radius]
```

**Example:** `/claim 2` claims a 5×5 area (radius 2 from where you stand).

### Auto-Claim

Enable auto-claim mode and chunks are claimed automatically as you walk:

```
/region auto
```

Walk into unclaimed chunks — they're claimed automatically for your targeted region. Run `/region auto` again to disable.

### Claiming Cost

If configured, each chunk costs money from your personal balance:

```yaml
# In regions.yml
chunk-price: 100.0
```

## Step 3: Check Your Claims

```
/region claimlist    # Opens a GUI showing all claimed chunks
/region info          # Shows region details
```

## Step 4: Unclaim Chunks

```
/unclaim                                   # Unclaim the chunk you're standing in
/region claimlist (click unclaim button)   # Unclaim from GUI
```

!!! warning "Sub-Area Deletion"

    Unclaiming a chunk containing a sub-area **permanently deletes** that sub-area.

## Chunk Limits

Your claim limit depends on:

1. [Ranks & Limits](../Configuration/Ranks and Limits.md) — base limits per group
2. [Rewards](../Configuration/Rewards.md) — bonus chunks for members and playtime
3. [Leveling](../Configuration/Leveling and XP.md) — bonus chunks from region XP

## Targeting Regions

Commands like `/claim` work on your **targeted** region — think of it as your "active" region.

```
/hs set [region]
```

To see which region is targeted:

```
/region info
```

## Next Steps

- [Customize with flags](Managing a Region.md)
- [Add trusted members](Managing a Region.md#managing-members)
- [Create sub-areas](Sub-Areas.md)
