# General Usage

For `Region` and `War` structures, any set method is directly updating the cache. Some methods are also used externally from both of these structures, called managers.

There are 3 available managers: `RegionsManager`, `ChunksManager`, and `WarsManager`.

## Samples
### Creating a Region:

```java
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.managers.*;
import tfagaming.projects.minecraft.homestead.structure.*;

// The region name
String name = "ExampleRegion";

// The owner of the region
Player player = Bukkit.getPlayer("TFA_Gaming");

// Verify if another region has the same name ('true' is recommended)
boolean verifyName = true;

Region region = RegionsManager.createRegion(name, player, verifyName);
```

### Fetching a region:

```java
import tfagaming.projects.minecraft.homestead.managers.*;
import tfagaming.projects.minecraft.homestead.structure.*;
import java.util.UUID;

Region region;

String name = "ExampleRegion";
UUID regionId = UUID.fromString("31c0eb1d-6df9-407d-937f-de6101dd0134");

// Find a region by name (case is NOT ignored)
RegionsManager.findRegion(name);

// Find a region by UUID
RegionsManager.findRegion(regionId);
```

### Delete a Region:

```java
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.managers.*;
import tfagaming.projects.minecraft.homestead.structure.*;

// The player is the executor of the deletion
// Optional, but if not provided in parameters, the event will not be triggered
Player player = Bukkit.getPlayer("TFA_Gaming");

// Finding a region by name
Region region = RegionsManager.findRegion("ExampleRegion");

// If the region exist (not null)
if (region != null){
    RegionsManager.deleteRegion(region.getUniqueId(), player);
}
```

### Managing Members:

```java
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.structure.*;

Region region = ...;

Player player = Bukkit.getPlayer("test_account");

region.addPlayerInvite(player); // Invite a player and wait until they accept it or deny it

region.addMember(player); // Add a member to the region (without invitation)

region.removePlayerInvite(player); // Revoke an invited player's invite

// Check if the player is a member
if (region.isPlayerMember(player)) {
	region.removeMember(player); // Remove the player as member of the region
}
```

### Getting and parsing flags:

!!! success "Same Method for other Flag Classes"

    This all applies to other flag classes, including `WorldFlags` and `RegionControlFlags`. They have the same methods as `PlayerFlags`.

```java
import tfagaming.projects.minecraft.homestead.flags.*;
import tfagaming.projects.minecraft.homestead.structure.*;

Region region = ...;

long flags = region.getPlayerFlags(); // Get global player flags
long flags = region.getWorldFlags(); // Get world/environment flags
long flags = region.getMember(player).getFlags(); // Get member's player flags
long flags = region.getMember(player).getRegionControlFlags(); // Get member's region control flags

List<String> flagNames = PlayerFlags.getAll(); // Get all player flag names in predefined format (lower-case and dashes)
long flag = PlayerFlags.valueOf("break-blocks"); // Get the flag value of "break-blocks"
```

### Calculating and using flags:

!!! success "Same Method for other Flag Classes"

    This all applies to other flag classes, including `WorldFlags` and `RegionControlFlags`. They have the same methods as `PlayerFlags`.

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

