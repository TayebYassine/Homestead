# Upkeep

Upkeep charges region owners a recurring maintenance fee for every chunk they have claimed. The fee is withdrawn from the region bank on a fixed schedule, which keeps large claims scarce and gives owners a concrete reason to keep the bank funded.

!!! info "Paid From Region Bank"

    Upkeep is deducted from the region bank, not from the owner's personal balance. Fund the bank first with `/region deposit`.

## How It Works

When a region's next payment date arrives, Homestead multiplies the number of claimed chunks by the `per-chunk` rate (minus any level discount) and withdraws that total from the region bank. The owner is notified of each successful charge along with the remaining balance.

**Example:** At $100 per chunk, a 10-chunk region owes $1,000 per payment period.

If the bank cannot cover the fee, chunks are unclaimed until the remaining territory fits the funds that are available, and the owner is notified of the loss. Payment is then retried on the following check until it succeeds. New regions are given a grace period before their first payment comes due: the first due date is scheduled one full `upkeep-timer` in the future, plus the extra `start-upkeep` grace period.

---

## Configuration

```yaml
# In regions.yml
upkeep:
  enabled: false
  per-chunk: 100.0          # Cost per chunk per payment period
  unclaim-chunks: true
  start-upkeep: 604800      # Grace period before the first payment (seconds)
  upkeep-timer: 604800      # Time between payments (seconds)
```

`enabled` turns the system on or off:

- **false** (default): No upkeep charges are made
- **true**: Owners are charged `per-chunk` for every claimed chunk each period

`unclaim-chunks` controls what happens when a payment fails:

- **true** (default): Chunks are unclaimed automatically until the bank can cover the fee
- **false**: Chunks stay claimed even if the bank cannot pay

### Time Values

Both time values are measured in seconds. Common durations:

| Duration | Seconds |
|:---------|:-------:|
| 1 day | 86400 |
| 1 week | 604800 |
| 2 weeks | 1209600 |
| 30 days | 2592000 |

---

## Leveling Bonus

Level-up rewards can reduce a region's upkeep bill by up to 50%. The discount is applied automatically to every charge, so higher-level regions pay proportionally less for the same land. View progress with `/region levels` or the region menu.

!!! warning "Restart Required"

    Changes to upkeep settings require a full server restart to take effect.

