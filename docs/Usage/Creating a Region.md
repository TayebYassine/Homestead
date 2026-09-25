# Creating a Region

This guide walks through creating a region, claiming chunks, and verifying protection.

---

## What Is a Region?

A **region** is your protected territory made up of claimed chunks. Each chunk is a 16×16 block area from bedrock to sky limit.

Inside your region:

- Only you and trusted members can build.
- You control interactions (containers, doors, PvP, and so on).
- Your builds are safe from griefing.
- You can customize permissions with [flags](../Configuration/Flags%20Overview.md).

---

## Step 1: Create a Region

```
/region create [name]
```

**Example:** `/region create MyBase`

This creates an empty region. You start with zero chunks claimed. Claim them in the next step.

!!! success "Auto-Target"

    Your first region is automatically targeted. See [Managing a Region](Managing%20a%20Region.md#targeting-regions) for multi-region targeting.

---

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

This shows claimed chunk borders with colored particles (owners see green, members yellow, visitors red). The display turns off automatically after three minutes; run `/region borders stop` to hide it sooner. See [Borders & Maps](Borders%20and%20Maps.md) for display types and colors.

### Claiming Multiple Chunks at Once

To claim a square of chunks centered on your position:

```
/claim radius [n]
```

The value of `n` can be **1** to **10**. The side length of the square is `(2n − 1)` chunks:

- `/claim radius 1` — 1×1 (the chunk you stand in)
- `/claim radius 2` — 3×3
- `/claim radius 3` — 5×5
- `/claim radius 10` — 19×19 (maximum)

**Example:** `/claim radius 3` claims a 5×5 area of chunks centered on where you stand.

!!! info "Adjacent Claims"

    By default, every new chunk must connect to chunks the region already owns. Disable `adjacent-chunks` in `regions.yml` to allow scattered claims. See [Other Settings](../Configuration/Other%20Settings.md#adjacent-chunks).

### Claiming with the Selection Tool

You can also select a rectangular area with the [selection tool](../Configuration/Other%20Settings.md#selection-tool) (a Golden Hoe by default), then run `/claim` to claim every selected chunk in one action.

### Auto-Claim

Enable auto-claim mode and chunks are claimed automatically as you walk:

```
/region auto
```

Walk into unclaimed chunks. They are claimed automatically for your targeted region. Run `/region auto` again to disable.

### Claiming Cost

If configured, each chunk costs money from the owner's personal balance:

```yaml
# In regions.yml
chunk-price: 100.0
```

Unclaiming refunds the same price. Set `chunk-price` to `0.0` for free claims. See [Economy Setup](../Economy/Setup.md) to enable an economy.

---

## Step 3: Check Your Claims

```
/region claimlist
/region info
```

- `/region claimlist` shows how many chunks the region has claimed; add `gui` (`/region claimlist gui`) to open a GUI of every claimed chunk.
- `/region info` shows region details, including which region is currently targeted.

---

## Step 4: Unclaim Chunks

```
/unclaim
```

This releases the chunk you are standing in. You can also unclaim from the claim list GUI (`/region claimlist gui`, then click the unclaim button).

!!! warning "Sub-Area Deletion"

    Unclaiming a chunk containing a sub-area **permanently deletes** that sub-area.

!!! warning "Adjacent Chunks Rule"

    With `adjacent-chunks` enabled, you cannot unclaim a chunk if doing so would split the region into disconnected pieces.

---

## Chunk Limits

Your claim limit depends on:

1. [Ranks & Limits](../Configuration/Ranks%20and%20Limits.md) — base limits per group
2. [Rewards](../Configuration/Rewards.md) — bonus chunks for members and playtime
3. [Leveling](../Configuration/Leveling%20and%20XP.md) — bonus chunks from region XP

---

## Targeting Regions

Commands like `/claim` and `/unclaim` work on your **targeted** region. Think of it as your "active" region.

```
/hs set [region]
```

To see which region is targeted:

```
/region info
```

You can target any region you own, any region where you are a trusted member, or (as an operator) any region on the server. See [Managing a Region](Managing%20a%20Region.md#targeting-regions).

---

## Next Steps

- [Customize with flags](Managing%20a%20Region.md#managing-flags)
- [Add trusted members](Managing%20a%20Region.md#managing-members)
- [Create sub-areas](Sub-Areas.md)

