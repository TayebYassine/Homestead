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

	/**
	 * Declares a new war between exactly two regions.
	 *
	 * @param name    The war display name
	 * @param prize   The reward given to the winning region; must be &gt; 0
	 * @param regionA The first participating region
	 * @param regionB The second participating region
	 */
	public static War declareWar(String name, double prize, Region regionA, Region regionB) {
		if (regionA.getUniqueId().equals(regionB.getUniqueId())) {
			throw new IllegalArgumentException("A war must involve two distinct regions.");
		}

		if (isRegionInWar(regionA.getUniqueId()) || isRegionInWar(regionB.getUniqueId())) {
			throw new IllegalStateException("One of the regions is currently in a war.");
		}

		War war = new War(name);

		war.setAutoUpdate(false);

		war.addRegion(regionA);
		war.addRegion(regionB);
		war.setPrize(prize);

		war.setAutoUpdate(true);

		Homestead.warsCache.putOrUpdate(war);

		return war;
	}

	public static List<War> getAll() {
		return Homestead.warsCache.getAll();
	}

	/** Returns the war with the exact UUID, or {@code null} if none exists. */
	public static War findWar(UUID id) {
		return getAll().stream()
				.filter(w -> w.getUniqueId().equals(id))
				.findFirst()
				.orElse(null);
	}

	/** Returns the war with the exact name (case-sensitive), or {@code null} if none exists. */
	public static War findWar(String name) {
		return getAll().stream()
				.filter(w -> w.getName().equals(name))
				.findFirst()
				.orElse(null);
	}

	/** Returns the war the given region is participating in, or {@code null}. */
	public static War findWarByRegion(UUID regionId) {
		return getAll().stream()
				.filter(w -> w.getRegions().stream()
						.anyMatch(r -> r.getUniqueId().equals(regionId)))
				.findFirst()
				.orElse(null);
	}

	/** Ends and removes the war with the given UUID. */
	public static void endWar(UUID id) {
		War war = findWar(id);
		if (war != null) {
			Homestead.warsCache.remove(war.getUniqueId());
		}
	}

	/**
	 * Collects all members and owners from every region in the war.
	 *
	 * @param warId The war UUID
	 */
	public static List<OfflinePlayer> getMembersOfWar(UUID warId) {
		War war = findWar(warId);
		if (war == null || war.getRegions().size() < 2) {
			return Collections.emptyList();
		}

		Set<OfflinePlayer> players = new HashSet<>();
		for (Region region : war.getRegions()) {
			region.getMembers().forEach(m -> players.add(m.bukkit()));
			players.add(region.getOwner());
		}

		return new ArrayList<>(players);
	}

	/** Returns {@code true} if the given player is a member or owner of any active war. */
	public static boolean isPlayerInWar(OfflinePlayer player) {
		return getAll().stream()
				.anyMatch(war -> getMembersOfWar(war.getUniqueId()).contains(player));
	}

	/**
	 * Removes the given region from whichever war it belongs to, without ending the war.
	 * The caller is responsible for checking the war's state afterward and ending it if needed.
	 *
	 * @param regionId The UUID of the region to remove
	 */
	public static War removeRegionFromWar(UUID regionId) {
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

	/** Checks whether any active war already carries the supplied name (case-insensitive). */
	public static boolean isNameUsed(String name) {
		return getAll().stream()
				.anyMatch(w -> w.getName().equalsIgnoreCase(name));
	}

	/** Returns {@code true} if the given region is currently participating in any war. */
	public static boolean isRegionInWar(UUID regionId) {
		return findWarByRegion(regionId) != null;
	}

	public static void cleanStartup() {
		Logger.debug("Cleaning up wars data...");

		List<War> warsToEnd = new ArrayList<>();

		for (War war : Homestead.warsCache.getAll()) {
			if (war.getRegions().size() < 2) {
				warsToEnd.add(war);
			}
		}

		for (War war : warsToEnd) {
			WarManager.endWar(war.getUniqueId());
		}

		if (warsToEnd.isEmpty()) {
			Logger.debug("No data corruption was found!");
		} else {
			Logger.debug(warsToEnd.size() + " corrupted war(s) were removed.");
		}
	}
}