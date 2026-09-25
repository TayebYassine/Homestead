# Permissions

Homestead separates permissions into two layers. **Command permissions** decide which commands a player can run, while **action permissions** decide what they are allowed to do inside regions. Player-facing nodes are allowed for everyone by default; admin nodes are reserved for operators. Any permission plugin can change this. [First Steps](../Getting%20Started/First%20Steps.md) shows a typical LuckPerms setup.

---

## Command Permissions

A command permission must be held before the command runs at all. Player subcommands check two nodes together: the base `homestead.commands.region` permission and the specific node listed below. Granting `homestead.commands.region` plus `homestead.commands.region.*` covers the entire table, and `/claim` and `/unclaim` have their own separate command nodes.

### Player Commands

| Permission | Command |
|:-----------|:--------|
| `homestead.commands.region.*` | All player region subcommands |
| `homestead.commands.region.accept` | `/region accept` |
| `homestead.commands.region.auto` | `/region auto` |
| `homestead.commands.region.balance` | `/region balance` |
| `homestead.commands.region.ban` | `/region ban` |
| `homestead.commands.region.banlist` | `/region banlist` |
| `homestead.commands.region.borders` | `/region borders` |
| `homestead.commands.region.chat` | `/region chat` |
| `homestead.commands.region.claimlist` | `/region claimlist` |
| `homestead.commands.region.create` | `/region create` |
| `homestead.commands.region.delete` | `/region delete` |
| `homestead.commands.region.deny` | `/region deny` |
| `homestead.commands.region.deposit` | `/region deposit` |
| `homestead.commands.region.flags` | `/region flags` |
| `homestead.commands.region.fly` | `/region fly` |
| `homestead.commands.region.help` | `/region help` |
| `homestead.commands.region.home` | `/region home` |
| `homestead.commands.region.info` | `/region info` |
| `homestead.commands.region.kick` | `/region kick` |
| `homestead.commands.region.leave` | `/region leave` |
| `homestead.commands.region.levels` | `/region levels` |
| `homestead.commands.region.logs` | `/region logs` |
| `homestead.commands.region.mail` | `/region mail` |
| `homestead.commands.region.members` | `/region members` |
| `homestead.commands.region.menu` | `/region menu` |
| `homestead.commands.region.merge` | `/region merge` |
| `homestead.commands.region.mergeaccept` | `/region mergeaccept` |
| `homestead.commands.region.player` | `/region player` |
| `homestead.commands.region.rate` | `/region rate` |
| `homestead.commands.region.rename` | `/region rename` |
| `homestead.commands.region.rewards` | `/region rewards` |
| `homestead.commands.region.set` | `/region set` |
| `homestead.commands.region.setdescription` | `/region setdescription` |
| `homestead.commands.region.setdisplayname` | `/region setdisplayname` |
| `homestead.commands.region.setmapcolor` | `/region setmapcolor` |
| `homestead.commands.region.setmapicon` | `/region setmapicon` |
| `homestead.commands.region.setmembertax` | `/region setmembertax` |
| `homestead.commands.region.setspawn` | `/region setspawn` |
| `homestead.commands.region.settime` | `/region settime` |
| `homestead.commands.region.setweather` | `/region setweather` |
| `homestead.commands.region.storage` | `/region storage` |
| `homestead.commands.region.subareas` | `/region subareas` |
| `homestead.commands.region.top` | `/region top` |
| `homestead.commands.region.trust` | `/region trust` |
| `homestead.commands.region.unban` | `/region unban` |
| `homestead.commands.region.untrust` | `/region untrust` |
| `homestead.commands.region.visit` | `/region visit` |
| `homestead.commands.region.war` | `/hs war` |
| `homestead.commands.region.withdraw` | `/region withdraw` |
| `homestead.commands.claim` | `/claim` |
| `homestead.commands.unclaim` | `/unclaim` |

### Admin Commands

Admin subcommands follow the same two-node pattern with a `homestead.commands.homesteadadmin` base, and they default to operators only. Each command is described on the [Admin Commands](Admin%20Commands.md) page.

| Permission | Command |
|:-----------|:--------|
| `homestead.commands.homesteadadmin.*` | All admin commands |
| `homestead.commands.homesteadadmin.claim` | `/hsadmin claim` |
| `homestead.commands.homesteadadmin.export` | `/hsadmin export` |
| `homestead.commands.homesteadadmin.import` | `/hsadmin import` |
| `homestead.commands.homesteadadmin.overrideflag` | `/hsadmin overrideflag` |
| `homestead.commands.homesteadadmin.plugin` | `/hsadmin plugin` |
| `homestead.commands.homesteadadmin.reload` | `/hsadmin reload` |
| `homestead.commands.homesteadadmin.transfer` | `/hsadmin transfer` |
| `homestead.commands.homesteadadmin.unclaim` | `/hsadmin unclaim` |
| `homestead.commands.homesteadadmin.updates` | `/hsadmin updates` |
| `homestead.admin.forceunclaim` | `/forceunclaim` |

