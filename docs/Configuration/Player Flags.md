# Player Flags

Player flags control what **non-members** can do inside your region. Members and owners bypass these restrictions.

| Flag | Bit | Default | Description                          |
|:-----|:---:|:-------:|:-------------------------------------|
| `break-blocks` | 1 | Deny | Break or destroy blocks              |
| `place-blocks` | 2 | Deny | Place blocks                         |
| `containers` | 4 | Deny | Open chests, barrels, shulker boxes  |
| `doors` | 8 | Deny | Open or close doors                  |
| `trap-doors` | 16 | Deny | Open or close trapdoors              |
| `fence-gates` | 32 | Deny | Open or close fence gates            |
| `use-anvil` | 64 | Deny | Use anvils                           |
| `redstone` | 128 | Deny | Interact with redstone components    |
| `levers` | 256 | Deny | Use levers                           |
| `buttons` | 512 | Deny | Press buttons                        |
| `pressure-plates` | 1024 | Deny | Trigger pressure plates              |
| `use-bells` | 2048 | Deny | Ring bells                           |
| `trigger-tripwire` | 4096 | Deny | Activate tripwires                   |
| `frost-walker` | 8192 | Deny | Use frost walker enchantment         |
| `harvest-crops` | 16384 | Deny | Harvest or break crops               |
| `block-trampling` | 32768 | Deny | Trample farmland or turtle eggs      |
| `general-interaction` | 65536 | Deny | Catch-all for other interactions     |
| `armor-stands` | 131072 | Deny | Remove armor from armor stands       |
| `interact-entities` | 262144 | Deny | Interact with entities               |
| `item-frame-interaction` | 524288 | Deny | Add/remove items from item frames    |
| `damage-passive-entities` | 1048576 | Deny | Kill passive mobs                    |
| `damage-hostile-entities` | 2097152 | Deny | Kill hostile mobs                    |
| `trade-villagers` | 4194304 | Deny | Trade with villagers                 |
| `ignite` | 8388608 | Deny | Start fires with flint and steel     |
| `vehicles` | 16777216 | Deny | Ride minecarts, horses, boats        |
| `teleport-spawn` | 33554432 | Deny | Teleport to region spawn             |
| `passthrough` | 67108864 | **Allow** | Enter or move through the region     |
| `pvp` | 134217728 | Deny | PvP combat                           |
| `take-fall-damage` | 268435456 | **Allow** | Fall damage (Deny for parkour areas) |
| `teleport` | 536870912 | Deny | Ender pearls / chorus fruit          |
| `throw-potions` | 1073741824 | Deny | Splash or lingering potions          |
| `pickup-items` | 2147483648 | **Allow** | Pick up dropped items                |
| `sleep` | 4294967296 | Deny | Use beds                             |
| `trigger-raid` | 8589934592 | Deny | Start raids with Bad Omen            |
| `elytra` | 17179869184 | **Allow** | Elytra flight                        |
| `spawn-entities` | 34359738368 | Deny | Use spawn eggs                       |
| `punch-sulfur-cubes` | 68719476736 | Deny | Punch sulfur cubes (26.2+)           |

??? tip "Passthrough = Private Region"

    Setting `passthrough` to **Deny** makes your region private — players cannot walk through it.
