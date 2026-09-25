# Welcome to Homestead

**Homestead** is an open-source land-claiming plugin for Minecraft, designed to give players full control over their land with multiple configurations and customizations.

The plugin offers more than 75 flags, allowing players to set specific permissions per player action or manipulate the environment of the land. Homestead provides the tools for a secure and customized experience for everyone.

---

<div class="grid cards" markdown>

-   :material-rocket-launch: **Getting Started**

    ---

    New to Homestead? Everything you need to install it and take your first steps.

    [:octicons-arrow-right-24: Prerequisites](Getting%20Started/Prerequisites.md)<br>
    [:octicons-arrow-right-24: Installation](Getting%20Started/Installation.md)<br>
    [:octicons-arrow-right-24: First Steps](Getting%20Started/First%20Steps.md)

- :material-tune-vertical: **Configuration**

    ---

    Customize every aspect of the plugin.

    [:octicons-arrow-right-24: Database](Configuration/Database.md)<br>
    [:octicons-arrow-right-24: Flags](Configuration/Flags%20Overview.md)<br>
    [:octicons-arrow-right-24: Ranks & Limits](Configuration/Ranks%20and%20Limits.md)

-   :material-currency-usd: **Economy**

    ---

    Banking, upkeep, taxes, renting, and selling.

    [:octicons-arrow-right-24: Regional Bank](Economy/Bank.md)<br>
    [:octicons-arrow-right-24: Upkeep](Economy/Upkeep.md)<br>
    [:octicons-arrow-right-24: Rent & Sell](Economy/Rent.md)

- :material-book-open-variant: **Usage**

    ---

    Guides for players and admins.

    [:octicons-arrow-right-24: Creating a Region](Usage/Creating%20a%20Region.md)<br>
    [:octicons-arrow-right-24: Managing a Region](Usage/Managing%20a%20Region.md)<br>
    [:octicons-arrow-right-24: Sub-Areas](Usage/Sub-Areas.md)

- :material-code-tags: **Development**

    ---

    Integrate Homestead into your own plugins.

    [:octicons-arrow-right-24: Developer API](Advanced/API.md)<br>
    [:octicons-arrow-right-24: API Examples](Advanced/API%20Examples.md)<br>
    [:octicons-arrow-right-24: PlaceholderAPI](Advanced/PlaceholderAPI.md)

- :material-lifebuoy: **Support**

    ---

    Get help, report bugs, check the changelog.

    [:octicons-arrow-right-24: FAQ](Getting%20Started/FAQ.md)<br>
    [:octicons-arrow-right-24: Changelog](Support/Changelog.md)<br>
    [:octicons-arrow-right-24: Getting Help](Support/Support.md)

</div>

---

## Why Homestead?

Here is a quick overview of what sets Homestead apart:

### :material-lightning-bolt-circle: Lightweight & Efficient

Region lookups go through a concurrent caching system, so protection checks stay fast even on servers with thousands of claims. Storage is thread-safe and memory-efficient, so protection checks do not drag down server TPS.

### :material-tune: Fully Customizable

Nearly every aspect of Homestead can be modified: custom language files, configurable flag defaults, adjustable claim limits per rank or group, editable menus, and per-world rules. When a server needs different behavior, there is usually a configuration option for it.

### :material-open-source-initiative: Open Source & Free

Homestead is completely free and open source, released under the Apache License 2.0. There are no paid features and no gated content, and the source code is available on GitHub.

### :material-update: Active Development

Regular updates bring new features, bug fixes, and compatibility with the latest Minecraft versions. The current release line supports server API versions **26.1** up to **26.3**.

### :material-database: Four Database Providers

Choose the database that fits your server: SQLite for small servers with no setup required, or MySQL, MariaDB, and PostgreSQL for larger ones. You can switch providers later without losing data. See the [Database](Configuration/Database.md) and [Database Migration](Configuration/Database%20Migration.md) guides.

### :material-door-open: Easy Migration

Moving from another claiming plugin is built in. Homestead imports claims (and trusted players where the source plugin supports them) from GriefPrevention, ClaimChunk, LandLord4, Lands, and HuskClaims with a single command:

```
/hsadmin import [plugin-name]
```

---

## Community

Homestead is developed in the open. Use these channels to follow development, report bugs, ask for help, or browse the API documentation:

- [GitHub Repository](https://github.com/TayebYassine/Homestead)
- [Discord Server](https://discord.gg/uh7gqDY6sz)
- [JavaDoc](https://tayebyassine.github.io/Homestead/javadoc/)

