# Control Flags

Control flags decide what **trusted members** can manage in your region. When a player first joins as a member, every control flag starts in the Deny state, so trust alone grants nothing. The owner hands out each responsibility individually. This least-privilege default means you can, for example, let a builder claim new chunks without also giving them access to the region bank.

Because control flags only ever apply to trusted members, they cannot be set globally the way player and world flags can.

!!! tip "GUI Only"

    Control flags are managed through the Region Menu GUI, not via commands. Open the Region Menu → Players Management → Trusted Players → right-click a player head.

| Flag | Bit | Default | What It Allows |
|:-----|:---:|:-------:|:---------------|
| `trust-players` | 1 | Deny | Invite and trust new players |
| `untrust-players` | 2 | Deny | Remove trusted players |
| `ban-players` | 4 | Deny | Ban players from the region |
| `unban-players` | 8 | Deny | Unban players |
| `claim-chunks` | 16 | Deny | Claim new chunks for the region |
| `unclaim-chunks` | 32 | Deny | Unclaim chunks |
| `deposit-money` | 64 | Deny | Deposit money into the region bank |
| `withdraw-money` | 128 | Deny | Withdraw money from the region bank |
| `set-global-flags` | 256 | Deny | Change global player flags |
| `set-world-flags` | 512 | Deny | Change world flags |
| `set-member-flags` | 1024 | Deny | Change flags for individual members |
| `set-spawn` | 2048 | Deny | Change the region spawn location |
| `manage-subareas` | 4096 | Deny | Create, delete, and resize sub-areas |
| `rename-region` | 8192 | Deny | Rename the region (name + display name) |
| `set-description` | 16384 | Deny | Update the region description |
| `manage-logs` | 32768 | Deny | View and manage region logs |
| `kick-players` | 65536 | Deny | Kick players from the region |
| `set-weather-and-time` | 131072 | Deny | Change region weather and time |

## Granting Control Flags

1. Open the Region Menu.
2. Click **Players Management**.
3. Click **Trusted Players**.
4. Right-click the member's head and toggle the flags they should have.

Anything left in Deny stays unavailable to that member, and you can revoke a flag the same way you granted it. Common splits include giving a market manager `deposit-money` and `withdraw-money` without `trust-players`, or letting a builder claim land with `claim-chunks` while keeping `rename-region` with the owner.

