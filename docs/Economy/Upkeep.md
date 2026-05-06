# Region Upkeep

Upkeep requires region owners to pay a fee regularly based on the number of chunks they've claimed. This prevents land hoarding and keeps the economy active.

Note that all the payment is done by region's bank, not the player's own wallet. You must deposit your money to the region's bank.

## How it works

1. **Calculate cost**: Multiply chunks claimed by the per-chunk cost
2. **Payment due**: Owners must pay from their region bank
3. **Grace period**: New regions get a grace period before first payment
4. **Auto-unclaim**: Optionally unclaim chunks if payment fails

**Example:** If upkeep is $100 per chunk, and you claimed 10 chunks, you'll owe $1,000 per upkeep period.

## Configuration

```yaml
upkeep:
  # Enable recurring maintenance fees
  enabled: false

  # Cost per chunk per payment period
  per-chunk: 100.0

  # Auto-unclaim chunks if payment fails
  unclaim-chunks: true

  # Grace period before first payment (seconds)
  # 604800 = 1 week, 1209600 = 2 weeks, 2592000 = 30 days
  start-upkeep: 604800

  # Payment frequency (seconds)
  upkeep-timer: 604800
```

!!! warning "Restart Required"

    If you change any upkeep settings, you **must restart** your server for changes to take effect.

## FAQs
### What happens if I don't pay the upkeep?

Any chunk that cannot be paid for will be automatically unclaimed.

For example, if you have 10 chunks claimed, and you can only afford 6 chunks, the plugin will automatically unclaim 4 random chunks.

### What is `start-upkeep`, or Grace period?

It's a period where region owners receive a short period to grind and earn more money before the actual upkeep starts.

It is useful for beginners and new joined players!
