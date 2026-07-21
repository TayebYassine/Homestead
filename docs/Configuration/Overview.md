# Configuration Overview

Homestead stores its configuration across several YAML files in the `plugins/Homestead/` directory.

## File Reference

| File | Purpose |
|------|---------|
| `config.yml` | Core settings — debug, language, database, integrations, Discord |
| `regions.yml` | Region behavior — borders, claiming rules, economy features, cooldowns |
| `flags.yml` | Default flag values, disabled flags, world rules |
| `limits.yml` | Claim limits per rank/group/player |
| `levels.yml` | Region leveling — XP per mob kill, level-up rewards |
| `menus/*.yml` | GUI menu customization (per language) |
| `languages/*.yml` | Translatable message strings (per language) |

## How to Reload

After editing any configuration file:

```
/hsadmin reload
```

!!! warning "Restart Required"

    Changes to `upkeep`, `taxes`, and certain database settings require a **full server restart** to take effect.

## Config Migrator

Homestead automatically migrates configuration files from older versions on startup. If you see migration messages in the console, your configs are being updated to the latest format. This preserves your custom settings when possible.

## Defaults

Default configuration files are generated when Homestead starts for the first time. If you want fresh defaults:

1. Stop the server
2. Delete the configuration file you want to reset
3. Start the server — Homestead will regenerate it
4. Re-apply your customizations

---

## Quick Links

- [Database](Database.md)
- [Ranks & Limits](Ranks and Limits.md)
- [Flags Overview](Flags Overview.md)
- [Leveling & XP](Leveling and XP.md)
- [Rewards](Rewards.md)
- [Other Settings](Other Settings.md)
