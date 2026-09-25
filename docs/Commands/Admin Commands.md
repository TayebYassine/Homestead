# Admin Commands

Administrative commands manage the plugin itself: reloading configuration, moving data between databases, claiming chunks for other players, and transferring ownership. They all run through `/homesteadadmin` (short: `/hsadmin`) and are granted to operators by default. Regular players cannot use them unless you deliberately hand out the permissions listed in [Permissions](Permissions.md). `/forceunclaim` is the one admin tool that lives outside the main command, with aliases of its own.

---

## Core Administration

Routine maintenance commands for applying configuration changes and keeping the plugin up to date.

- `/hsadmin reload` — reload all configuration files
- `/hsadmin plugin` — show the plugin version and information
- `/hsadmin updates` — check for available updates

Run `/hsadmin reload` after editing any file in `plugins/Homestead` so the changes take effect without restarting the server.

## Database Operations

Homestead can move its stored regions and claims between database providers, and it can import claims created by other land-claiming plugins so players do not lose their territory when you switch.

- `/hsadmin export [provider]` — export data to a different database provider
- `/hsadmin import [plugin]` — import claims from another plugin

Supported providers are `SQLite`, `MySQL`, `MariaDB`, and `PostgreSQL`. Supported import plugins are `GriefPrevention`, `LandLord`, `ClaimChunk`, `Lands`, and `HuskClaims`.

!!! info "Console Only"

    Both commands must be run from the server console. The export runs asynchronously. Do not stop the server until the console reports that it has finished.

## Claim Management

Admins can add or remove chunks for any region without being a member of it, which is useful for fixing claims or helping players.

- `/hsadmin claim [region] [here/x] [z] (radius)` — claim chunks for a region
- `/hsadmin unclaim [region] [here/x] [z] (radius)` — unclaim chunks from a region
- `/forceunclaim` — unclaim the chunk you are standing in, even if it is not yours

Pass `here` to use the chunk you are standing in, or give chunk coordinates (`x` and `z`); the optional radius controls the size of the square.

For example:

```
/hsadmin claim MyBase here         # Claim the chunk you're standing in for MyBase
/hsadmin claim MyBase here 3       # Claim a 3-chunk radius
/hsadmin claim MyBase -123 45      # Claim a specific chunk by coordinates
```

`/forceunclaim` also responds to `/opunclaim` and `/adminunclaim`.

---

## Region Management

Two commands handle ownership transfers and server-wide flag corrections.

- `/hsadmin transfer [region] [player]` — transfer region ownership to another player
- `/hsadmin overrideflag [global/world/member] {player} [flag] (allow/deny)` — override a flag across all regions

### Flag Override Examples

```
/hsadmin overrideflag global pvp deny
/hsadmin overrideflag member Steve pvp deny
/hsadmin overrideflag world fire-spread allow
```

Unlike the per-region `/region flags` command, this override is applied across the whole server at once. It is useful when a flag was disabled in the configuration after some players had already changed its state.

---

## Permissions Reference

Each admin subcommand requires the base `homestead.commands.homesteadadmin` permission together with its own node; operators hold both by default, and the wildcard covers every specific node. `/forceunclaim` uses a separate node outside that tree.

| Permission | Command Access |
|:-----------|:---------------|
| `homestead.commands.homesteadadmin.*` | All admin commands |
| `homestead.commands.homesteadadmin.reload` | `/hsadmin reload` |
| `homestead.commands.homesteadadmin.export` | `/hsadmin export` |
| `homestead.commands.homesteadadmin.import` | `/hsadmin import` |
| `homestead.commands.homesteadadmin.plugin` | `/hsadmin plugin` |
| `homestead.commands.homesteadadmin.updates` | `/hsadmin updates` |
| `homestead.commands.homesteadadmin.claim` | `/hsadmin claim` |
| `homestead.commands.homesteadadmin.unclaim` | `/hsadmin unclaim` |
| `homestead.commands.homesteadadmin.transfer` | `/hsadmin transfer` |
| `homestead.commands.homesteadadmin.overrideflag` | `/hsadmin overrideflag` |
| `homestead.admin.forceunclaim` | `/forceunclaim` |
| `homestead.operator` | Full operator access to all regions |

