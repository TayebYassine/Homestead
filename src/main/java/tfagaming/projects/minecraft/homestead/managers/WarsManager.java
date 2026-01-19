package tfagaming.projects.minecraft.homestead.managers;

import org.bukkit.OfflinePlayer;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.logs.Logger;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.War;

import java.util.*;

public final class WarsManager {
	private WarsManager() {
	}

	public static War declareWar(String name, double prize, List<Region> regions) {
		War war = new War(name);
		regions.forEach(war::addRegion);
		war.setPrize(prize);
		return war;
	}

	public static List<War> getAll() {
		return Homestead.warsCache.getAll();
	}

	public static War findWar(UUID id) {
		return getAll().stream()
				.filter(w -> w.getUniqueId().equals(id))
				.findFirst()
				.orElse(null);
	}

	public static War findWar(String name) {
		return getAll().stream()
				.filter(w -> w.getName().equals(name))
				.findFirst()
				.orElse(null);
	}

	public static War findWarByRegionId(UUID regionId) {
		return getAll().stream()
				.filter(w -> w.getRegions().stream()
						.anyMatch(r -> r.getUniqueId().equals(regionId)))
				.findFirst()
				.orElse(null);
	}

	public static void endWar(UUID id) {
		War war = findWar(id);
		if (war != null) {
			Homestead.warsCache.remove(war.getUniqueId());
		}
	}

	public static List<OfflinePlayer> getMembersOfWar(UUID warId) {
		War war = findWar(warId);
		if (war == null || war.getRegions().size() < 2) {
			return Collections.emptyList();
		}

		Region first = war.getRegions().get(0);
		Region second = war.getRegions().get(1);

		Set<OfflinePlayer> players = new HashSet<>();

		first.getMembers().forEach(m -> players.add(m.getBukkitOfflinePlayer()));
		second.getMembers().forEach(m -> players.add(m.getBukkitOfflinePlayer()));

		players.add(first.getOwner());
		players.add(second.getOwner());

		return new ArrayList<>(players);
	}

	public static boolean isPlayerInWar(OfflinePlayer player) {
		return getAll().stream()
				.anyMatch(war -> getMembersOfWar(war.getUniqueId()).contains(player));
	}

	public static War surrenderRegionFromFirstWarFound(UUID regionId) {
		for (War war : getAll()) {
			for (Region region : war.getRegions()) {
				if (region.getUniqueId().equals(regionId)) {
					war.removeRegion(region);
					return war;
				}
			}
		}
		return null;
	}

	public static boolean isNameUsed(String name) {
		return getAll().stream()
				.anyMatch(w -> w.getName().equalsIgnoreCase(name));
	}

	public static boolean isRegionInWar(UUID regionId) {
		return getAll().stream()
				.anyMatch(w -> w.getRegions().stream()
						.anyMatch(r -> r.getUniqueId().equals(regionId)));
	}

	public static void cleanStartup() {
		Logger.warning("Cleaning up wars data...");

		int updated = 0;

		for (War war : Homestead.warsCache.getAll()) {
			if (war.getRegions().size() < 2) {
				WarsManager.endWar(war.getUniqueId());
				updated++;
			}
		}

		if (updated == 0) {
			Logger.info("No data corruption was found!");
		} else {
			Logger.info(updated + " updates have been applied to wars data.");
		}
	}
}