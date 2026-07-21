# Language

Homestead includes an English (US) language file by default. You can translate it to any language or customize messages.

## Available Languages

| Language | File | Available |
|:---------|:-----|:---------:|
| English (US) | `en-US.yml` | :material-check: |
| Spanish (ES) | `es-ES.yml` | :material-check: |
| Hungarian (HU) | `hu-HU.yml` | :material-check: |

## Changing the Language

```yaml
# In config.yml
language: "en-US"
```

## Creating a Translation

### Step 1: Copy the Default

Navigate to `plugins/Homestead/languages/` and copy `en-US.yml`.

### Step 2: Rename

Rename it to your language code, e.g., `fr-FR.yml`, `de-DE.yml`.

### Step 3: Translate

Translate the message values while keeping the keys intact.

**Correct:**
```yaml
# English
90: "You created region {region-name}!"

# Spanish — CORRECT
90: "¡Has creado la región {region-name}!"
```

**Wrong — don't translate variable names:**
```yaml
90: "¡Has creado la región {nombre-de-región}!"  # WRONG
```

### Step 4: Apply

```yaml
# In config.yml
language: "fr-FR"
```

Then run `/hsadmin reload`.

## Important Rules

### Variable Names

Variables look like `{this}` and are replaced with actual data. **Do not translate them.**

### Color Codes

Minecraft color codes start with `&`:

```yaml
error-message: "&cError: You don't have permission!"
success-message: "&aSuccess! Region created."
```

MiniMessage tags (`<red>`, `<bold>`, etc.) are also supported.

## Location

```
plugins/Homestead/languages/en-US.yml
plugins/Homestead/menus/en-US.yml
```
