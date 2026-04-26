package tfagaming.projects.minecraft.homestead.sessions;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.RegionsFile;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public final class TargetRegionSession {
	private static final Random random = new Random();
	public static final HashMap<UUID, Long> SESSIONS = new HashMap<UUID, Long>();

	private TargetRegionSession() {

	}

	public static void newSession(Player player, Region region) {
		SESSIONS.put(player.getUniqueId(), region.getUniqueId());
	}

	public static void newSession(Player player) {
		List<Region> regions = RegionManager.getRegionsOwnedByPlayer(player);

		if (!regions.isEmpty()) {
			SESSIONS.putIfAbsent(player.getUniqueId(), regions.getFirst().getUniqueId());
		} else {
			SESSIONS.putIfAbsent(player.getUniqueId(), -1L);
		}
	}

	public static Region getRegion(OfflinePlayer player) {
		Long session = SESSIONS.get(player.getUniqueId());
		long regionId = session == null ? -1L : session;

		Region region = RegionManager.findRegion(regionId);

		if (region == null && Resources.<RegionsFile>get(ResourceType.Regions).getBoolean("autoset-target-region") && player.isOnline() && !RegionManager.getRegionsOwnedByPlayer(player).isEmpty()) {
			randomizeRegion((Player) player);

			return getRegion(player);
		}

		return region;
	}

	public static void setRegion(OfflinePlayer player, Region region) {
		SESSIONS.put(player.getUniqueId(), region.getUniqueId());
	}

	public static void setRegion(OfflinePlayer player, String regionName) {
		Region region = RegionManager.findRegion(regionName);

		if (region == null) return;

		SESSIONS.put(player.getUniqueId(), region.getUniqueId());
	}

	public static void randomizeRegion(
			Player player) {
		List<Region> regions = RegionManager.getRegionsOwnedByPlayer(player);

		if (regions.isEmpty()) {
			SESSIONS.put(player.getUniqueId(), null);
		} else {
			int randomIndex = random.nextInt(regions.size());

			setRegion(player, regions.get(randomIndex));
		}
	}

	public static boolean hasSession(Player player) {
		return SESSIONS.containsKey(player.getUniqueId()) && getRegion(player) != null;
	}

	public static void removeSession(Player player) {
		SESSIONS.remove(player.getUniqueId());
	}
}
