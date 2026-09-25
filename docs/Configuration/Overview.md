# Configuration Overview

Homestead stores its configuration across several YAML files in the `plugins/Homestead/` directory. Each file owns a different slice of the plugin's behavior, so knowing where a setting lives saves time when you need to tune it. Everything ships with sensible defaults; you only need to edit these files when your server's needs diverge from the stock setup.

## File Reference

- **`config.yml`** — Core settings: debug mode, language and menu selection, database provider, plugin integrations (WorldGuard, FAWE, PlaceholderAPI), dynamic maps, and Discord webhooks.
- **`regions.yml`** — Region behavior: claiming rules, borders, cooldowns, economy features, wars, rewards, and teleport delays.
- **`flags.yml`** — Flags configuration: default flag values for new regions, disabled flags, and per-world rules for unclaimed chunks.
- **`limits.yml`** — Claim limits: how many regions, chunks, members, and sub-areas each player may have, assigned statically, by permission group, or by permission node.
- **`levels.yml`** — Region leveling: XP per mob kill and the bonuses unlocked at each level.
- **`menus/*.yml`** — Menu (GUI) configuration, one file per language.
- **`languages/*.yml`** — Translatable message strings, one file per language.

## How to Reload

After editing a configuration file, apply the changes without restarting the server:

```
/hsadmin reload
```

!!! warning "Restart Required"

    Changes to `upkeep`, `taxes`, `renting`, and certain database settings require a **full server restart** to take effect.

    Reloading a setting that actually needs a restart can leave the plugin running with mismatched state, which may eventually cause data corruption. When a restart is required, restart the server instead of reloading.

## Configuration Migrator

Homestead automatically migrates configuration files from older versions on startup. If you see migration messages in the console, your configs are being updated to the latest format. Custom values are preserved whenever possible, so upgrading the plugin rarely means re-editing these files by hand.

## Defaults

Default configuration files are generated the first time Homestead starts. To reset a single file to fresh defaults:

1. **Stop** the server.
2. **Delete** the configuration file you want to reset.
3. **Start** the server; Homestead regenerates it.
4. **Re-apply** your customizations.

!!! warning "Back Up Before Deleting"

    Deleting a file discards every customization inside it. Copy the file somewhere safe first so you can compare your old settings against the regenerated defaults.

---

## Quick Links

- [Database](Database.md)
- [Database Migration](Database%20Migration.md)
- [Ranks & Limits](Ranks%20and%20Limits.md)
- [Flags Overview](Flags%20Overview.md)
- [Leveling & XP](Leveling%20and%20XP.md)
- [Rewards](Rewards.md)
- [Other Settings](Other%20Settings.md)
