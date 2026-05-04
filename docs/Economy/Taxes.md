# Member Taxes

Member taxes require trusted players to pay a fee to maintain their membership in a region. This helps region owners offset upkeep costs or generates income.

## How it works

1. **Owner sets tax amount** (within configured limits)
2. **Members pay taxes** to stay trusted in the region
3. **Non-payment** results in automatic untrust
4. **Payments go** to the region bank

**Example:** A region owner sets $50 weekly taxes. Each trusted member must pay $50/week or they'll be automatically untrusted.

## Configuration

```yaml
# Member Taxes System
# Require members to pay fees to stay in region
taxes:
  enabled: false
  min-tax: 0.0
  max-tax: 10000.0
  tax-timer: 604800
```

!!! warning "Restart Required"

    If you change any tax settings, you **must restart** your server for changes to take effect.

