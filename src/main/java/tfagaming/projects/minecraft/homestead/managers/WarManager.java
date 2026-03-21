package tfagaming.projects.minecraft.homestead.managers;

import org.bukkit.OfflinePlayer;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.logs.Logger;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.War;

import java.util.*;

public final class WarManager {
	private WarManager() {
	}

	/** @param name  The war display name
	 *  @param prize The reward given to the winning region
	 *  @param regions The participating regions (minimum 2)
	 */
	public static War declareWar(String name, double prize, List<Region> regions) {
		War war = new War(name);
		regions.forEach(war::addRegion);
		war.setPrize(prize);
		Homestead.warsCache.putOrUpdate(war);
		return war;
	}

	public static List<War> getAll() {
		return Homestead.warsCache.getAll();
	}

	/** Returns the war with the exact UUID, or null if none exists. */
	public static War findWar(UUID id) {
		return getAll().stream()
				.filter(w -> w.getUniqueId().equals(id))
				.findFirst()
				.orElse(null);
	}

	/** Returns the war with the exact name, or null if none exists. */
	public static War findWar(String name) {
		return getAll().stream()
				.filter(w -> w.getName().equals(name))
				.findFirst()
				.orElse(null);
	}

	/** Returns the war the given region is participating in, or null. */
	public static War findWarByRegionId(UUID regionId) {
		return getAll().stream()
				.filter(w -> w.getRegions().stream()
						.anyMatch(r -> r.getUniqueId().equals(regionId)))
				.findFirst()
				.orElse(null);
	}

	/** Ends and removes the war with the given UUID from cache. */
	public static void endWar(UUID id) {
		War war = findWar(id);
		if (war != null) {
			Homestead.warsCache.remove(war.getUniqueId());
		}
	}

	/**
	 * Collects all members and owners from the first two regions of a war.
	 * @param warId The war UUID
	 */
	public static List<OfflinePlayer> getMembersOfWar(UUID warId) {
		War war = findWar(warId);
		if (war == null || war.getRegions().size() < 2) {
			return Collections.emptyList();
		}

		Region first = war.getRegions().get(0);
		Region second = war.getRegions().get(1);

		Set<OfflinePlayer> players = new HashSet<>();

		first.getMembers().forEach(m -> players.add(m.bukkit()));
		second.getMembers().forEach(m -> players.add(m.bukkit()));

		players.add(first.getOwner());
		players.add(second.getOwner());

		return new ArrayList<>(players);
	}

	/** Returns true if the given player is a member or owner of any active war. */
	public static boolean isPlayerInWar(OfflinePlayer player) {
		return getAll().stream()
				.anyMatch(war -> getMembersOfWar(war.getUniqueId()).contains(player));
	}

	/**
	 * Removes the given region from whichever war it belongs to.
	 * @param regionId The region UUID to surrender
	 * @return The war the region was removed from, or null if not found
	 */
	public static War surrenderRegionFromFirstWarFound(UUID regionId) {
		for (War war : getAll()) {
			for (Region region : war.getRegions()) {
				if (region.getUniqueId().equals(regionId)) {
					war.removeRegion(region);
					if (war.getRegions().size() < 2) {
						endWar(war.getUniqueId());
					}
					return war;
				}
			}
		}
		return null;
	}

	/** Checks whether any active war already carries the supplied name. */
	public static boolean isNameUsed(String name) {
		return getAll().stream()
				.anyMatch(w -> w.getName().equalsIgnoreCase(name));
	}

	/** Returns true if the given region is currently participating in any war. */
	public static boolean isRegionInWar(UUID regionId) {
		return getAll().stream()
				.anyMatch(w -> w.getRegions().stream()
						.anyMatch(r -> r.getUniqueId().equals(regionId)));
	}

	public static void cleanStartup() {
		Logger.debug("Cleaning up wars data...");

		List<War> warsToEnd = new ArrayList<>();
		int updated = 0;

		for (War war : Homestead.warsCache.getAll()) {
			if (war.getRegions().size() < 2) {
				warsToEnd.add(war);
			}
		}

		for (War war : warsToEnd) {
			WarManager.endWar(war.getUniqueId());
			updated++;
		}

		if (updated == 0) {
			Logger.debug("No data corruption was found!");
		} else {
			Logger.debug(updated + " updates have been applied to wars data.");
		}
	}
}