---

## Action Permissions

Action permissions control what players can **do** within regions. They work together with the command permissions above: several subcommands refuse to run without a matching action. `/region visit` also requires `homestead.actions.regions.teleport`, and `/region war` also requires `homestead.actions.regions.war`. A `.*` wildcard grants every node in its group.

### Region Actions

Permissions for managing a region as a whole: creating and deleting it, chatting, mailing, banking, storage, and wars.

| Permission | Allows |
|:-----------|:-------|
| `homestead.actions.regions.*` | All region actions |
| `homestead.actions.regions.change_owner` | Transfer region ownership |
| `homestead.actions.regions.chat` | Use region chat |
| `homestead.actions.regions.create` | Create regions |
| `homestead.actions.regions.delete` | Delete regions |
| `homestead.actions.regions.deposit_bank` | Deposit to region bank |
| `homestead.actions.regions.fly` | Fly within region |
| `homestead.actions.regions.kick` | Kick players from region |
| `homestead.actions.regions.mail` | Send/read region mail |
| `homestead.actions.regions.merge` | Merge regions |
| `homestead.actions.regions.rate` | Rate regions |
| `homestead.actions.regions.storage` | Use region storage |
| `homestead.actions.regions.teleport` | Teleport to/from regions |
| `homestead.actions.regions.war` | Declare war |
| `homestead.actions.regions.withdraw_bank` | Withdraw from region bank |

### Chunk Actions

These nodes gate claiming and releasing chunks. The `/claim` and `/unclaim` commands check them directly, so they are required alongside the command permissions above.

| Permission | Allows |
|:-----------|:-------|
| `homestead.actions.regions.chunks.*` | All chunk actions |
| `homestead.actions.regions.chunks.claim` | Claim chunks |
| `homestead.actions.regions.chunks.unclaim` | Unclaim chunks |

### Player Management

Controls who can add or remove other players: trusting, banning, and their reversals.

| Permission | Allows |
|:-----------|:-------|
| `homestead.actions.regions.players.*` | All player management |
| `homestead.actions.regions.players.ban` | Ban players |
| `homestead.actions.regions.players.trust` | Trust players |
| `homestead.actions.regions.players.unban` | Unban players |
| `homestead.actions.regions.players.untrust` | Untrust players |

### Sub-Area Actions

Each sub-area operation (creating, deleting, resizing, renaming, flag changes, and its player list) has its own node below.

| Permission | Allows |
|:-----------|:-------|
| `homestead.actions.regions.subareas.*` | All sub-area actions |
| `homestead.actions.regions.subareas.create` | Create sub-areas |
| `homestead.actions.regions.subareas.delete` | Delete sub-areas |
| `homestead.actions.regions.subareas.resize` | Resize sub-areas |
| `homestead.actions.regions.subareas.players.*` | All sub-area player actions |
| `homestead.actions.regions.subareas.players.add` | Add players to sub-areas |
| `homestead.actions.regions.subareas.players.remove` | Remove players from sub-areas |
| `homestead.actions.regions.subareas.update.name` | Rename sub-areas |
| `homestead.actions.regions.subareas.update.flags.*` | All sub-area flag updates |
| `homestead.actions.regions.subareas.update.flags.global` | Update sub-area global flags |
| `homestead.actions.regions.subareas.update.flags.members` | Update sub-area member flags |

### Update Actions

Changing a region's settings is gated separately: name, display name, description, spawn, time, weather, map appearance, and flags.

| Permission | Allows |
|:-----------|:-------|
| `homestead.actions.regions.update.*` | All region update actions |
| `homestead.actions.regions.update.description` | Update region description |
| `homestead.actions.regions.update.displayname` | Update region display name |
| `homestead.actions.regions.update.flags.*` | All flag update actions |
| `homestead.actions.regions.update.flags.global` | Update global player flags |
| `homestead.actions.regions.update.flags.members` | Update member flags |
| `homestead.actions.regions.update.flags.world` | Update world flags |
| `homestead.actions.regions.update.map_color` | Update region map color |
| `homestead.actions.regions.update.map_icon` | Update region map icon |
| `homestead.actions.regions.update.name` | Rename region |
| `homestead.actions.regions.update.spawn` | Set region spawn |
| `homestead.actions.regions.update.time` | Set region time |
| `homestead.actions.regions.update.weather` | Set region weather |

---

## Special Permissions

Two nodes sit outside the normal command and action groups.

| Permission | Effect |
|:-----------|:-------|
| `homestead.operator` | :material-alert: **Full operator access**. Manage any region, bypass all restrictions |
| `homestead.group.[name]` | Assign a specific limits group (when using `permissions` method) |

The `homestead.group.[name]` node assigns a player to a limits group when the `permissions` method is used in `limits.yml`. See [Ranks & Limits](../Configuration/Ranks%20and%20Limits.md).

!!! warning "Operator Permission"

    `homestead.operator` grants complete access to all regions. Only give it to trusted admins.

