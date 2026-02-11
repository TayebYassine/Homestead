# FAQ - Frequently Asked Question

Search for something in the FAQ first; if none of the searches are what you wanted to see, then read the entire documentation by searching the keywords alternative to your issue.

If still nothing, then create an issue and report it. We will do our best to resolve your problem. Read more right [here](./Support.md).

## I cannot use any commands!

You have set up the Permissions plugin incorrectly.

If you're using LuckPerms, which is for sure, you did not add the recommended permissions in the default group,
and the rest of the groups don't inherit the permissions from the previous groups; for example,
the VIP group doesn't inherit the permissions from the default group.

The images below are the correct way to set up the permissions.

=== "List of Groups"

    <img src="https://i.imgur.com/zseMXj1.png">

=== "Group: VIP"

    <img src="https://i.imgur.com/gxJFT81.png">

=== "Group: Default"

    <img src="https://i.imgur.com/WE6ab46.png">

The following permissions are enough to allow players to run every Homestead command.

```
homestead.commands.region
homestead.commands.region.*
homestead.commands.claim
homestead.commands.unclaim
```

More permissions are located right [here](./Configuration/Permissions.md).

## WorldGuard replaces /region command

You can use `/homestead`, `/hs`, `/rg` as an alternative to the command `/region`.

## Always reached the limit?

This depends on how you set up Homestead; there are two methods for limits, either static or by groups (read more right [here](./Configuration/Ranks%20and%20Limits.md)).

- If you are using static, then this is OP permission-based, which means non-OP players will all have the same limits, while OP players (operators) will have different limits.
- If you have groups, you need to install a permissions plugin that supports groups.

Any failure for loading groups will use the fallback group "default." So, if you are in the parent-like VIP or Admin, yet you are still having default limit values,
then you have an issue with your permissions' plugin.

Any group that is not defined in the configuration file will always have limits of 0, which means you cannot create regions, claim chunks, and literally you can do nothing. 
**Every group created on the server by LuckPerms or by any permissions plugin MUST be registered in the configuration file.**

