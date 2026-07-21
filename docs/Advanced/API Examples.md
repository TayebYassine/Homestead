# API Examples

!!! warning "Model ID Change"

    As of release 5.2.0.0, all model IDs use [Twitter Snowflake IDs](https://en.wikipedia.org/wiki/Snowflake_ID) instead of UUID v4.

## Creating a Region

```java
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.models.Region;

Player player = Bukkit.getPlayer("TFA_Gaming");
Region region = RegionManager.createRegion("ExampleRegion", player);
```

## Fetching a Region

```java
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.models.Region;

// By name (case-insensitive)
Region region = RegionManager.findRegion("ExampleRegion");

// By ID
Region region = RegionManager.findRegion(309031393541763072L);
```

## Deleting a Region

```java
import tfagaming.projects.minecraft.homestead.managers.RegionManager;

long regionId = 309031393541763072L;

Region region = RegionManager.findRegion(regionId);
if (region != null) {
    RegionManager.deleteRegion(regionId);
}
```

## Claiming a Chunk

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
    // Handle error
}
```

## Unclaiming a Chunk

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

## Managing Members

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
InviteManager.deleteInvitesOfPlayer(player);

// Delete invites of a region
InviteManager.deleteInvitesOfRegion(region);

// Get invites of a player
InviteManager.getInvitesOfPlayer(player);

// Get invites of a region
InviteManager.getInvitesOfRegion(region);

// Check if invited
boolean invited = InviteManager.isInvited(region, player);
```

## Working with Flags

```java
import tfagaming.projects.minecraft.homestead.flags.*;
import tfagaming.projects.minecraft.homestead.models.Region;

Region region = ...;

// Get flag values
long playerFlags = region.getPlayerFlags();
long worldFlags = region.getWorldFlags();

// Get all flag names
List<String> flagNames = PlayerFlags.getAll();

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

## Listening to Events

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
        event.getPlayer();
        event.getRegion();
        event.getAmount();
    }

    @EventHandler
    public void onRegionChat(RegionChatEvent event) {
        event.getSender();
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
