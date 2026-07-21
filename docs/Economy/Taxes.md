# Member Taxes

Member taxes require trusted players to pay a fee to maintain their membership.

## How It Works

1. Owner sets a tax amount
2. Members must pay regularly to stay trusted
3. Non-payment results in automatic untrust
4. Payments go to the region bank

## Commands

| Command | Description |
|:--------|:------------|
| `/region setmembertax [amount]` | Set the member tax |
| `/region setmembertax 0` | Disable taxes |

## Configuration

```yaml
# In regions.yml
taxes:
  enabled: false
  min-tax: 0.0
  max-tax: 10000.0
  tax-timer: 604800    # Payment frequency (seconds)
```

!!! warning "Restart Required"

    Tax setting changes require a full server restart.
