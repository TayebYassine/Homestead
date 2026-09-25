# Member Taxes

Member taxes require trusted players to pay a recurring fee to keep their membership in a region. Owners use them to fund the region bank from their member base, and members who fall behind lose their trust automatically, so the system doubles as a soft activity requirement.

## How It Works

The owner sets a tax amount for the region, and each trusted member pays that amount out of their personal balance on a fixed schedule. Every successful payment is deposited straight into the region bank. The first charge comes due one full tax period after a member is first counted, so every new member gets a grace cycle before paying.

When a member's payment falls due, one of two things happens:

- If their balance covers the tax, the amount is withdrawn, deposited into the region bank, and their next due date is scheduled.
- If their balance is too low, the member is automatically untrusted (removed from the region), and the removal is recorded in the region log.

## Commands

- `/region setmembertax [amount]` — set the amount each trusted member pays per period
- `/region setmembertax 0` — disable taxes for this region

The amount must fall within the `min-tax` and `max-tax` bounds configured in `regions.yml`. Only the region owner can change the tax.

---

## Configuration

```yaml
# In regions.yml
taxes:
  enabled: false
  min-tax: 0.0          # Lowest amount an owner may set
  max-tax: 10000.0      # Highest amount an owner may set
  tax-timer: 604800     # Time between payments (seconds)
```

`enabled` is the master switch:

- **false** (default): The tax system is off; `/region setmembertax` is unavailable
- **true**: Owners can set a regional tax, and collection runs on the `tax-timer` schedule

`min-tax` and `max-tax` bound what owners are allowed to charge; the default minimum of `0.0` lets an owner leave a particular region tax-free, while the default maximum caps any single tax at $10,000 per period. `tax-timer` is the length of each payment period in seconds. The default of 604800 is one week.

!!! warning "Restart Required"

    Tax setting changes require a full server restart to take effect.

