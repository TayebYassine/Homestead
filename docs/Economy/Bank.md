# Regional Bank

Every region has its own bank account, separate from the player's personal balance.

## Features

- :material-check: Deposit and withdraw money
- :material-check: Secure — money stays in the region bank
- :material-check: Persistent — survives ownership transfers

## Commands

| Command | Description |
|:--------|:------------|
| `/region deposit [amount]` | Add money to the region bank |
| `/region withdraw [amount]` | Remove money from the region bank |
| `/region balance` | Check current bank balance |

## Configuration

```yaml
# In limits.yml
max-bank-deposit: 100000000
```

## Control Flags

Trusted members can be given bank access:

| Flag | Allows |
|:-----|:-------|
| `deposit-money` | Deposit to bank |
| `withdraw-money` | Withdraw from bank |

See [Control Flags](../Configuration/Control Flags.md).
