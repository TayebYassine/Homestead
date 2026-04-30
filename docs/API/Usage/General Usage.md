# General Usage

For `Region` and `War` structures, any set method is directly updating the cache. Some methods are also used externally from both of these structures, called managers.

There are 3 available managers: `RegionsManager`, `ChunksManager`, and `WarsManager`.

## Samples
### Creating a Region:

```java
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.managers.*;


// The owner of the region
Player player = Bukkit.getPlayer("TFA_Gaming");

Region region = RegionManager.createRegion("ExampleRegion", player);
```

### Fetching a region:

```java
import tfagaming.projects.minecraft.homestead.managers.*;

import java.util.UUID;

Region region;

String name = "ExampleRegion";
UUID regionId = UUID.fromString("31c0eb1d-6df9-407d-937f-de6101dd0134");

// Find a region by name (case is ignored)
RegionManager.findRegion(name);

// Find a region by UUID
RegionManager.findRegion(regionId);
```

### Delete a Region:

```java
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.managers.*;


// The player is the executor of the deletion (optional)
Player player = Bukkit.getPlayer("TFA_Gaming");

UUID regionId = UUID.fromString("31c0eb1d-6df9-407d-937f-de6101dd0134");

// If the region exist (not null)
if (region != null) {
    RegionManager.deleteRegion(regionId, player);
}
```

### Claiming a Chunk:

```java
import org.bukkit.*;
import tfagaming.projects.minecraft.homestead.managers.*;


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

### Unclaiming a Chunk:

```java
import org.bukkit.*;
import tfagaming.projects.minecraft.homestead.managers.*;


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

### Managing Members:

```java
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;


Region region = ...;

Player player = Bukkit.getPlayer("test_account");

region.addPlayerInvite(player); // Invite a player and wait until they accept it or deny it

InviteManager.deleteInvitesOfPlayer(region, player); // Add a member to the region (without invitation)

InviteManager.deleteInvitesOfPlayer(region, player); // Revoke an invited player's invite

// Check if the player is a member
if (MemberManager.isMemberOfRegion(region, player)) {
	region.removeMember(player); // Remove the player as member of the region
}
```

### Getting and parsing flags:

!!! success "Same Method for other Flag Classes"

    This all applies to other flag classes, including `WorldFlags` and `RegionControlFlags`. They have the same methods as `PlayerFlags`.

```java
import tfagaming.projects.minecraft.homestead.flags.*;


Region region = ...;

long flags = region.getPlayerFlags(); // Get global player flags
long flags = region.getWorldFlags(); // Get world/environment flags
long flags = MemberManager.getMemberOfRegion(region, player).getFlags(); // Get member's player flags
long flags = MemberManager.getMemberOfRegion(region, player).getRegionControlFlags(); // Get member's region control flags

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
		OfflinePlayer untrustedPlayer = event.getUntrustedPlayer();
		PlayerLeftRegionEvent.UntrustReason reason = event.getReason();

		switch (reason) {
			case EXECUTION -> {
				// A player (region owner for example) untrusted that player
			}
			case LEFT -> {
				// The player left the region, manually
			}
			case TAXES -> {
				// The player didn't pay taxes, left automatically
			}
		}
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
