# Homestead

**Homestead** is an open-source land-claiming plugin for Minecraft, designed to give players full control over their land with multiple configurations and customizations.

The plugin offers more than 75 flags, allowing players to set specific permissions per player action or manipulate the environment of the land. Homestead provides the tools for a secure and customized experience for everyone.

## Features

### Protection & Security

- **Grief Prevention** — Define fully customizable regions that block unauthorized building, theft, and PvP.
- **12+ API Integrations** — Native support for WorldGuard regions (spawn, arenas, events), PlaceholderAPI, and more.
- **75+ Flags** — Fine-grained control over mob spawning, fire spread, crop trampling, redstone, and other interactions.
- **Sub-Areas** — Nest independent areas inside a parent region with their own permissions.
    - Create public market stalls inside private towns.
    - Lease hotel rooms, apartments, or shop plots.
    - Set per-member permissions for each sub-area.
- **Administrative Tools** — Staff can moderate any region without interfering with player ownership.

---

### Navigation & Visualization

- **Region Teleportation** — Travel to any region instantly with a single command.
- **Live Map Support** — Regions are rendered in real time on web maps.
    - Compatible with Dynmap, Pl3xMap, BlueMap, and Squaremap.
    - Players can set custom region colors and icons directly on the map.

---

### Economy & Management

- **GUI Management** — Rename regions, manage flags, control members, and adjust settings through menus. No commands required.
- **Regional Analytics** — View member activity, transaction history, and region statistics in real time.
- **Region Bank** — Deposit and withdraw funds tied to a region directly.
- **Region Market**
    - **Leasing** — Rent land for configurable durations with automated payment collection.
    - **Ownership Transfers** — Player-to-player region sales with built-in fraud protection.
- **Leveling System** — Regions earn XP over time, unlocking upgrades and rewards.
- **Reward System** — Players earn rewards through activity and by adding trusted members.
- **Taxation** — Weekly or monthly taxes based on member count or claimed chunks.
- **Upkeep Costs** — Automatic maintenance fees that discourage abandoned or inactive claims.

---

### Social & Community

- **Player Permissions** — Assign specific rights to individual members within a region.
- **Private Region Chat** — Members communicate through an isolated chat channel.
- **Access Control** — Whitelist and blacklist players for entry, building, and interaction independently.

---

### Integrations & Development

- **Plugin Compatibility** — Full support for Vault, WorldGuard, PlaceholderAPI, LuckPerms, and major permission systems.
- **Developer API** — Extend Homestead with custom integrations using the public API.

---

### Premium Features

- **Data Safety** — Migration tools prevent corruption and support switching database providers.
- **Plugin Migration** — Import data from other land claiming plugins.
- **Full Localization** — Every message, menu, and configuration string is translatable within the configuration files.
- **Staff Overrides** — Staff can bypass protections, edit any region, and roll back damage (with other plugins) without affecting the owner.
- **Optimized Performance** — Thread-safe, memory-efficient storage ensures protection checks have no impact on server TPS.
- **MiniMessage and Legacy Chat Format Support** — Edit any message string with legacy Minecraft color codes and with [PaperMC Adventure MiniMessage](https://docs.papermc.io/adventure/minimessage/) for advanced message component.

## Prerequisites

This depends on which software you are using. No worries, here is a guide.

### Spigot / PaperMC / Purpur / Pufferfish...

> [!IMPORTANT]
> As of Homestead 5.0.1.0, the API was changed from the Spigot API to the PaperMC API, implementing a new platform bridge to make Spigot run with the PaperMC API.
> 
> Any software that was forked from the Spigot source will generally not work with Homestead.

- **Minecraft 1.21** to **1.21.8** — **Homestead 1.0.0** to **4.2.0**
- **Minecraft 1.21.9** to latest — **Homestead 4.3.0** to latest

#### Dependencies

- [Vault](https://www.spigotmc.org/resources/34315/)
- Any Permissions plugin, like [LuckPerms](https://www.spigotmc.org/resources/28140/). (optional)
- Any Economy plugin, like [EssentialsX](https://www.spigotmc.org/resources/9089/) with built-in Economy API. (optional)

### Folia

- **Minecraft 1.21.9** to latest — **Homestead 5.0.1.0** to latest

#### Dependencies

- [VaultUnlocked](https://modrinth.com/plugin/vaultunlocked/version/2.16.0) version **2.16.0**
- Any Permissions plugin, like [LuckPerms](https://www.spigotmc.org/resources/28140/). (optional)
- Economy plugins supported by VaultUnlocked, like [iConomyUnlocked](https://modrinth.com/plugin/iconomyunlocked). (optional)

## Developer API
How to include the API with Maven:

```xml
<repository>
  <id>homestead-github</id>
  <url>https://maven.pkg.github.com/TayebYassine/Homestead</url>
</repository>

<dependency>
  <groupId>tfagaming.projects.minecraft.homestead</groupId>
  <artifactId>homestead</artifactId>
  <version>5.0.2.0</version>
  <classifier>api</classifier>
  <scope>provided</scope>
</dependency>
```

How to include the API with Gradle:

```kotlin
repositories {
    maven("https://maven.pkg.github.com/TayebYassine/Homestead")
}

dependencies {
    compileOnly("tfagaming.projects.minecraft.homestead:homestead:5.0.2.0:api")
}
```

All versions are available in [GitHub Packages](https://github.com/TayebYassine/Homestead/packages/2787077/versions).

## Contributing

Click here: [CONTRIBUTING.md](./CONTRIBUTING.md)

## License

Homestead is released under the [Apache License 2.0](./LICENSE).
