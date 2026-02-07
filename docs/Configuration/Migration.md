# Migration

Switching from another land claiming plugin to Homestead? The built-in migration system makes it easy to transfer all your existing claims without losing data.

## Supported Plugins

Homestead can import data from these popular claiming plugins:

| Plugin          | Status               | Notes                                       |
|-----------------|----------------------|---------------------------------------------|
| GriefPrevention | :material-check:     | Fully supported, can import trusted players |
| ClaimChunk      | :material-check:     | Fully supported                             |
| LandLord4       | :material-check:     | Fully supported                             |
| Lands           | :material-check:     | Fully supported, can import trusted players |
| HuskClaims      | :material-check:     | Fully supported                             |

!!! info "Need Support for Another Plugin?"

    If your claiming plugin isn't listed, please [contact the developers](../Support.md) to request migration support.

## Migration Process

!!! tip "Test on a Backup First"

    If possible, test the migration on a development server or backup before doing it on your production server. This lets you verify everything works without risk.

Follow these steps to safely migrate your data:

### Step 1: Prepare for Migration

!!! danger "Do Not Delete Anything Yet"

    Do **not** uninstall your old claiming plugin or delete its data files. The migration tool needs these files to import your claims.

1. Create a **full backup** of your server
2. Backup your old plugin's data folder
3. Don't let players play on your server

### Step 2: Install Homestead

1. Download the latest Homestead version
2. Place it in your `plugins` folder
3. Restart the server

### Step 3: Run the Migration Command

Start your server with **both** plugins installed (old + Homestead).

Run the migration command:
```
/hsadmin import [plugin]
```

Replace `[plugin]` with the name of your old plugin:

- **GriefPrevention**: `/hsadmin import GriefPrevention`
- **ClaimChunk**: `/hsadmin import ClaimChunk`
- **LandLord4**: `/hsadmin import LandLord`
- **Lands**: `/hsadmin import Lands`
- **HuskClaims**: `/hsadmin import Huskclaims`

### Step 4: Wait for Completion

The migration process will begin importing all claims. This may take several minutes depending on how many claims exist.

!!! warning "Don't Stop the Server"

    Do not stop the server or reload the plugin while migration is running. This could corrupt your data.

### Step 5: Verify the Migration

After the migration completes:

1. Check if all regions were imported:
   ```
   /region top
   ```

2. Visit a few claim locations and verify:
    - Boundaries are correct
    - Permissions work properly
    - Trusted players were transferred

3. Check console logs for any errors or warnings

### Step 6: Remove the Old Plugin

Only after confirming everything works:

1. Stop the server
2. Remove the old claiming plugin JAR from `plugins` folder
3. (Optional) Keep the old plugin's data folder as backup
4. Start the server

## What Gets Migrated?

✓ Claimed chunk locations  
✓ Region owners  
✓ Trusted members/players (not for all plugins)

✗ Permissions, groups, roles... 

!!! info "Why Not Everything?"

    Each plugin uses different systems for flags, economy, and advanced features. Homestead imports core claim data and uses its own defaults for everything else.
