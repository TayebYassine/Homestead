# Regional Bank

Every region has its own bank account, separate from each player's personal balance. The bank exists so that a territory can fund itself: [upkeep](Upkeep.md) is drawn from it, [member taxes](Taxes.md) are paid into it, and trusted members can chip in for shared expenses without exposing their own wallets.

Money held in the bank belongs to the region itself rather than to any individual. It stays with the region when ownership changes hands (for example, after a [sale](Sell.md)), so a new owner inherits whatever savings the previous owner left behind. Deposits and withdrawals require an economy plugin; see [Economy Setup](Setup.md) if you have not connected one yet.

## Commands

- `/region deposit [amount]` — move money from your personal balance into the region bank; use `all` to deposit everything you carry
- `/region withdraw [amount]` — take money out of the region bank; use `all` to withdraw the full balance
- `/region balance [region]` — check the current bank balance

## Deposit Limit

The most a single region bank can hold is defined per rank in `limits.yml`:

```yaml
# In limits.yml
max-bank-deposit: 100000000
```

A deposit that would push the bank to or past this limit is rejected with a "bank is full" message. Because the limit is part of the rank system, different permission groups can carry different caps. See [Ranks & Limits](../Configuration/Ranks%20and%20Limits.md).

---

## Control Flags

Access to the bank is granted to trusted members through two control flags:

- `deposit-money` — allows the member to deposit money into the region bank
- `withdraw-money` — allows the member to withdraw money from the region bank

Both actions are denied by default; the owner can allow them per player or for the whole region. See [Control Flags](../Configuration/Control%20Flags.md).

