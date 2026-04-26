package tfagaming.projects.minecraft.homestead.managers;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.api.events.RegionCreateEvent;
import tfagaming.projects.minecraft.homestead.api.events.RegionDeleteEvent;
import tfagaming.projects.minecraft.homestead.integrations.FastAsyncWorldEditAPI;
import tfagaming.projects.minecraft.homestead.logs.Logger;
import tfagaming.projects.minecraft.homestead.models.*;
import tfagaming.projects.minecraft.homestead.models.serialize.SeLocation;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.ConfigFile;
import tfagaming.projects.minecraft.homestead.resources.files.RegionsFile;
import tfagaming.projects.minecraft.homestead.sessions.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.storage.StorageManager;
import tfagaming.projects.minecraft.homestead.tools.minecraft.economy.UpkeepUtility;

import java.util.*;
import java.util.stream.Collectors;

import static tfagaming.projects.minecraft.homestead.managers.RegionManager.RegionSorting.*;

/**
 * Handles creating, deleting, and updating regions.<br>
 * This is a utility class that helps manage regions more easily. Updating and setting data to regions is generally done to the {@link Region} object.
 */
public final class RegionManager {
	private RegionManager() {
	}

	/**
	 * Creates a region, optionally ensuring the name is unique by appending a counter.
	 * @param name The region name
	 * @param player The owner of the region
	 */
	public static Region createRegion(String name, OfflinePlayer player) {
		String newName = name;
		int counter = 1;

		while (RegionManager.isNameUsed(newName)) {
			newName = name + counter;
			counter++;
		}

		Region region = new Region(newName, player);

		if (Resources.<RegionsFile>get(ResourceType.Regions).getBoolean("upkeep.enabled")) {
			int delay = Resources.<RegionsFile>get(ResourceType.Regions).getInt("upkeep.start-upkeep");
			region.setUpkeepAt(UpkeepUtility.getNewUpkeepAt() + (delay != 0 ? delay * 1000L : 0));
		}

		Homestead.regionsCache.putOrUpdate(region);

		RegionCreateEvent event = new RegionCreateEvent(region, player);
		Homestead.getInstance().runSyncTask(() -> Bukkit.getPluginManager().callEvent(event));

		return region;
	}

	/** Returns a list of every loaded region, directly from dynamic cache. */
	public static List<Region> getAll() {
		return Homestead.regionsCache.getAll();
	}

	/**
	 * Retrieves the region with the exact ID, or null if none exists.
	 * @param id The region ID
	 */
	public static Region findRegion(long id) {
		return Homestead.regionsCache.get(id);
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
	 * Permanently deletes the specified region and all related data.
	 * If configured, all linked chunks are regenerated via WorldEdit.
	 * A {@link RegionDeleteEvent} is fired on the next server tick.
	 * @param id The region ID
	 * @param player Executor (optional)
	 */
	public static void deleteRegion(long id, OfflinePlayer... player) {
		Region region = findRegion(id);

		if (region == null) {
			return;
		}

		// Delete related sub-areas
		for (SubArea subArea : SubAreaManager.getSubAreasOfRegion(id)) {
			SubAreaManager.deleteSubArea(subArea.getUniqueId());
		}

		// Delete related chunks
		for (RegionChunk chunk : ChunkManager.getChunksOfRegion(id)) {
			ChunkManager.deleteChunk(chunk.getUniqueId());
		}

		// Delete related members
		for (RegionMember member : MemberManager.getMembersOfRegion(id)) {
			MemberManager.removeMember(member.getUniqueId());
		}

		// Delete related logs
		for (RegionLog log : LogManager.getLogs(id)) {
			LogManager.deleteLog(log.getUniqueId());
		}

		// Delete related rates
		RateManager.deleteAll(id);

		// Delete related invites
		InviteManager.deleteInvitesOfRegion(id);

		// Delete related banned players
		BanManager.unbanAllPlayers(id);

		// Delete related level
		LevelManager.deleteLevelByRegion(id);

		// Delete related storage
		StorageManager.deleteStorage(id);

		// Regenerate chunks if configured
		if (Resources.<ConfigFile>get(ResourceType.Config).regenerateChunksWithFAWE() && !Homestead.isFolia()) {
			Homestead.getInstance().runAsyncTask(() -> {
				for (RegionChunk chunk : ChunkManager.getChunksOfRegion(id)) {
					World world = Bukkit.getWorld(chunk.getWorldId());
					if (world != null) {
						FastAsyncWorldEditAPI.regenerateChunk(world, world.getChunkAt(chunk.getX(), chunk.getZ()));
					}
				}
			});
		}

		// Clear player sessions
		for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
			if (TargetRegionSession.hasSession(onlinePlayer) && TargetRegionSession.getRegion(onlinePlayer).getUniqueId() == id) {
				TargetRegionSession.randomizeRegion(onlinePlayer);
			}
		}

		Homestead.regionsCache.remove(id);

		RegionDeleteEvent event = new RegionDeleteEvent(region, player.length > 0 ? player[0] : null);
		Homestead.getInstance().runSyncTask(() -> Bukkit.getPluginManager().callEvent(event));
	}

