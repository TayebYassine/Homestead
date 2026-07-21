# Merging Regions

If you own multiple regions, you can merge them into one. This combines all claimed chunks under a single region.

## Initiate a Merge

```
/region merge [source-region]
```

**Example:** `/region merge MyBase`

This starts the merge process. The source region's owner must accept.

## Accept a Merge

```
/region mergeaccept
```

## What Happens

- All chunks from the source region are transferred to the target region
- The source region is deleted
- All settings from the target region remain (flags, members, bank, etc.)
- Members from the source region become members of the target region

!!! warning "Owner Required"

    Both regions must be owned by you. You cannot merge regions owned by different players.

## Why Merge?

- **Reorganize**: Combine scattered regions into one
- **Simplify**: Manage one large region instead of two small ones
- **Extend protection**: Easier than claiming every chunk individually

## Related Commands

| Command | Description |
|:--------|:------------|
| `/region merge [source]` | Initiate a merge |
| `/region mergeaccept` | Accept a pending merge request |
