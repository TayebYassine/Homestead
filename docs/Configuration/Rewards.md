# Rewards

The rewards system increases accessibility for your server's players by encouraging more interaction and longer playtime.

For now, you can only reward them by chunks.

## By Each Member

For each member in a region, they will boost your region's maximum amount of chunks to claim.

In the configuration file, for each member, the region owner will get 2 chunks. For example, if the player has 3 members in their region, they will get 6 more chunks to claim!

```yaml
rewards:
  # For each member of a region:
  for-each-member:
    # Gives to the region owner the amount of chunks to claim:
    chunks: 2 # Give 2 chunks
```

## By Playtime

The longer the player stays playing on the server (also not being AFK), the more chunks they will get!

By default, this is how they will be rewarded:

|    Time    | Reward (Chunks) |
|:----------:|:---------------:|
| 30 minutes |        1        |
|   1 hour   |        2        |
|  3 hours   |        4        |
|   1 day    |        8        |   

```yaml
rewards:
  by-playtime:
    - minutes: 30
      hours: 0
      days: 0

      # If the player has 30 minutes of playtime, they will get 1 more chunks to claim!
      chunks: 1

    - minutes: 0
      hours: 1
      days: 0

      # If the player has 1 hour of playtime, they will get 2 more chunks to claim!
      chunks: 2

    - minutes: 0
      hours: 3
      days: 0

      # If the player has 3 hours of playtime, they will get 4 more chunks to claim!
      chunks: 4

    - minutes: 0
      hours: 0
      days: 1

      # If the player has 1 day of playtime, they will get 8 more chunks to claim!
      chunks: 8
```