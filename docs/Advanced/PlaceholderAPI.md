# PlaceholderAPI

Homestead automatically registers its placeholders with PlaceholderAPI. No expansion packs or extra downloads needed.

## Installation

1. Install [PlaceholderAPI](https://www.spigotmc.org/resources/6245/)
2. Restart the server
3. Placeholders are available immediately

## Placeholders

### Region Information

| Placeholder | Description |
|:------------|:------------|
| `%homestead_region_name%` | Name of player's primary (targeted) region |
| `%homestead_region_current%` | Name of region the player is currently standing in |
| `%homestead_region_claimed_chunks%` | Number of chunks claimed in the current region |
| `%homestead_region_max_chunks%` | Maximum chunks the player can claim |
| `%homestead_region_trusted_members%` | Number of trusted members in current region |
| `%homestead_region_max_members%` | Maximum members allowed |

### Economy Information

| Placeholder | Description |
|:------------|:------------|
| `%homestead_region_bank%` | Balance of the player's region bank |
| `%homestead_upkeep_amount%` | Amount due for next upkeep payment |
| `%homestead_upkeep_at%` | When the next upkeep payment is due |

### War Information

| Placeholder | Description |
|:------------|:------------|
| `%homestead_war_name%` | Name of active war |
| `%homestead_war_prize%` | Prize amount for winning the war |

## Default Values

Configure fallback values for when no region is selected:

```yaml
# In config.yml
placeholderapi:
  default:
    region_bank: "None"
    region_name: "None"
    region_claimed_chunks: "0"
    region_max_chunks: "0"
    region_trusted_members: "0"
    region_max_members: "0"
    region_current: "&2Wilderness"
    upkeep_amount: "0,00"
    upkeep_at: "Never"
    war_name: "None"
    war_prize: "0,00"
```
