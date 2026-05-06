# Control Flags

Region Control Flags are flags control what trusted players can manage in your region.

Once a member joins the region, all the flags are in Deny state.

## Table of Flags

| Flag Name              | Bitwise Value  | Default State  | Description                                           |
|------------------------|:--------------:|:--------------:|-------------------------------------------------------|
| `trust-players`        |      `1`       |      Deny      | Invite and trust players                              |
| `untrust-players`      |      `2`       |      Deny      | Untrust players                                       |
| `ban-players`          |      `4`       |      Deny      | Ban players                                           |
| `unban-players`        |      `8`       |      Deny      | Unban players                                         |
| `claim-chunks`         |      `16`      |      Deny      | Claim chunks                                          |
| `unclaim-chunks`       |      `32`      |      Deny      | Unclaim chunks                                        |
| `deposit-money`        |      `64`      |      Deny      | Deposit money to bank                                 |
| `withdraw-money`       |     `128`      |      Deny      | Withdraw money from bank                              |
| `set-global-flags`     |     `256`      |      Deny      | Manage global player flags                            |
| `set-world-flags`      |     `512`      |      Deny      | Manage world flags                                    |
| `set-member-flags`     |     `1024`     |      Deny      | Manage member flags                                   |
| `set-spawn`            |     `2048`     |      Deny      | Change region spawn location                          |
| `manage-subareas`      |     `4096`     |      Deny      | Manage sub-areas                                      |
| `rename-region`        |     `8192`     |      Deny      | Rename the region (including displayname)             |
| `set-description`      |    `16384`     |      Deny      | Update region description                             |
| `manage-logs`          |    `32768`     |      Deny      | Manage logs                                           |
| `kick-players`         |    `65536`     |      Deny      | Kick players that are wandering within region borders |
| `set-weather-and-time` |    `131072`    |      Deny      | Configure weather and time                            |
