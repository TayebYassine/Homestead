# Database Migration

Switching to Homestead does not mean starting from scratch. Homestead can import claims from other popular land claiming plugins, so your players keep the land they already built on. The importer reads the old plugin's data, converts it into Homestead regions, and writes it to the configured database.

## Supported Plugins

| Plugin              | Notes                              |
|---------------------|------------------------------------|
| **GriefPrevention** | Imports claims and trusted players |
| **ClaimChunk**      | Imports claims                     |
| **LandLord4**       | Imports claims                     |
| **Lands**           | Imports claims and trusted players |
| **HuskClaims**      | Imports claims                     |

!!! question "Plugin Not Listed?"

    [Contact us](../Support/Support.md) to request migration support for your plugin.

    Requesting support for a new plugin may take anywhere from days to weeks, depending on whether that plugin exposes a public API.

## Migration Process

!!! danger "Backup First"

    Always make a full server backup before migrating.

    We will not be held liable if you make a mistake and have no server backup.

**Step 1:** Leave your old plugin installed. Do **not** delete its data. The importer reads it directly.

**Step 2:** Install Homestead and start the server once so it generates its configuration files.

**Step 3:** Run the import command:

```
/hsadmin import [plugin-name]
```

For example:

```
/hsadmin import GriefPrevention
```

**Step 4:** Wait for the import to finish. Do **not** stop the server while the migration is running, or the import may be left half-written.

**Step 5:** Verify the result by checking regions with `/hs` or `/hs top`.

**Step 6:** Once you are satisfied, uninstall the old plugin.

### What Gets Imported

- :material-check: Claimed chunk locations
- :material-check: Region owners
- :material-check: Trusted players (if the source plugin supports them)
- :material-check: Economy data (if the source plugin supports it)
- :material-close: Custom flags, advanced settings, sub-areas, and similar plugin-specific features

!!! question "Why not everything?"

    Converting every plugin's feature set into Homestead is a serious and complex task. The importer focuses on the data that matters most (claims, owners, and trust), so expect to reconfigure everything else by hand.

    Announce the migration to your players in advance so nobody is surprised when flags or settings reset.

## Change Between Providers

To move your Homestead data from one provider to another (for example, from SQLite to MySQL), export from the current database first and then switch:

1. **Configure** the new provider's connection details in `config.yml`.
2. **Leave** the current `provider` value unchanged for now.
3. **Run** `/hsadmin reload`.
4. **Run** `/hsadmin export [provider]`.
5. **Wait** for the export to complete.
6. **Stop** the server.
7. **Change** `provider` to the new value in `config.yml`.
8. **Start** the server. Homestead now reads from the new database.

