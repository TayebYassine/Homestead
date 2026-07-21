# Developer API

Homestead provides a public API for developers to integrate with the plugin. You can manage regions, chunks, members, flags, and more programmatically.

## Installation

### Maven

```xml
<repository>
    <id>homestead-github</id>
    <url>https://maven.pkg.github.com/TayebYassine/Homestead</url>
</repository>

<dependency>
    <groupId>tfagaming.projects.minecraft.homestead</groupId>
    <artifactId>homestead</artifactId>
    <version>5.2.0.0</version>
    <classifier>api</classifier>
    <scope>provided</scope>
</dependency>
```

### Gradle

```kotlin
repositories {
    maven("https://maven.pkg.github.com/TayebYassine/Homestead")
}

dependencies {
    compileOnly("tfagaming.projects.minecraft.homestead:homestead:5.2.0.0:api")
}
```

## JavaDoc

Full API documentation is available:

- [JavaDoc Home](https://tayebyassine.github.io/Homestead/javadoc/)
- [Events Package](https://tayebyassine.github.io/Homestead/javadoc/tfagaming/projects/minecraft/homestead/api/events/package-summary.html)
- [Managers Package](https://tayebyassine.github.io/Homestead/javadoc/tfagaming/projects/minecraft/homestead/managers/package-summary.html)
- [Models Package](https://tayebyassine.github.io/Homestead/javadoc/tfagaming/projects/minecraft/homestead/models/package-summary.html)

## API Events

Homestead fires events for many actions. Listen to them like any Bukkit event.

### Region Lifecycle

| Event | Fired When |
|:------|:-----------|
| `RegionCreateEvent` | A region is created |
| `RegionDeleteEvent` | A region is deleted |

### Chunk Actions

| Event | Fired When |
|:------|:-----------|
| `ChunkClaimEvent` | A chunk is claimed |
| `ChunkUnclaimEvent` | A chunk is unclaimed |

### Player Movement

| Event | Fired When |
|:------|:-----------|
| `PlayerJoinRegionEvent` | A player enters a region |
| `PlayerLeftRegionEvent` | A player leaves a region |
| `PlayerJoinSubAreaEvent` | A player enters a sub-area |
| `PlayerLeftSubAreaEvent` | A player leaves a sub-area |

### Member Management

| Event | Fired When |
|:------|:-----------|
| `InvitePlayerEvent` | A player is invited to a region |
| `RevokePlayerInviteEvent` | A player's invite is revoked |
| `BulkDeleteInvitesEvent` | Multiple invites are deleted |

### Ban Management

| Event | Fired When |
|:------|:-----------|
| `BanPlayerEvent` | A player is banned from a region |
| `UnbanPlayerEvent` | A player is unbanned |
| `BulkUnbanPlayersEvent` | Multiple players are unbanned |

### Banking

| Event | Fired When |
|:------|:-----------|
| `BankDepositEvent` | Money is deposited to a region bank |
| `BankWithdrawEvent` | Money is withdrawn from a region bank |

### Communication

| Event | Fired When |
|:------|:-----------|
| `RegionChatEvent` | A message is sent in region chat |
| `PlayerMailEvent` | A player receives mail |

### Region Updates

| Event | Fired When |
|:------|:-----------|
| `RegionNameUpdateEvent` | Region name is changed |
| `RegionDisplaynameUpdateEvent` | Region display name is changed |
| `RegionDescriptionUpdateEvent` | Region description is changed |
| `RegionLocationUpdateEvent` | Region location data is updated |
| `RegionOwnerUpdateEvent` | Region ownership is transferred |

## API Usage Examples

See [API Examples](API Examples.md) for code examples.
