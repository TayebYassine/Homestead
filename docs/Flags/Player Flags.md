# Player Flags

Player flags control what non-members can do inside regions. Members and owners typically have all permissions by default.

Region owners can set specific permissions for each trusted player.

## Table of Flags

| Flag Name                 | Bitwise Value | Default State | Description                                 |
|---------------------------|---------------|---------------|---------------------------------------------|
| `break-blocks`            | `1`           | false         | Break/destroy blocks                        |
| `place-blocks`            | `2`           | false         | Place blocks                                |
| `containers`              | `4`           | false         | Open chests, barrels, shulker boxes         |
| `doors`                   | `8`           | false         | Open/close doors                            |
| `trap-doors`              | `16`          | false         | Open/close trapdoors                        |
| `fence-gates`             | `32`          | false         | Open/close fence gates                      |
| `use-anvil`               | `64`          | false         | Use anvils                                  |
| `redstone`                | `128`         | false         | Interact with redstone components           |
| `levers`                  | `256`         | false         | Use levers                                  |
| `buttons`                 | `512`         | false         | Press buttons                               |
| `pressure-plates`         | `1024`        | false         | Trigger pressure plates                     |
| `use-bells`               | `2048`        | false         | Ring bells                                  |
| `trigger-tripwire`        | `4096`        | false         | Activate tripwires                          |
| `frost-walker`            | `8192`        | false         | Use frost walker boots                      |
| `harvest-crops`           | `16384`       | false         | Harvest/break crops                         |
| `block-trampling`         | `32768`       | false         | Trample farmland/turtle eggs                |
| `general-interaction`     | `65536`       | false         | General interaction catch-all               |
| `armor-stands`            | `131072`      | false         | Remove armor from armor stands              |
| `interact-entities`       | `262144`      | false         | Interact with entities                      |
| `item-frame-interaction`  | `524288`      | false         | Add/remove items from item frames           |
| `damage-passive-entities` | `1048576`     | false         | Kill passive mobs                           |
| `damage-hostile-entities` | `2097152`     | false         | Kill hostile mobs                           |
| `trade-villagers`         | `4194304`     | false         | Trade with villagers                        |
| `ignite`                  | `8388608`     | false         | Start fires with flint and steel            |
| `vehicles`                | `16777216`    | false         | Ride minecarts, horses, boats               |
| `teleport-spawn`          | `33554432`    | false         | Teleport to region spawn                    |
| `passthrough`             | `67108864`    | true          | Enter/move through region (false = private) |
| `pvp`                     | `134217728`   | false         | PvP combat                                  |
| `take-fall-damage`        | `268435456`   | true          | Fall damage (false for parkour)             |
| `teleport`                | `536870912`   | false         | Ender pearls/chorus fruit                   |
| `throw-potions`           | `1073741824`  | false         | Splash/lingering potions                    |
| `pickup-items`            | `2147483648`  | true          | Pick up dropped items                       |
| `sleep`                   | `4294967296`  | false         | Use beds                                    |
| `trigger-raid`            | `8589934592`  | false         | Start raids with Bad Omen                   |
| `elytra`                  | `17179869184` | true          | Elytra flight                               |
| `spawn-entities`          | `34359738368` | false         | Use spawn eggs                              |
