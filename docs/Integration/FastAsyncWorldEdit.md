# FastAsyncWorldEdit

**FAWE**, or **FastAsyncWorldEdit**, is a world editing plugin that includes chunk regeneration features.

## Chunk Regeneration

When a player deletes a region, Homestead can automatically regenerate all claimed chunks back to their original state. This removes all buildings, chests, and changes made to the land.

!!! danger "Data Loss"

    Regenerating chunks **permanently deletes** everything in those chunks. This cannot be undone. Make sure players understand this before enabling the feature.

```yaml
fastasyncworldedit:
  regenerate-chunks: false
```
