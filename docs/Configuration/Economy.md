# Economy

The economy system adds depth to survival servers by allowing players to rent regions, sell land, and manage upkeep costs. When an economy plugin is detected, Homestead automatically enables economy features.

!!! info "Economy Plugin Required"

    You need an economy plugin like Vault, EssentialsX, or similar for these features to work. Homestead will automatically detect and integrate with your economy plugin.

## Regional Banking

Every region has its own bank account where owners can securely store money. The regional bank is separate from a player's personal balance and stays with the region even if ownership changes.

### **Features:**

- Deposit money into your region's bank
- Withdraw funds when needed
- Safe storage that can't be lost on death
- Useful for saving up for upkeep or member taxes

**Note:** This feature cannot be disabled as it's essential for other economy features.

## Renting Regions

Allow players to rent out their regions to others for a set price and duration. This creates opportunities for passive income and helps new players get started without claiming their own land.

### How Renting Works

1. **Region Owner** sets a rental price and duration
2. **Renter** pays the rental fee
3. **Renter** gains access to the region for the rental period
4. **After expiration**, the renter loses access automatically

### Configuration

```yaml
renting:
  # Enable renting? Set to true to allow, false to disable.
  enabled: true

  # Minimum rental price players can set
  min-rent: 500.0
  
  # Maximum rental price players can set
  max-rent: 10000000.0
```

### Setting Rent Limits

- `min-rent`: Prevents players from renting regions for very low prices
- `max-rent`: Prevents inflation by capping maximum rental costs

**Example:** With `min-rent: 500.0`, players can't rent a region for less than $500. This prevents exploits where players might rent land for $1.

## Selling Regions

Players can list their regions for sale, allowing others to purchase ownership. This is useful for players who no longer want a region or want to profit from their land.

### How Selling Works

1. **Seller** sets a sale price for their region
2. **Buyer** purchases the region
3. **Ownership transfers** completely to the buyer
4. **All region settings remain**, including trusted members and flags

!!! warning "Permanent Transfer"

    When a region is sold, the original owner loses all access. Make sure you really want to sell before listing!

### Configuration

```yaml
selling:
  # Enable selling? Set to true to allow, false to disable.
  enabled: true

  # Minimum selling price
  min-sell: 10.0
  
  # Maximum selling price
  max-sell: 1000000000.0
```

### Setting Sale Limits

- `min-sell`: Minimum price for selling a region
- `max-sell`: Maximum price to prevent excessive inflation

## Region Upkeep

Upkeep requires region owners to pay a fee regularly based on the number of chunks they've claimed. This prevents land hoarding and keeps the economy active.

### How Upkeep Works

1. **Calculate cost**: Multiply chunks claimed by the per-chunk cost
2. **Payment due**: Owners must pay from their region bank
3. **Grace period**: New regions get a grace period before first payment
4. **Auto-unclaim**: Optionally unclaim chunks if payment fails

**Example:** If upkeep is $100 per chunk and you claimed 10 chunks, you'll owe $1,000 per upkeep period.

### Configuration

!!! warning "Restart Required"

    If you change any upkeep settings, you **must restart** your server for changes to take effect.

```yaml
upkeep:
  # Enable upkeep system? Set to true to require, false to disable.
  enabled: false

  # Cost per claimed chunk
  per-chunk: 100.0
  
  # Automatically unclaim chunks if player can't pay?
  unclaim-chunks: true
  
  # Grace period for new regions (in seconds)
  # Default: 604800 seconds = 1 week
  start-upkeep: 604800
  
  # How often upkeep is due (in seconds)
  # Default: 604800 seconds = 1 week
  upkeep-timer: 604800
```

### Understanding Upkeep Settings

**per-chunk:**  
The amount charged for each claimed chunk. Higher values make claiming expensive, while lower values are more forgiving.

**unclaim-chunks:**

- `true`: Automatically unclaim chunks the player can't afford
- `false`: Keep chunks claimed but mark the region as owing money

**start-upkeep:**
Grace period before the first upkeep payment is due. This gives new players time to establish themselves.

- `604800` seconds = 1 week
- `1209600` seconds = 2 weeks
- `2592000` seconds = 30 days

**upkeep-timer:**
How often upkeep payments are required.

- `604800` seconds = 1 week
- `1209600` seconds = 2 weeks
- `2592000` seconds = 30 days

### Balancing Upkeep

**For Competitive Servers:**

- Higher per-chunk costs ($100-500)
- Shorter timer (3-7 days)
- Enable auto-unclaim

**For Casual Servers:**

- Lower per-chunk costs ($10-50)
- Longer timer (14-30 days)
- Disable auto-unclaim (let players fall behind)

## Member Taxes

Member taxes require trusted players to pay a fee to maintain their membership in a region. This helps region owners offset upkeep costs or generates income.

### How Member Taxes Work

1. **Owner sets tax amount** (within configured limits)
2. **Members pay taxes** to stay trusted in the region
3. **Non-payment** results in automatic untrust
4. **Payments go** to the region bank

**Example:** A region owner sets $50 weekly taxes. Each trusted member must pay $50/week or they'll be automatically untrusted.

### Configuration

!!! warning "Restart Required"

    If you change any tax settings, you **must restart** your server for changes to take effect.

```yaml
taxes:
  # Enable member taxes? Set to true to allow, false to disable.
  enabled: false

  # Minimum tax owners can set
  min-tax: 0.0
  
  # Maximum tax owners can set
  max-tax: 10000.0
  
  # How often taxes are due (in seconds)
  # Default: 604800 seconds = 1 week
  tax-timer: 604800
```

### Understanding Tax Settings

**min-tax:**  
Minimum amount owners can charge for membership. Set to `0.0` to allow free membership.

**max-tax:**  
Maximum tax to prevent owners from charging excessive fees.

**tax-timer:**  
How often members must pay.

- `604800` seconds = 1 week
- `1209600` seconds = 2 weeks
- `2592000` seconds = 30 days

### Using Member Taxes

**Covering Upkeep:**  
Set member taxes to offset your upkeep costs. For example, if your upkeep is $500/week and you have 5 members, charge $100/week in taxes.

**Generating Profit:**  
Charge more than your upkeep to earn passive income from your region.

**Free Membership:**  
Set `max-tax: 0.0` and owners can choose not to charge anything.

## Economy Integration

Homestead works with these economy plugins:

- Vault (with any economy plugin)
- EssentialsX
- Any plugin that provides Vault integration
