package tfagaming.projects.minecraft.homestead.listeners;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.RegionsFile;
import tfagaming.projects.minecraft.homestead.sessions.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.structure.Region;

public class PlayerEnterEndExitPortalListener implements Listener {
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerPortal(PlayerPortalEvent event) {
		if (!Resources.<RegionsFile>get(ResourceType.Regions).teleportPlayersBackToTegionSpawnWhenEnteringEndExitPortal()) {
			return;
		}

		if (event.getCause() == PlayerTeleportEvent.TeleportCause.END_PORTAL) {
			Player player = event.getPlayer();

			if (player.getWorld().getEnvironment() == World.Environment.THE_END) {
				Region region = TargetRegionSession.getRegion(player);

				if (region == null || region.getLocation() == null) {
					return;
				}

				Location location = region.getLocation().bukkit();

				event.setTo(location);
			}
		}
	}
}
