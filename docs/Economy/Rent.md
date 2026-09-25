# Rent

Players can rent out their regions (or individual sub-areas) for a fixed term in exchange for a one-time payment. Renting gives owners a source of passive income and gives renters temporary access to a territory without a permanent trust.

## How It Works

1. **Owner** configures the rental: price, duration, and an optional security deposit.
2. **Renter** finds the rent sign, reviews the offer in the confirmation menu, and accepts it.
3. **Renter** pays the price plus the security deposit upfront; the price goes to the owner, and access is granted for the rental term.
4. **After the term ends**, access is revoked, and the security deposit is returned to the renter.

## Via Signs

Place a sign inside the region you own. The plugin validates the sign and fills in the region name; right-clicking it opens the confirmation menu described above.

```
Line 1: [Rent]
Line 2: (Region name)
Line 3: (Leave empty)
Line 4: (Leave empty)
```

Duration is entered in whole days and must fall within the configured minimum and maximum (see below).

## Via Command

- `/region rent` — open the rent configuration menu for your target region, where you can set the price, duration, permanent mode, security deposit, notice to vacate, and cancel or end an active rental
- `/region rent [sub-area]` — configure renting for a specific sub-area instead

Only the region owner can configure rent.

---

## Configuration

```yaml
# In regions.yml
renting:
  enabled: true
  price:
    default: 1500.0          # Price assigned to newly created regions
    min: 500.0               # Lowest allowed price
    max: 10000000.0          # Highest allowed price
  duration:
    default: 7               # Default duration in days
    min: 1                   # Minimum duration in days
    max: 84                  # Maximum duration in days
  security-deposit:
    default: 500.0           # Deposit assigned to newly created regions
    min: 0.0                 # Lowest allowed deposit
    max: 100000.0            # Highest allowed deposit
  notice-to-vacate: 3        # Default notice period in days
```

`enabled` is the master switch:

- **true** (default): Rent signs work, and owners can configure rentals
- **false**: Renting is disabled; `/region rent` reports that the feature is off

The `price`, `duration`, and `security-deposit` blocks each define a default for newly created regions plus the minimum and maximum owners may choose from in the rent menu. `notice-to-vacate` sets the default number of days an owner must give a renter when ending a fixed-term contract early; the renter keeps access until that notice period expires (or until the original end date, whichever comes first).

!!! info "Sub-Area Renting"

    Rent signs can be placed inside sub-areas too. A sign placed within a sub-area rents that sub-area rather than the whole region.

!!! warning "Restart Required"

    Changes to renting settings require a full server restart to take effect.

