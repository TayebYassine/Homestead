# Plugin Integrations

Homestead integrates with several popular plugins to extend functionality.

---

## WorldGuard

Prevent players from claiming chunks inside WorldGuard-protected regions.

```yaml
# In config.yml
worldguard:
  protect-existing-regions: true
```

- **true**: Claims that intersect a WorldGuard region are rejected
- **false**: WorldGuard regions are ignored when claiming

Useful for protecting spawn areas, PvP arenas, and event zones from player claims.

---

## FastAsyncWorldEdit (FAWE)

Regenerate chunks back to their original state when a region's chunks are unclaimed or the region is deleted. This removes all builds, chests, and terrain changes.

```yaml
# In config.yml
fastasyncworldedit:
  regenerate-chunks: false
```

- **false** (default): Unclaimed/deleted chunks keep their modifications
- **true**: Chunks are restored to their original generated state

!!! danger "Irreversible"

    Regenerated chunks are permanently deleted. This cannot be undone.

!!! warning "Not Folia-Compatible"

    FAWE chunk regeneration does not work on Folia servers.

---

## Web Map Integration

Homestead automatically displays claimed regions on supported web maps. See [Borders & Maps](../Usage/Borders%20and%20Maps.md) for full details.

The following map plugins are auto-detected. No extra configuration is needed once they are installed:

- **BlueMap**
- **squaremap**
- **Pl3xMap**
- **dynmap**

Claimed regions appear automatically with configurable colors, descriptions, and (on Pl3xMap/squaremap) custom icons.

---

## Discord Webhook

Send server activity to a Discord channel via webhook. Each event can be toggled independently and its message template customized.

```yaml
# In config.yml
discord:
  enabled: false
  webhook_url: "YOUR_DISCORD_WEBHOOK_URL"
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
      message: "**[REGIONS]** Region display-name update: {0} -> {1}"
    region_description_update:
      enabled: true
      message: "**[REGIONS]** Region description update: {0} -> {1}"
    region_private_chat:
      enabled: true
      message: "**[CHAT]** Player {0} (`{1}`) -> {2}: *{3}*"
    region_owner_transfer:
      enabled: true
      message: "**[REGIONS]** Region owner changed: {0} -> {1} (Region: {2})"
    player_send_mail:
      enabled: true
      message: "**[MAILS]** Player {0} (`{1}`) -> {2}: *{3}*"
    player_join_region:
      enabled: true
      message: "**[MEMBERS]** Player {0} (`{1}`) joined region: {2}"
    player_left_region:
      enabled: true
      message: "**[MEMBERS]** Player {0} (`{1}`) left region: {2}"
```

- **enabled**: Master toggle for all Discord messages
- **webhook_url**: Your Discord webhook URL (format: `https://discord.com/api/webhooks/...`)
- **events.\*.enabled**: Toggle individual event notifications
- **events.\*.message**: Message template; `{0}`, `{1}`, … are replaced with event-specific values (region name, player name, etc.)

!!! warning "Keep Webhook Secret"

    Do not share your webhook URL with anyone. It's not a bot token. Treat it like a password.

---

## Metrics (bStats & FastStats)

Anonymous usage statistics help the developer understand how Homestead is used. No personal data is collected.

```yaml
# In config.yml
metrics: true
```

- **true** (default): Anonymous usage stats are sent
- **false**: Metrics collection is disabled

- [bStats Privacy Policy](https://bstats.org/privacy-policy)
- [FastStats Privacy Policy](https://faststats.dev/privacy)

---

## Vault / VaultUnlocked / ServiceIO

Homestead uses Vault (or the VaultUnlocked / ServiceIO backends) for:

- **Economy**: Banking, upkeep, taxes, rent, sell
- **Permissions**: Group-based limits (alternative to LuckPerms) — when the limits method is set to `groups`, the player's current group is read through the permissions provider

See [Economy Setup](../Economy/Setup.md) for details.

