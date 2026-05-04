# Renting Regions

Allow players to rent out their regions to others for a set price and duration. This creates opportunities for passive income and helps new players get started without claiming their own land.

## How it works

1. **Region Owner** sets a rental price and duration
2. **Renter** pays the rental fee
3. **Renter** gains access to the region for the rental period
4. **After expiration**, the renter loses access automatically

## Configuration

```yaml
# Region Renting System
renting:
  enabled: true
  min-rent: 500.0
  max-rent: 10000000.0
```

