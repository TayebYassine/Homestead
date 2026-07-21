# Rent

Players can rent out their regions for passive income.

## How It Works

1. **Owner** sets price and duration
2. **Renter** pays the fee
3. **Renter** gains access for the rental period
4. **After expiration**, access is revoked

## Via Signs

Place a sign with this format:

```
Line 1: [Rent]
Line 2: (Region name)
Line 3: (Price — e.g. 15000)
Line 4: (Duration — e.g. 7d)
```

### Duration Format

| Unit | Meaning | Example |
|:----:|:--------|:--------|
| `s` | Seconds | `10s` |
| `m` | Minutes | `30m` |
| `h` | Hours | `6h` |
| `d` | Days | `3d` |
| `w` | Weeks | `2w` |

## Via Command

| Command | Description |
|:--------|:------------|
| `/region set rent [price] [duration]` | Set rental price & duration |
| `/region set rent 0` | Remove from rent market |

## Configuration

```yaml
# In regions.yml
renting:
  enabled: true
  min-rent: 500.0
  max-rent: 10000000.0
```

!!! info "Sub-Area Renting"

    Rent signs can be placed inside sub-areas too.
