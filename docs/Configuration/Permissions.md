# Permissions

Permissions control which commands and features players can access in Homestead. This guide shows you how to set up permissions using LuckPerms, the recommended permissions plugin.

## Prerequisites

You need a permissions plugin to manage Homestead permissions. We recommend:

- **LuckPerms** (recommended, modern, feature-rich)
- UltraPermissions
- PermissionsEx
- GroupManager

!!! info "Why LuckPerms?"

    LuckPerms is actively maintained, has a user-friendly web editor, and is the most popular permissions plugin. The examples in this guide use LuckPerms.

## Quick Setup with LuckPerms

### Step 1: Install LuckPerms

1. Download LuckPerms from [their website](https://luckperms.net)
2. Place it in your `plugins` folder
3. Restart your server

### Step 2: Open the Web Editor

Run this command in-game or console:
```
/lp editor
```

This generates a web link. Click it to open the editor in your browser.

### Step 3: Select a Group

On the left side, click **"Groups"** → Select **"default"**

The "default" group is automatically given to all players when they join.

### Step 4: Add Permissions

1. Scroll to the bottom of the page
2. Find the **"Enter permission"** box
3. Type or select a permission (see below for which ones)
4. Click **"+ Add"**
5. Repeat for all permissions you want to grant

### Step 5: Save Changes

1. Click **"Save"** at the top-right
2. Type `/lp editor` again
3. Click the link
4. Your changes should now be visible on the web editor

### Step 6: Apply to Server

Changes are automatically applied! Players may need to:

- Rejoin the server
- Or use `/lp user [name] permission check` to refresh

## Recommended Permissions for All Players

Grant these permissions to your "default" group so all players can use basic Homestead features:

```
homestead.commands.region
homestead.commands.region.*
homestead.commands.claim
homestead.commands.unclaim
```

These allow players to:

- Use the main `/region` command
- Use all `/region` sub-commands (`/region menu`, `/region set [args]`...)
- Claim chunks with `/claim`
- Unclaim chunks with `/unclaim`

## All Available Permissions

### Operator Permissions

!!! danger "Operator-Only Permissions"

    **DO NOT** give these permissions to regular players, moderators, or anyone who isn't a server operator. They allow complete control over all regions and can cause serious damage if misused.

| Permission                                     | Description                           |
|------------------------------------------------|---------------------------------------|
| `homestead.operator`                           | Full Homestead operator access        |
| `homestead.admin.forceunclaim`                 | Force unclaim any region              |
| `homestead.commands.homesteadadmin`            | Access `/hsadmin` command             |
| `homestead.commands.homesteadadmin.import`     | Import data from other plugins        |
| `homestead.commands.homesteadadmin.export`     | Export data to other databases        |
| `homestead.commands.homesteadadmin.plugin`     | Manage plugin operations              |
| `homestead.commands.homesteadadmin.reload`     | Reload configuration files            |
| `homestead.commands.homesteadadmin.updates`    | Check for plugin updates              |

**What These Do:**

- `homestead.operator`: Grants access to all admin features, can edit any region
- `homestead.admin.forceunclaim`: Can delete anyone's regions
- `/hsadmin` commands: Manage imports, exports, updates, and system settings

### Region Management Permissions

Permissions for players to manage their own regions:

| Permission                             | Description                              |
|----------------------------------------|------------------------------------------|
| `homestead.region.*`                   | All region management permissions        |
| `homestead.region.create`              | Create new regions                       |
| `homestead.region.delete`              | Delete owned regions                     |
| `homestead.region.chat`                | Use region chat                          |
| `homestead.region.mail`                | Send mail to region members              |
| `homestead.region.players.trust`       | Trust players in regions                 |
| `homestead.region.players.untrust`     | Untrust players from regions             |
| `homestead.region.players.ban`         | Ban players from regions                 |
| `homestead.region.players.unban`       | Unban players from regions               |
| `homestead.region.flags.global`        | Edit global player flags                 |
| `homestead.region.flags.world`         | Edit world/environment flags             |
| `homestead.region.flags.members`       | Edit member-specific flags               |
| `homestead.region.subareas.create`     | Create sub-areas                         |
| `homestead.region.subareas.delete`     | Delete sub-areas                         |
| `homestead.region.subareas.rename`     | Rename sub-areas                         |
| `homestead.region.subareas.flags`      | Edit sub-area flags                      |
| `homestead.region.bank`                | Access region banking                    |
| `homestead.region.dynamicmaps.icon`    | Change region icon on maps               |
| `homestead.region.dynamicmaps.color`   | Change region color on maps              |
| `homestead.region.war`                 | Declare war on regions                   |

**Using Wildcards:**

- `homestead.region.*` gives **all** of the above permissions at once
- Useful for VIP ranks or players you trust completely

### Command Permissions

Permissions for individual commands:

| Permission                          | Command                                |
|-------------------------------------|----------------------------------------|
| `homestead.commands.claim`          | `/claim`                               |
| `homestead.commands.unclaim`        | `/unclaim`                             |
| `homestead.commands.region`         | `/region`, `/hs`, `/homestead`, `/rg`  |
| `homestead.commands.region.*`       | All `/region [subcommand]` commands    |

**Sub-Command Permissions:**

| Permission                            | Command                    |
|---------------------------------------|----------------------------|
| `homestead.commands.region.accept`    | `/region accept`           |
| `homestead.commands.region.auto`      | `/region auto`             |
| `homestead.commands.region.balance`   | `/region balance`          |
| `homestead.commands.region.banlist`   | `/region banlist`          |
| `homestead.commands.region.ban`       | `/region ban`              |
| `homestead.commands.region.chat`      | `/region chat`             |
| `homestead.commands.region.claimlist` | `/region claimlist`        |
| `homestead.commands.region.create`    | `/region create`           |
| `homestead.commands.region.delete`    | `/region delete`           |
| `homestead.commands.region.deny`      | `/region deny`             |
| `homestead.commands.region.deposit`   | `/region deposit`          |
| `homestead.commands.region.flags`     | `/region flags`            |
| `homestead.commands.region.help`      | `/region help`             |
| `homestead.commands.region.home`      | `/region home`             |
| `homestead.commands.region.kick`      | `/region kick`             |
| `homestead.commands.region.logs`      | `/region logs`             |
| `homestead.commands.region.mail`      | `/region mail`             |
| `homestead.commands.region.members`   | `/region members`          |
| `homestead.commands.region.menu`      | `/region menu`             |
| `homestead.commands.region.player`    | `/region player`           |
| `homestead.commands.region.borders`   | `/region borders`          |
| `homestead.commands.region.info`      | `/region info`             |
| `homestead.commands.region.rename`    | `/region rename`           |
| `homestead.commands.region.set`       | `/region set`              |
| `homestead.commands.region.subareas`  | `/region subareas`         |
| `homestead.commands.region.top`       | `/region top`              |
| `homestead.commands.region.trust`     | `/region trust`            |
| `homestead.commands.region.unban`     | `/region unban`            |
| `homestead.commands.region.untrust`   | `/region untrust`          |
| `homestead.commands.region.visit`     | `/region visit`            |
| `homestead.commands.region.war`       | `/region war`              |
| `homestead.commands.region.withdraw`  | `/region withdraw`         |

## Permission Setup Examples

### Default Players (Everyone)

```yaml
# Using LuckPerms commands:
/lp group default permission set homestead.commands.region true
/lp group default permission set homestead.commands.region.* true
/lp group default permission set homestead.commands.claim true
/lp group default permission set homestead.commands.unclaim true
```

**Result:** Players can create regions, claim chunks, trust members, and use all basic features.

### VIP Players (Paid Rank)

Additional permissions for VIP players:

```yaml
/lp group vip permission set homestead.region.war true
/lp group vip permission set homestead.region.dynamicmaps.icon true
/lp group vip permission set homestead.region.dynamicmaps.color true
```

**Result:** VIPs can declare wars and customize their region's appearance on maps.

### Moderators (Staff)

Moderators need limited admin access:

```yaml
/lp group moderator permission set homestead.commands.region.* true
/lp group moderator permission set homestead.admin.forceunclaim true
```

**Result:** Mods can manage regions and force-unclaim rule-breaking claims.

### Administrators (Full Staff)

Administrators need complete access:

```yaml
/lp group admin permission set homestead.operator true
```

**Result:** Admins have full Homestead access including `/hsadmin` commands.

## Group-Specific Limits

Homestead uses permission groups to assign claiming limits. See [Ranks and Limits](./Ranks%20and%20Limits.md) for:

- Setting chunk limits per group
- Member limits per group
- Sub-area limits per group
- Cooldowns per group

## Advanced Permission Setups

### Temporary Permissions

**Grant someone temporary admin access (1 hour):**
```yaml
/lp user PlayerName permission settemp homestead.operator true 1h
```

**Grant VIP features for 30 days:**
```yaml
/lp user PlayerName parent addtemp vip 30d
```

### Per-World Permissions

**Allow claiming only in the overworld:**
```yaml
/lp group default permission set homestead.commands.claim true world
/lp group default permission set homestead.commands.claim false world_nether
/lp group default permission set homestead.commands.claim false world_the_end
```

### Negative Permissions

**Give all region permissions except war:**
```yaml
/lp group default permission set homestead.region.* true
/lp group default permission set homestead.region.war false
```

## Checking Permissions

### Check What a Player Has

```yaml
/lp user PlayerName permission info
```

### Check What a Group Has

```yaml
/lp group default permission info
```

### Test a Specific Permission

```yaml
/lp user PlayerName permission check homestead.commands.region.create
```

## Troubleshooting

**Player can't use any commands:**
- Make sure they have `homestead.commands.region` permission
- Verify they're in a group with permissions
- Check if they're being denied by another permission plugin

**Commands show in tab-complete but don't work:**
- Permission is missing or set to `false`
- Check group inheritance (parent groups)
- Look for permission conflicts with other plugins

**Operator can't use `/hsadmin`:**
- Operators should automatically have access
- Verify they have OP status: `/op PlayerName`
- Check if permissions plugin is overriding OP status

## Quick Reference

**Essential Permissions for Playing:**
```
homestead.commands.region
homestead.commands.region.*
homestead.commands.claim
homestead.commands.unclaim
```

**Full Player Access (No Admin):**
```
homestead.region.*
homestead.commands.region.*
homestead.commands.claim
homestead.commands.unclaim
```

**Full Admin Access:**
```
homestead.operator
```

## Getting More Help

- [LuckPerms Documentation](https://luckperms.net/wiki)
- [LuckPerms Discord](https://discord.gg/luckperms)
- [Homestead Support](../Support.md)
