# Menus

Menus (also called GUIs - Graphical User Interfaces) provide a visual way for players to interact with Homestead without typing commands. Menus appear as chest inventories with clickable items.

## Overview

Homestead uses menus for:

- Viewing and managing regions
- Configuring flags
- Managing trusted members
- Accessing region settings
- And much more!

All menu customization is done in the `menus.yml` file located in the Homestead plugin folder.

## Customizing Menu Titles

Menu titles appear at the top of the chest inventory interface.

### Configuration

In `menus.yml`, find the `menu-titles` section:

```yaml
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

### Using Color Codes

You can use Minecraft color codes to style titles:

```yaml
menu-titles:
  1: "&6&lRegion: &2&l{region}"  # Gold, bold "Region:", green, bold region name
  2: "&c&lPlayer Flags"          # Red, bold title
  5: "&b» &fMembers &b«"         # Aqua arrows, white text
```

**Common Color Codes:**

- `&0` - Black
- `&1` - Dark Blue
- `&2` - Dark Green
- `&3` - Dark Aqua
- `&4` - Dark Red
- `&5` - Dark Purple
- `&6` - Gold
- `&7` - Gray
- `&8` - Dark Gray
- `&9` - Blue
- `&a` - Green
- `&b` - Aqua
- `&c` - Red
- `&d` - Light Purple
- `&e` - Yellow
- `&f` - White

**Formatting Codes:**

- `&l` - **Bold**
- `&o` - *Italic*
- `&n` - <u>Underline</u>
- `&m` - ~~Strikethrough~~
- `&r` - Reset formatting

## Customizing Menu Buttons

Buttons are the clickable items in menus. Each button has a name, description (lore), and appearance (item type).

### Configuration

In `menus.yml`, find the `buttons` section:

```yaml
buttons:
  0:
    name: "&cBack"
    lore:
      - "&7Return to the previous menu."
    type: RED_STAINED_GLASS_PANE
    
  1:
    name: "&9Previous"
    lore:
      - "&7Go to the previous page."
    type: ARROW
    
  2:
    name: "&9Next"
    lore:
      - "&7Go to the next page."
    type: ARROW
```

### Button Properties

**name:**  
The button's display name. Supports color codes.

```yaml
name: "&a&lConfirm"  # Green, bold "Confirm"
```

**lore:**  
A list of description lines shown when hovering over the button.

```yaml
lore:
  - "&7Click to confirm this action."
  - "&cThis cannot be undone!"
  - ""
  - "&eLeft-click to continue"
```

**type:**  
The Minecraft item/block to use as the button. See below for options.

## Button Types

### Using Regular Items

You can use any Minecraft material name:

```yaml
type: DIAMOND          # A diamond item
type: IRON_SWORD       # An iron sword
type: OAK_DOOR         # An oak door
type: GRASS_BLOCK      # A grass block
type: ENCHANTED_BOOK   # An enchanted book
```

**Finding Material Names:**

1. Visit the [Minecraft Wiki](https://minecraft.wiki/)
2. Search for the item you want
3. Look for the "Data values" or "ID" section
4. Use the namespaced ID without `minecraft:` (e.g., `DIAMOND_SWORD`, not `minecraft:diamond_sword`)

### Using Player Heads

Player heads let you use custom textures for buttons, creating unique icons.

**Format:**
```yaml
type: PLAYERHEAD-<texture>
```

Where `<texture>` is the texture ID from Minecraft-Heads.com.

### Getting Custom Player Head Textures

**Step-by-Step Guide:**

1. **Visit [Minecraft-Heads.com](https://minecraft-heads.com/)**

2. **Search** for a head design you like (e.g., "grass block", "warning sign", "arrow up")

3. **Select** the head you want to use

4. **Scroll down** to find the "Minecraft URL" section

5. **Copy the URL**, which looks like:
   ```
   http://textures.minecraft.net/texture/f9e986ccac3dc804f1bebe054dfb3e800480b7e08b2e7c6a86c84621c756c142
   ```

6. **Extract the texture ID** (everything after `/texture/`):
   ```
   f9e986ccac3dc804f1bebe054dfb3e800480b7e08b2e7c6a86c84621c756c142
   ```

7. **Use it in your button**:
   ```yaml
   type: PLAYERHEAD-f9e986ccac3dc804f1bebe054dfb3e800480b7e08b2e7c6a86c84621c756c142
   ```

!!! tip "Popular Head Categories"

    - **Arrows**: Up, down, left, right navigation
    - **Symbols**: Checkmarks, X marks, warning signs
    - **Blocks**: Realistic versions of Minecraft blocks
    - **Letters**: Individual alphabet letters
    - **Numbers**: 0-9 for displays
    - **Misc**: Gears, keys, locks, coins

### Fallback Behavior

If a material name doesn't exist (wrong spelling, version mismatch, etc.), Homestead automatically uses a **BARRIER** block as a fallback.

!!! warning "Version Compatibility"

    If you're using a newer Minecraft version than Homestead's API version, some materials might not be recognized. For example, if Minecraft 1.21.9 adds "COPPER_GOLEM_STATUE", Homestead might not recognize it until updated. It will show as a barrier block instead.

## Reloading Changes

After editing `menus.yml`, reload the configuration:

```
/hsadmin reload
```

Players may need to close and reopen menus to see changes.

## Best Practices

1. **Use consistent colors** for similar actions (green for confirm, red for cancel)
2. **Keep lore concise**; players should quickly understand the button
3. **Test thoroughly** after making changes
4. **Back up** `menus.yml` before major changes
5. **Use player heads** for unique, professional-looking menus
6. **Consider color-blind players**; don't rely only on color to convey meaning
