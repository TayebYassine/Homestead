package tfagaming.projects.minecraft.homestead.managers;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
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
import tfagaming.projects.minecraft.homestead.resources.files.FlagsFile;
import tfagaming.projects.minecraft.homestead.resources.files.LanguageFile;
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
	 * @return The created region.
	 */
	public static Region createRegion(String name, OfflinePlayer player) {
		String newName = name;
		int counter = 1;

		while (RegionManager.isNameUsed(newName)) {
			newName = name + counter;
			counter++;
		}

		Region region = new Region(newName, player);
		region.setDisplayName(newName);
		region.setDescription(Resources.<LanguageFile>get(ResourceType.Language).getString("default.region-description").replace("{owner}", region.getOwnerName()));
		region.setPlayerFlags(Resources.<FlagsFile>get(ResourceType.Flags).getDefaultPlayerFlags());
		region.setWorldFlags(Resources.<FlagsFile>get(ResourceType.Flags).getDefaultWorldFlags());

		if (Resources.<RegionsFile>get(ResourceType.Regions).getBoolean("upkeep.enabled")) {
			int delay = Resources.<RegionsFile>get(ResourceType.Regions).getInt("upkeep.start-upkeep");
			region.setUpkeepAt(UpkeepUtility.getNewUpkeepAt() + (delay != 0 ? delay * 1000L : 0));
		}

		Homestead.REGION_CACHE.putOrUpdate(region);

		RegionCreateEvent event = new RegionCreateEvent(region, player);
		Homestead.getInstance().runSyncTask(() -> Bukkit.getPluginManager().callEvent(event));

		return region;
	}

	/**
	 * Returns the total number of regions in the server.
	 * @return Region count.
	 */
	public static int getRegionCount() {
		return getAll().size();
	}

	/** Returns a list of every loaded region, directly from dynamic cache. */
	public static List<Region> getAll() {
		return Homestead.REGION_CACHE.getAll();
	}

	/**
	 * Retrieves the region with the exact ID, or null if none exists.
	 * @param id The region ID
	 * @return The Region, or {@code null}.
	 */
	public static Region findRegion(long id) {
		return Homestead.REGION_CACHE.get(id);
	}

	/**
	 * Retrieves the region with the exact name (case-insensitive), or null if none exists.
	 * @param name The region name
	 * @return The Region, or {@code null}.
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
	 * Returns all region names for tab-completion purposes.
	 * @return List of region names.
	 */
	public static List<String> getRegionNames() {
		return getAll().stream()
				.map(Region::getName)
				.collect(Collectors.toList());
	}

	/**
	 * Searches regions by name containing the given text (case-insensitive).
	 * @param query The search query
	 * @return List of matching regions.
	 */
	public static List<Region> searchRegions(String query) {
		String lower = query.toLowerCase();
		return getAll().stream()
				.filter(r -> r.getName().toLowerCase().contains(lower))
				.collect(Collectors.toList());
	}

	/**
	 * Returns a random region from the cache.
	 * @return A random region, or {@code null} if none exist.
	 */
	public static Region getRandomRegion() {
		List<Region> all = getAll();
		if (all.isEmpty()) return null;
		return all.get(new Random().nextInt(all.size()));
	}

	/**
	 * Permanently deletes the specified region and all related data.
	 * If configured, all linked chunks are regenerated via FastAsyncWorldEdit.
	 * @param id The region ID
	 * @param player Executor (optional)
	 */
	public static void deleteRegion(long id, OfflinePlayer... player) {
		Region region = findRegion(id);

		if (region == null) {
			return;
		}

		// Collect chunks before deletion for potential regeneration
		List<RegionChunk> chunksToRegen = new ArrayList<>(ChunkManager.getChunksOfRegion(id));

		// Delete related sub-areas
		SubAreaManager.deleteSubAreasOfRegion(region);

		// Delete related chunks
		for (RegionChunk chunk : ChunkManager.getChunksOfRegion(id)) {
			ChunkManager.deleteChunk(chunk.getUniqueId());
		}

		// Delete related members
		MemberManager.removeAllMembersOfRegion(region);

		// Delete related logs
		LogManager.deleteLogsOfRegion(region);

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
				for (RegionChunk chunk : chunksToRegen) {
					World world = chunk.getWorld();
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

		Homestead.REGION_CACHE.remove(id);

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

	/**
	 * Safely renames a region, ensuring the new name is unique.
	 * @param region The region to rename
	 * @param newName The desired new name
	 * @return The actual name assigned (may have counter appended).
	 */
	public static String renameRegion(Region region, String newName) {
		String actualName = newName;
		int counter = 1;

		while (isNameUsed(actualName) && !actualName.equalsIgnoreCase(region.getName())) {
			actualName = newName + counter;
			counter++;
		}

		region.setName(actualName);
		return actualName;
	}

	/**
	 * Checks if a region has no chunks and no members (essentially empty).
	 * @param region The region
	 * @return {@code true} if the region is empty.
	 */
	public static boolean isRegionEmpty(Region region) {
		return ChunkManager.getChunksOfRegion(region.getUniqueId()).isEmpty()
				&& MemberManager.getMembersOfRegion(region.getUniqueId()).isEmpty();
	}

	/**
	 * Returns how many milliseconds ago the region was created.
	 * @param region The region
	 * @return Age in milliseconds.
	 */
	public static long getRegionAge(Region region) {
		return System.currentTimeMillis() - region.getCreatedAt();
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

	/**
	 * Alias for {@link #sortRegionsAlpha()} with a more descriptive name.
	 * @return Regions sorted alphabetically.
	 */
	public static List<Region> getRegionsSortedByName() {
		return sortRegionsAlpha();
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
	 * @return List of owned regions.
	 */
	public static List<Region> getRegionsOwnedByPlayer(OfflinePlayer player) {
		return getRegionsOwnedByPlayer(player.getUniqueId());
	}

	/**
	 * Supplies every region whose owner matches the given UUID.
	 * @param ownerId The owner UUID
	 * @return List of owned regions.
	 */
	public static List<Region> getRegionsOwnedByPlayer(UUID ownerId) {
		return getAll().stream()
				.filter(r -> r.getOwnerId().equals(ownerId))
				.collect(Collectors.toList());
	}

	/**
	 * Supplies every region that lists the given player as a member.
	 * @param player The player
	 * @return List of regions.
	 */
	public static List<Region> getRegionsHasPlayerAsMember(OfflinePlayer player) {
		return getRegionsHasPlayerAsMember(player.getUniqueId());
	}

	/**
	 * Supplies every region that lists the given player as a member.
	 * @param playerId The player UUID
	 * @return List of regions.
	 */
	public static List<Region> getRegionsHasPlayerAsMember(UUID playerId) {
		List<Region> regions = new ArrayList<>();
		for (Region region : getAll()) {
			if (MemberManager.isMemberOfRegion(region.getUniqueId(), playerId)) {
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
	 * Returns all regions in a specific world.
	 * @param world The world
	 * @return List of regions with chunks in that world.
	 */
	public static List<Region> getRegionsInWorld(World world) {
		return getRegionsInWorld(world.getUID());
	}

	/**
	 * Returns all regions in a specific world.
	 * @param worldId The world UUID
	 * @return List of regions with chunks in that world.
	 */
	public static List<Region> getRegionsInWorld(UUID worldId) {
		Set<Long> regionIds = ChunkManager.getChunksInWorld(worldId).stream()
				.map(RegionChunk::getRegionId)
				.collect(Collectors.toSet());

		List<Region> regions = new ArrayList<>();
		for (Long id : regionIds) {
			Region region = findRegion(id);
			if (region != null) regions.add(region);
		}
		return regions;
	}

	/**
	 * Returns regions within a chunk radius of a location.
	 * @param location The center location
	 * @param chunkRadius The radius in chunks
	 * @return List of nearby regions.
	 */
	public static List<Region> getRegionsNearLocation(Location location, int chunkRadius) {
		Chunk center = location.getChunk();
		World world = location.getWorld();
		if (world == null) return Collections.emptyList();

		int cx = center.getX(), cz = center.getZ();
		Set<Long> nearbyRegions = new HashSet<>();

		for (int x = -chunkRadius; x <= chunkRadius; x++) {
			for (int z = -chunkRadius; z <= chunkRadius; z++) {
				Chunk chunk = world.getChunkAt(cx + x, cz + z);
				RegionChunk rc = ChunkManager.findChunk(chunk);
				if (rc != null) {
					nearbyRegions.add(rc.getRegionId());
				}
			}
		}

		List<Region> result = new ArrayList<>();
		for (Long id : nearbyRegions) {
			Region region = findRegion(id);
			if (region != null) result.add(region);
		}
		return result;
	}

	/**
	 * Returns the region that owns the chunk at the given location.
	 * @param location The location
	 * @return The region, or {@code null} if unclaimed.
	 */
	public static Region getRegionAtLocation(Location location) {
		if (location.getWorld() == null) return null;
		return getRegionAtChunk(location.getChunk());
	}

	/**
	 * Returns the region that owns the given chunk.
	 * @param chunk The chunk
	 * @return The region, or {@code null} if unclaimed.
	 */
	public static Region getRegionAtChunk(Chunk chunk) {
		RegionChunk rc = ChunkManager.findChunk(chunk);
		if (rc == null) return null;
		return findRegion(rc.getRegionId());
	}

	/**
	 * Returns regions with bank balance within a range.
	 * @param min Minimum bank balance (inclusive)
	 * @param max Maximum bank balance (inclusive)
	 * @return List of regions.
	 */
	public static List<Region> getRegionsByBankRange(double min, double max) {
		return getAll().stream()
				.filter(r -> r.getBank() >= min && r.getBank() <= max)
				.collect(Collectors.toList());
	}

	/**
	 * Returns the sum of all region bank balances.
	 * @return Total server wealth in regions.
	 */
	public static double getTotalServerBank() {
		return getAll().stream()
				.mapToDouble(Region::getBank)
				.sum();
	}

	/**
	 * Returns the average bank balance across all regions.
	 * @return Average balance, or {@code 0.0} if no regions.
	 */
	public static double getAverageBank() {
		List<Region> all = getAll();
		if (all.isEmpty()) return 0.0;
		return getTotalServerBank() / all.size();
	}

	/**
	 * Returns the richest region.
	 * @return The region with the highest bank balance, or {@code null}.
	 */
	public static Region getRichestRegion() {
		return getAll().stream()
				.max(Comparator.comparingDouble(Region::getBank))
				.orElse(null);
	}

	/**
	 * Returns the poorest region.
	 * @return The region with the lowest bank balance, or {@code null}.
	 */
	public static Region getPoorestRegion() {
		return getAll().stream()
				.min(Comparator.comparingDouble(Region::getBank))
				.orElse(null);
	}

	/**
	 * Returns regions that have at least one sub-area.
	 * @return List of regions with sub-areas.
	 */
	public static List<Region> getRegionsWithSubAreas() {
		Set<Long> regionIds = SubAreaManager.getAll().stream()
				.map(SubArea::getRegionId)
				.collect(Collectors.toSet());

		List<Region> result = new ArrayList<>();
		for (Long id : regionIds) {
			Region region = findRegion(id);
			if (region != null) result.add(region);
		}
		return result;
	}

	/**
	 * Returns regions that have at least one member.
	 * @return List of regions with members.
	 */
	public static List<Region> getRegionsWithMembers() {
		return getAll().stream()
				.filter(r -> MemberManager.getMemberCount(r) > 0)
				.collect(Collectors.toList());
	}

	/**
	 * Returns regions that have at least one ban.
	 * @return List of regions with bans.
	 */
	public static List<Region> getRegionsWithBans() {
		return getAll().stream()
				.filter(BanManager::hasActiveBans)
				.collect(Collectors.toList());
	}

	/**
	 * Returns regions that have pending invites.
	 * @return List of regions with invites.
	 */
	public static List<Region> getRegionsWithInvites() {
		return getAll().stream()
				.filter(r -> InviteManager.getInviteCount(r) > 0)
				.collect(Collectors.toList());
	}

	/**
	 * Returns regions with no activity (no logs) since a given timestamp.
	 * @param since The timestamp threshold
	 * @return List of inactive regions.
	 */
	public static List<Region> getInactiveRegions(long since) {
		return getAll().stream()
				.filter(r -> {
					RegionLog latest = LogManager.getLatestLog(r);
					return latest == null || latest.getSentAt() < since;
				})
				.collect(Collectors.toList());
	}

	/**
	 * Returns regions whose upkeep is due (upkeepAt <= current time + buffer).
	 * @param bufferMillis Buffer time in milliseconds
	 * @return List of regions needing upkeep.
	 */
	public static List<Region> getRegionsNeedingUpkeep(long bufferMillis) {
		long threshold = System.currentTimeMillis() + bufferMillis;
		return getAll().stream()
				.filter(r -> r.getUpkeepAt() <= threshold)
				.collect(Collectors.toList());
	}

	/**
	 * Checks if a region can afford its upkeep based on current bank balance.
	 * @param region The region
	 * @param upkeepCost The upkeep cost
	 * @return {@code true} if bank >= upkeep cost.
	 */
	public static boolean canAffordUpkeep(Region region, double upkeepCost) {
		return region.getBank() >= upkeepCost;
	}

	/**
	 * Produces a list ordered by the requested metric.
	 * Ordering is descending for numeric criteria, ascending for creation date.
	 * @param type The sorting method
	 * @return Sorted list of regions.
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
					.sorted(Comparator.comparingLong(Region::getCreatedAt).reversed())
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

	/**
	 * Averages the region's ranks across all metrics to give a global standing.
	 * Missing regions in a category count as last place.
	 * @param id The region ID
	 * @return Average rank across all categories.
	 */
	public static int getGlobalRank(long id) {
		Region target = findRegion(id);
		if (target == null) return 0;

		int sum = 0;
		int categories = 0;
		for (RegionSorting type : RegionSorting.values()) {
			List<Region> sorted = sortRegions(type);
			int rank = sorted.size();
			for (int i = 0; i < sorted.size(); i++) {
				if (sorted.get(i).getUniqueId() == id) {
					rank = i + 1;
					break;
				}
			}
			sum += rank;
			categories++;
		}
		return categories == 0 ? 0 : (int) Math.round((double) sum / categories);
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
	 * Removes all regions with invalid references:<br>
	 * - Owners whose UUID no longer maps to a known player<br>
	 * - Worlds that no longer exist (for spawn location and welcome sign)<br><br>
	 * Also cleans invalid spawn locations and welcome signs.
	 * @return Number of corrupted regions removed + fixes applied.
	 */
	public static int cleanupInvalidRegions() {
		List<Long> toRemove = new ArrayList<>();
		int fixes = 0;

		for (Region region : getAll()) {
			OfflinePlayer owner = region.getOwner();
			if (owner == null || owner.getName() == null) {
				toRemove.add(region.getUniqueId());
				continue;
			}

			SeLocation spawnLoc = region.getLocation();
			if (spawnLoc != null && spawnLoc.toBukkit() == null) {
				region.resetLocation();
				fixes++;
			}

			SeLocation welcomeSignLoc = region.getWelcomeSign();
			if (welcomeSignLoc != null && welcomeSignLoc.toBukkit() == null) {
				region.setWelcomeSign(null);
				fixes++;
			}
		}

		for (Long id : toRemove) {
			deleteRegion(id);
		}
		return toRemove.size() + fixes;
	}

	public enum RegionSorting {
		BANK,
		CHUNKS_COUNT,
		MEMBERS_COUNT,
		RATING,
		CREATION_DATE
	}
}