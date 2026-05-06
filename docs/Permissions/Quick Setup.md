# Quick Setup

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