# Language

Homestead ships with an English (US) language file. Every in-game message (chat prompts, error text, menu strings, clickable confirmations) lives in a single YAML file per language under the `languages/` folder, so you can translate the plugin into any language or simply reword messages to match your server's tone.

---

## Available Languages

Three translation files are included:

- **English (US)** — `en-US.yml` (default)
- **Spanish (ES)** — `es-ES.yml`
- **Hungarian (HU)** — `hu-HU.yml`

The menu GUIs ship in the same three languages under `menus/`.

---

## Changing the Language

Set the language code in `config.yml`:

```yaml
# In config.yml
language: "en-US"
```

Then reload with `/hsadmin reload`.

---

## Creating a Translation

### Step 1: Copy the Default

Navigate to `plugins/Homestead/languages/` and copy `en-US.yml`.

### Step 2: Rename

Rename it to your language code, e.g., `fr-FR.yml`, `de-DE.yml`. Use the `language-Region` format (two-letter lowercase language, hyphen, two-letter uppercase region).

### Step 3: Translate

Translate the message values while keeping the keys intact. Keys are nested and each may be a single string or a list of lines:

**Correct:**
```yaml
# English (en-US.yml)
create:
  4:
    - "{__prefix__} &7Region '&2{0}&7' has been created successfully."
    - "{__prefix__} &7Click &9<click:run_command:/claim>here</click> &7to claim the chunk you're currently standing in, or &9<click:run_command:/hs menu>here</click> &7to open the main menu."

# Spanish (es-ES.yml): CORRECT (note single quotes around embedded apostrophes)
create:
  4:
    - '{__prefix__} &7Región ''&2{0}&7'' ha sido creada exitosamente.'
    - '{__prefix__} &7Haz clic &9<click:run_command:/claim>aquí</click> &7para reclamar el chunk en el que estás parado, o &9<click:run_command:/hs menu>aquí</click> &7para abrir el menú principal.'
```

**Wrong: don't translate variable names:**
```yaml
create:
  4:
    - '{__prefix__} &7Región ''&2{0}&7'' ha sido creada exitosamente.'
    - '{__prefix__} &7Haz clic ...'
# WRONG if you write {nombre-de-región} instead of {0}
```

**Wrong: don't renumber the keys:**
```yaml
# WRONG: the key must stay 4
  5:
    - "{__prefix__} &7Region '&2{0}&7' has been created successfully."
```

### Step 4: Apply

```yaml
# In config.yml
language: "fr-FR"
```

Then run `/hsadmin reload`.

---

## Important Rules

### Variable Names

Placeholders come in two forms and neither should be translated or renamed:

- **Positional placeholders** — `{0}`, `{1}`, `{2}`… replaced in order with runtime data (region name, price, count, etc.).
- **Named placeholders** — `{__prefix__}`, `{flag}`, `{region}`, `{amount}`, `{player}`… replaced by their specific values.

**Wrong:**
```yaml
# WRONG: {0} must stay {0}
- '{__prefix__} &7Región ''&2{nombre}&7'' ha sido creada exitosamente.'
```

### Color Codes

Minecraft color codes start with `&` and work everywhere:

```yaml
error-message: "&cError: You don't have permission!"
success-message: "&aSuccess! Region created."
```

### Formatting Tags

The language file header notes that legacy `&` codes and MiniMessage tags (`<red>`, `<bold>`, `<click:…>`, `<hover:…>`) are supported on **PaperMC and its forks** (Folia, Purpur, etc.). MiniMessage is **not** supported on Spigot. Remove any XML-like tags before using the file there.

!!! warning "Spigot Servers"

    If your server runs Spigot (not Paper), strip MiniMessage tags such as `<click:…>`, `<hover:…>`, and `<red>` from translated strings. Legacy `&` color codes remain valid.

---

## Location

```
plugins/Homestead/languages/en-US.yml
plugins/Homestead/menus/en-US.yml
```

