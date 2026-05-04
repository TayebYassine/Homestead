# What are Flags?

Flags are permissions that control what players and the environment can do inside your region. Homestead includes 35+ player flags and 20+ world flags, giving you complete control over your land.

## Understanding Flags

- **Player Flags** control what players (including non-members) can do in your region. 
- **World Flags** control environmental effects and mob behavior.
- **Region Control Flags** control what trusted players can manage in your region.

Each flag can be set to:

- **Allow** (`true`): The action is permitted
- **Deny** (`false`): The action is blocked

## Commands

- For everyone (non-members): `/hs flags global [flag] [state]`
- For trusted players: `/hs flags member [member] [flag] [state]`
- For environment (world): `/hs flags world [flag] [state]`

To manage control flags for a specific trusted plater, you must navigate the GUI from: **Region Menu** -> **Players Management** -> **Trusted players** -> Right-click on Player head

## Setting Default Flag Values

Default flags apply to all newly created regions. Players can change these later for their own regions.

In **flags.yml**:

```yaml
default-players-flags:
  break-blocks: false      # Deny by default
  place-blocks: false      # Deny by default
  containers: false        # Deny by default
  passthrough: true        # Allow by default
  # ... more flags
  
default-world-flags:
  passive-entity-spawn: true   # Allow by default
  hostile-entity-spawn: true   # Allow by default
  entity-grief: false          # Deny by default
  # ... more flags
```
