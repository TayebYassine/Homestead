package tfagaming.projects.minecraft.homestead.managers;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.api.events.RegionCreateEvent;
import tfagaming.projects.minecraft.homestead.api.events.RegionDeleteEvent;
import tfagaming.projects.minecraft.homestead.flags.FlagsCalculator;
import tfagaming.projects.minecraft.homestead.flags.PlayerFlags;
import tfagaming.projects.minecraft.homestead.integrations.WorldEditAPI;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.serializable.*;
import tfagaming.projects.minecraft.homestead.tools.java.Formatters;
import tfagaming.projects.minecraft.homestead.tools.java.ListUtils;
import tfagaming.projects.minecraft.homestead.tools.other.UpkeepUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles creating, deleting, and updating regions.<br>
 * This is a utility class that helps manage regions more easily. Updating and setting data to regions is generally done to the {@link Region} object.
 */
public final class RegionsManager {
	private RegionsManager() { }

	/**
	 * Creates a new region owned by the given player.
	 * The region's upkeep timer is scheduled if upkeep is enabled.
	 * A {@link RegionCreateEvent} is fired on the next server tick.
	 * @param name The region name
	 * @param player The owner of the region
	 */
	public static Region createRegion(String name, OfflinePlayer player) {
		Region region = new Region(name, player);

		boolean isEnabled = Homestead.config.get("upkeep.enabled");
		int delay = Homestead.config.get("upkeep.start-upkeep");

		if (isEnabled) {
			region.setUpkeepAt(UpkeepUtils.getNewUpkeepAt() + (delay != 0 ? delay * 1000L : 0));
		}

		Homestead.regionsCache.putOrUpdate(region);

		RegionCreateEvent event = new RegionCreateEvent(region, player);
		Homestead.getInstance().runSyncTask(() -> Bukkit.getPluginManager().callEvent(event));

		return region;
	}

	/**
	 * Creates a region, optionally ensuring the name is unique by appending a counter.
	 * Upkeep and event logic is identical to {@link #createRegion(String, OfflinePlayer)}.
	 * @param name The region name
	 * @param player The owner of the region
	 * @param verifyName Verify if another region has the same name
	 */
	public static Region createRegion(String name, OfflinePlayer player, boolean verifyName) {
		if (verifyName) {
			String newName = name;
			int counter = 1;

			while (RegionsManager.isNameUsed(newName)) {
				newName = name + counter;
				counter++;
			}

			Region region = new Region(newName, player);

			if ((boolean) Homestead.config.get("upkeep.enabled")) {
				int delay = Homestead.config.get("upkeep.start-upkeep");

				region.setUpkeepAt(UpkeepUtils.getNewUpkeepAt() + (delay != 0 ? delay * 1000L : 0));
			}

			Homestead.regionsCache.putOrUpdate(region);

			return region;
		} else {
			return createRegion(name, player);
		}
	}

	/** Returns an immutable view of every loaded region. */
	public static List<Region> getAll() {
		return Homestead.regionsCache.getAll();
	}

	/**
	 * Retrieves the region with the exact UUID, or null if none exists.
	 * @param id The region UUID
	 */
	public static Region findRegion(UUID id) {
		for (Region region : getAll()) {
			if (region.getUniqueId().equals(id)) {
				return region;
			}
		}

		return null;
	}

	/**
	 * Retrieves the region with the exact name (case-insensitive), or null if none exists.
	 * @param name The region name
	 */
	public static Region findRegion(String name) {
		for (Region region : getAll()) {
			if (region.getName().equals(name)) {
				return region;
			}
		}

		return null;
	}

	/**
	 * Permanently deletes the specified region.
	 * If configured, all linked chunks are regenerated via WorldEdit.
	 * A {@link RegionDeleteEvent} is fired on the next server tick.
	 * @param id The region UUID
	 * @param player Executor (optional)
	 */
	public static void deleteRegion(UUID id, OfflinePlayer... player) {
		Region region = findRegion(id);

		if (region == null) {
			return;
		}

		if (Homestead.config.regenerateChunksWithWorldEdit()) {
			for (SerializableChunk chunk : region.getChunks()) {
				Homestead.getInstance().runAsyncTask(() -> {
					WorldEditAPI.regenerateChunk(chunk.getWorld(), chunk.getX(), chunk.getZ());
				});
			}
		}

		Homestead.regionsCache.remove(id);

		RegionDeleteEvent event = new RegionDeleteEvent(region, player.length > 0 ? player[0] : null);
		Homestead.getInstance().runSyncTask(() -> Bukkit.getPluginManager().callEvent(event));
	}

