# Editing a Region

Once you've created a region, you can customize it extensively through various management commands. This guide covers how to target regions and use the full range of editing features.

## Understanding Targeted Regions

Most region management commands work on your **targeted region**. The targeted region is the region that will be affected when you run commands like `/claim`, `/unclaim`, `/region rename`, and others.

### What is a Targeted Region?

Think of the targeted region as your "active" or "selected" region. When you have multiple regions, you need to tell Homestead which one you want to work with.

**Example scenario:**

- You own two regions: "MyBase" and "MyShop"
- You want to claim more chunks for "MyShop"
- You target "MyShop"
- Now `/claim` will add chunks to "MyShop" instead of "MyBase"

### Setting a Targeted Region

**Command:**
```
/region set target [region-name]
```

**Examples:**
```
/region set target MyBase
/region set target Shop_District
/region set target Farm
```

### Who Can Target What?

**Region Owners:**

- Can target any region they own

**Trusted Members:**

- Can target regions where they're trusted
- Some commands may be restricted to owners only
- Region control flags applies to trusted players

**Operators:**

- Can target **any** region on the server
- Useful for administration and moderation

!!! info "Default Target"

    When you create your first region, it's automatically targeted. You only need to manually target when you have multiple regions.

## Managing Your Region

### Renaming Your Region

Change your region's name while keeping all settings, claims, and members intact.

**Command:**
```
/region rename [new-name]
```

**Example:**
```
/region rename BetterBaseName
```

### Setting Region Home/Spawn

Set a spawn point where players teleport when visiting your region.

**Command:**
```
/region set spawn
```

**How it works:**

- Stand where you want the home point
- Run the command
- The location is saved (coordinates + direction you're facing)
- Players use `/region visit [region]` to teleport here

### Changing Region Icon (Map Display)

If your server has web map integration (BlueMap, Squaremap, etc.), customize how your region appears.

**Command:**
```
/region set icon [icon-name]
```

### Changing Region Color (Map Display)

Customize the color of your region on web maps.

**Command:**
```
/region set mapcolor [color-name]
```

## Managing Members

### Trusting Players

Give players build permissions and access to your region.

**Command:**
```
/region trust [player]
```

**Example:**
```
/region trust Steve
/region trust Alex
```

**What trusted members can do:**

- Build and break blocks in your region
- Open containers (chests, barrels, etc.)
- Use doors, gates, buttons
- Access other protected features
- Their specific permissions depend on member flags

**Acceptance system:**

- By default, players must accept trust invitations
- They'll receive a notification
- They use `/region accept` to join
- Server may disable this for instant trust

### Untrusting Players

Remove a player's access to your region.

**Command:**
```
/region untrust [player]
```

**Example:**
```
/region untrust Steve
```

**What happens:**

- Player immediately loses all build permissions
- They can no longer access member-only features
- They can still enter if passthrough is allowed

### Banning Players

Prevent specific players from entering your region.

**Command:**
```
/region ban [player]
```

**Example:**
```
/region ban Griefer123
```

**What happens:**

- Player cannot enter your region
- If they're inside when banned, they're teleported out
- They receive a ban notification
- Overrides the passthrough flag

**When to use:**

- Remove unwanted visitors
- Prevent specific players from causing trouble
- Protect against known griefers

### Unbanning Players

Remove a ban and allow the player to enter again.

**Command:**
```
/region unban [player]
```

**Example:**
```
/region unban Griefer123
```

### Kicking Players

Temporarily remove someone from your region.

**Command:**
```
/region kick [player]
```

**Example:**
```
/region kick AnnoyingPlayer
```

**What happens:**

- Player is teleported out of your region
- They can enter again immediately
- Useful for quick removals without banning

### Viewing Members and Bans

**See trusted members:**
```
/region members
```

**See banned players:**
```
/region banlist
```

Both commands show lists in chat or open a GUI depending on server configuration.

## Managing Flags

Flags control what players can do in your region. See the [Flags](../Configuration/Flags.md) documentation for details on all available flags.

### Viewing Flags

**Open flags menu:**
```
/region flags
```

This opens a GUI where you can:

- See all flags and their current values
- Toggle flags on/off
- View flag descriptions

### Setting Flags via Command

**Command:**
```
/region flags [type] (type = member → player-name) [flag-name] [allow/deny]
```

**Examples:**
```
/region flags global pvp allow
/region flags member Steve break-blocks deny
/region flags world wars allow
```

## Economy Features

If your server has economy enabled, you can manage region finances.

### Regional Banking

**Deposit money:**
```
/region deposit [amount]
```

**Withdraw money:**
```
/region withdraw [amount]
```

**Check balance:**
```
/region balance
```

### Setting Member Taxes

Require members to pay regular fees to stay trusted.

**Command:**
```
/region set tax [amount]
```

**Example:**
```
/region set tax 500
```

All members must pay $500 per tax period (configured by server) or they'll be automatically untrusted.

**Disable taxes:**
```
/region set tax 0
```

## Communication Features

### Region Chat

Talk privately with region members.

**Toggle region chat:**
```
/region chat [message]
```

## Deleting Your Region

Permanently remove a region and all its data.

**Command:**
```
/region delete [region-name] [confirm]
```

**Example:**
```
/region delete OldBase confirm
```

!!! danger "Permanent Action"

    This **cannot be undone**! All chunks will be unclaimed, all settings lost, and all members removed. If WorldEdit chunk regeneration is enabled, your builds will be deleted too!

## Advanced Management

### Visiting Other Regions

**Command:**
```
/region visit [region-name]
```
or
```
/region visit [player-name]
```

**What happens:**

- You teleport to the region's home point
- Only works if the region allows visits
- Some regions may be private

### Viewing Region Information

**Command:**
```
/region info (region-name)
```

**Shows:**

- Region name and owner
- Number of chunks claimed
- Number of members
- Bank balance (if economy enabled)
- Other statistics

### Viewing Player Info

**Check another player's regions:**
```
/region player [username]
```

**Shows:**

- All regions they own
- Regions they're trusted in
- Their claiming limits
- Other stats

## Quick Reference

### Essential Commands

| Command                       | Purpose                   |
|-------------------------------|---------------------------|
| `/region set target [region]` | Set active region         |
| `/region rename [name]`       | Rename region             |
| `/region trust [player]`      | Add member                |
| `/region untrust [player]`    | Remove member             |
| `/region ban [player]`        | Ban from region           |
| `/region flags`               | Open flags menu           |
| `/region info`                | View region details       |
| `/region delete [region]`     | Delete region permanently |

### Economy Commands

| Command                           | Purpose              |
|-----------------------------------|----------------------|
| `/region deposit [amount]`        | Add money to bank    |
| `/region withdraw [amount]`       | Take money from bank |
| `/region balance`                 | Check bank balance   |
| `/region set tax [amount]`        | Set member taxes     |

### Communication Commands

| Command                  | Purpose             |
|--------------------------|---------------------|
| `/region chat`           | Toggle region chat  |
| `/region members`        | List members        |
| `/region banlist`        | List banned players |

## Tips for Region Management

1. **Regularly check your bank balance** if upkeep is enabled
2. **Review trusted members** periodically to ensure they're still active
3. **Update flags** as your region's purpose changes
4. **Use sub-areas** for different zones (shops, PvP, farms)
5. **Set a good home location** for visitors
6. **Communicate with members** using region chat/mail