	/**
	 * Merges all data from one region into another and deletes the source region.
	 * Transfers bank balance, chunks, sub-areas, and members.
	 * @param from The region to merge from (will be deleted)
	 * @param to The region to merge into
	 */
	public static void mergeRegions(Region from, Region to) {
		if (from.getUniqueId() == to.getUniqueId()) {
			return;
		}

		final double bank = from.getBank();
		final List<RegionChunk> chunks = ChunkManager.getChunksOfRegion(from.getUniqueId());
		final List<SubArea> subAreas = SubAreaManager.getSubAreasOfRegion(from.getUniqueId());
		final List<RegionMember> members = MemberManager.getMembersOfRegion(from.getUniqueId());

		to.depositBank(bank);

		for (RegionChunk chunk : chunks) {
			chunk.setRegionId(to.getUniqueId());
		}

		for (SubArea subArea : subAreas) {
			subArea.setRegionId(to.getUniqueId());
		}

		for (RegionMember member : members) {
			member.setRegionId(to.getUniqueId());
		}

		deleteRegion(from.getUniqueId());
	}

	/** Collects every unique owner across all regions. */
	public static List<OfflinePlayer> getAllOwners() {
		return getAll().stream()
				.map(Region::getOwner)
				.distinct()
				.collect(Collectors.toList());
	}

	/** Supplies all regions sorted alphabetically by name. */
	public static List<Region> sortRegionsAlpha() {
		List<Region> regions = getAll();
		regions.sort(Comparator.comparing(Region::getName, String.CASE_INSENSITIVE_ORDER));
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
		return getAll().stream()
				.filter(r -> r.isOwner(player))
				.collect(Collectors.toList());
	}

	/**
	 * Supplies every region that lists the given player as a member.
	 * @param player The player
	 */
	public static List<Region> getRegionsHasPlayerAsMember(OfflinePlayer player) {
		List<Region> regions = new ArrayList<>();
		for (Region region : getAll()) {
			if (MemberManager.isMemberOfRegion(region, player)) {
				regions.add(region);
			}
		}
		return regions;
	}

	/** Supplies regions flagged as public (passthrough + teleport-spawn). */
	public static List<Region> getPublicRegions() {
		List<Region> regions = new ArrayList<>();
		for (Region region : getAll()) {
			if (region.isPublic()) {
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
					.sorted(Comparator.<Region>comparingInt(region -> ChunkManager.getChunksOfRegion(region.getUniqueId()).size()).reversed())
					.collect(Collectors.toList());
			case MEMBERS_COUNT -> getAll().stream()
					.sorted(Comparator.<Region>comparingInt(region -> MemberManager.getMembersOfRegion(region.getUniqueId()).size()).reversed())
					.collect(Collectors.toList());
			case RATING -> getAll().stream()
					.sorted(Comparator.<Region>comparingDouble(RateManager::getAverageRating).reversed())
					.collect(Collectors.toList());
			case CREATION_DATE -> getAll().stream()
					.sorted(Comparator.comparingLong(Region::getCreatedAt))
					.collect(Collectors.toList());
			default -> new ArrayList<>();
		};
	}

	/** Computes the 1-based rank of a region within the given sorting; 0 if not found. */
	public static int getRank(RegionSorting type, long id) {
		List<Region> regions = sortRegions(type);
		for (int i = 0; i < regions.size(); i++) {
			if (regions.get(i).getUniqueId() == id) {
				return i + 1;
			}
		}
		return 0;
	}

	/** Averages the region's ranks across four core metrics to give a global standing. */
	public static int getGlobalRank(long id) {
		int sum = 0;
		for (RegionSorting type : new RegionSorting[]{BANK, CHUNKS_COUNT, MEMBERS_COUNT, RATING, CREATION_DATE}) {
			List<Region> sorted = sortRegions(type);
			for (int i = 0; i < sorted.size(); i++) {
				if (sorted.get(i).getUniqueId() == id) {
					sum += i + 1;
					break;
				}
			}
		}
		return (int) Math.round(sum / 5.0);
	}

	/** Checks whether any region already carries the supplied name, ignoring case. */
	public static boolean isNameUsed(String name) {
		return findRegion(name) != null;
	}

	/** Tests whether the player's current chunk is claimed by the supplied region. */
	public static boolean isPlayerInsideRegion(Player player, Region region) {
		Chunk location = player.getLocation().getChunk();
		for (RegionChunk chunk : ChunkManager.getChunksOfRegion(region.getUniqueId())) {
			if (chunk.getWorldId().equals(location.getWorld().getUID())
					&& chunk.getX() == location.getX()
					&& chunk.getZ() == location.getZ()) {
				return true;
			}
		}
		return false;
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
			long regionId = region.getUniqueId();

			// Clean invalid spawn location
			SeLocation spawnLoc = region.getLocation();
			if (spawnLoc != null && spawnLoc.getWorld() == null) {
				region.resetLocation();
				updated++;
			}

			// Clean invalid welcome sign
			SeLocation welcomeSignLoc = region.getWelcomeSign();
			if (welcomeSignLoc != null && welcomeSignLoc.getWorld() == null) {
				region.setWelcomeSign(null);
				updated++;
			}

			// Check owner validity
			if (region.getOwner() == null || region.getOwner().getName() == null) {
				regionsToDelete.add(region);
			}
		}

		for (Region region : regionsToDelete) {
			deleteRegion(region.getUniqueId());
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
