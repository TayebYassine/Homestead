# Region Chat & Mail

Communication features let region members stay connected.

## Region Chat

Talk privately with all region members. Messages are only visible to the region's trusted members.

```
/region chat [message]
```

**Examples:**
```
/region chat                # Toggle region chat mode on/off
/region chat Hey everyone!  # Send a single message in region chat
```

When in region chat mode, all your messages go to region chat instead of global chat.

### Configuration

```yaml
# In regions.yml
log-private-chat: true  # Log region chat to console
```

Region chat can also be forwarded to Discord via webhook.

## Region Mail

Send messages to all trusted members of a region at once.

```
/region mail [region] [message]
```

**Example:**
```
/region mail MyBase Can you check the farm?
```

### Reading Mail

Players are notified of unread logs on join:

```
&eWelcome back! There are &63 &eunread mails...
```

### Mailing History

```
/region logs         # View all region activity logs
```

The logs system records all significant actions: member additions, flag changes, bank transactions, bans, and more.

### Permissions

| Permission | Allows |
|:-----------|:-------|
| `homestead.actions.regions.chat` | Use region chat |
| `homestead.actions.regions.mail` | Send and read mail |

### API Events

- `RegionChatEvent` — When a message is sent in region chat
- `PlayerMailEvent` — When a player receives mail
