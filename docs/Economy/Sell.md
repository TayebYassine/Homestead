# Selling Regions

Players can list their regions for sale, allowing others to purchase ownership. This is useful for players who no longer want a region or want to profit from their land.

## How it works

1. **Seller** sets a sale price for their region
2. **Buyer** purchases the region
3. **Ownership transfers** completely to the buyer
4. **All region settings remain**, including trusted members and flags

!!! warning "Permanent Transfer"

    When a region is sold, the original owner loses all access. Make sure you really want to sell before listing!

## Configuration

```yaml
# Region Selling System
selling:
  enabled: true
  min-sell: 10.0
  max-sell: 1000000000.0
```
