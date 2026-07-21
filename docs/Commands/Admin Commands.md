# Admin Commands

All admin commands are accessible via `/homesteadadmin` or `/hsadmin`.

## Core Administration

| Command | Description |
|:--------|:------------|
| `/hsadmin reload` | Reload all configuration files |
| `/hsadmin plugin` | Show plugin version and info |
| `/hsadmin updates` | Check for available updates |

## Database Operations

| Command | Description |
|:--------|:------------|
| `/hsadmin export [provider]` | Export data to a different database provider |
| `/hsadmin import [plugin]` | Import claims from another plugin |

**Providers:** `SQLite`, `MySQL`, `MariaDB`, `PostgreSQL`, `MongoDB`, `YAML`

**Import plugins:** `GriefPrevention`, `LandLord`, `ClaimChunk`, `Lands`, `HuskClaims`

## Claim Management

| Command | Description |
|:--------|:------------|
| `/hsadmin claim [region] [here/x] [z] (radius)` | Claim chunks for a region |
| `/hsadmin unclaim [region] [here/x] [z] (radius)` | Unclaim chunks from a region |
| `/forceunclaim` | Unclaim any chunk (even if not yours) |

**Examples:**
```
/hsadmin claim MyBase here         # Claim the chunk you're standing in for MyBase
/hsadmin claim MyBase here 3       # Claim a 3-chunk radius
/hsadmin claim MyBase -123 45      # Claim a specific chunk by coordinates
```

Aliases for `/forceunclaim`: `/opunclaim`, `/adminunclaim`

## Region Management

| Command | Description |
|:--------|:------------|
| `/hsadmin transfer [region] [player]` | Transfer region ownership to another player |
| `/hsadmin flagsoverride [global/world/member] {player} [flag] (allow/deny)` | Override a disabled flag |

### Flag Override Examples

```
/hsadmin flagsoverride global pvp deny
/hsadmin flagsoverride member Steve pvp deny
/hsadmin flagsoverride world fire-spread allow
```

This overrides the flag for all regions at once. Useful when a flag was disabled in config after some players already changed its state.

## Permissions Reference

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
| `homestead.commands.homesteadadmin.flagsoverride` | `/hsadmin flagsoverride` |
| `homestead.admin.forceunclaim` | `/forceunclaim` |
| `homestead.operator` | Full operator access to all regions |
