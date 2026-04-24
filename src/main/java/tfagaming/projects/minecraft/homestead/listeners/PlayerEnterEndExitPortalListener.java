package tfagaming.projects.minecraft.homestead.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.RegionsFile;
import tfagaming.projects.minecraft.homestead.sessions.TargetRegionSession;

import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtility;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Solution to {@link PlayerPortalEvent} since it doesn't emit when a player enters the Exit Portal,
 * which is weird. This listener will save players locations in the End when they're near to the
 * Exit Portal (or End Fountain) by 8 blocks radius. If a player changes the world from End to any,
 * and near the Exit Portal, and the last location's block is {@code END_PORTAL} ({@link Material}), it will teleport them
 * back to their region spawn (depending on the server settings).
 */
public class PlayerEnterEndExitPortalListener implements Listener {

	private static final Map<UUID, Location> lastLocations = new ConcurrentHashMap<>();

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();

		if (player.getWorld().getEnvironment() != World.Environment.THE_END) {
			lastLocations.remove(player.getUniqueId());
			return;
		}

		Location loc = event.getTo();

		double distanceSquared = loc.getX() * loc.getX() + loc.getZ() * loc.getZ();
		if (distanceSquared <= 64.0) {
			lastLocations.put(player.getUniqueId(), loc.clone());
		} else {
			lastLocations.remove(player.getUniqueId());
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
		Player player = event.getPlayer();

		if (event.getFrom().getEnvironment() != World.Environment.THE_END) {
			return;
		}

		Location lastLoc = lastLocations.remove(player.getUniqueId());

		if (lastLoc == null) {
			return;
		}

		Block blockAt = lastLoc.getBlock();

		if (blockAt.getType() != Material.END_PORTAL) {
			return;
		}

		if (!Resources.<RegionsFile>get(ResourceType.Regions)
				.teleportPlayersBackToTegionSpawnWhenEnteringEndExitPortal()) {
			return;
		}

		Region region = TargetRegionSession.getRegion(player);

		if (region == null || region.getLocation() == null) {
			return;
		}

		Location targetLocation = region.getLocation().bukkit();

		PlayerUtility.teleportPlayer(player, targetLocation);
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		lastLocations.remove(event.getPlayer().getUniqueId());
	}
}
