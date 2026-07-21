# Database Migration

## Migrate from Another Plugin

Homestead can import claims from other popular land claiming plugins, making it easy to switch.

### Supported Plugins

| Plugin | Notes |
|--------|-------|
| **GriefPrevention** | Imports claims + trusted players |
| **ClaimChunk** | Full support |
| **LandLord4** | Full support |
| **Lands** | Imports claims + trusted players |
| **HuskClaims** | Full support |

!!! question "Plugin Not Listed?"

    [Contact us](../Support/Support.md) to request migration support for your plugin.

### Migration Process

!!! danger "Backup First"

    Always make a full server backup before migrating.

**Step 1:** Keep your old plugin installed. Do NOT delete its data.

**Step 2:** Install Homestead and start the server.

**Step 3:** Run the import command:

```
/hsadmin import [plugin-name]
```

Example: `/hsadmin import GriefPrevention`

**Step 4:** Wait for the import to complete. Do NOT stop the server during migration.

**Step 5:** Verify the migration by checking regions with `/hs`.

**Step 6:** Once confirmed, remove the old plugin.

### What Gets Imported

- :material-check: Claimed chunk locations
- :material-check: Region owners
- :material-check: Trusted players (where supported)
- :material-close: Custom flags, economy data, advanced settings (each plugin uses different systems)

---

## Change Between Providers

To switch your Homestead database from one provider to another (e.g., SQLite → MySQL):

1. Configure the new provider's connection details in `config.yml`
2. Keep the current `provider` value unchanged
3. Run `/hsadmin reload`
4. Run `/hsadmin export [target-provider]`  
   Example: `/hsadmin export mysql`
5. Wait for the export to complete
6. Stop the server
7. Change `provider` to the new value in `config.yml`
8. Start the server — Homestead now uses the new database
