# Economy

The Economy is an important feature for survival servers, especially when players want to rent their regions to other players, or maybe they need to pay taxes to upkeep their regions.

If the economy feature is enabled (an economy plugin was found), 5 sub-features will be enabled by default:

## Bank

Each region has a bank, where region owners deposit and withdraw money. It's the safest place for players to keep their money!

This feature cannot be disabled.

## Renting Regions

It's an option for players to rent their region to other players for an amount of money for a defined period.

```yaml
renting:
  # Enable this setting? true to enable, false to disable.
  enabled: true

  min-rent: 500.0
  max-rent: 10000000.0
```

## Selling Regions

It's an option for players to sell their region to other players for an amount of money.

```yaml
selling:
  # Enable this setting? true to enable, false to disable.
  enabled: true

  min-sell: 10.0
  max-sell: 1000000000.0
```

## Region Upkeep

Require all region owners to pay for upkeep for every week (configurable) based on the number of chunks they've claimed.

!!! warning

    If you change one of the settings for the upkeep system, you **must restart the server**.

```yaml
upkeep:
  # Enable this setting? true to enable, false to disable.
  enabled: false

  per-chunk: 100.0          # $ per chunk
  unclaim-chunks: true      # Unclaim chunks that can't be paid for
  start-upkeep: 604800      # 1-week grace period for new regions (seconds)
  upkeep-timer: 604800      # Due again 1 week after last payment
```

## Member Taxes

Require all members of a region to pay taxes to stay trusted for every week (configurable).

!!! warning

    If you change one of the settings for the member taxes system, you **must restart the server**.

```yaml
taxes:
  # Enable this setting? true to enable, false to disable.
  enabled: false

  min-tax: 0.0
  max-tax: 10000.0
  tax-timer: 604800 # 1 week
```
