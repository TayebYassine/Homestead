# Sell

Players can list their regions for sale to other players.

## How It Works

1. **Seller** sets a sale price
2. **Buyer** purchases the region
3. **Ownership** transfers completely
4. **Settings** remain (members, flags, bank)

!!! warning "Permanent Transfer"

    When sold, the original owner loses all access. Make sure before listing!

## Via Signs

Place a sign with this format:

```
Line 1: [HSell]
Line 2: (Region name)
Line 3: (Price — e.g. 15000)
Line 4: (Leave empty)
```

## Via Command

| Command | Description |
|:--------|:------------|
| `/region set sell [price]` | Set sale price |
| `/region set sell 0` | Remove from sale |

## Configuration

```yaml
# In regions.yml
selling:
  enabled: true
  min-sell: 10.0
  max-sell: 1000000000.0
```

## Cooldown

```yaml
# In regions.yml
cooldown:
  region-transfer-ownership:
    value: 604800  # 7 days
```