	/**
	 * Appends a human-written log entry to the region's audit trail.
	 * @param id The region UUID
	 * @param author The author
	 * @param message The message
	 */
	public static void addNewLog(UUID id, String author, String message) {
		Region region = findRegion(id);

		if (region == null) {
			return;
		}

		region.addLog(new SerializableLog(author, message));
	}

	/**
	 * Appends a translated log entry using the plugin's language file.
	 * @param id The region UUID
	 * @param messagePath The message path from the language file
	 */
	public static void addNewLog(UUID id, int messagePath) {
		Region region = findRegion(id);

		if (region == null) {
			return;
		}

		String message = Homestead.language.get("logs." + messagePath);

		region.addLog(new SerializableLog(Homestead.language.get("default.author"), message));
	}

	/**
	 * Appends a translated and token-replaced log entry.
	 * @param id The region UUID
	 * @param messagePath The message path from the language file
	 * @param replacements Replacements for variables
	 */
	public static void addNewLog(UUID id, int messagePath, Map<String, String> replacements) {
		Region region = findRegion(id);

		if (region == null) {
			return;
		}

		String message = Homestead.language.get("logs." + messagePath);

		region.addLog(new SerializableLog(Homestead.language.get("default.author"),
				Formatters.replace(message, replacements)));
	}

	/** Collects every unique owner across all regions. */
	public static List<OfflinePlayer> getAllOwners() {
		List<OfflinePlayer> players = new ArrayList<OfflinePlayer>();

		for (Region region : getAll()) {
			players.add(region.getOwner());
		}

		return ListUtils.removeDuplications(players);
	}

	/** Supplies all regions sorted alphabetically by name. */
	public static List<Region> sortRegionsAlpha() {
		List<Region> regions = getAll();

		regions.sort((r1, r2) -> r1.getName().compareToIgnoreCase(r2.getName()));

		return regions;
	}

	/** Supplies only regions that have a welcome sign configured. */
	public static List<Region> getRegionsWithWelcomeSigns() {
		List<Region> filtered = new ArrayList<>();

		for (Region region : getAll()) {
			if (region.getWelcomeSign() != null) {
				filtered.add(region);
			}
		}

		return filtered;
	}

	/** Supplies owners of regions that possess a welcome sign. */
	public static List<OfflinePlayer> getPlayersWithRegionsHasWelcomeSigns() {
		List<OfflinePlayer> filtered = new ArrayList<>();

		for (Region region : getAll()) {
			if (region.getWelcomeSign() != null) {
				filtered.add(region.getOwner());
			}
		}

		return filtered;
	}

	/**
	 * Supplies every region whose owner matches the given player.
	 * @param player The player
	 */
	public static List<Region> getRegionsOwnedByPlayer(OfflinePlayer player) {
		List<Region> regions = new ArrayList<Region>();

		for (Region region : getAll()) {
			if (region.getOwner().getUniqueId().equals(player.getUniqueId())) {
				regions.add(region);
			}
		}

		return regions;
	}

	/**
	 * Supplies every region that lists the given player as a member.
	 * @param player The player
	 */
	public static List<Region> getRegionsHasPlayerAsMember(OfflinePlayer player) {
		List<Region> regions = new ArrayList<Region>();

		for (Region region : getAll()) {
			if (region.isPlayerMember(player)) {
				regions.add(region);
			}
		}

		return regions;
	}

	/** Supplies regions flagged as public (passthrough + teleport-spawn). */
	public static List<Region> getPublicRegions() {
		List<Region> regions = new ArrayList<Region>();

		for (Region region : getAll()) {
			long flags = region.getPlayerFlags();

			if (FlagsCalculator.isFlagSet(flags, PlayerFlags.PASSTHROUGH)
					&& FlagsCalculator.isFlagSet(flags, PlayerFlags.TELEPORT_SPAWN)) {
				regions.add(region);
			}
		}

		return regions;
	}

	/**
	 * Supplies regions that have invited the given player.
	 * @param player The player
	 */
	public static List<Region> getRegionsInvitedPlayer(OfflinePlayer player) {
		List<Region> regions = new ArrayList<Region>();

		for (Region region : getAll()) {
			if (region.isPlayerInvited(player)) {
				regions.add(region);
			}
		}

		return regions;
	}

