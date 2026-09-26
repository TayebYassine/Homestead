# Player Commands

Every in-game Homestead command runs through `/region` and its aliases `/rg`, `/hs`, and `/homestead`, so `/region create MyBase` and `/hs create MyBase` do exactly the same thing. Only `/claim` and `/unclaim` stand alone as separate commands. In the syntax below, parentheses mark optional values; everything else must be provided.

Most subcommands act on your **targeted region**, the region selected with `/hs set [region]`. Each subcommand has its own permission node, listed on the [Permissions](Permissions.md) page, and [Managing a Region](../Usage/Managing%20a%20Region.md) covers the whole workflow.

---

## Region Management

Regions are the foundation of Homestead: each one owns a group of claimed chunks along with its members, flags, bank, and settings. This section covers creating regions, renaming them, setting their descriptions and map appearance, and choosing which region later commands target.

- `/region create [name]` — create a new region
- `/region delete confirm` — permanently delete the targeted region
- `/region rename [name]` — rename the targeted region
- `/region setdisplayname [name]` — set the region's display name; supports color codes
- `/region setdescription [text]` — set the region's description
- `/region setmapcolor [color]` — change the region's color on web maps
- `/region setmapicon [icon]` — change the region's icon on web maps
- `/hs set [region]` — select the targeted region for later commands
- `/region menu` — open the region management GUI
- `/region merge [region]` — merge another region into this one
- `/region mergeaccept` — accept a pending merge

Merging takes two steps: `/region merge` proposes the merge and `/region mergeaccept` completes it, and both regions must belong to the same owner. See [Merging Regions](../Usage/Merging%20Regions.md) for the full flow.

## Chunk Commands

Claiming is how a region grows. Each claimed chunk adds protection and counts toward the owner's chunk limit. Claim the chunk beneath your feet, expand outward with a radius, or enable auto-claim so chunks are claimed simply by walking into them.

- `/claim` — claim the chunk you are standing in
- `/claim radius (n)` — claim a square of chunks around you (`n` is 1–10; area = `(2n−1)²`)
- `/unclaim` — release the chunk you are standing in
- `/region auto` — toggle auto-claim mode, which claims chunks as you walk
- `/region claimlist` — view every claimed chunk in a GUI
- `/region borders (stop)` — toggle the border display; `stop` ends it

