# Example Usage

!!! warning "Model IDs Changed"

    As of release 5.2.0.0, all model IDs have been changed from UUID v4 to
    [Twitter Snowflake IDs](https://en.wikipedia.org/wiki/Snowflake_ID).

### Creating a Region

```java
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;

// The owner of the region
Player player = Bukkit.getPlayer("TFA_Gaming");

Region region = RegionManager.createRegion("ExampleRegion", player);
```

### Fetching a region

```java
import tfagaming.projects.minecraft.homestead.managers.RegionManager;

Region region;

String name = "ExampleRegion";
long regionId = 309031393541763072L;

// Find a region by name (case is ignored)
RegionManager.findRegion(name);

// Find a region by ID
RegionManager.findRegion(regionId);
```

### Delete a Region

```java
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;

long regionId = 309031393541763072L;

// If the region exist (not null)
if (region != null) {
    RegionManager.deleteRegion(regionId);
}
```

### Claiming a Chunk

```java
import org.bukkit.*;
import tfagaming.projects.minecraft.homestead.managers.ChunkManager;

// The chunk to claim
Chunk chunk = ...;

// The region which will have the chunk added
Region region = ...;

ChunkManager.Error error = ChunkManager.claimChunk(region.getUniqueId(), chunk);

// If Homestead claimed the chunk
if (error == null) {
    System.out.println("Chunk claimed successfully!");    
} else {
    System.err.println("Cannot claim the chunk!");
}
```

### Unclaiming a Chunk

```java
import org.bukkit.*;
import tfagaming.projects.minecraft.homestead.managers.ChunkManager;

// The chunk to unclaim
Chunk chunk = ...;

// The region which will have the chunk removed
Region region = ...;

ChunkManager.Error error = ChunkManager.unclaimChunk(region.getUniqueId(), chunk);

// If Homestead unclaimed the chunk
if (error == null) {
    System.out.println("Chunk unclaimed successfully!");    
} else {
    System.err.println("Cannot unclaim the chunk!");
}
```

### Managing Members

```java
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.managers.InviteManager;

Player target = Bukkit.getPlayer("test_account");
Region region = ...;

// Invite a player
InviteManager.invitePlayer(region, target);

// Delete invites
InviteManager.deleteInvitesOfPlayer(player); // All invites that invited the player
InviteManager.deleteInvitesOfRegion(region); // All invites that created by the region

// Get invites
InviteManager.getInvitesOfPlayer(player);
InviteManager.getInvitesOfRegion(region);

// Returns 'true' if the player is invited by that region
boolean res = InviteManager.isInvited(region, player);
```

### Getting and parsing flags

!!! success "Same Method for other Flag Classes"

    This all applies to other flag classes, including `WorldFlags` and `RegionControlFlags`. They have the same methods as `PlayerFlags`.

```java
import tfagaming.projects.minecraft.homestead.flags.*;


Region region = ...;

long flags = region.getPlayerFlags(); // Get global player flags
long flags = region.getWorldFlags(); // Get world/environment flags

List<String> flagNames = PlayerFlags.getAll(); // Get all player flag names in predefined format (lower-case and dashes)
long flag = PlayerFlags.valueOf("break-blocks"); // Get the flag value of "break-blocks"
```

### Calculating and using flags:

!!! failure "Mixing Flags"

    Never mix flags, which means, for example, don't use `WorldFlags` flags for global player flags. Each class has its own properties,
    even if they have the same value, but it will be confusing for you in the future by property names.

```java
import tfagaming.projects.minecraft.homestead.flags.*;

long flags = region.getPlayerFlags(); // Get global player flags
long newFlags;

newFlags = FlagsCalculator.addFlag(flags, PlayerFlags.BREAK_BLOCKS); // → All non-member players CAN break blocks
newFlags = FlagsCalculator.removeFlag(flags, PlayerFlags.CONTAINERS); // → All non-member players CANNOT open containers

// Check if all non-members HAS (= true) the flag "teleport-spawn"
if (FlagsCalculator.isFlagSet(flags, PlayerFlags.TELEPORT_SPAWN)) {
	// Do something
}

region.setPlayerFlags(newFlags); // Update global player flags
```

## Events

Example usage of events, list of Homestead events: [Region Events](../Events/Region%20Events.md), [Chunk Events](../Events/Chunk%20Events.md)

```java

import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import tfagaming.projects.minecraft.homestead.api.events.*;


public class HomesteadCustomEvents implements Listener {
	@EventHandler
	public void onPlayerClaimsChunk(ChunkClaimEvent event) {
		event.getRegion(); // The region
		event.getChunk(); // The chunk
	}

	@EventHandler
	public void onPlayerUnclaimEvent(ChunkUnclaimEvent event) {
        ...
	}

	@EventHandler
	public void onPlayerCreatesRegion(RegionCreateEvent event) {
        ...
	}

	@EventHandler
	public void onPlayerDeletesRegion(RegionDeleteEvent event) {
        ...
	}

	@EventHandler
	public void onPlayerBanPlayerFromRegion(BanPlayerEvent event) {
        ...
	}

	@EventHandler
	public void onPlayerUntrustFromRegion(PlayerLeftRegionEvent event) {
        ...
	}
}
```

Make sure to register the event in the main plugin's class!

```java
public void onEnable() {
    // ...
    Bukkit.getPluginManager().registerEvents(new HomesteadCustomEvents(), this);
}
```
