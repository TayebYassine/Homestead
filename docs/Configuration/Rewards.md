# Rewards

The rewards system increases accessibility for your server's players by encouraging more interaction and longer playtime.

For now, you can only reward them by chunks.

## By Each Member

For each member in a region, they will boost your region's maximum amount of chunks to claim.

In the configuration file, for each member, the region owner will get 2 chunks. For example, if the player has 3 members in their region, they will get 6 more chunks to claim!

```yaml
rewards:
  for-each-member: # Given to the owner when someone joins a region.
    chunks: 2 # 0 = disable
    subareas: 1 # 0 = disable
```

## By Playtime

The longer the player stays playing on the server (also not being AFK), the more chunks they will get!

```yaml
rewards:
  by-playtime: # Compares highest play-time; keep chunks and subareas rising as time rises.
    - minutes: 30
      hours: 0
      days: 0
      chunks: 1
      subareas: 1
    - minutes: 0
      hours: 3
      days: 0
      chunks: 2
      subareas: 2
    - ...
```