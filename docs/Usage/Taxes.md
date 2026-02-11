# Member Taxes

Member taxes require trusted players to pay regular fees to remain members of a region. This helps region owners offset upkeep costs and manage their communities.

## What Are Member Taxes?

Member taxes are periodic payments that trusted members must pay to stay in a region. If a member doesn't pay, they're automatically untrusted.

### How It Works

1. **Owner sets tax amount** using `/region set tax [amount]`
2. **Payment is due** on a schedule (set by server)
3. **Automatic deduction** from member's personal balance
4. **Money goes to region bank** for the owner to use
5. **Non-payment = automatic untrust**

## Setting Up Taxes

!!! warning "Disallow Members from Withdrawing Region Money"

    Allowing specific trusted players in a region to withdraw money from the region increases a potential risk of tax evasion.
    How? The players can withdraw the money, and the plugin will automatically take the money again and deposit it to the region,
    which allows players to tax evade. Allow players to deposit money, not to withdraw from the bank!

    **Note:** Even allowing only one player to withdraw money, they can give the money to the other members so they can evade taxes!
    Tax evasion seems easy, I think? __Don't do it__ in real life by the way.

### For Region Owners

**Set the tax amount:**
```
/region set tax [amount]
```

**Examples:**
```
/region set tax 500
/region set tax 1000
/region set tax 0 (disable taxes)
```

### Payment Schedule

Taxes are collected on the same schedule as upkeep (typically weekly).

## For Region Members

### Paying Taxes

**Payment is automatic:**

1. Tax timer expires
2. Money is deducted from your personal balance
3. Money is deposited in the region bank

**No manual payment needed!**

### What Happens if You Can't Pay

**If you don't have enough money:**

- You're automatically untrusted from the region
- You lose all build permissions
- You can be re-trusted after paying

**To avoid this:**

- Keep enough money in your personal balance
- Check tax amount with `/region members`
- Save money before the payment date
- Ask the owner if you need help

## Use Cases

### Covering Upkeep

**Example scenario:**

- Region upkeep: $2,000/week
- Members: 4 people
- Tax per member: $500/week
- Result: Members cover entire upkeep cost

**Benefits:**

- Owner doesn't pay alone
- Fair cost sharing
- Community-funded region

## Key Concepts

- **Automatic payment** from member's personal balance
- **Goes to region bank** for owner's use
- **Auto-untrust** if member can't pay
- **Scheduled payments** (weekly, bi-weekly, monthly)
