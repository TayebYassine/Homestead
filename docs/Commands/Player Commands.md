# Player Commands

All player commands are accessible via `/region`, `/rg`, `/hs`, or `/homestead`.

## Region Management

| Command | Description |
|:--------|:------------|
| `/region create [name]` | Create a new region |
| `/region delete confirm` | Permanently delete the targeted region |
| `/region rename [name]` | Rename the targeted region |
| `/region setdisplayname [name]` | Set display name (supports colors) |
| `/region setdescription [text]` | Set region description |
| `/region setmapcolor [color]` | Change region color on web maps |
| `/region setmapicon [icon]` | Change region icon on web maps |
| `/hs set [region]` | Set the targeted region |
| `/region menu` | Open the region management GUI |
| `/region merge [region]` | Merge another region into this one |
| `/region mergeaccept` | Accept a pending merge |

## Chunk Commands

| Command | Description |
|:--------|:------------|
| `/claim` | Claim the chunk you're standing in |
| `/claim [radius]` | Claim chunks in a radius (1–10) |
| `/unclaim` | Unclaim the chunk you're standing in |
| `/region auto` | Toggle auto-claim mode (walk to claim) |
| `/region claimlist` | View all claimed chunks in a GUI |
| `/region borders [stop]` | Toggle border display |

## Member Management

| Command | Description |
|:--------|:------------|
| `/region trust [player]` | Invite a player to your region |
| `/region untrust [player]` | Remove a player from your region |
| `/region accept [region]` | Accept a trust invitation |
| `/region deny [region]` | Deny a trust invitation |
| `/region kick [player]` | Kick a player from your region |
| `/region members` | List trusted members |
| `/hs leave confirm` | Leave the targeted region as a member |

## Ban Management

| Command | Description |
|:--------|:------------|
| `/region ban [player] [reason]` | Ban a player from your region |
| `/region unban [player]` | Unban a player |
| `/region banlist` | List banned players |

## Flag Commands

| Command | Description |
|:--------|:------------|
| `/region flags` | Open the flags GUI |
| `/region flags global [flag] (allow/deny)` | Set a global player flag |
| `/region flags member [player] [flag] (allow/deny)` | Set a member's flag |
| `/region flags world [flag] (allow/deny)` | Set a world flag |

## Teleport Commands

| Command | Description |
|:--------|:------------|
| `/region home` | Teleport to your region's spawn |
| `/region visit [region/player] (index)` | Visit a region |
| `/region setspawn` | Set the region's spawn point |
| `/region fly` | Toggle flight within the region |

## Economy Commands

| Command | Description |
|:--------|:------------|
| `/region balance [region]` | Check region bank balance |
| `/region deposit [amount/all]` | Deposit money into region bank |
| `/region withdraw [amount/all]` | Withdraw money from region bank |
| `/region setmembertax [amount]` | Set member tax amount |

## Region Info

| Command | Description |
|:--------|:------------|
| `/region info [region]` | View region details |
| `/region player [player]` | View a player's regions and stats |
| `/region top` | View region leaderboard |
| `/region rate [region]` | Rate a region (opens GUI) |
| `/region rewards` | View your reward progress |

## Communication

| Command | Description |
|:--------|:------------|
| `/region chat [message]` | Send a message to region chat |
| `/region mail [region] [message]` | Send mail to a region's members |
| `/region logs` | View region activity logs |

## Sub-Area Commands

| Command | Description |
|:--------|:------------|
| `/hs subareas create [name]` | Create a sub-area from selection |
| `/hs subareas conf [name] delete` | Delete a sub-area |
| `/hs subareas conf [name] rename [new-name]` | Rename a sub-area |
| `/hs subareas conf [name] resize` | Resize sub-area from current selection |
| `/hs subareas conf [name] flags [flag] (allow/deny)` | Set sub-area flags |
| `/hs subareas conf [name] players add [player]` | Add player to sub-area |
| `/hs subareas conf [name] players remove [player]` | Remove player from sub-area |
| `/hs subareas conf [name] players flags [player] [flag] (allow/deny)` | Set player-specific sub-area flags |

## Region Settings

| Command | Description |
|:--------|:------------|
| `/region settime [time]` | Set region time |
| `/region setweather [weather]` | Set region weather |
| `/region storage` | Open region shared storage |

## War Commands

| Command | Description |
|:--------|:------------|
| `/hs war declare [region] [prize] [name]` | Declare war on a region |
| `/hs war surrender` | Surrender an active war |

## Help

| Command | Description |
|:--------|:------------|
| `/region help [page]` | Show command help |
| `/region levels` | View region level & XP info |
