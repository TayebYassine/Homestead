# Disabled Flags

Disabled flags are locked so that neither players nor operators can change them. Homestead forces them back to their default values from `flags.yml`, which protects gameplay-critical rules from accidental edits, for example, keeping a flag pinned no matter what anyone runs in chat.

Disabling a flag does not remove it; it simply stops the state from being toggled through the normal flag commands or GUI. A flag can still be Allow or Deny while disabled, depending on the default set in `flags.yml`.

## Configuration

List the flags you want to lock under `disabled-flags` in `flags.yml`:

```yaml
# In flags.yml
disabled-flags:
  - "player-glowing"
  - "take-fall-damage"
```

If you do not want any flags disabled, use an empty list:

```yaml
disabled-flags: []
```

## Overriding After Disabling

Disabling a flag does not rewrite regions that already changed it. If some regions were modified before you disabled the flag, force the intended state everywhere with:

```
/hsadmin overrideflag [global/world/member] [flag] (allow/deny)
/hsadmin overrideflag member [player] [flag] (allow/deny)
```

This overrides the flag for all regions at once.

!!! warning "Override After Every Change"

    Whenever you add a flag to `disabled-flags`, run the override command so existing regions snap to the intended state. New regions pick up the default automatically.

