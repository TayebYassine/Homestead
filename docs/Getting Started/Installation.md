# Installation

## Step-by-Step

1. **Download** the latest `.jar` from one of these sources:
    - [SpigotMC](https://www.spigotmc.org/resources/121873/)
    - [Modrinth](https://modrinth.com/plugin/homestead-plugin)
    - [Hangar](https://hangar.papermc.io/TayebYassine/Homestead)

2. **Stop** your server completely.

3. **Place** the `.jar` file in your server's `plugins/` folder.

4. **Start** the server. Homestead will generate its default configuration files.

5. (Optional) Run `/hsadmin updates` to check for the latest version.

!!! success "All Set"

    Homestead is now running! Head over to the [First Steps](First Steps.md) guide to set things up.

## Post-Install Checklist

- [ ] Install and configure [Vault](https://www.spigotmc.org/resources/34315/) (or ServiceIO/VaultUnlocked)
- [ ] Set up [LuckPerms](https://luckperms.net) for permission groups
- [ ] Configure your [Database](../Configuration/Database.md)
- [ ] Review and adjust [Ranks & Limits](../Configuration/Ranks and Limits.md)
- [ ] Test claiming with a non-OP player

## Updating

To update Homestead:

1. Download the new version `.jar`
2. Stop the server
3. Replace the old `.jar` in `plugins/`
4. Start the server
5. Run `/hsadmin reload`

!!! warning "Always Backup"

    Before any update, back up your server and the `plugins/Homestead/` folder.
