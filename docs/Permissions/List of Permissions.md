# List of Permissions

## Command Permissions

Each command and sub-command have its own permission.

### Player Commands (`/hs`)

- `homestead.commands.region.accept` — `/hs accept`
- `homestead.commands.region.auto` — `/hs auto`
- `homestead.commands.region.balance` — `/hs balance`
- `homestead.commands.region.ban` — `/hs ban`
- `homestead.commands.region.banlist` — `/hs banlist`
- `homestead.commands.region.borders` — `/hs borders`
- `homestead.commands.region.chat` — `/hs chat`
- `homestead.commands.region.claimlist` — `/hs claimlist`
- `homestead.commands.region.create` — `/hs create`
- `homestead.commands.region.delete` — `/hs delete`
- `homestead.commands.region.deny` — `/hs deny`
- `homestead.commands.region.deposit` — `/hs deposit`
- `homestead.commands.region.flags` — `/hs flags`
- `homestead.commands.region.fly` — `/hs fly`
- `homestead.commands.region.help` — `/hs help`
- `homestead.commands.region.home` — `/hs home`
- `homestead.commands.region.info` — `/hs info`
- `homestead.commands.region.kick` — `/hs kick`
- `homestead.commands.region.leave` — `/hs leave`
- `homestead.commands.region.levels` — `/hs levels`
- `homestead.commands.region.logs` — `/hs logs`
- `homestead.commands.region.mail` — `/hs mail`
- `homestead.commands.region.members` — `/hs members`
- `homestead.commands.region.menu` — `/hs menu`
- `homestead.commands.region.merge` — `/hs merge`
- `homestead.commands.region.mergeaccept` — `/hs mergeaccept`
- `homestead.commands.region.player` — `/hs player`
- `homestead.commands.region.rate` — `/hs rate`
- `homestead.commands.region.rename` — `/hs rename`
- `homestead.commands.region.rewards` — `/hs rewards`
- `homestead.commands.region.set` — `/hs set`
- `homestead.commands.region.setdescription` — `/hs setdescription`
- `homestead.commands.region.setdisplayname` — `/hs setdisplayname`
- `homestead.commands.region.setmapcolor` — `/hs setmapcolor`
- `homestead.commands.region.setmapicon` — `/hs setmapicon`
- `homestead.commands.region.setmembertax` — `/hs setmembertax`
- `homestead.commands.region.setspawn` — `/hs setspawn`
- `homestead.commands.region.settime` — `/hs settime`
- `homestead.commands.region.setweather` — `/hs setweather`
- `homestead.commands.region.storage` — `/hs storage`
- `homestead.commands.region.subareas` — `/hs subareas`
- `homestead.commands.region.top` — `/hs top`
- `homestead.commands.region.trust` — `/hs trust`
- `homestead.commands.region.unban` — `/hs unban`
- `homestead.commands.region.untrust` — `/hs untrust`
- `homestead.commands.region.visit` — `/hs visit`
- `homestead.commands.region.war` — `/hs war`
- `homestead.commands.region.withdraw` — `/hs withdraw`

### Admin Commands (`/homesteadadmin`)

- `homestead.commands.homesteadadmin.claim` — `/homesteadadmin claim`
- `homestead.commands.homesteadadmin.export` — `/homesteadadmin export`
- `homestead.commands.homesteadadmin.flagsoverride` — `/homesteadadmin flagsoverride`
- `homestead.commands.homesteadadmin.import` — `/homesteadadmin import`
- `homestead.commands.homesteadadmin.plugin` — `/homesteadadmin plugin`
- `homestead.commands.homesteadadmin.reload` — `/homesteadadmin reload`
- `homestead.commands.homesteadadmin.transfer` — `/homesteadadmin transfer`
- `homestead.commands.homesteadadmin.unclaim` — `/homesteadadmin unclaim`
- `homestead.commands.homesteadadmin.updates` — `/homesteadadmin updates`

### Command Wildcards

| Permission                            | Grants Access To           |
|---------------------------------------|----------------------------|
| `homestead.commands.homesteadadmin.*` | All admin commands         |
| `homestead.commands.region.*`         | All player region commands |

## Action Permissions

Easier to understand and to toggle for players or groups.

### Region Actions

- `homestead.actions.regions.change_owner` — Change Owner
- `homestead.actions.regions.chat` — Chat
- `homestead.actions.regions.create` — Create
- `homestead.actions.regions.delete` — Delete
- `homestead.actions.regions.deposit_bank` — Deposit Bank
- `homestead.actions.regions.fly` — Fly
- `homestead.actions.regions.kick` — Kick
- `homestead.actions.regions.mail` — Mail
- `homestead.actions.regions.rate` — Rate
- `homestead.actions.regions.storage` — Storage
- `homestead.actions.regions.teleport` — Teleport
- `homestead.actions.regions.update.description` — Update Description
- `homestead.actions.regions.update.displayname` — Update Display-name
- `homestead.actions.regions.update.flags.global` — Update Flags (Global)
- `homestead.actions.regions.update.flags.members` — Update Flags (Members)
- `homestead.actions.regions.update.flags.world` — Update Flags (World)
- `homestead.actions.regions.update.map_color` — Update Map Color
- `homestead.actions.regions.update.map_icon` — Update Map Icon
- `homestead.actions.regions.update.name` — Update Name
- `homestead.actions.regions.update.spawn` — Update Spawn
- `homestead.actions.regions.update.time` — Update Time
- `homestead.actions.regions.update.weather` — Update Weather
- `homestead.actions.regions.war` — War
- `homestead.actions.regions.withdraw_bank` — Withdraw Bank

### Chunk Actions

- `homestead.actions.regions.chunks.claim` — Claim
- `homestead.actions.regions.chunks.unclaim` — Unclaim

### Player Management Actions

- `homestead.actions.regions.players.ban` — Ban
- `homestead.actions.regions.players.trust` — Trust
- `homestead.actions.regions.players.unban` — Unban
- `homestead.actions.regions.players.untrust` — Untrust

### Subarea Actions

- `homestead.actions.regions.subareas.create` — Create
- `homestead.actions.regions.subareas.delete` — Delete
- `homestead.actions.regions.subareas.players.add` — Players Add
- `homestead.actions.regions.subareas.players.remove` — Players Remove
- `homestead.actions.regions.subareas.resize` — Resize
- `homestead.actions.regions.subareas.update.flags.global` — Update Flags (Global)
- `homestead.actions.regions.subareas.update.flags.members` — Update Flags (Members)
- `homestead.actions.regions.subareas.update.name` — Update Name

### Action Wildcards

| Permission                                          | Grants Access To               |
|-----------------------------------------------------|--------------------------------|
| `homestead.actions.regions.*`                       | All region actions             |
| `homestead.actions.regions.chunks.*`                | All chunk actions              |
| `homestead.actions.regions.players.*`               | All player management actions  |
| `homestead.actions.regions.subareas.*`              | All subarea actions            |
| `homestead.actions.regions.subareas.players.*`      | All subarea player actions     |
| `homestead.actions.regions.subareas.update.flags.*` | All subarea flag actions       |
| `homestead.actions.regions.update.*`                | All region update actions      |
| `homestead.actions.regions.update.flags.*`          | All region flag update actions |

## Operator Permission

- `homestead.operator` — Grants full operator access to Homestead

!!! danger "Operator Permission"

    This permission is extremely dangerous and only given to high-ranked staff members,
    like Admin or Owner. If a player has that permission, they can manage any region.
    We do not recommend any server owners give this permission to trainee or moderator groups
    unless the staff members are trusted.

