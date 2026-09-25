# First Steps

With Homestead installed, you are ready to configure the basics: permissions, claim limits, and your first protected region. Work through the sections in order. Each one builds on the previous, and by the end you will have a working region with a trusted friend and a feel for the menus.

---

## 1. Configure Basic Permissions

As of version 6.x, [LuckPerms](https://luckperms.net/) is no longer required to configure permissions for players. If you do use LuckPerms (for example, to manage groups or restrict which sub-commands each group can run), run the following commands to grant the recommended permissions to the `default` group:

```
/lp group default permission set homestead.commands.region true
/lp group default permission set homestead.commands.region.* true
/lp group default permission set homestead.commands.claim true
/lp group default permission set homestead.commands.unclaim true
```

Without these, players may find that Homestead commands silently do nothing when they run them.

!!! tip "Disable Specific Sub-commands"

    `homestead.commands.region.*` grants access to all player region sub-commands.

    To disallow specific sub-commands, add `homestead.commands.region.<sub-command>` for the sub-command you want to block and set its value to **false**.

For the full list of permission nodes, see [Permissions](../Commands/Permissions.md).

---

## 2. Set Claim Limits

Claim limits control how many regions, chunks, and members each player can have. By default, Homestead uses `static` limits: non-operator players all receive the same limits, while server operators receive a larger set. This means the plugin works out of the box with no permissions plugin involved, and you can tune the numbers to fit your server's play style.

Edit `plugins/Homestead/limits.yml`:

```yaml
# In limits.yml
limits:
  method: 'static'           # 'static', 'groups', or 'permissions'
  static:
    non-op:
      regions: 1             # Max regions per player
      chunks-per-region: 4   # Max claimed chunks per region
      members-per-region: 2  # Max trusted members per region
```

The values above are the defaults: each player gets one region of up to four chunks and can trust two friends. Raise them for a more generous server, or lower them for a tighter one. When you are ready to differentiate player groups (VIP, staff, and so on), switch to the `groups` or `permissions` method. See [Ranks & Limits](../Configuration/Ranks%20and%20Limits.md).

Then run `/hsadmin reload` to apply your changes without restarting the server.

---

## 3. Test as a Player

Before you invite anyone else, walk through the claiming flow yourself exactly as a player would. This confirms that your permissions and limits are set correctly and gives you a feel for what new players will experience.

1. **Create a region** with `/hs create MyBase`. This creates an empty region and makes it your target.
2. **Walk to a chunk** you want to protect.
3. **Claim it** with `/claim`.
4. **Walk to an adjacent chunk** and claim it too. By default, Homestead requires claimed chunks to stay connected (you can change this in `regions.yml`).
5. **Check your claims** with `/hs claimlist` to see every chunk in the region.
6. **Check your region borders** with `/hs borders` to visualize where the region begins and ends.

If any of these steps fail, revisit section 1 to confirm your permissions, or section 2 to confirm your limits allow more than one chunk.

---

## 4. Trust a Friend

A region is yours alone until you invite someone. To let a friend build with you, run the following command to send them an invitation:

```
/hs trust PlayerName
```

After you have sent the invitation, tell your friend to accept it:

```
/hs accept MyBase
```

Your friend is now a trusted member of your region! You can now fine-tune what they are allowed to do (for example, letting them break blocks and open containers):

```
/hs flags member PlayerName break-blocks allow
/hs flags member PlayerName containers allow
```

See [Flags Overview](../Configuration/Flags%20Overview.md) to learn what each flag controls.

---

## 5. Menus

Homestead includes chest GUI menus so you can configure your region with your mouse instead of typing commands. Run the following command to open the main menu:

```
/hs menu
```

From the menu you can view region info, manage members, adjust flags, and more. Menu titles and buttons are customizable. See [Menus](../Advanced/Menus.md) for details.

