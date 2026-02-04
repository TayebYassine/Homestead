# Permissions

In the following guide, you will know how to set permissions for a group using the LuckPerms plugin.

1. Use **/lp editor** to generate a link for the web editor of LuckPerms. Once the link is generated, click on it and open it in a web browser.
2. On your left screen, go to "Groups", and then select "default". The "default" group is the group that is automatically given to newly joined players.
3. At the bottom of the page, there is a combobox with a placeholder "Enter permissions", click on it. Select the permissions you want to allow players to use their commands, for example, if you select `homestead.commands.region.create`, this will allow players with the "default" group to use the command **/region create <args>**.
4. Once you select all permissions, click on "+ Add". But before that, if you want to disallow the permissions for the selected group, click on "true" to change it to "false", and then "+ Add".
5. You're good to go! You may need to leave the server and rejoin to update the commands for the client-side.

### Recommended Permissions

The following permissions are recommended to give to all players within the group `default`.

- `homestead.commands.region`
- `homestead.commands.region.*`
- `homestead.commands.claim`
- `homestead.commands.unclaim`

### Operators
- homestead.admin.forceunclaim → `/forceunclaim`
- homestead.commands.homesteadadmin → `/homesteadadmin`, `/hsadmin`
- homestead.commands.homesteadadmin.import → `/hsadmin import (args)`
- homestead.commands.homesteadadmin.export → `/hsadmin export (args)`
- homestead.commands.homesteadadmin.plugin → `/hsadmin plugin (args)`
- homestead.commands.homesteadadmin.reload → `/hsadmin reload (args)`
- homestead.commands.homesteadadmin.updates → `/hsadmin updates (args)`
- homestead.operator

!!! danger "Operator Permissions"

    Giving the permission `homestead.operator` or any above it to a group that doesn't have operator role (like moderator or administrator) will cause major risks
    and grief on the server.

    The permission is powerful enough to manage any region and access the administrator commands that are made only for server operators to use Homestead.

    Please, double-check all the permissions you have set before applying changes.

### Region Management
Specific permissions for players to manage their regions.

- homestead.region.\*: This will give the group with all the permissions below.
- homestead.region.create
- homestead.region.delete
- homestead.region.chat
- homestead.region.mail
- homestead.region.players.trust
- homestead.region.players.untrust
- homestead.region.players.ban
- homestead.region.players.unban
- homestead.region.flags.global
- homestead.region.flags.world
- homestead.region.flags.members
- homestead.region.subareas.create
- homestead.region.subareas.delete
- homestead.region.subareas.rename
- homestead.region.subareas.flags
- homestead.region.bank
- homestead.region.dynamicmaps.icon
- homestead.region.dynamicmaps.color
- homestead.region.war

### Commands

- homestead.commands.claim → `/claim`
- homestead.commands.unclaim → `/unclaim`
- homestead.commands.region → `/homestead`, `/hs`, `/region`, `/rg`
- homestead.commands.region.\*: This permission has the children of all the sub-commands below (`/homestead [subcmd] (args)`).
- homestead.commands.region.accept
- homestead.commands.region.auto
- homestead.commands.region.balance
- homestead.commands.region.banlist
- homestead.commands.region.ban
- homestead.commands.region.chat
- homestead.commands.region.claimlist
- homestead.commands.region.create
- homestead.commands.region.delete
- homestead.commands.region.deny
- homestead.commands.region.deposit
- homestead.commands.region.flags
- homestead.commands.region.help
- homestead.commands.region.home
- homestead.commands.region.kick
- homestead.commands.region.logs
- homestead.commands.region.mail
- homestead.commands.region.members
- homestead.commands.region.menu
- homestead.commands.region.player
- homestead.commands.region.borders
- homestead.commands.region.info
- homestead.commands.region.rename
- homestead.commands.region.set
- homestead.commands.region.subareas
- homestead.commands.region.top
- homestead.commands.region.trust
- homestead.commands.region.unban
- homestead.commands.region.untrust
- homestead.commands.region.visit
- homestead.commands.region.war
- homestead.commands.region.withdraw
