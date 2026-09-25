<div align="center">

![Spiget Downloads](https://img.shields.io/spiget/downloads/121873?logo=spigotmc&label=Downloads&color=orange)
![Modrinth Downloads](https://img.shields.io/modrinth/dt/homestead-plugin?logo=modrinth&label=Downloads)
![Hangar Downloads](https://img.shields.io/hangar/dt/homestead?logo=image%2Fpng%3Bbase64%2CiVBORw0KGgoAAAANSUhEUgAAABkAAAAgBAMAAAAVss41AAAAJFBMVEVHcEz%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%8Uel1nAAAAC3RSTlMAH47fTa3EafI0C3ZKri0AAADwSURBVHicTZGvC8JQEMdPDeKSYBEsEwSFFbGJZcVkMRiEV7S6IgiCWNS6ZlBhxWZ3%2Fti8f87bfbfplePDvXff7%2Fs%2BIqlyr0p5FQy%2F3JwuzBw2s9GAk5qAaozqK3mAaKkL09FcR3XAE9cc0ELB8hVC6KfX4qtSCdQ%2B6LCB9TWYGyndt7LVzVbufQ7Pk8xJW9S9qEqwPORQ7Nop1Tk2zGNS8VfALWld0N3hk7QdTr7NM1DyQA9HT5qEHuZTmTLPaKvkzchaCQUgW0wOx3jD22goVypCwc2DFvVRlnWhIzk08uSttfj8%2FQMd%2Fc3fJ9HNRv8CUjKn1XnSu4wAAAAASUVORK5CYII%3D&label=Downloads&color=blue)
![GitHub Issues](https://img.shields.io/github/issues/TayebYassine/Homestead?logo=github&label=Issues)
<br><br>
![Spiget Version](https://img.shields.io/spiget/version/121873?logoColor=blue&label=Version)
![Spiget Rating](https://img.shields.io/spiget/stars/121873?label=Rating)
![Static Badge](https://img.shields.io/badge/Minecraft-26.1%E2%80%9426.3-green?color=%235b8731)
![Static Badge](https://img.shields.io/badge/Java-25-orange)

</div>

# Homestead

**Homestead** is an open-source land-claiming plugin for Minecraft, designed to give players full control over their land with multiple configurations and customizations.

The plugin offers more than 75 flags, allowing players to set specific permissions per player action or manipulate the environment of the land. Homestead provides the tools for a secure and customized experience for everyone.

## Features

### Protection & Security

- **Grief Prevention** — Define fully customizable regions that block unauthorized building, theft, and PvP.
- **12+ API Integrations** — Native support for WorldGuard regions (spawn, arenas, events), PlaceholderAPI, and more.
- **75+ Flags** — Fine-grained control over interactions such as mob spawning, fire spread, crop trampling, and redstone.
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

- **GUI Management** — Rename regions, manage flags, control members, and adjust settings through menus — no commands required.
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
- **Plugin Migration** — Import data from other land-claiming plugins.
- **Full Localization** — Every message, menu, and configuration string is translatable within the configuration files.
- **Staff Overrides** — Staff can bypass protections, edit any region, and roll back damage (with other plugins) without affecting the owner.
- **Optimized Performance** — Thread-safe, memory-efficient storage ensures protection checks have no impact on server TPS.
- **MiniMessage and Legacy Chat Format Support** — Edit any message string using legacy Minecraft color codes or [PaperMC Adventure MiniMessage](https://docs.papermc.io/adventure/minimessage/) tags for advanced message components.

## Prerequisites

Before installing Homestead, make sure your server runs one of the platforms below and that the matching bridge plugin is installed. Homestead uses that bridge to reach the economy and permission services provided by your other plugins — it must be present even if you do not plan to use economy features or you rely on static claim limits.

### <img src="https://avatars.githubusercontent.com/u/4350249?s=280&v=4" width="20" height="20"/> Spigot

Spigot is one of the oldest Minecraft server platforms, but it lacks the major performance improvements found in newer forks, so we recommend PaperMC. If you prefer Spigot, Homestead supports the API from **26.1** up to **26.3**.

Homestead requires the [Vault](https://www.spigotmc.org/resources/34315/) plugin as a bridge to connect to the economy and permission services provided by other plugins. Vault must be installed on your server even if you would rather not use economy features or you are using static limits.

The following service providers have been verified and tested with Vault:

|                 |                                                  Vault                                                  |
|:----------------|:-------------------------------------------------------------------------------------------------------:|
| **Economy**     | [EssentialsX](https://essentialsx.net/), [iConomyUnlocked](https://modrinth.com/plugin/iconomyunlocked) |
| **Permissions** |                                   [LuckPerms](https://luckperms.net/)                                   |

### <img src="https://avatars.githubusercontent.com/u/7608950?s=200&v=4" width="20" height="20"/> PaperMC / <img src="https://avatars.githubusercontent.com/u/94729614?s=200&v=4" width="20" height="20" /> Purpur

PaperMC is the modern, high-performance, and more advanced Minecraft server software. Homestead supports the API from **26.1** up to **26.3**.

Homestead requires the [Vault](https://www.spigotmc.org/resources/34315/) plugin as a bridge to connect to the economy and permission services provided by other plugins. Vault must be installed on your server even if you would rather not use economy features or you are using static limits.

The original Vault plugin has been outdated for over 6 years but may still work. Alternatively, you can use [VaultUnlocked](https://www.spigotmc.org/resources/117277/) or [ServiceIO](https://modrinth.com/plugin/service-io) instead.

The following service providers have been verified and tested with Vault, VaultUnlocked, and ServiceIO:

|                 |                                                  Vault                                                  |                         VaultUnlocked                          |                                                ServiceIO                                                |
|:----------------|:-------------------------------------------------------------------------------------------------------:|:--------------------------------------------------------------:|:-------------------------------------------------------------------------------------------------------:|
| **Economy**     | [EssentialsX](https://essentialsx.net/), [iConomyUnlocked](https://modrinth.com/plugin/iconomyunlocked) | [iConomyUnlocked](https://modrinth.com/plugin/iconomyunlocked) | [EssentialsX](https://essentialsx.net/), [iConomyUnlocked](https://modrinth.com/plugin/iconomyunlocked) |
| **Permissions** |                                   [LuckPerms](https://luckperms.net/)                                   |                             (none)                             |                                   [LuckPerms](https://luckperms.net/)                                   |

### <img src="https://avatars.githubusercontent.com/u/7608950?s=200&v=4" width="20" height="20"/> Folia

Folia is a fork of PaperMC that introduces region-based multithreading. It is significantly more performant than PaperMC, but many plugins have not been updated to support it. Homestead is one of the exceptions: it supports the API from **26.1** up to **26.3**.

On Folia, Homestead requires either the [VaultUnlocked](https://www.spigotmc.org/resources/117277/) or [iConomyUnlocked](https://modrinth.com/plugin/iconomyunlocked) plugin, because one of them acts as a bridge to connect to the economy and permission services provided by other plugins. One of these plugins must be installed on your server even if you would rather not use economy features or you are using static limits.

The following service providers have been verified and tested with VaultUnlocked and ServiceIO:

|                 |                         VaultUnlocked                          |                           ServiceIO                            |
|:---------------:|:--------------------------------------------------------------:|:--------------------------------------------------------------:|
|   **Economy**   | [iConomyUnlocked](https://modrinth.com/plugin/iconomyunlocked) | [iConomyUnlocked](https://modrinth.com/plugin/iconomyunlocked) |
| **Permissions** |                             (none)                             |              [LuckPerms](https://luckperms.net/)               |

## Developer API

Homestead exposes a public API so other plugins can manage regions, chunks, members, and flags programmatically and listen to Homestead events. All API versions are published on [GitHub Packages](https://github.com/TayebYassine/Homestead/packages/2787077/versions), and full JavaDoc is available online at [tayebyassine.github.io/Homestead/javadoc](https://tayebyassine.github.io/Homestead/javadoc/).

To include the API with Maven, add the repository and dependency:

```xml
<repository>
  <id>homestead-github</id>
  <url>https://maven.pkg.github.com/TayebYassine/Homestead</url>
</repository>

<dependency>
  <groupId>me.tayebyassine.homestead</groupId>
  <artifactId>homestead</artifactId>
  <version>6.0.0.0</version>
  <classifier>api</classifier>
  <scope>provided</scope>
</dependency>
```

To include the API with Gradle:

```kotlin
repositories {
    maven("https://maven.pkg.github.com/TayebYassine/Homestead")
}

dependencies {
    compileOnly("me.tayebyassine.homestead:homestead:6.0.0.0:api")
}
```

## Metrics

Homestead collects anonymous usage data and sends it to [bStats](https://bstats.org/plugin/bukkit/Homestead/25286) and [FastStats](https://faststats.dev/project/homestead/minecraft-plugin), two well-known metrics services in the Minecraft plugin industry. No personal data is collected.

Metrics are **enabled** by default. You can disable them entirely in the plugin's configuration files if you prefer.
<details>
<summary>Metrics Embedded Images</summary>

<img src="https://faststats.dev/embed/default:516c7d15-a0ff-400e-9e60-c017ac4b5777:servers-and-players?w=800&h=300&theme=dark" />

<img src="https://bstats.org/signatures/bukkit/Homestead.svg" />

</details>

## Contributing

Contributions are welcome! Whether you are fixing a bug, improving the documentation, or adding a feature, feel free to open a pull request.

Please read the guidelines before you open one: [CONTRIBUTING.md](./CONTRIBUTING.md)

## License

Homestead is released under the [Apache License 2.0](./LICENSE).
