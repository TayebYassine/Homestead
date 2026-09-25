# Bedrock Edition Setup

Bedrock (mobile/console) players can use Homestead's full feature set via **BedrockGUI**, which converts all 27 Homestead GUI menus into native Bedrock forms.

---

## How It Works

1. **Geyser** translates Minecraft Java <-> Bedrock protocol
2. **Floodgate** lets Bedrock players join without a Java account
3. **BedrockGUI** intercepts Homestead's GUI calls and renders them as native Bedrock form modals
4. **Homestead Addon** maps every Homestead menu (regions, flags, members, sub-areas, etc.) to a Bedrock-compatible form

Java players are unaffected. They continue using the standard chest GUIs.

---

## Requirements

You need all four components:

- **Geyser** — [geysermc.org](https://geysermc.org/)
- **Floodgate** — [geysermc.org](https://geysermc.org/)
- **BedrockGUI** (Paper) — [SpigotMC](https://www.spigotmc.org/resources/119592/), file `BedrockGUI-Paper-2.0.9.jar`
- **Homestead Addon** — [GitHub Releases](https://github.com/pintux98/BedrockGUI/releases), file `BedrockGUI-HomesteadAddon-2.0.9.jar`

---

## Installation

### Step 1: Install Geyser & Floodgate

Follow the [Geyser setup guide](https://geysermc.org/wiki/). Both Geyser and Floodgate must be installed and working before BedrockGUI.

### Step 2: Install BedrockGUI

1. Download `BedrockGUI-Paper-2.0.9.jar`
2. Place it in your `plugins/` folder
3. Restart the server

### Step 3: Install the Homestead Addon

1. Download `BedrockGUI-HomesteadAddon-2.0.9.jar`
2. Place it in your `plugins/` folder
3. Restart the server

!!! success "JARs Required"

    Both `BedrockGUI-Paper-2.0.9.jar` **and** `BedrockGUI-HomesteadAddon-2.0.9.jar` must be present.

---

## What Gets Converted

All 27 Homestead GUI menus are converted to native Bedrock forms, covering:

- **Regions** — region list and region settings
- **Members** — member list, add/remove members
- **Invites** — pending invites
- **Bans** — ban list, ban/unban players
- **Flags** — global flags, world flags, member flags, control flags
- **Sub-Areas** — sub-area list, create/manage sub-areas
- **Chunks** — claimed chunks list
- **Map** — map color picker, map icon picker
- **Progression** — levels & XP, rewards
- **Logs** — region activity logs
- **Rating** — rate a region
- **Other** — welcome signs, weather/time settings, top regions

Nothing is lost in translation. Every Homestead feature remains accessible from Bedrock.

---

## Configuration

### BedrockGUI Config

The Homestead addon has its own configuration in `plugins/BedrockGUI/addons/homestead/config.yml`:

```yaml
# Whether to replace Homestead's built-in GUIs with Bedrock forms
integrated-gui: true

# Whether to register hs_* actions for custom menu builders
register-actions: true
```

- **integrated-gui**:
    - **true** (default): Bedrock players see native forms instead of chest GUIs
    - **false**: Set this if you want to build custom forms using the `hs_*` actions
- **register-actions**:
    - **true** (default): Registers action handlers so custom BedrockGUI menus can trigger Homestead operations
    - **false**: Custom `hs_*` actions are not registered

Reload after changing config:

```
/bedrockgui reload
```

### Custom Menu Actions

If `integrated-gui` is disabled and `register-actions` is enabled, you can build custom Bedrock forms that invoke Homestead actions. The addon registers a set of `hs_*` actions accessible from any BedrockGUI menu configuration.

---

## Commands

| Command | Permission | Description |
|:--------|:-----------|:------------|
| `/bedrockgui reload` | `bedrockgui.admin` | Reload BedrockGUI configuration |
| `/bedrockgui open <menu> [player]` | `bedrockgui.admin` | Open a Bedrock form for a player |
| `/homesteadaddon` | — | Homestead addon admin command |

---

## Notes

- Java players continue using standard chest GUIs — only Bedrock players see native forms
- The addon intercepts `/region` (and aliases: `/rg`, `/hs`, `/homestead`) when they would open a GUI
- All Homestead features are accessible — nothing is lost in translation
- For custom menu design, see the [BedrockGUI Web Designer](https://designer.pintux.org/) and [Documentation](https://pintux.gitbook.io/pintux-support/)

