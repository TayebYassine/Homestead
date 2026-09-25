# Merging Regions

If you own multiple regions (or want to combine regions with another owner), you can merge them into one. All claimed chunks, sub-areas, members, and bank funds move into a single surviving region.

---

## How Merging Works

Merging is a two-step, two-party process:

1. The **source** region's owner targets the source region and sends a merge request naming the **destination** region.
2. The **destination** region's owner accepts the request.
3. On acceptance, the source region is **deleted** and everything it owns is transferred to the destination.

Only the region owner (or an operator) can send or accept a merge request. Both owners must be online at the time. The request notifies the destination owner via chat, and the request fails if they are offline.

---

## Initiate a Merge

Target the region that will be **absorbed** (the source, which will be deleted), then run:

```
/region merge [destination-region]
```

**Example:** (targeting `OldBase`, then) `/region merge NewBase`

This sends a merge request to the owner of `NewBase`. Your targeted region (`OldBase`) is the source; the named region is the destination that survives.

Requirements:

- You must be the owner of the source region (or an operator).
- The source and destination must be different regions.
- The destination owner must be online to receive the notification.
- No other merge request may already involve either region.

---

## Accept a Merge

The destination region's owner must target their region and run:

```
/region mergeaccept
```

This completes the merge: the source region's data is folded into the destination, and the source is deleted.

---

## What Happens

- All chunks from the source region transfer to the destination.
- All sub-areas from the source transfer to the destination.
- All members from the source become members of the destination.
- The source region's bank balance is deposited into the destination's bank.
- The source region is deleted permanently.
- The destination region keeps its own settings (flags, spawn, name, description, and so on).

!!! warning "Source Region Is Deleted"

    The source region cannot be recovered after the merge. Make sure you are targeting the correct region before running `/region merge`.

---

## Why Merge?

- **Reorganize** — combine scattered regions into one.
- **Simplify** — manage one large region instead of two small ones.
- **Extend protection** — easier than claiming every chunk individually when regions are adjacent.

---

## Related Commands

| Command | Description |
|:--------|:------------|
| `/region merge [destination]` | Send a merge request (targeted region becomes the source) |
| `/region mergeaccept` | Accept a pending merge (targeted region is the destination) |

Both commands require the `homestead.actions.regions.merge` permission (granted by default).

