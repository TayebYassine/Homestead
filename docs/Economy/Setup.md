# Economy Setup

Homestead integrates with economy plugins for banking, upkeep, taxes, renting, and selling.

## Requirements

- **[Vault](https://www.spigotmc.org/resources/34315/)** + an economy plugin, **OR**
- **[ServiceIO](https://modrinth.com/plugin/service-io)** (Paper/Purpur)
- **[VaultUnlocked](https://modrinth.com/plugin/vaultunlocked/version/2.16.0)** (Folia)

## Economy Plugins

| Plugin | Notes |
|:-------|:------|
| [EssentialsX](https://www.spigotmc.org/resources/9089/) | Most common |
| [CMI](https://www.spigotmc.org/resources/3742/) | All-in-one |
| [iConomyUnlocked](https://modrinth.com/plugin/iconomyunlocked) | Lightweight |
| [ItemEconomy II](https://github.com/adrianvic/ItemEconomy) | Item-based |

## How It Works

1. Install an economy plugin and Vault/ServiceIO
2. Restart the server
3. Homestead auto-detects the economy provider

## Testing

```
/region balance      # Check bank balance
/region deposit 100  # Deposit money
/region withdraw 50  # Withdraw money
```

## Feature Overview

| Feature | Description |
|:--------|:------------|
| [Regional Bank](Bank.md) | Each region has its own bank |
| [Upkeep](Upkeep.md) | Recurring fees per chunk |
| [Member Taxes](Taxes.md) | Members pay to stay trusted |
| [Rent](Rent.md) | Rent out regions |
| [Sell](Sell.md) | Sell regions to other players |
