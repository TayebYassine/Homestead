# API Examples

Practical snippets for the most common API tasks. All examples use the published API coordinates from [Developer API](API.md).

!!! warning "Model ID Change"

    As of release 5.2.0.0, all model IDs use [Twitter Snowflake IDs](https://en.wikipedia.org/wiki/Snowflake_ID) instead of UUID v4.

---

## Creating a Region

Create a new region owned by a player:

```java
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.models.Region;

Player player = Bukkit.getPlayer("TFA_Gaming");
Region region = RegionManager.createRegion("ExampleRegion", player);
```

---

## Fetching a Region

Look up a region by name (case-insensitive) or by its numeric ID:

```java
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.models.Region;

// By name (case-insensitive)
Region region = RegionManager.findRegion("ExampleRegion");

// By ID
Region region = RegionManager.findRegion(309031393541763072L);
```

---

## Deleting a Region

Delete a region by ID after confirming it exists:

```java
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.models.Region;

long regionId = 309031393541763072L;

Region region = RegionManager.findRegion(regionId);
if (region != null) {
    RegionManager.deleteRegion(regionId);
}
```

---

## Claiming a Chunk

Claim a chunk for a region. The return value is a `ChunkManager.Error`. `null` means success:

```java
import org.bukkit.Chunk;
import tfagaming.projects.minecraft.homestead.managers.ChunkManager;
import tfagaming.projects.minecraft.homestead.models.Region;

Region region = ...;
Chunk chunk = ...;

ChunkManager.Error error = ChunkManager.claimChunk(region.getUniqueId(), chunk);

if (error == null) {
    // Success
} else {
    // Handle error (e.g. error == ChunkManager.Error.CHUNK_NOT_ADJACENT_TO_REGION)
}
```

Possible errors: `REGION_NOT_FOUND`, `CHUNK_NOT_FOUND`, `CHUNK_IN_DISABLED_WORLD`, `CHUNK_NOT_ADJACENT_TO_REGION`, `CHUNK_WOULD_SPLIT_REGION`.

---

## Unclaiming a Chunk

Remove a chunk from a region:

```java
import org.bukkit.Chunk;
import tfagaming.projects.minecraft.homestead.managers.ChunkManager;
import tfagaming.projects.minecraft.homestead.models.Region;

Region region = ...;
Chunk chunk = ...;

ChunkManager.Error error = ChunkManager.unclaimChunk(region.getUniqueId(), chunk);

if (error == null) {
    // Success
}
```

---

## Managing Members

Invite players, revoke invites, and check invitation status:

```java
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.managers.InviteManager;
import tfagaming.projects.minecraft.homestead.models.Region;

Player target = Bukkit.getPlayer("test_account");
Region region = ...;

// Invite a player
InviteManager.invitePlayer(region, target);

// Delete invites of a player
InviteManager.deleteInvitesOfPlayer(target);

// Delete invites of a region
InviteManager.deleteInvitesOfRegion(region);

// Get invites of a player
InviteManager.getInvitesOfPlayer(target);

// Get invites of a region
InviteManager.getInvitesOfRegion(region);

// Check if invited
boolean invited = InviteManager.isInvited(region, target);
```

---

## Working with Flags

Read and modify bitwise flag values on a region:

```java
import tfagaming.projects.minecraft.homestead.flags.*;
import tfagaming.projects.minecraft.homestead.models.Region;

Region region = ...;

// Get flag values
long playerFlags = region.getPlayerFlags();
long worldFlags = region.getWorldFlags();

// Get all flag names
List<String> flagNames = PlayerFlags.getFlags();

// Get a specific flag value
long flag = PlayerFlags.valueOf("break-blocks");

// Modify flags
long newFlags = FlagsCalculator.addFlag(playerFlags, PlayerFlags.BREAK_BLOCKS);
// → Non-members CAN break blocks

newFlags = FlagsCalculator.removeFlag(playerFlags, PlayerFlags.CONTAINERS);
// → Non-members CANNOT open containers

// Check if a flag is set
if (FlagsCalculator.isFlagSet(playerFlags, PlayerFlags.PVP)) {
    // PvP is allowed
}

// Apply changes
region.setPlayerFlags(newFlags);
```

!!! warning "Don't Mix Flag Types"

    Each flag class (`PlayerFlags`, `WorldFlags`, `ControlFlags`) has its own values. Don't use `WorldFlags` values for player flags.

---

## Listening to Events

Register a listener to react to Homestead events:

```java
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import tfagaming.projects.minecraft.homestead.api.events.*;

public class HomesteadListener implements Listener {

    @EventHandler
    public void onChunkClaim(ChunkClaimEvent event) {
        event.getRegion();  // The region
        event.getChunk();   // The chunk being claimed
    }

    @EventHandler
    public void onRegionCreate(RegionCreateEvent event) {
        event.getRegion();
    }

    @EventHandler
    public void onPlayerJoinRegion(PlayerJoinRegionEvent event) {
        event.getPlayer();
        event.getRegion();
    }

    @EventHandler
    public void onBankDeposit(BankDepositEvent event) {
        event.getRegion();
        event.getAmount();
    }

    @EventHandler
    public void onRegionChat(RegionChatEvent event) {
        event.getPlayer();
        event.getMessage();
        event.getRegion();
    }
}
```

### Register Your Listener

```java
public void onEnable() {
    Bukkit.getPluginManager().registerEvents(new HomesteadListener(), this);
}
```

