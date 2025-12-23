# Homestead
**Homestead** is a lightweight, open-source, land claiming plugin designed to give players full control over their land. Perfect for survival and SMP servers, Homestead lets players claim chunks in any world while enforcing personalized claim limits.

Each claimed region includes **50+ customizable flags**, enabling owners to configure gameplay settings, from PvP and mob spawning to block interaction and environmental effects. Whether you’re building a town, managing a private base, or running a faction server, Homestead provides the tools for a secure and customized experience.

Homestead is compatible with **Minecraft 1.21** (and higher) and supports any server software that handles plugins based on Bukkit API.

## Developer API
How to include the API with Maven:

```yaml
<repository>
  <id>homestead-github</id>
  <url>https://maven.pkg.github.com/TayebYassine/Homestead</url>
</repository>

<dependency>
  <groupId>tfagaming.projects.minecraft.homestead</groupId>
  <artifactId>homestead</artifactId>
  <version>{LATEST_VERSION}</version>
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
    compileOnly("tfagaming.projects.minecraft.homestead:homestead:{LATEST_VERSION}:api")
}
```

Replace `{LATEST_VERSION}` with the current latest version available, check [SpigotMC](https://www.spigotmc.org/resources/121873/), [Modrinth](https://modrinth.com/plugin/homestead-plugin/versions?c=release), or [Hangar](https://hangar.papermc.io/TayebYassine/Homestead/versions?channel=Release).

## Contributing

Click here: [CONTRIBUTING.md](./CONTRIBUTING.md)

## License

Homestead is released under the [Apache License 2.0](./LICENSE).
