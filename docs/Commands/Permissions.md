# Permissions

## Command Permissions

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

| Permission | Command |
|:-----------|:--------|
| `homestead.commands.homesteadadmin.*` | All admin commands |
| `homestead.commands.homesteadadmin.claim` | `/hsadmin claim` |
| `homestead.commands.homesteadadmin.export` | `/hsadmin export` |
| `homestead.commands.homesteadadmin.flagsoverride` | `/hsadmin flagsoverride` |
| `homestead.commands.homesteadadmin.import` | `/hsadmin import` |
| `homestead.commands.homesteadadmin.plugin` | `/hsadmin plugin` |
| `homestead.commands.homesteadadmin.reload` | `/hsadmin reload` |
| `homestead.commands.homesteadadmin.transfer` | `/hsadmin transfer` |
| `homestead.commands.homesteadadmin.unclaim` | `/hsadmin unclaim` |
| `homestead.commands.homesteadadmin.updates` | `/hsadmin updates` |
| `homestead.admin.forceunclaim` | `/forceunclaim` |

## Action Permissions

Action permissions control what players can **do** within regions.

### Region Actions

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

| Permission | Allows |
|:-----------|:-------|
| `homestead.actions.regions.chunks.*` | All chunk actions |
| `homestead.actions.regions.chunks.claim` | Claim chunks |
| `homestead.actions.regions.chunks.unclaim` | Unclaim chunks |

### Player Management

| Permission | Allows |
|:-----------|:-------|
| `homestead.actions.regions.players.*` | All player management |
| `homestead.actions.regions.players.ban` | Ban players |
| `homestead.actions.regions.players.trust` | Trust players |
| `homestead.actions.regions.players.unban` | Unban players |
| `homestead.actions.regions.players.untrust` | Untrust players |

### Sub-Area Actions

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

## Special Permissions

| Permission | Effect |
|:-----------|:-------|
| `homestead.operator` | :material-alert: **Full operator access** — manage any region, bypass all restrictions |
| `homestead.group.[name]` | Assign a specific limits group (when using `permissions` method) |

!!! danger "Operator Permission"

    `homestead.operator` grants complete access to all regions. Only give this to trusted admins.
