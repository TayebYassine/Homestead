# Troubleshooting

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

Make sure groups inherit properly. VIP must inherit from default.

### /region Command Conflicts with WorldGuard

**Cause:** WorldGuard registers its own `/region` command.

**Fix:** Use aliases: `/homestead`, `/hs`, or `/rg`.

### Players Always Hit Claim Limits

**Cause:** Incorrect limits configuration.

**Fix:**

1. Check `limits.yml` — is `method` set to `static`, `groups`, or `permissions`?
2. If using `groups`, make sure **every** permission group is defined in the config
3. Undefined groups get zero limits
4. Check per-player overrides in `player-limits`

### Can't See Region Borders

**Cause:** Particle visibility issues.

**Fix:**

1. Check if resource packs disable particles
2. Check Minecraft accessibility settings
3. Try switching to `type: blocks` in `regions.yml`

### Can't Claim Chunks

**Possible causes:**

- World is disabled (`disabled-worlds-exact` or `disabled-worlds-pattern` in `regions.yml`)
- WorldGuard protects the area (`worldguard.protect-existing-regions: true`)
- Player has reached their chunk limit
- Chunks are not adjacent (if `adjacent-chunks: true`)
- Player doesn't have enough money (if `chunk-price > 0`)

### Database Errors After Switching Providers

**Fix:**

1. Verify the new provider's connection details in `config.yml`
2. Make sure the database server is running and accessible
3. Check that the database/schema exists
4. If importing, ensure both old and new providers are configured before running `/hsadmin export`

### Configuration Changes Not Taking Effect

**Fix:**

1. Run `/hsadmin reload`
2. For upkeep, taxes, and certain settings: restart the server
3. Check console for error messages during reload

### Plugin Is Lagging the Server

**Possible fixes:**

1. Increase `cache-interval` in `config.yml` (try 120–300 seconds)
2. Reduce `dynamic-maps.update-interval` (try 120 seconds)
3. Disable `metrics` if not needed
4. Check for plugin conflicts
5. Ensure you're using the correct database provider for your server size

### Migration Didn't Import Everything

**Cause:** Each plugin stores data differently. Homestead imports core claim data (locations, owners, trusted players) and uses its own defaults for flags, economy, and advanced features.

**See:** [Database Migration](../Configuration/Database Migration.md) for details on what gets imported.

## Getting More Help

If your issue isn't listed here:

- Check the [FAQ](../Getting Started/FAQ.md)
- Join the [Discord](https://discord.gg/uh7gqDY6sz)
- Open a [GitHub Issue](https://github.com/TayebYassine/Homestead/issues)
