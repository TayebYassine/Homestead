# Economy Setup

Homestead does not run an economy of its own. It connects to your server's existing balance system so that regions can hold money in a shared bank, charge recurring upkeep, collect member taxes, and process rent and sale payments between players. Once a provider is detected, every economy feature below works without further setup.

---

## Requirements

Homestead needs one of the following bridges to talk to your economy:

- **[Vault](https://www.spigotmc.org/resources/34315/)** plus a Vault-compatible economy plugin — the standard option on Spigot and Paper
- **[ServiceIO](https://modrinth.com/plugin/service-io)** — an alternative bridge for Paper/Purpur
- **[VaultUnlocked](https://modrinth.com/plugin/vaultunlocked/version/2.16.0)** — the bridge for Folia

Install one of these alongside an economy plugin before enabling any economy feature.

## Economy Plugins

Any Vault-style economy plugin will work. Common choices include:

- [EssentialsX](https://www.spigotmc.org/resources/9089/) — the most common option
- [CMI](https://www.spigotmc.org/resources/3742/) — an all-in-one suite
- [iConomyUnlocked](https://modrinth.com/plugin/iconomyunlocked) — a lightweight plugin
- [ItemEconomy II](https://github.com/adrianvic/ItemEconomy) — an item-based economy

## How It Works

Setup takes three steps:

1. Install your economy plugin together with Vault, ServiceIO, or VaultUnlocked.
2. Restart the server so the economy plugin can register its provider.
3. Homestead detects the provider automatically at startup. No extra Homestead setting is required.

If no provider is found, the economy features stay dormant until one becomes available. Nothing breaks, but deposits, upkeep, taxes, renting, and selling will not run.

---

## Testing

Confirm the connection with the region bank commands:

```
/region balance      # Check bank balance
/region deposit 100  # Deposit money
/region withdraw 50  # Withdraw money
```

If money moves in and out of the bank correctly, the economy bridge is working and the features below are ready to configure.

## Feature Overview

Each system has its own documentation page:

- [Regional Bank](Bank.md) — a per-region account that funds upkeep and receives tax payments
- [Upkeep](Upkeep.md) — recurring fees charged for every claimed chunk
- [Member Taxes](Taxes.md) — payments trusted members make to keep their membership
- [Rent](Rent.md) — rent out a region or sub-area for a fixed term
- [Sell](Sell.md) — transfer a region to a buyer for a one-time price

