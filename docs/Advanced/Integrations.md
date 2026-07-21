# Plugin Integrations

Homestead integrates with several popular plugins to extend functionality.

## WorldGuard

Prevent players from claiming chunks inside WorldGuard-protected regions.

```yaml
# In config.yml
worldguard:
  protect-existing-regions: true
```

Useful for protecting spawn areas, PvP arenas, and event zones from player claims.

## FastAsyncWorldEdit (FAWE)

Regenerate chunks back to their original state when a region is deleted — removes all builds, chests, and terrain changes.

```yaml
# In config.yml
fastasyncworldedit:
  regenerate-chunks: false
```

!!! danger "Irreversible"

    Regenerated chunks are permanently deleted. This cannot be undone.

!!! warning "Not Folia-Compatible"

    FAWE chunk regeneration does not work on Folia servers.

## Web Map Integration

Homestead automatically displays claimed regions on supported web maps. See [Borders & Maps](../Usage/Borders and Maps.md) for full details.

| Plugin | Auto-Detected |
|:-------|:-------------:|
| BlueMap | :material-check: |
| Squaremap | :material-check: |
| Pl3xMap | :material-check: |
| dynmap | :material-check: |

No extra configuration needed — claimed regions appear automatically.

## Discord Webhook

Send server activity to a Discord channel via webhook.

```yaml
# In config.yml
discord:
  enabled: false
  webhook_url: "YOUR DISCORD WEBHOOK URL"  # NOT a bot token!
  events:
    region_create:
      enabled: true
      message: "**[REGIONS]** Region created: {0}"
    region_delete:
      enabled: true
      message: "**[REGIONS]** Region deleted: {0}"
    region_rename:
      enabled: true
      message: "**[REGIONS]** Region name update: {0} -> {1}"
    region_displayname_update:
      enabled: true
    region_description_update:
      enabled: true
    region_private_chat:
      enabled: true
      message: "**[CHAT]** {0}: {3}"
    region_owner_transfer:
      enabled: true
    player_send_mail:
      enabled: true
      message: "**[MAILS]** {0} -> {2}: {3}"
    player_join_region:
      enabled: true
    player_left_region:
      enabled: true
```

!!! warning "Keep Webhook Secret"

    Do not share your webhook URL with anyone. It's not a bot token.

## Metrics (bStats & FastStats)

Anonymous usage statistics help the developer understand how Homestead is used. No personal data is collected.

```yaml
# In config.yml
metrics: true
```

- [bStats Privacy Policy](https://bstats.org/privacy-policy)
- [FastStats Privacy Policy](https://faststats.dev/privacy)

## Vault / ServiceIO

Homestead uses Vault (or ServiceIO) for:
- **Economy**: Banking, upkeep, taxes, rent, sell
- **Permissions**: Group-based limits (alternative to LuckPerms)

See [Economy Setup](../Economy/Setup.md) for details.
