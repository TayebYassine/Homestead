# Upkeep

Upkeep requires region owners to pay recurring fees from the region bank based on chunk count.

!!! info "Paid From Region Bank"

    Upkeep is deducted from the region bank, not the player's personal balance.

## How It Works

1. Each chunk costs a set amount per payment period
2. Fee deducted from the region bank
3. If the bank can't cover it, chunks are auto-unclaimed (optional)
4. New regions get a grace period

**Example:** $100/chunk, 10 chunks = $1,000 due per period.

## Configuration

```yaml
# In regions.yml
upkeep:
  enabled: false
  per-chunk: 100.0
  unclaim-chunks: true
  start-upkeep: 604800    # Grace period (seconds)
  upkeep-timer: 604800    # Payment frequency (seconds)
```

### Time Values

| Duration | Seconds |
|:---------|:-------:|
| 1 day | 86400 |
| 1 week | 604800 |
| 2 weeks | 1209600 |
| 30 days | 2592000 |

## Leveling Bonus

Level-up rewards can reduce upkeep costs by up to 50%.

!!! warning "Restart Required"

    Changes to upkeep settings require a full server restart.
