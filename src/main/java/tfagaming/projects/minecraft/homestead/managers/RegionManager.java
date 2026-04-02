package tfagaming.projects.minecraft.homestead.managers;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.api.events.RegionCreateEvent;
import tfagaming.projects.minecraft.homestead.api.events.RegionDeleteEvent;
import tfagaming.projects.minecraft.homestead.integrations.WorldEditAPI;
import tfagaming.projects.minecraft.homestead.logs.Logger;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.ConfigFile;
import tfagaming.projects.minecraft.homestead.resources.files.LanguageFile;
import tfagaming.projects.minecraft.homestead.resources.files.RegionsFile;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.SubArea;
import tfagaming.projects.minecraft.homestead.structure.serializable.*;
import tfagaming.projects.minecraft.homestead.tools.java.Formatter;
import tfagaming.projects.minecraft.homestead.tools.java.ListUtils;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.other.UpkeepUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Handles creating, deleting, and updating regions.<br>
 * This is a utility class that helps manage regions more easily. Updating and setting data to regions is generally done to the {@link Region} object.
 */
public final class RegionManager {
	private RegionManager() {
	}

	/**
	 * Creates a new region owned by the given player.
	 * The region's upkeep timer is scheduled if upkeep is enabled.
	 * A {@link RegionCreateEvent} is fired on the next server tick.
	 * @param name The region name
	 * @param player The owner of the region
	 */
	public static Region createRegion(String name, OfflinePlayer player) {
		Region region = new Region(name, player);

		if (Resources.<RegionsFile>get(ResourceType.Regions).getBoolean("upkeep.enabled")) {
			int delay = Resources.<RegionsFile>get(ResourceType.Regions).getInt("upkeep.start-upkeep");

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

			while (RegionManager.isNameUsed(newName)) {
				newName = name + counter;
				counter++;
			}

			Region region = new Region(newName, player);

			if (Resources.<RegionsFile>get(ResourceType.Regions).getBoolean("upkeep.enabled")) {
				int delay = Resources.<RegionsFile>get(ResourceType.Regions).getInt("upkeep.start-upkeep");

				region.setUpkeepAt(UpkeepUtils.getNewUpkeepAt() + (delay != 0 ? delay * 1000L : 0));
			}

			Homestead.regionsCache.putOrUpdate(region);

			RegionCreateEvent event = new RegionCreateEvent(region, player);
			Homestead.getInstance().runSyncTask(() -> Bukkit.getPluginManager().callEvent(event));

			return region;
		} else {
			return createRegion(name, player);
		}
	}

