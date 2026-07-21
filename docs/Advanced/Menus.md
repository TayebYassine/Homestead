# Menus (GUI)

Homestead uses chest GUIs for visual management of regions, flags, members, and more. All menu content is customizable.

## Menu Files

Menu files are in `plugins/Homestead/menus/` — one per language.

```
plugins/Homestead/menus/en-US.yml
plugins/Homestead/menus/es-ES.yml
plugins/Homestead/menus/hu-HU.yml
```

## Customizing Titles

```yaml
# In menus/[language].yml
menu-titles:
  0: "Regions"
  1: "Region: &2{region}"
  2: "Player Flags (Global)"
  3: "World Flags"
  4: "Member Flags"
  5: "Region Members"
  6: "Sub-Areas"
  7: "Region Bank"
  8: "Region Settings"
```

Color codes are supported: `&6&lGold & Bold`

## Customizing Buttons

Each button has three properties:

### Name

```yaml
name: "&a&lConfirm"
```

### Lore (Description)

```yaml
lore:
  - "&7Click to confirm this action."
  - "&cThis cannot be undone!"
  - ""
  - "&eLeft-click to continue"
```

### Type (Item)

**Regular items:**
```yaml
type: DIAMOND
type: IRON_SWORD
type: GRASS_BLOCK
```

**Player heads (custom textures):**
```yaml
type: PLAYERHEAD-f9e986cc3ac80...c756c142
```

Get texture IDs from [Minecraft-Heads.com](https://minecraft-heads.com/).

**NexoMC custom items:**
```yaml
type: NEXO-custom_arrow_button
```

!!! warning "NexoMC No Dashes"

    Use underscores (`_`) instead of dashes (`-`) for NexoMC item IDs.

### Fallback

If a material name doesn't exist, a **BARRIER** block is shown instead.

## Reload

```
/hsadmin reload
```

Players may need to close and reopen menus to see changes.
