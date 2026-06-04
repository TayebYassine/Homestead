# Disable Flags

A disabled flag is a flag that cannot be modified by normal players and server operators.

Go to **flags.yml**, you will see a key called `disabled-flags`. It holds an array of flag names that are disabled.

```yaml
disabled-flags:
  - "use-bells"
  - "take-fall-damage"
  - ...
```

If you added a flag to the disabled flags list, but some players already changed the state before disabling it, apply the change using: `/hsadmin flagsoverride [global/world/member] {player} [flag] [state]`