	/** Returns a list of every loaded region, directly from dynamic cache. */
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
			if (region.getName().equalsIgnoreCase(name)) {
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

		for (SubArea subArea : SubAreaManager.getSubAreasOfRegion(id)) {
			SubAreaManager.deleteSubArea(subArea.getUniqueId());
		}

		if (Resources.<ConfigFile>get(ResourceType.Config).regenerateChunksWithWorldEdit()) {
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
	 * Appends a system log entry to the region using a language-file message path.
	 * @param id The region UUID
	 * @param messagePath The key under "logs." in the language file
	 */
	public static void addNewLog(UUID id, int messagePath) {
		Region region = findRegion(id);

		if (region == null) {
			return;
		}

		String message = Resources.<LanguageFile>get(ResourceType.Language).getString("logs." + messagePath);

		region.addLog(new SerializableLog(Resources.<LanguageFile>get(ResourceType.Language).getString("default.log-author"), message));
	}

	/**
	 * Appends a system log entry with placeholder substitution applied to the message.
	 * @param id The region UUID
	 * @param messagePath The key under "logs." in the language file
	 * @param placeholder Placeholder values to substitute into the message
	 */
	public static void addNewLog(UUID id, int messagePath, Placeholder placeholder) {
		Region region = findRegion(id);

		if (region == null) {
			return;
		}

		String message = Resources.<LanguageFile>get(ResourceType.Language).getString("logs." + messagePath);

		region.addLog(new SerializableLog(Resources.<LanguageFile>get(ResourceType.Language).getString("default.log-author"),
				Formatter.applyPlaceholders(message, placeholder)));
	}

	/**
	 * Merges all data from one region into another and deletes the source region.
	 * Transfers bank balance, chunks, sub-areas, and members.
	 * @param from The region to merge from (will be deleted)
	 * @param to The region to merge into
	 */
	public static void mergeRegions(Region from, Region to) {
		if (from.getUniqueId().equals(to.getUniqueId())) {
			return;
		}

		final double bank = from.getBank();
		final List<SerializableChunk> chunks = from.getChunks();
		final List<SubArea> subAreas = SubAreaManager.getSubAreasOfRegion(from.getUniqueId());
		final List<SerializableMember> members = from.getMembers();

		to.addBalanceToBank(bank);

		for (SerializableChunk chunk : chunks) {
			to.addChunk(chunk);
		}

		for (SubArea subArea : subAreas) {
			subArea.setRegionId(to.getUniqueId());
		}

		for (SerializableMember member : members) {
			to.addMember(member.bukkit());
		}

		deleteRegion(from.getUniqueId());
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
			if (region.isOwner(player)) {
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
			if (region.isPublic()) {
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
		double sum = getRank(RegionSorting.BANK, id)
				+ getRank(RegionSorting.CHUNKS_COUNT, id)
				+ getRank(RegionSorting.MEMBERS_COUNT, id)
				+ getRank(RegionSorting.RATING, id)
				+ getRank(RegionSorting.CREATION_DATE, id);

		return (int) Math.round(sum / 5.0);
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
			if (chunk.getWorldId().equals(location.getWorld().getUID()) && chunk.getX() == location.getX() && chunk.getZ() == location.getZ()) {
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
	public static void cleanStartup() {
		Logger.debug("Cleaning up regions data...");

		List<Region> regionsToDelete = new ArrayList<>();
		int updated = 0;

		for (Region region : getAll()) {
			// Members
			List<SerializableMember> membersToRemove = new ArrayList<>();
			for (SerializableMember member : region.getMembers()) {
				if (member.bukkit() == null) {
					membersToRemove.add(member);
				}
			}

			for (SerializableMember member : membersToRemove) {
				region.removeMember(member);
				updated++;
			}

			// Banned players
			List<SerializableBannedPlayer> bannedToRemove = new ArrayList<>();
			for (SerializableBannedPlayer bannedPlayer : region.getBannedPlayers()) {
				if (bannedPlayer.bukkit() == null) {
					bannedToRemove.add(bannedPlayer);
				}
			}

			for (SerializableBannedPlayer bannedPlayer : bannedToRemove) {
				region.unbanPlayer(bannedPlayer.getPlayerId());
				updated++;
			}

			// Rates
			List<SerializableRate> ratesToRemove = new ArrayList<>();
			for (SerializableRate rate : region.getRates()) {
				OfflinePlayer rater = rate.bukkit();
				if (rater == null) {
					ratesToRemove.add(rate);
				}
			}

			for (SerializableRate rate : ratesToRemove) {
				region.removePlayerRate(rate.getPlayerId());
				updated++;
			}

			// Chunks
			List<SerializableChunk> chunksToRemove = new ArrayList<>();
			for (SerializableChunk chunk : region.getChunks()) {
				World world = chunk.getWorld();

				if (world == null) {
					chunksToRemove.add(chunk);
				}
			}

			for (SerializableChunk chunk : chunksToRemove) {
				region.removeChunk(chunk);
				updated++;
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
				regionsToDelete.add(region);
			}
		}

		for (Region region : regionsToDelete) {
			RegionManager.deleteRegion(region.getUniqueId());
			updated++;
		}

		if (updated == 0) {
			Logger.debug("No data corruption was found!");
		} else {
			Logger.debug(updated + " updates have been applied to regions data.");
		}
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
