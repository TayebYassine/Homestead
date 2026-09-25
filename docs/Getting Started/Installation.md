# Installation

Installing Homestead takes only a few minutes. You will download the plugin JAR, place it in your server's `plugins/` folder, and start the server once so Homestead can generate its default configuration files. If you have not set up your server yet, check the [Prerequisites](Prerequisites.md) page first to confirm your server software, Java version, and bridge plugin are supported.

---

## Step-by-Step

1. **Download** the latest `.jar` file from one of these sources:
    - [SpigotMC](https://www.spigotmc.org/resources/121873/)
    - [Modrinth](https://modrinth.com/plugin/homestead-plugin)
    - [Hangar](https://hangar.papermc.io/TayebYassine/Homestead)

    Pick whichever source you prefer.

2. **Stop** your server completely. Never copy a new plugin into a running server — Bukkit-based servers expect plugins to be present at startup.

3. **Place** the `.jar` file in your server's `plugins/` folder.

4. **Start** the server. On the first startup, Homestead generates its default configuration files under `plugins/Homestead/`, including `config.yml`, `regions.yml`, `limits.yml`, and `flags.yml`.

!!! success "All Set"

    Homestead is now running! Head over to the [First Steps](First%20Steps.md) guide to set things up.

---

## Updating

Updating Homestead follows the same basic flow as installing: stop the server, swap the JAR, and start again.

1. **Download** the new version's `.jar` file.
2. **Stop** the server.
3. **Replace** the old `.jar` in the `plugins/` folder with the new one.
4. **Start** the server.

Your configuration files live in `plugins/Homestead/` and are not touched when you replace the JAR. Homestead also includes a configuration migrator that automatically updates older configuration files on startup, so most updates do not require you to edit your configs by hand. If a problem does appear, delete the affected configuration files and let Homestead regenerate them, then re-apply your customizations.

!!! warning "Always Backup"

    Before any update, back up your server, the `plugins/Homestead/` folder, and the database.

    Make a regular copy of your database as well, so that if a problem occurs your data is not permanently lost.
