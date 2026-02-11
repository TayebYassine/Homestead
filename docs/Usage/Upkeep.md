# Upkeep

Upkeep is a recurring payment system that requires region owners to pay maintenance fees based on the number of chunks they've claimed. This prevents land hoarding and keeps the server economy active.

## What is Upkeep?

Think of upkeep as "rent" for your claimed chunks. Every configured period (typically weekly), you must pay money from your region's bank to keep your chunks claimed.

### Why Upkeep Exists

**Server Benefits:**

- Prevents players from claiming excessive land they don't use
- Encourages active players over inactive ones
- Creates a money sink for the economy
- Keeps the world from being completely claimed
- Promotes cooperation (share regions to split costs)

**Player Benefits:**

- Ensures you only claim what you actually need
- Rewards active participation
- Creates opportunities for region trading
- Makes unclaimed land available for new players

!!! info "Server Configuration"

    Upkeep is optional and must be enabled by server admins. Check with `/region menu` to see if upkeep is active on the server.

## How Upkeep Works

### The Formula

```
Upkeep Cost = Number of Chunks × Cost Per Chunk
```

**Example:**

- You have claimed: **10 chunks**
- Server's cost per chunk: **$100**
- Your upkeep: 10 × $100 = **$1,000**

### Payment Schedule

Upkeep is due on a regular schedule set by server admins.

**Check your next payment:**
```
/region menu
```

Look for "Upkeep" button.

### Grace Period

New regions get a **grace period** before the first upkeep payment is due. This gives you time to:

- Build your base
- Gather resources
- Earn money
- Get established

!!! tip "Use Your Grace Period"

    Don't wait until the last minute! Start saving for upkeep early. Set aside money in your region bank during the grace period.

## Paying Upkeep

### Automatic Payment

When upkeep is due, Homestead **automatically** deducts the cost from your **region bank**.

**Process:**

1. Upkeep timer expires
2. Homestead checks your region bank balance
3. If you have enough money → Payment successful
4. If you don't have enough → Partial payment or unclaim (see below)

### Depositing to Region Bank

To prepare for upkeep, deposit money into your region's bank:

**Command:**
```
/region deposit [amount]
```

**Examples:**
```
/region deposit 1000
/region deposit 5000
/region deposit 10000
```

**Check your balance:**
```
/region balance
```

### Recommended Strategy

**Calculate your upkeep cost:**

1. Count your claimed chunks: `/region claimlist`
2. Know the cost per chunk (server setting)
3. Multiply: chunks × cost = upkeep

**Save in advance:**

- Deposit at least 2-3 upkeep payments worth
- This gives you a buffer if you're offline during payment
- Example: If upkeep is $1,000, deposit $3,000

## What Happens if You Can't Pay?

### Full Payment Possible

**If your bank has enough money:**

- [x] Payment is automatically deducted
- [x] All chunks remain claimed
- [x] Next payment scheduled

### Partial Payment Only

**If you have some money but not enough for all chunks:**

*This is an example:*

- Your region: 10 chunks
- Bank balance: $600
- Cost per chunk: $100
- Can afford: 6 chunks

**Result:**

- 6 chunks remain claimed (paid $600)
- 4 chunks are automatically unclaimed
- Usually the last-claimed chunks are unclaimed first

!!! warning "Chunk Loss"

    When chunks are auto-unclaimed, anything built in those chunks becomes unprotected! Other players can claim and build there.

### Zero Balance

**If you have no money at all:**

- All chunks may be unclaimed immediately
- Your builds become unprotected

**Prevention:**

- Always keep money in your region bank
- Set up member taxes to help pay upkeep
- Monitor your balance regularly

## Managing Upkeep Costs

### Reduce Chunk Count

The simplest way to lower upkeep: claim fewer chunks.

**Unclaim unused chunks:**
```
/unclaim
```

Or use the claim list menu:
```
/region claimlist
```

### Use Member Taxes

See [Taxes](./Taxes.md) for more details.

## Key Concepts

- **Automatic payment** from region bank
- **Grace period** for new regions
- **Auto-unclaim** if you can't pay (usually)
- **Member taxes** can help cover costs
- **Regular deposits** prevent loss
