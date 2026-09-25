# Prerequisites

## Server Software

Homestead supports all major server software built on the [Bukkit](https://github.com/Bukkit) API:

- [Paper](https://papermc.io/downloads/paper)
- [Purpur](https://purpurmc.org/download/purpur)
- [Spigot](https://getbukkit.org/download/spigot)
- [Folia](https://papermc.io/downloads/folia)

All four run Minecraft versions **26.1** through **26.3**, require **Java 25**, and are supported by Homestead **6.x+**.

!!! warning "Unsupported Server Software"

    If you are using software other than **Paper**, **Purpur**, **Spigot**, or **Folia**, there is a high chance that Homestead will not work. Homestead may be compatible with **Pufferfish**, **Leaves**, and **UniverseSpigot**, but the four listed above are the only software verified and tested by the developers.

---

## Dependencies

Homestead does not handle economy or permissions itself. Instead, it uses a bridge plugin ([Vault](https://www.spigotmc.org/resources/34315/) or a modern alternative) to connect to the economy and permission services provided by other plugins. The bridge must be installed on your server even if you would rather not use economy features or you are using static limits.

The sections below describe which bridge to use with each server software and list the service providers that have been verified and tested with it.

### Spigot

Spigot is one of the oldest Minecraft server software options, but it lacks many of the performance improvements found in Paper. We recommend PaperMC instead. If you prefer Spigot, Homestead supports the API from **26.1** up to **26.3**.

Spigot uses Vault as its bridge. The following service providers are verified with Vault on Spigot:

- **Economy**: [EssentialsX](https://essentialsx.net/), [iConomyUnlocked](https://modrinth.com/plugin/iconomyunlocked)
- **Permissions**: [LuckPerms](https://luckperms.net/)

### PaperMC / Purpur

PaperMC is the modern, high-performance, and more advanced Minecraft server software. Homestead supports the API from **26.1** up to **26.3**.

Paper and Purpur can use Vault as well as the modern alternatives described in the tip below. Verified service providers for each bridge:

- **Vault**
    - **Economy**: [EssentialsX](https://essentialsx.net/), [iConomyUnlocked](https://modrinth.com/plugin/iconomyunlocked)
    - **Permissions**: [LuckPerms](https://luckperms.net/)
- **VaultUnlocked**
    - **Economy**: [iConomyUnlocked](https://modrinth.com/plugin/iconomyunlocked)
    - **Permissions**: none verified
- **ServiceIO**
    - **Economy**: [EssentialsX](https://essentialsx.net/), [iConomyUnlocked](https://modrinth.com/plugin/iconomyunlocked)
    - **Permissions**: [LuckPerms](https://luckperms.net/)

!!! tip "Alternative to Legacy Vault"

    The original Vault plugin has been outdated for over 6 years but may still work. You can use [VaultUnlocked](https://www.spigotmc.org/resources/117277/) or [ServiceIO](https://modrinth.com/plugin/service-io) instead.

### Folia

Folia is a fork of PaperMC that introduces region-based multithreading. It is significantly more performant than PaperMC, but it lacks support for many plugins. Luckily, Homestead supports Folia, with API support from **26.1** up to **26.3**.

On Folia, Homestead requires [VaultUnlocked](https://www.spigotmc.org/resources/117277/) or [ServiceIO](https://modrinth.com/plugin/service-io) as its bridge to economy and permission services. One of the two must be installed on your server, even if you would rather not use economy features or you are using static limits.

Verified service providers for each bridge on Folia:

- **VaultUnlocked**
    - **Economy**: [iConomyUnlocked](https://modrinth.com/plugin/iconomyunlocked)
    - **Permissions**: none verified
- **ServiceIO**
    - **Economy**: [EssentialsX](https://essentialsx.net/), [iConomyUnlocked](https://modrinth.com/plugin/iconomyunlocked)
    - **Permissions**: [LuckPerms](https://luckperms.net/)

