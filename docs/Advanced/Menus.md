# Menus (GUI)

Homestead uses chest GUIs for visual management of regions, flags, members, and more. All menu content (titles, button names, lore lines, and the item shown for each button) is customizable.

---

## Menu Files

Menu files live in `plugins/Homestead/menus/`, one per language:

```
plugins/Homestead/menus/en-US.yml
plugins/Homestead/menus/es-ES.yml
plugins/Homestead/menus/hu-HU.yml
```

---

## File Structure

Menus are organized under three top-level sections:

```yaml
# Shared default buttons
defaults:
  back: ...
  prev-page: ...
  next-page: ...
  empty-slot: ...

# Per-menu configuration
menus:
  <menu-key>:
    title: "..."
    buttons:
      0: ...
      1: ...

# Internal type mappings (do NOT edit)
button-types: ...
```

### Defaults

Shared buttons used across menus:

```yaml
defaults:
  back:
    name: "&c&lBack"
    lore:
      - "&7Return to the previous menu."
    type: RED_STAINED_GLASS_PANE
  prev-page:
    name: "&9&lPrevious"
    lore:
      - "&7Go to the previous page."
    type: ARROW
  next-page:
    name: "&9&lNext"
    lore:
      - "&7Go to the next page."
    type: ARROW
  empty-slot:
    name: " "
    type: GRAY_STAINED_GLASS_PANE
```

### Buttons

Each button within a menu is defined by its **name**, **lore** (description), and **type** (item):

```yaml
menus:
  region_menu:
    title: "Region - &2{region}"
    buttons:
      0:
        name: "&a&lConfirm"
        lore:
          - "&7Click to confirm this action."
          - "&cThis cannot be undone!"
          - ""
          - "&eLeft-click to continue"
        type: DIAMOND
```

Button IDs are local to each menu and are used to position items within the GUI.

---

## Customizing Titles

Each menu has a `title` that supports color codes:

```yaml
menus:
  regions:
    title: "Regions"
  region_menu:
    title: "Region - &2{region}"
  region_global_player_flags:
    title: "Player Flags - &9Global"
  region_world_flags:
    title: "World Flags"
  region_member_flags:
    title: "Player Flags - &3{playername}&r"
  region_players_management:
    title: "Manage Players"
  sub_areas:
    title: "Sub-Areas"
  miscellaneous_settings:
    title: "Miscellaneous Settings"
```

Color codes are supported: `&6&lGold & Bold` and hex colors: `&#FF00FF`.

---

## Customizing Buttons

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

**Player head using the viewer's skin:**
```yaml
type: PLAYERHEAD-this
```

**ItemsAdder custom items:**
```yaml
type: IA-custom_sword
```

**NexoMC custom items:**
```yaml
type: NEXO-custom_arrow_button
# or
type: NEXOMC-custom_arrow_button
```

!!! warning "NexoMC No Dashes"

    Use underscores (`_`) instead of dashes (`-`) for NexoMC item IDs.

**CraftEngine custom items:**
```yaml
type: CE-custom_item
```

**Dynamic types (resolved internally):**
```yaml
type: CUSTOM::GETBYWORLD   # Resolves to the correct world head (overworld/nether/the_end)
```

### Fallback

If a material name doesn't exist, a **BARRIER** block is shown instead.

---

## Button-Types (Do Not Edit)

The `button-types` section at the bottom of the file maps world heads internally:

```yaml
# Do NOT modify these
button-types:
  world:
    overworld: PLAYERHEAD-...
    nether: PLAYERHEAD-...
    the_end: PLAYERHEAD-...
```

---

## Button-Levels (Do Not Edit)

The `button-levels` section controls the rewards displayed in the Levels menu and must stay synchronized with `config.yml`:

```yaml
# Make sure the level rewards are synchronized as in config.yml
button-levels:
  5:
    - "&a✔ 1 Chunk"
    - "&a✔ 5% Upkeep price"
  10:
    - "&a✔ 2 Chunks"
    - "&a✔ 1 Member"
  # ...
```

---

## Reload

```
/hsadmin reload
```

Players may need to close and reopen menus to see changes.

