# Region Chat & Mail

Communication features let region members stay connected.

---

## Region Chat

Talk privately with all region members. Messages are only visible to the region's trusted members (and the owner).

```
/region chat [message]
```

**Examples:**

```
/region chat                # Toggle region chat mode on/off
/region chat Hey everyone!  # Send a single message in region chat
```

When region chat mode is on, every message you type is sent to the region chat instead of global chat. Run `/region chat` again with no message to turn it off.

MiniMessage formatting tags are not allowed in region chat (or mail): plain text with legacy color codes only.

### Configuration

```yaml
# In regions.yml
log-private-chat: true  # Log region chat to console
```

When `log-private-chat` is `true`, every region chat message is also written to the server console for moderation.

Region chat can also be forwarded to Discord via webhook. See [Plugin Integrations](../Advanced/Integrations.md).

---

## Region Mail

Send a message to all trusted members of a region at once, even when the owner is offline.

```
/region mail [region] [message]
```

**Example:**

```
/region mail MyBase Can you check the farm?
```

MiniMessage tags are forbidden here as well. Each player can have at most **10 unread mails** in a region at a time; older ones must be read (cleared) before more can be sent.

### Reading Mail

Players are notified of unread logs on join:

```
&eWelcome back! There are &63 &eunread mails...
```

The `{unread-logs}` and `{regions-invited}` placeholders in the welcome message (configured in `regions.yml`) show the count of unread mails and pending invites.

### Mailing History

```
/region logs
```

This opens the region activity log GUI. The logs system records significant actions: member additions, flag changes, bank transactions, bans, claims, sub-area changes, weather/time updates, and more. Mail messages appear here as unread entries until the recipient reads them.

### Permissions

| Permission | Allows |
|:-----------|:-------|
| `homestead.actions.regions.chat` | Use region chat |
| `homestead.actions.regions.mail` | Send and read mail |

### API Events

- `RegionChatEvent` — fired when a message is sent in region chat (used for Discord forwarding).
- `PlayerMailEvent` — fired when a player receives mail.

