# Language

Homestead includes an English (US) language file by default. You can translate it to any language or customize the messages to match your server's style.

## Changing the Language

The language setting is in `config.yml`:

```yaml
language: "en-US"  # The language file name (without .yml extension)
```

To use a different language:

1. Navigate to the `Homestead/languages/` folder
2. Copy the `en-US.yml` file
3. Rename it to your language code (e.g., `fr-FR.yml`, `es-ES.yml`, `de-DE.yml`)
4. Translate the messages in the new file
5. Update `config.yml` to use your new language file
6. Reload or restart the server

## Language File Location

Language files are stored in:
```
plugins/Homestead/languages/
```

Default file:
```
plugins/Homestead/languages/en-US.yml
```

## Common Language Codes

This is the table for common language codes if you don't know your local language code!

| Language             | Code  | Example Filename |
|----------------------|-------|------------------|
| English (US)         | en-US | en-US.yml        |
| English (UK)         | en-GB | en-GB.yml        |
| Spanish              | es-ES | es-ES.yml        |
| French               | fr-FR | fr-FR.yml        |
| German               | de-DE | de-DE.yml        |
| Portuguese (Brazil)  | pt-BR | pt-BR.yml        |
| Russian              | ru-RU | ru-RU.yml        |
| Chinese (Simplified) | zh-CN | zh-CN.yml        |
| Japanese             | ja-JP | ja-JP.yml        |
| Korean               | ko-KR | ko-KR.yml        |
| Dutch                | nl-NL | nl-NL.yml        |
| Italian              | it-IT | it-IT.yml        |
| Polish               | pl-PL | pl-PL.yml        |

## Translating the Language File

### Using AI Translation Tools

You can use AI tools like ChatGPT, Claude, or Google Translate to translate the language file:

1. **Copy the `en-US.yml` file** content
2. **Use an AI tool** with a prompt like:
   ```
   Please translate this Minecraft plugin language file to [your language].
   Keep all variable names (text in curly braces like {region-name}) unchanged.
   Only translate the text outside the variables.
   ```
3. **Paste the English content** from `en-US.yml`
4. **Save the translation** to a new file with your language code
5. **Review the translation** to ensure it sounds natural

### Manual Translation

If you prefer to translate manually:

1. Open `en-US.yml` in a text editor
2. Create a new file for your language
3. Copy each line and translate only the message part
4. **Do not translate variable names** (see below)

## Important Translation Rules

### DO NOT Change Variable Names

Variables are placeholders that get replaced with actual data. They look like `{this}`.

**Correct Translation Example (English to Spanish):**

```yaml
# English
90: "You created region {region-name}!"

# Spanish - CORRECT
90: "¡Has creado la región {region-name}!"

# Spanish - WRONG (don't do this!)
90: "¡Has creado la región {nombre-de-región}!"
```

Always keep these **exactly as they appear** in the English file.

### Keep Color Codes

Minecraft color codes start with `&` followed by a number or letter:

```yaml
# Keep the color codes (&c, &a, &7, etc.)
error-message: "&cError: You don't have permission!"
success-message: "&aSuccess! Region created."
info-message: "&7Region: &2{region-name}"
```

**Common Color Codes:**

- `&c` - Red (errors)
- `&a` - Green (success)
- `&e` - Yellow (warnings)
- `&7` - Gray (info)
- `&2` - Dark green (emphasis)
- `&b` - Aqua (links)

Translate the text but keep the color codes in place.

### Keep Formatting Codes

These special codes control text appearance:

- `&l` - **Bold**
- `&o` - *Italic*
- `&n` - <u>Underline</u>
- `&m` - ~~Strikethrough~~
- `&r` - Reset formatting

## Testing Your Translation

After creating a new language file:

1. Set the language in `config.yml`:
   ```yaml
   language: "es-ES"  # Your language code
   ```

2. Reload the plugin:
   ```
   /hsadmin reload
   ```

3. Test various commands to see your translations:
   ```
   /region help
   /region create TestRegion
   /region info
   ```

4. Check for:
    - Missing translations (shows the English version)
    - Broken variables (shows the variable name instead of data)
    - Formatting issues (weird colors or symbols)

## Common Issues

**Variables don't work (showing {region-name} instead of actual name):**

- Check that you didn't modify the variable name
- Make sure curly braces `{}` are still present
- Verify the variable spelling is exact

**Missing translations:**

- Compare your file with `en-US.yml` to find missing keys
- Every message key must be present even if not translated

**Colors don't work:**

- Ensure color codes start with `&` not `§`
- Check that you didn't accidentally delete the `&`

**Plugin won't load language file:**

- Verify the filename matches what's in `config.yml`
- Check that the file is in `plugins/Homestead/languages/`
- Make sure the file extension is `.yml`
- Validate YAML syntax using an online YAML validator

## Customizing Messages

Even if you're keeping English, you can customize messages to match your server's style!
