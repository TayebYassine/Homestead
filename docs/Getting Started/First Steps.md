# First Steps

## 1. Configure Basic Permissions

All players need access to basic Homestead commands. With [LuckPerms](https://luckperms.net):

```
/lp group default permission set homestead.commands.region.* true
/lp group default permission set homestead.commands.claim true
/lp group default permission set homestead.commands.unclaim true
```

!!! tip "Wildcard"

    `homestead.commands.region.*` grants access to all player region subcommands.

## 2. Set Claim Limits

Edit `plugins/Homestead/limits.yml`:

```yaml
limits:
  method: 'static'
  static:
    non-op:
      regions: 1
      chunks-per-region: 4
      members-per-region: 2
```

Then run `/hsadmin reload`.

## 3. Test as a Player

1. **Create a region**: `/region create MyBase`
2. **Walk to a chunk** you want to protect
3. **Claim it**: `/claim`
4. **Walk to an adjacent chunk** and claim it too
5. **Check your claim**: `/region claimlist`

## 4. Trust a Friend

```
/region trust PlayerName
```

If the trust acceptance system is enabled (default), the player must run:

```
/region accept MyBase
```

## 5. Customize Protection

Open the flags GUI:

```
/region flags
```

Toggle which actions non-members can perform in your region — PvP, block breaking, container access, and more.

---

## What's Next?

- [Create a proper region](</Usage/Creating a Region.md>) with all the details
- [Configure ranks & limits](../Configuration/Ranks and Limits.md) for different player groups
- [Set up economy](../Economy/Setup.md) for banking, upkeep, and renting
- [Customize flags](../Configuration/Flags Overview.md) to fine-tune protection
