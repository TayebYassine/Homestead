# Disabled Flags

Disabled flags cannot be modified by players or operators — they are forced to their default values from `flags.yml`.

## Configuration

In `flags.yml`:

```yaml
disabled-flags:
  - "use-bells"
  - "take-fall-damage"
  - "wither-damage"
  - "explosion-damage"
```

## Overriding After Disabling

If some players already changed a flag's state before you disabled it, use the flagsoverride command:

```
/hsadmin flagsoverride [global/world/member] [flag] (allow/deny)
/hsadmin flagsoverride member [player] [flag] (allow/deny)
```

**Examples:**
```
/hsadmin flagsoverride global pvp deny
/hsadmin flagsoverride world fire-spread allow
/hsadmin flagsoverride member Steve break-blocks allow
```

This overrides the flag for all regions at once.
