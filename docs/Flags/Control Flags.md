# Control Flags

Region Control Flags are flags control what trusted players can manage in your region.

## Table of Flags

| Flag Name              | Bitwise Value | Default State | Description                                           |
|------------------------|---------------|---------------|-------------------------------------------------------|
| `trust-players`        | `1`           | false         | Invite and trust players                              |
| `untrust-players`      | `2`           | false         | Untrust players                                       |
| `ban-players`          | `4`           | false         | Ban players                                           |
| `unban-players`        | `8`           | false         | Unban players                                         |
| `claim-chunks`         | `16`          | false         | Claim chunks                                          |
| `unclaim-chunks`       | `32`          | false         | Unclaim chunks                                        |
| `deposit-money`        | `64`          | false         | Deposit money to bank                                 |
| `withdraw-money`       | `128`         | false         | Withdraw money from bank                              |
| `set-global-flags`     | `256`         | false         | Manage global player flags                            |
| `set-world-flags`      | `512`         | false         | Manage world flags                                    |
| `set-member-flags`     | `1024`        | false         | Manage member flags                                   |
| `set-spawn`            | `2048`        | false         | Change region spawn location                          |
| `manage-subareas`      | `4096`        | false         | Manage sub-areas                                      |
| `rename-region`        | `8192`        | false         | Rename the region (including displayname)             |
| `set-description`      | `16384`       | false         | Update region description                             |
| `manage-logs`          | `32768`       | false         | Manage logs                                           |
| `kick-players`         | `65536`       | false         | Kick players that are wandering within region borders |
| `set-weather-and-time` | `131072`      | false         | Configure weather and time                            |