	/**
	 * Produces a list ordered by the requested metric.
	 * Ordering is descending for numeric criteria, ascending for creation date.
	 * @param type The sorting method
	 */
	public static List<Region> sortRegions(RegionSorting type) {
        return switch (type) {
            case BANK -> getAll().stream()
                    .sorted(Comparator.comparingDouble(Region::getBank).reversed())
                    .collect(Collectors.toList());
            case CHUNKS_COUNT -> getAll().stream()
                    .sorted(Comparator.comparingInt(region -> ((Region) region).getChunks().size()).reversed())
                    .collect(Collectors.toList());
            case MEMBERS_COUNT -> getAll().stream()
                    .sorted(Comparator.comparingInt((region) -> ((Region) region).getMembers().size()).reversed())
                    .collect(Collectors.toList());
            case RATING -> getAll().stream()
                    .sorted(Comparator
                            .comparingDouble((region) -> getAverageRating((Region) region))
                            .reversed())
                    .collect(Collectors.toList());
            case CREATION_DATE -> getAll().stream()
                    .sorted(Comparator.comparingLong(Region::getCreatedAt))
                    .collect(Collectors.toList());
            default -> new ArrayList<>();
        };
	}

	/** Computes the 1-based rank of a region within the given sorting; 0 if not found. */
	public static int getRank(RegionSorting type, UUID id) {
		List<Region> regions = sortRegions(type);

		for (int i = 0; i < regions.size(); i++) {
			Region region = regions.get(i);

			if (region.getUniqueId().equals(id)) {
				return i + 1;
			}
		}

		return 0;
	}

	/** Averages the region's ranks across four core metrics to give a global standing. */
	public static int getGlobalRank(UUID id) {
		return (getRank(RegionSorting.BANK, id) + getRank(RegionSorting.CHUNKS_COUNT, id)
				+ getRank(RegionSorting.MEMBERS_COUNT, id)
				+ getRank(RegionSorting.RATING, id)) / 4;
	}

	/** Checks whether any region already carries the supplied name, ignoring case. */
	public static boolean isNameUsed(String name) {
		for (Region region : getAll()) {
			if (region.getName().equalsIgnoreCase(name)) {
				return true;
			}
		}

		return false;
	}

	/** Tests whether the player's current chunk is claimed by the supplied region. */
	public static boolean isPlayerInsideRegion(Player player, Region region) {
		Chunk location = player.getLocation().getChunk();

		for (SerializableChunk chunk : region.getChunks()) {
			if (chunk.getX() == location.getX() && chunk.getZ() == location.getZ()) {
				return true;
			}
		}

		return false;
	}

	/** Calculates the mean of all player-submitted ratings for the region. */
	public static double getAverageRating(Region region) {
		List<SerializableRate> rates = region.getRates();

		if (rates == null || rates.isEmpty()) {
			return 0.0;
		}

		int totalRate = 0;
		for (SerializableRate rate : rates) {
			totalRate += rate.getRate();
		}

		return (double) totalRate / rates.size();
	}

	/**
	 * Cleans stale references during server startup:
	 * missing worlds, offline players, invalid chunks, sub-areas, spawn or welcome signs.
	 * Returns the number of corrective actions performed.
	 */
	public static int cleanStartup() {
		int updated = 0;

		for (Region region : getAll()) {
			for (SerializableMember member : region.getMembers()) {
				if (member.getBukkitOfflinePlayer() == null) {
					region.removeMember(member);
					updated++;
				}
			}

			for (SerializableBannedPlayer bannedPlayer : region.getBannedPlayers()) {
				if (bannedPlayer.getBukkitOfflinePlayer() == null) {
					region.unbanPlayer(bannedPlayer.getPlayerId());
					updated++;
				}
			}

			for (SerializableRate rate : region.getRates()) {
				OfflinePlayer rater = rate.getBukkitOfflinePlayer();

				if (rater == null) {
					region.removePlayerRate(rate.getPlayerId());
					updated++;
				}
			}

			for (SerializableChunk chunk : region.getChunks()) {
				World world = chunk.getWorld();

				if (world == null) {
					region.removeChunk(chunk);
					updated++;
				}
			}

			for (SerializableSubArea area : region.getSubAreas()) {
				World world = area.getWorld();

				if (world == null) {
					region.removeSubArea(area.getId());
					updated++;
				}
			}

			SerializableLocation spawnLoc = region.getLocation();

			if (spawnLoc != null && spawnLoc.getWorld() == null) {
				region.setLocationToNull();
				updated++;
			}

			SerializableLocation welcomeSignLoc = region.getWelcomeSign();

			if (welcomeSignLoc != null && welcomeSignLoc.getWorld() == null) {
				region.setWelcomeSign(null);
				updated++;
			}

			if (region.getOwner() == null) {
				RegionsManager.deleteRegion(region.getUniqueId());
				updated++;
			}
		}

		return updated;
	}

	/** Supported metrics for leaderboard-style sorting. */
	public enum RegionSorting {
		BANK,
		CHUNKS_COUNT,
		MEMBERS_COUNT,
		RATING,
		CREATION_DATE
	}
}