!!! info "Adjacent Claims"

    By default, new chunks must connect to chunks the region already owns. Disable `adjacent-chunks` to allow scattered claims. See [Other Settings](../Configuration/Other%20Settings.md#adjacent-chunks).

Border appearance (particles, blocks, or glow) is configured on the [Borders & Maps](../Usage/Borders%20and%20Maps.md) page.

## Member Management

Trusted members can build and interact inside a region according to its flags. Trusting sends an invitation that the other player must accept, and the owner can remove members with `/region untrust` or `/region kick`. A member can also leave voluntarily with `/region leave confirm`.

- `/region trust [player]` — invite a player to your region
- `/region untrust [player]` — remove a player from your region
- `/region accept [region]` — accept a trust invitation
- `/region deny [region]` — deny a trust invitation
- `/region kick [player]` — kick a player from your region
- `/region members` — list the region's trusted members
- `/hs leave confirm` — leave the targeted region as a member

!!! info "Invitation Required"

    Trust invitations must be accepted with `/region accept` unless the trust acceptance system is turned off in `regions.yml`. See [Other Settings](../Configuration/Other%20Settings.md#trust-acceptance-system).

## Ban Management

Region bans keep specific players out of a single territory without affecting the rest of the server. The ban list belongs to the region and is managed with the three commands below.

- `/region ban [player] (reason)` — ban a player from your region, optionally with a reason
- `/region unban [player]` — lift a player's region ban
- `/region banlist` — list the players banned from the region

---

## Flag Commands

Flags decide what visitors, members, and the world are allowed to do inside a region, from PvP and block breaking to fire spread. Open the GUI to browse them, or set individual flags from chat with the `global`, `member`, and `world` variants.

- `/region flags` — open the flags GUI
- `/region flags global [flag] (allow/deny)` — set a flag for every player in the region
- `/region flags member [player] [flag] (allow/deny)` — set a flag for one member
- `/region flags world [flag] (allow/deny)` — set a world flag for the region

Leave off `allow`/`deny` to toggle the flag's current state. The flag types themselves are explained in [Flags Overview](../Configuration/Flags%20Overview.md).

## Teleport Commands

These commands move players between region spawn points and control flight inside a region.

- `/region home` — teleport to your region's spawn
- `/region visit [region/player] (index)` — visit a region
- `/region setspawn` — set the region's spawn point
- `/region fly` — toggle flight within the region

Teleports can require a countdown before they happen. See [Teleportation](../Usage/Teleportation.md) and the [delayed teleport settings](../Configuration/Other%20Settings.md#delayed-teleport).

## Economy Commands

Every region has a shared bank that funds upkeep and member taxes. Use these commands to check the balance, move money in and out, and set what members owe.

- `/region balance [region]` — check the region bank balance
- `/region deposit [amount/all]` — deposit money into the region bank
- `/region withdraw [amount/all]` — withdraw money from the region bank
- `/region setmembertax [amount]` — set the member tax amount

Deposits and withdrawals require an economy plugin. See [Economy Setup](../Economy/Setup.md). Bank behavior is covered in [Regional Bank](../Economy/Bank.md) and taxes in [Member Taxes](../Economy/Taxes.md).

---

## Region Info

Inspection tools for looking up regions, comparing statistics, and collecting rewards.

- `/region info [region]` — view a region's details
- `/region player [player]` — view a player's regions and stats
- `/region top` — view the region leaderboard
- `/region rate [region]` — rate a region (opens a GUI)
- `/region rewards` — view your reward progress

## Communication

Private channels for talking to a region's members, leaving messages for them, and reviewing what has happened there.

- `/region chat [message]` — send a message to region chat
- `/region mail [region] [message]` — send mail to a region's members
- `/region logs` — view the region's activity logs

See [Region Chat & Mail](../Usage/Region%20Chat%20and%20Mail.md) for details.

## Sub-Area Commands

Sub-areas carve out zones inside a region with their own flags and player lists, useful for shared bases, farms, or shops. Create one from a selection with the [selection tool](../Configuration/Other%20Settings.md#selection-tool), then manage it through the `conf` subcommands below. All of these commands operate on the targeted region.

- `/hs subareas create [name]` — create a sub-area from the current selection
- `/hs subareas tool` — receive the selection tool
- `/hs subareas conf [name] delete` — delete a sub-area
- `/hs subareas conf [name] rename [new-name]` — rename a sub-area
- `/hs subareas conf [name] resize` — resize the sub-area from the current selection
- `/hs subareas conf [name] flags [flag] (allow/deny)` — set sub-area flags
- `/hs subareas conf [name] players add [player]` — add a player to the sub-area
- `/hs subareas conf [name] players remove [player]` — remove a player from the sub-area
- `/hs subareas conf [name] players flags [player] [flag] (allow/deny)` — set player-specific sub-area flags

The whole feature can be turned off in `regions.yml`. See [Sub-Areas](../Usage/Sub-Areas.md).

---

## Region Settings

Environment controls and the region's shared chest.

- `/region settime [time]` — set the region's time
- `/region setweather [weather]` — set the region's weather
- `/region storage` — open the region's shared storage

Changing time and weather requires the `set-weather-and-time` control flag (see [Control Flags](../Configuration/Control%20Flags.md)), and shared storage must be enabled in `regions.yml` first. See [Region Storage](../Usage/Region%20Storage.md).

## War Commands

Wars let two regions fight over a prize, with special rules forced on during the war. A war is declared by your targeted region against the region you name, and either side can back down at any time.

- `/hs war declare [region] [prize] [name]` — declare war on a region
- `/hs war surrender` — surrender an active war

!!! warning "Wars Disabled by Default"

    Wars must be enabled in `regions.yml` before these commands work. See [Wars](../Usage/Wars.md).

## Help

Two commands for finding your way around and checking progress.

- `/region help [page]` — show command help
- `/region levels` — view the region's level and XP progress

Region leveling is explained in [Leveling & XP](../Usage/Leveling%20and%20XP.md).

