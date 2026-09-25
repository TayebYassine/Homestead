# Troubleshooting

This page is a runbook of the most common Homestead problems and how to fix them. Find the section that matches your symptom, apply the suggested fix, and reload or restart as directed. If your issue is not covered here, see [Getting More Help](#getting-more-help) at the bottom of the page.

---

## Common Issues

### Players Can't Use Any Commands

**Cause:** Permissions not configured correctly.

**Fix:** Add these permissions to your default group:

```
homestead.commands.region
homestead.commands.region.*
homestead.commands.claim
homestead.commands.unclaim
```

After saving, make sure groups inherit properly. VIP must inherit from the default group; if it does not, VIP players will be missing these permissions as well and will hit the same error.

### /region Command Conflicts with WorldGuard

**Cause:** WorldGuard registers its own `/region` command, so the two commands collide.

**Fix:** Use one of Homestead's aliases instead: `/homestead`, `/hs`, or `/rg`.

### Players Always Hit Claim Limits

**Cause:** Incorrect limits configuration.

If players cannot claim as many chunks as they should, the configuration in `limits.yml` is usually at fault. Work through these checks:

1. Check `limits.yml` — is `method` set to `static`, `groups`, or `permissions`? Each mode reads limits from a different place, so confirm the mode you intend is the one in use.
2. If using `groups`, make sure **every** permission group is defined in the config. Undefined groups get zero limits, so a player in an unlisted group cannot claim at all.
3. Check per-player overrides in `player-limits`. An override set there applies to that player specifically and may be lower than the group's limit.

### Can't See Region Borders

**Cause:** Particle visibility issues.

If `/region borders` shows nothing, the problem is usually on the client side rather than in Homestead itself. Work through these checks in order:

1. Check whether a resource pack disables or replaces particles.
2. Check Minecraft's accessibility settings, which can reduce or hide particles.
3. Try switching to `type: blocks` in `regions.yml`, which draws borders as fake blocks instead of particles.

### Can't Claim Chunks

**Possible causes:**

- The world is disabled for claiming (`disabled-worlds-exact` or `disabled-worlds-pattern` in `regions.yml`)
- WorldGuard protects the area (`worldguard.protect-existing-regions: true`)
- The player has reached their chunk limit
- The chunks are not adjacent (if `adjacent-chunks: true`)
- The player does not have enough money (if `chunk-price` is greater than `0`)

### Database Errors After Switching Providers

**Fix:**

1. Verify the new provider's connection details in `config.yml`.
2. Make sure the database server is running and accessible from the game server.
3. Check that the database/schema exists.
4. If importing, ensure both the old and new providers are configured before running `/hsadmin export`.

### Configuration Changes Not Taking Effect

**Fix:**

1. Run `/hsadmin reload` to apply the change.
2. For upkeep, taxes, and certain other settings, a full server restart is required instead.
3. Check the console for error messages during reload.

### Plugin Is Lagging the Server

**Possible fixes:**

1. Increase `cache-interval` in `config.yml` (try 120–300 seconds).
2. Reduce `dynamic-maps.update-interval` (try 120 seconds).
3. Disable `metrics` if you do not need them.
4. Check for plugin conflicts.
5. Ensure you are using the correct database provider for your server size.

### Migration Didn't Import Everything

**Cause:** Each plugin stores data differently. Homestead imports core claim data (locations, owners, trusted players) and uses its own defaults for flags, economy, and advanced features. Anything outside that core set is not carried over automatically.

**See:** [Database Migration](../Configuration/Database%20Migration.md) for details on what gets imported.

---

## Getting More Help

If your issue is not listed here:

- Check the [FAQ](../Getting%20Started/FAQ.md)
- Join the [Discord](https://discord.gg/uh7gqDY6sz)
- Open a [GitHub Issue](https://github.com/TayebYassine/Homestead/issues)
