package tfagaming.projects.minecraft.homestead.database.providers;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.models.*;
import tfagaming.projects.minecraft.homestead.models.serialize.SeBlock;
import tfagaming.projects.minecraft.homestead.models.serialize.SeLocation;
import tfagaming.projects.minecraft.homestead.models.serialize.SeRent;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class YAML implements Provider {
	private final File regionsFolder;
	private final File regionMembersFolder;
	private final File regionChunksFolder;
	private final File regionLogsFolder;
	private final File regionRatesFolder;
	private final File regionInvitesFolder;
	private final File regionBannedPlayersFolder;
	private final File warsFolder;
	private final File warRegionsFolder;
	private final File subAreasFolder;
	private final File levelsFolder;

	public YAML(File dataFolder) throws IOException {
		this.regionsFolder = prepareDataFolder(dataFolder, "regions");
		this.regionMembersFolder = prepareDataFolder(dataFolder, "region_members");
		this.regionChunksFolder = prepareDataFolder(dataFolder, "region_chunks");
		this.regionLogsFolder = prepareDataFolder(dataFolder, "region_logs");
		this.regionRatesFolder = prepareDataFolder(dataFolder, "region_rates");
		this.regionInvitesFolder = prepareDataFolder(dataFolder, "region_invites");
		this.regionBannedPlayersFolder = prepareDataFolder(dataFolder, "region_banned_players");
		this.warsFolder = prepareDataFolder(dataFolder, "wars");
		this.warRegionsFolder = prepareDataFolder(dataFolder, "war_regions");
		this.subAreasFolder = prepareDataFolder(dataFolder, "subareas");
		this.levelsFolder = prepareDataFolder(dataFolder, "levels");
	}

	private static World resolveWorld(String value) {
		if (value == null || value.isBlank()) return null;
		try {
			return Bukkit.getWorld(UUID.fromString(value.trim()));
		} catch (IllegalArgumentException ignored) {
			return Bukkit.getWorld(value.trim());
		}
	}

	private File prepareDataFolder(File dataFolder, String dirName) throws IOException {
		File dir = new File(dataFolder, dirName);
		if (!dir.exists() && !dir.mkdirs()) {
			throw new IOException("Unable to create '" + dirName + "' directory, path: " + dir.getAbsolutePath());
		}
		return dir;
	}


	@Override
	public void prepareTables() throws Exception {
		if (legacyFilesExist()) {
			migrateFromLegacy();
		}
	}

	private boolean legacyFilesExist() {
		File[] regionFiles = regionsFolder.listFiles((dir, name) -> name.startsWith("region_") && name.endsWith(".yml"));
		if (regionFiles == null || regionFiles.length == 0) return false;

		YamlConfiguration cfg = YamlConfiguration.loadConfiguration(regionFiles[0]);
		return cfg.contains("chunks") || cfg.contains("members");
	}

	private void migrateFromLegacy() throws Exception {
		Map<String, Long> regionIdMap = new HashMap<>();
		Map<String, Long> subAreaIdMap = new HashMap<>();

		List<Region> newRegions = new ArrayList<>();
		List<RegionMember> newMembers = new ArrayList<>();
		List<RegionChunk> newChunks = new ArrayList<>();
		List<RegionLog> newLogs = new ArrayList<>();
		List<RegionRate> newRates = new ArrayList<>();
		List<RegionInvite> newInvites = new ArrayList<>();
		List<RegionBannedPlayer> newBanned = new ArrayList<>();
		List<SubArea> newSubAreas = new ArrayList<>();
		List<Level> newLevels = new ArrayList<>();
		List<War> newWars = new ArrayList<>();
		Map<Long, List<Long>> warRegionMap = new LinkedHashMap<>();


		File[] regionFiles = regionsFolder.listFiles((dir, name) -> name.startsWith("region_") && name.endsWith(".yml"));
		if (regionFiles != null) {
			for (File file : regionFiles) {
				YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
				String oldId = cfg.getString("id");
				if (oldId == null) continue;
				try {
					long newId = Homestead.SNOWFLAKE.nextId();
					regionIdMap.put(oldId, newId);

					UUID ownerId = UUID.fromString(Objects.requireNonNull(cfg.getString("ownerId")));
					long createdAt = cfg.getLong("createdAt");
					Region region = new Region(newId, cfg.getString("name"), ownerId, createdAt);

					region.setDisplayName(cfg.getString("displayName"));
					region.setDescription(cfg.getString("description"));
					region.setPlayerFlags(cfg.getLong("playerFlags"));
					region.setWorldFlags(cfg.getLong("worldFlags"));
					region.setBank(cfg.getDouble("bank"));
					region.setMapColor(cfg.getInt("mapColor"));
					region.setUpkeepAt(cfg.getLong("upkeepAt"));
					region.setTaxes(cfg.getDouble("taxesAmount"));
					region.setWeather(cfg.getInt("weather"));
					region.setTime(cfg.getInt("time"));
					region.setMapIcon(cfg.getString("icon"));

					String locStr = cfg.getString("location");
					if (LegacyParsers.isNotBlank(locStr)) region.setLocation(SeLocation.deserialize(locStr));

					String wsStr = cfg.getString("welcomeSign");
					if (LegacyParsers.isNotBlank(wsStr)) region.setWelcomeSign(SeLocation.deserialize(wsStr));

					String rentStr = cfg.getString("rent");
					if (LegacyParsers.isNotBlank(rentStr)) region.setRent(SeRent.deserialize(rentStr));

					newRegions.add(region);

					for (String part : cfg.getStringList("chunks")) {
						if (part == null || part.isBlank()) continue;
						RegionChunk c = LegacyParsers.parseLegacyChunk(newId, part);
						if (c != null) newChunks.add(c);
					}

					for (String part : cfg.getStringList("members")) {
						if (part == null || part.isBlank()) continue;
						RegionMember m = LegacyParsers.parseLegacyMember(newId, RegionMember.LinkageType.REGION, part);
						if (m != null) newMembers.add(m);
					}

					for (String part : cfg.getStringList("rates")) {
						if (part == null || part.isBlank()) continue;
						RegionRate r = LegacyParsers.parseLegacyRate(newId, part);
						if (r != null) newRates.add(r);
					}

					for (String part : cfg.getStringList("invitedPlayers")) {
						if (part == null || part.isBlank()) continue;
						try {
							UUID pid = UUID.fromString(part.trim());
							newInvites.add(new RegionInvite(Homestead.SNOWFLAKE.nextId(), newId, pid, createdAt));
						} catch (IllegalArgumentException ignored) {
						}
					}

					for (String part : cfg.getStringList("bannedPlayers")) {
						if (part == null || part.isBlank()) continue;
						RegionBannedPlayer b = LegacyParsers.parseLegacyBannedPlayer(newId, part);
						if (b != null) newBanned.add(b);
					}

					for (String part : cfg.getStringList("logs")) {
						if (part == null || part.isBlank()) continue;
						RegionLog l = LegacyParsers.parseLegacyLog(newId, part);
						if (l != null) newLogs.add(l);
					}
				} catch (Exception ignored) {
				}
			}
		}


		File[] subFiles = subAreasFolder.listFiles((dir, name) -> name.startsWith("subarea_") && name.endsWith(".yml"));
		if (subFiles != null) {
			for (File file : subFiles) {
				YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
				String oldId = cfg.getString("id");
				String oldRegionId = cfg.getString("regionId");
				if (oldId == null || oldRegionId == null) continue;
				try {
					Long newRegionId = regionIdMap.get(oldRegionId);
					if (newRegionId == null) continue;

					long newSubAreaId = Homestead.SNOWFLAKE.nextId();
					subAreaIdMap.put(oldId, newSubAreaId);

					UUID worldId = LegacyParsers.resolveWorldUUID(cfg.getString("worldName"));
					if (worldId == null) continue;

					var point1 = LegacyParsers.parseLegacyBlock(worldId, cfg.getString("point1"));
					var point2 = LegacyParsers.parseLegacyBlock(worldId, cfg.getString("point2"));
					if (point1 == null || point2 == null) continue;

					String rentStr = cfg.getString("rent");
					SeRent rent = LegacyParsers.isNotBlank(rentStr) ? SeRent.deserialize(rentStr) : null;

					SubArea subArea = new SubArea(
							newSubAreaId,
							newRegionId,
							cfg.getString("name"),
							worldId,
							point1,
							point2,
							cfg.getLong("flags"),
							rent,
							cfg.getLong("createdAt"));
					newSubAreas.add(subArea);

					for (String part : cfg.getStringList("members")) {
						if (part == null || part.isBlank()) continue;
						RegionMember m = LegacyParsers.parseLegacyMember(newSubAreaId, RegionMember.LinkageType.SUBAREA, part);
						if (m != null) newMembers.add(m);
					}
				} catch (Exception ignored) {
				}
			}
		}


		File[] levelFiles = levelsFolder.listFiles((d, n) -> n.startsWith("level_") && n.endsWith(".yml"));
		if (levelFiles != null) {
			for (File file : levelFiles) {
				YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
				String oldRegionId = cfg.getString("regionId");
				if (oldRegionId == null) continue;
				try {
					Long newRegionId = regionIdMap.get(oldRegionId);
					if (newRegionId == null) continue;
					newLevels.add(new Level(
							Homestead.SNOWFLAKE.nextId(),
							newRegionId,
							cfg.getInt("level"),
							cfg.getLong("experience"),
							cfg.getLong("totalExperience"),
							cfg.getLong("createdAt")));
				} catch (Exception ignored) {
				}
			}
		}


		File[] warFiles = warsFolder.listFiles((dir, name) -> name.startsWith("war_") && name.endsWith(".yml"));
		if (warFiles != null) {
			for (File file : warFiles) {
				YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
				String oldWarId = cfg.getString("id");
				if (oldWarId == null) continue;
				try {
					long newWarId = Homestead.SNOWFLAKE.nextId();
					List<Long> mappedRegionIds = new ArrayList<>();
					for (String raw : cfg.getStringList("regions")) {
						if (raw == null || raw.isBlank()) continue;
						Long mapped = regionIdMap.get(raw.trim());
						if (mapped != null) mappedRegionIds.add(mapped);
					}
					War war = new War(
							newWarId,
							cfg.getString("name"),
							cfg.getString("displayName"),
							cfg.getString("description"),
							mappedRegionIds,
							cfg.getDouble("prize"),
							cfg.getLong("startedAt"));
					newWars.add(war);
					warRegionMap.put(newWarId, mappedRegionIds);
				} catch (Exception ignored) {
				}
			}
		}


		deleteFilesInFolder(regionsFolder, "region_", ".yml");
		deleteFilesInFolder(subAreasFolder, "subarea_", ".yml");
		deleteFilesInFolder(levelsFolder, "level_", ".yml");
		deleteFilesInFolder(warsFolder, "war_", ".yml");


		batchInsertRegions(newRegions);
		batchInsertRegionMembers(newMembers);
		batchInsertRegionChunks(newChunks);
		batchInsertRegionLogs(newLogs);
		batchInsertRegionRates(newRates);
		batchInsertRegionInvites(newInvites);
		batchInsertRegionBannedPlayers(newBanned);
		batchInsertSubAreas(newSubAreas);
		batchInsertLevels(newLevels);
		batchInsertWars(newWars);
		batchInsertWarRegions(warRegionMap);
	}

	private void deleteFilesInFolder(File folder, String prefix, String suffix) {
		File[] files = folder.listFiles((dir, name) -> name.startsWith(prefix) && name.endsWith(suffix));
		if (files != null) {
			for (File f : files) f.delete();
		}
	}


	private void batchInsertRegions(List<Region> rows) throws Exception {
		if (rows.isEmpty()) return;
		for (Region r : rows) {
			YamlConfiguration cfg = new YamlConfiguration();
			cfg.set("id", r.getUniqueId());
			cfg.set("name", r.getName());
			cfg.set("displayName", r.getDisplayName());
			cfg.set("description", r.getDescription());
			cfg.set("ownerId", r.getOwnerId().toString());
			cfg.set("location", r.getLocation() != null ? r.getLocation().serialize() : null);
			cfg.set("playerFlags", r.getPlayerFlags());
			cfg.set("worldFlags", r.getWorldFlags());
			cfg.set("taxes", r.getTaxes());
			cfg.set("bank", r.getBank());
			cfg.set("mapColor", r.getMapColor());
			cfg.set("mapIcon", r.getMapIcon());
			cfg.set("rent", r.getRent() != null ? r.getRent().serialize() : null);
			cfg.set("weather", r.getWeather());
			cfg.set("time", r.getTime());
			cfg.set("welcomeSign", r.getWelcomeSign() != null ? r.getWelcomeSign().serialize() : null);
			cfg.set("upkeepAt", r.getUpkeepAt());
			cfg.set("createdAt", r.getCreatedAt());

			File out = new File(regionsFolder, "region_" + r.getUniqueId() + ".yml");
			cfg.save(out);
		}
	}

	private void batchInsertRegionMembers(List<RegionMember> rows) throws Exception {
		if (rows.isEmpty()) return;
		for (RegionMember m : rows) {
			YamlConfiguration cfg = new YamlConfiguration();
			cfg.set("id", m.getUniqueId());
			cfg.set("playerId", m.getPlayerId().toString());
			cfg.set("linkageType", m.getLinkageType().getValue());
			cfg.set("regionId", m.getRegionId());
			cfg.set("subAreaId", m.getSubAreaId());
			cfg.set("playerFlags", m.getPlayerFlags());
			cfg.set("controlFlags", m.getControlFlags());
			cfg.set("joinedAt", m.getJoinedAt());
			cfg.set("taxesAt", m.getTaxesAt());

			File out = new File(regionMembersFolder, "region_member_" + m.getUniqueId() + ".yml");
			cfg.save(out);
		}
	}

	private void batchInsertRegionChunks(List<RegionChunk> rows) throws Exception {
		if (rows.isEmpty()) return;
		for (RegionChunk c : rows) {
			YamlConfiguration cfg = new YamlConfiguration();
			cfg.set("id", c.getUniqueId());
			cfg.set("regionId", c.getRegionId());
			cfg.set("worldId", c.getWorldId().toString());
			cfg.set("x", c.getX());
			cfg.set("z", c.getZ());
			cfg.set("claimedAt", c.getClaimedAt());
			cfg.set("forceLoaded", c.isForceLoaded());

			File out = new File(regionChunksFolder, "region_chunk_" + c.getUniqueId() + ".yml");
			cfg.save(out);
		}
	}

	private void batchInsertRegionLogs(List<RegionLog> rows) throws Exception {
		if (rows.isEmpty()) return;
		for (RegionLog l : rows) {
			YamlConfiguration cfg = new YamlConfiguration();
			cfg.set("id", l.getUniqueId());
			cfg.set("regionId", l.getRegionId());
			cfg.set("author", l.getAuthor());
			cfg.set("message", l.getMessage());
			cfg.set("sentAt", l.getSentAt());
			cfg.set("read", l.isRead());

			File out = new File(regionLogsFolder, "region_log_" + l.getUniqueId() + ".yml");
			cfg.save(out);
		}
	}

	private void batchInsertRegionRates(List<RegionRate> rows) throws Exception {
		if (rows.isEmpty()) return;
		for (RegionRate r : rows) {
			YamlConfiguration cfg = new YamlConfiguration();
			cfg.set("id", r.getUniqueId());
			cfg.set("regionId", r.getRegionId());
			cfg.set("playerId", r.getPlayerId().toString());
			cfg.set("rate", r.getRate());
			cfg.set("ratedAt", r.getRatedAt());

			File out = new File(regionRatesFolder, "region_rate_" + r.getUniqueId() + ".yml");
			cfg.save(out);
		}
	}

	private void batchInsertRegionInvites(List<RegionInvite> rows) throws Exception {
		if (rows.isEmpty()) return;
		for (RegionInvite i : rows) {
			YamlConfiguration cfg = new YamlConfiguration();
			cfg.set("id", i.getUniqueId());
			cfg.set("regionId", i.getRegionId());
			cfg.set("playerId", i.getPlayerId().toString());
			cfg.set("invitedAt", i.getInvitedAt());

			File out = new File(regionInvitesFolder, "region_invite_" + i.getUniqueId() + ".yml");
			cfg.save(out);
		}
	}

	private void batchInsertRegionBannedPlayers(List<RegionBannedPlayer> rows) throws Exception {
		if (rows.isEmpty()) return;
		for (RegionBannedPlayer b : rows) {
			YamlConfiguration cfg = new YamlConfiguration();
			cfg.set("id", b.getUniqueId());
			cfg.set("regionId", b.getRegionId());
			cfg.set("playerId", b.getPlayerId().toString());
			cfg.set("reason", b.getReason());
			cfg.set("bannedAt", b.getBannedAt());

			File out = new File(regionBannedPlayersFolder, "region_banned_player_" + b.getUniqueId() + ".yml");
			cfg.save(out);
		}
	}

	private void batchInsertSubAreas(List<SubArea> rows) throws Exception {
		if (rows.isEmpty()) return;
		for (SubArea s : rows) {
			YamlConfiguration cfg = new YamlConfiguration();
			cfg.set("id", s.getUniqueId());
			cfg.set("regionId", s.getRegionId());
			cfg.set("name", s.getName());
			cfg.set("worldId", s.getWorldId().toString());
			cfg.set("point1", s.getPoint1().serialize());
			cfg.set("point2", s.getPoint2().serialize());
			cfg.set("playerFlags", s.getPlayerFlags());
			cfg.set("rent", s.getRent() != null ? s.getRent().serialize() : null);
			cfg.set("createdAt", s.getCreatedAt());

			File out = new File(subAreasFolder, "subarea_" + s.getUniqueId() + ".yml");
			cfg.save(out);
		}
	}

	private void batchInsertLevels(List<Level> rows) throws Exception {
		if (rows.isEmpty()) return;
		for (Level l : rows) {
			YamlConfiguration cfg = new YamlConfiguration();
			cfg.set("id", l.getUniqueId());
			cfg.set("regionId", l.getRegionId());
			cfg.set("level", l.getLevel());
			cfg.set("experience", l.getExperience());
			cfg.set("totalExperience", l.getTotalExperience());
			cfg.set("createdAt", l.getCreatedAt());

			File out = new File(levelsFolder, "level_" + l.getUniqueId() + ".yml");
			cfg.save(out);
		}
	}

	private void batchInsertWars(List<War> rows) throws Exception {
		if (rows.isEmpty()) return;
		for (War w : rows) {
			YamlConfiguration cfg = new YamlConfiguration();
			cfg.set("id", w.getUniqueId());
			cfg.set("name", w.getName());
			cfg.set("displayName", w.getDisplayName());
			cfg.set("description", w.getDescription());
			cfg.set("prize", w.getPrize());
			cfg.set("startedAt", w.getStartedAt());

			File out = new File(warsFolder, "war_" + w.getUniqueId() + ".yml");
			cfg.save(out);
		}
	}

	private void batchInsertWarRegions(Map<Long, List<Long>> warRegionMap) throws Exception {
		if (warRegionMap.isEmpty()) return;
		for (var entry : warRegionMap.entrySet()) {
			for (long regionId : entry.getValue()) {
				YamlConfiguration cfg = new YamlConfiguration();
				cfg.set("warId", entry.getKey());
				cfg.set("regionId", regionId);

				File out = new File(warRegionsFolder, "war_region_" + entry.getKey() + "_" + regionId + ".yml");
				cfg.save(out);
			}
		}
	}


	@Override
	public List<Region> importRegions() throws Exception {
		List<Region> list = new ArrayList<>();
		File[] files = regionsFolder.listFiles((dir, name) -> name.startsWith("region_") && name.endsWith(".yml"));
		if (files == null) return list;

		for (File file : files) {
			YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
			long id = cfg.getLong("id");
			UUID ownerId = UUID.fromString(Objects.requireNonNull(cfg.getString("ownerId")));
			long created = cfg.getLong("createdAt");

			Region region = new Region(id, cfg.getString("name"), ownerId, created);
			region.setDisplayName(cfg.getString("displayName"));
			region.setDescription(cfg.getString("description"));
			region.setPlayerFlags(cfg.getLong("playerFlags"));
			region.setWorldFlags(cfg.getLong("worldFlags"));
			region.setTaxes(cfg.getDouble("taxes"));
			region.setBank(cfg.getDouble("bank"));
			region.setMapColor(cfg.getInt("mapColor"));
			region.setMapIcon(cfg.getString("mapIcon"));
			region.setWeather(cfg.getInt("weather"));
			region.setTime(cfg.getInt("time"));
			region.setUpkeepAt(cfg.getLong("upkeepAt"));

			String locStr = cfg.getString("location");
			if (locStr != null) region.setLocation(SeLocation.deserialize(locStr));
			String wsStr = cfg.getString("welcomeSign");
			if (wsStr != null) region.setWelcomeSign(SeLocation.deserialize(wsStr));
			String rentStr = cfg.getString("rent");
			if (rentStr != null) region.setRent(SeRent.deserialize(rentStr));

			list.add(region);
		}
		return list;
	}

	@Override
	public List<RegionMember> importRegionMembers() throws Exception {
		List<RegionMember> list = new ArrayList<>();
		File[] files = regionMembersFolder.listFiles((dir, name) -> name.startsWith("region_member_") && name.endsWith(".yml"));
		if (files == null) return list;

		for (File file : files) {
			YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
			UUID playerId = UUID.fromString(Objects.requireNonNull(cfg.getString("playerId")));
			int typeVal = cfg.getInt("linkageType");
			long regionId = cfg.getLong("regionId");
			long subAreaId = cfg.getLong("subAreaId");

			RegionMember.LinkageType type =
					typeVal == RegionMember.LinkageType.REGION.getValue()
							? RegionMember.LinkageType.REGION
							: RegionMember.LinkageType.SUBAREA;
			long linkageId = type == RegionMember.LinkageType.REGION ? regionId : subAreaId;

			RegionMember member = new RegionMember(playerId, type, linkageId);
			member.setPlayerFlags(cfg.getLong("playerFlags"));
			member.setControlFlags(cfg.getLong("controlFlags"));
			member.setJoinedAt(cfg.getLong("joinedAt"));
			member.setTaxesAt(cfg.getLong("taxesAt"));
			list.add(member);
		}
		return list;
	}

	@Override
	public List<RegionChunk> importRegionChunks() throws Exception {
		List<RegionChunk> list = new ArrayList<>();
		File[] files = regionChunksFolder.listFiles((dir, name) -> name.startsWith("region_chunk_") && name.endsWith(".yml"));
		if (files == null) return list;

		for (File file : files) {
			YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
			list.add(new RegionChunk(
					cfg.getLong("id"),
					cfg.getLong("regionId"),
					UUID.fromString(Objects.requireNonNull(cfg.getString("worldId"))),
					cfg.getInt("x"),
					cfg.getInt("z"),
					cfg.getLong("claimedAt"),
					cfg.getBoolean("forceLoaded")));
		}
		return list;
	}

	@Override
	public List<RegionLog> importRegionLogs() throws Exception {
		List<RegionLog> list = new ArrayList<>();
		File[] files = regionLogsFolder.listFiles((dir, name) -> name.startsWith("region_log_") && name.endsWith(".yml"));
		if (files == null) return list;

		for (File file : files) {
			YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
			list.add(new RegionLog(
					cfg.getLong("id"),
					cfg.getLong("regionId"),
					cfg.getString("author"),
					cfg.getString("message"),
					cfg.getLong("sentAt"),
					cfg.getBoolean("read")));
		}
		return list;
	}

	@Override
	public List<RegionRate> importRegionRates() throws Exception {
		List<RegionRate> list = new ArrayList<>();
		File[] files = regionRatesFolder.listFiles((dir, name) -> name.startsWith("region_rate_") && name.endsWith(".yml"));
		if (files == null) return list;

		for (File file : files) {
			YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
			list.add(new RegionRate(
					cfg.getLong("id"),
					cfg.getLong("regionId"),
					UUID.fromString(Objects.requireNonNull(cfg.getString("playerId"))),
					cfg.getInt("rate"),
					cfg.getLong("ratedAt")));
		}
		return list;
	}

	@Override
	public List<RegionInvite> importRegionInvites() throws Exception {
		List<RegionInvite> list = new ArrayList<>();
		File[] files = regionInvitesFolder.listFiles((dir, name) -> name.startsWith("region_invite_") && name.endsWith(".yml"));
		if (files == null) return list;

		for (File file : files) {
			YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
			list.add(new RegionInvite(
					cfg.getLong("id"),
					cfg.getLong("regionId"),
					UUID.fromString(Objects.requireNonNull(cfg.getString("playerId"))),
					cfg.getLong("invitedAt")));
		}
		return list;
	}

	@Override
	public List<RegionBannedPlayer> importRegionBannedPlayers() throws Exception {
		List<RegionBannedPlayer> list = new ArrayList<>();
		File[] files = regionBannedPlayersFolder.listFiles((dir, name) -> name.startsWith("region_banned_player_") && name.endsWith(".yml"));
		if (files == null) return list;

		for (File file : files) {
			YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
			list.add(new RegionBannedPlayer(
					cfg.getLong("id"),
					cfg.getLong("regionId"),
					UUID.fromString(Objects.requireNonNull(cfg.getString("playerId"))),
					cfg.getString("reason"),
					cfg.getLong("bannedAt")));
		}
		return list;
	}

	@Override
	public List<SubArea> importSubAreas() throws Exception {
		List<SubArea> list = new ArrayList<>();
		File[] files = subAreasFolder.listFiles((dir, name) -> name.startsWith("subarea_") && name.endsWith(".yml"));
		if (files == null) return list;

		for (File file : files) {
			YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
			UUID worldId = UUID.fromString(Objects.requireNonNull(cfg.getString("worldId")));
			SeBlock p1 = SeBlock.deserialize(cfg.getString("point1"));
			SeBlock p2 = SeBlock.deserialize(cfg.getString("point2"));
			if (p1 == null || p2 == null) continue;
			String rentStr = cfg.getString("rent");
			SeRent rent = rentStr != null ? SeRent.deserialize(rentStr) : null;
			list.add(new SubArea(
					cfg.getLong("id"),
					cfg.getLong("regionId"),
					cfg.getString("name"),
					worldId, p1, p2,
					cfg.getLong("playerFlags"),
					rent,
					cfg.getLong("createdAt")));
		}
		return list;
	}

	@Override
	public List<Level> importLevels() throws Exception {
		List<Level> list = new ArrayList<>();
		File[] files = levelsFolder.listFiles((d, n) -> n.startsWith("level_") && n.endsWith(".yml"));
		if (files == null) return list;

		for (File file : files) {
			YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
			list.add(new Level(
					cfg.getLong("id"),
					cfg.getLong("regionId"),
					cfg.getInt("level"),
					cfg.getLong("experience"),
					cfg.getLong("totalExperience"),
					cfg.getLong("createdAt")));
		}
		return list;
	}

	@Override
	public List<War> importWars() throws Exception {
		Map<Long, List<Long>> warRegions = new HashMap<>();
		File[] juncFiles = warRegionsFolder.listFiles((dir, name) -> name.startsWith("war_region_") && name.endsWith(".yml"));
		if (juncFiles != null) {
			for (File file : juncFiles) {
				YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
				warRegions.computeIfAbsent(cfg.getLong("warId"), k -> new ArrayList<>()).add(cfg.getLong("regionId"));
			}
		}

		List<War> list = new ArrayList<>();
		File[] files = warsFolder.listFiles((dir, name) -> name.startsWith("war_") && name.endsWith(".yml"));
		if (files == null) return list;

		for (File file : files) {
			YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
			long warId = cfg.getLong("id");
			list.add(new War(
					warId,
					cfg.getString("name"),
					cfg.getString("displayName"),
					cfg.getString("description"),
					warRegions.getOrDefault(warId, new ArrayList<>()),
					cfg.getDouble("prize"),
					cfg.getLong("startedAt")));
		}
		return list;
	}


	@Override
	public void exportRegions(List<Region> regions) throws Exception {
		Set<Long> existingIds = scanExistingIds(regionsFolder, "region_", ".yml");
		Set<Long> cacheIds = new HashSet<>();

		for (Region region : regions) {
			long id = region.getUniqueId();
			cacheIds.add(id);

			YamlConfiguration cfg = new YamlConfiguration();
			cfg.set("id", id);
			cfg.set("name", region.getName());
			cfg.set("displayName", region.getDisplayName());
			cfg.set("description", region.getDescription());
			cfg.set("ownerId", region.getOwnerId().toString());
			cfg.set("location", region.getLocation() != null ? region.getLocation().serialize() : null);
			cfg.set("playerFlags", region.getPlayerFlags());
			cfg.set("worldFlags", region.getWorldFlags());
			cfg.set("taxes", region.getTaxes());
			cfg.set("bank", region.getBank());
			cfg.set("mapColor", region.getMapColor());
			cfg.set("mapIcon", region.getMapIcon());
			cfg.set("rent", region.getRent() != null ? region.getRent().serialize() : null);
			cfg.set("weather", region.getWeather());
			cfg.set("time", region.getTime());
			cfg.set("welcomeSign", region.getWelcomeSign() != null ? region.getWelcomeSign().serialize() : null);
			cfg.set("upkeepAt", region.getUpkeepAt());
			cfg.set("createdAt", region.getCreatedAt());

			File out = new File(regionsFolder, "region_" + id + ".yml");
			cfg.save(out);
		}

		existingIds.removeAll(cacheIds);
		for (long deletedId : existingIds) {
			new File(regionsFolder, "region_" + deletedId + ".yml").delete();
		}
	}

	@Override
	public void exportRegionMembers(List<RegionMember> members) throws Exception {
		Set<Long> existingIds = scanExistingIds(regionMembersFolder, "region_member_", ".yml");
		Set<Long> cacheIds = new HashSet<>();

		for (RegionMember m : members) {
			long id = m.getUniqueId();
			cacheIds.add(id);

			YamlConfiguration cfg = new YamlConfiguration();
			cfg.set("id", id);
			cfg.set("playerId", m.getPlayerId().toString());
			cfg.set("linkageType", m.getLinkageType().getValue());
			cfg.set("regionId", m.getRegionId());
			cfg.set("subAreaId", m.getSubAreaId());
			cfg.set("playerFlags", m.getPlayerFlags());
			cfg.set("controlFlags", m.getControlFlags());
			cfg.set("joinedAt", m.getJoinedAt());
			cfg.set("taxesAt", m.getTaxesAt());

			File out = new File(regionMembersFolder, "region_member_" + id + ".yml");
			cfg.save(out);
		}

		existingIds.removeAll(cacheIds);
		for (long deletedId : existingIds) {
			new File(regionMembersFolder, "region_member_" + deletedId + ".yml").delete();
		}
	}

	@Override
	public void exportRegionChunks(List<RegionChunk> chunks) throws Exception {
		Set<Long> existingIds = scanExistingIds(regionChunksFolder, "region_chunk_", ".yml");
		Set<Long> cacheIds = new HashSet<>();

		for (RegionChunk c : chunks) {
			long id = c.getUniqueId();
			cacheIds.add(id);

			YamlConfiguration cfg = new YamlConfiguration();
			cfg.set("id", id);
			cfg.set("regionId", c.getRegionId());
			cfg.set("worldId", c.getWorldId().toString());
			cfg.set("x", c.getX());
			cfg.set("z", c.getZ());
			cfg.set("claimedAt", c.getClaimedAt());
			cfg.set("forceLoaded", c.isForceLoaded());

			File out = new File(regionChunksFolder, "region_chunk_" + id + ".yml");
			cfg.save(out);
		}

		existingIds.removeAll(cacheIds);
		for (long deletedId : existingIds) {
			new File(regionChunksFolder, "region_chunk_" + deletedId + ".yml").delete();
		}
	}

	@Override
	public void exportRegionLogs(List<RegionLog> logs) throws Exception {
		Set<Long> existingIds = scanExistingIds(regionLogsFolder, "region_log_", ".yml");
		Set<Long> cacheIds = new HashSet<>();

		for (RegionLog l : logs) {
			long id = l.getUniqueId();
			cacheIds.add(id);

			YamlConfiguration cfg = new YamlConfiguration();
			cfg.set("id", id);
			cfg.set("regionId", l.getRegionId());
			cfg.set("author", l.getAuthor());
			cfg.set("message", l.getMessage());
			cfg.set("sentAt", l.getSentAt());
			cfg.set("read", l.isRead());

			File out = new File(regionLogsFolder, "region_log_" + id + ".yml");
			cfg.save(out);
		}

		existingIds.removeAll(cacheIds);
		for (long deletedId : existingIds) {
			new File(regionLogsFolder, "region_log_" + deletedId + ".yml").delete();
		}
	}

	@Override
	public void exportRegionRates(List<RegionRate> rates) throws Exception {
		Set<Long> existingIds = scanExistingIds(regionRatesFolder, "region_rate_", ".yml");
		Set<Long> cacheIds = new HashSet<>();

		for (RegionRate r : rates) {
			long id = r.getUniqueId();
			cacheIds.add(id);

			YamlConfiguration cfg = new YamlConfiguration();
			cfg.set("id", id);
			cfg.set("regionId", r.getRegionId());
			cfg.set("playerId", r.getPlayerId().toString());
			cfg.set("rate", r.getRate());
			cfg.set("ratedAt", r.getRatedAt());

			File out = new File(regionRatesFolder, "region_rate_" + id + ".yml");
			cfg.save(out);
		}

		existingIds.removeAll(cacheIds);
		for (long deletedId : existingIds) {
			new File(regionRatesFolder, "region_rate_" + deletedId + ".yml").delete();
		}
	}

	@Override
	public void exportRegionInvites(List<RegionInvite> invites) throws Exception {
		Set<Long> existingIds = scanExistingIds(regionInvitesFolder, "region_invite_", ".yml");
		Set<Long> cacheIds = new HashSet<>();

		for (RegionInvite i : invites) {
			long id = i.getUniqueId();
			cacheIds.add(id);

			YamlConfiguration cfg = new YamlConfiguration();
			cfg.set("id", id);
			cfg.set("regionId", i.getRegionId());
			cfg.set("playerId", i.getPlayerId().toString());
			cfg.set("invitedAt", i.getInvitedAt());

			File out = new File(regionInvitesFolder, "region_invite_" + id + ".yml");
			cfg.save(out);
		}

		existingIds.removeAll(cacheIds);
		for (long deletedId : existingIds) {
			new File(regionInvitesFolder, "region_invite_" + deletedId + ".yml").delete();
		}
	}

	@Override
	public void exportRegionBannedPlayers(List<RegionBannedPlayer> bannedPlayers) throws Exception {
		Set<Long> existingIds = scanExistingIds(regionBannedPlayersFolder, "region_banned_player_", ".yml");
		Set<Long> cacheIds = new HashSet<>();

		for (RegionBannedPlayer b : bannedPlayers) {
			long id = b.getUniqueId();
			cacheIds.add(id);

			YamlConfiguration cfg = new YamlConfiguration();
			cfg.set("id", id);
			cfg.set("regionId", b.getRegionId());
			cfg.set("playerId", b.getPlayerId().toString());
			cfg.set("reason", b.getReason());
			cfg.set("bannedAt", b.getBannedAt());

			File out = new File(regionBannedPlayersFolder, "region_banned_player_" + id + ".yml");
			cfg.save(out);
		}

		existingIds.removeAll(cacheIds);
		for (long deletedId : existingIds) {
			new File(regionBannedPlayersFolder, "region_banned_player_" + deletedId + ".yml").delete();
		}
	}

	@Override
	public void exportSubAreas(List<SubArea> subAreas) throws Exception {
		Set<Long> existingIds = scanExistingIds(subAreasFolder, "subarea_", ".yml");
		Set<Long> cacheIds = new HashSet<>();

		for (SubArea s : subAreas) {
			long id = s.getUniqueId();
			cacheIds.add(id);

			YamlConfiguration cfg = new YamlConfiguration();
			cfg.set("id", id);
			cfg.set("regionId", s.getRegionId());
			cfg.set("name", s.getName());
			cfg.set("worldId", s.getWorldId().toString());
			cfg.set("point1", s.getPoint1().serialize());
			cfg.set("point2", s.getPoint2().serialize());
			cfg.set("playerFlags", s.getPlayerFlags());
			cfg.set("rent", s.getRent() != null ? s.getRent().serialize() : null);
			cfg.set("createdAt", s.getCreatedAt());

			File out = new File(subAreasFolder, "subarea_" + id + ".yml");
			cfg.save(out);
		}

		existingIds.removeAll(cacheIds);
		for (long deletedId : existingIds) {
			new File(subAreasFolder, "subarea_" + deletedId + ".yml").delete();
		}
	}

	@Override
	public void exportLevels(List<Level> levels) throws Exception {
		Set<Long> existingIds = scanExistingIds(levelsFolder, "level_", ".yml");
		Set<Long> cacheIds = new HashSet<>();

		for (Level l : levels) {
			long id = l.getUniqueId();
			cacheIds.add(id);

			YamlConfiguration cfg = new YamlConfiguration();
			cfg.set("id", id);
			cfg.set("regionId", l.getRegionId());
			cfg.set("level", l.getLevel());
			cfg.set("experience", l.getExperience());
			cfg.set("totalExperience", l.getTotalExperience());
			cfg.set("createdAt", l.getCreatedAt());

			File out = new File(levelsFolder, "level_" + id + ".yml");
			cfg.save(out);
		}

		existingIds.removeAll(cacheIds);
		for (long deletedId : existingIds) {
			new File(levelsFolder, "level_" + deletedId + ".yml").delete();
		}
	}

	@Override
	public void exportWars(List<War> wars) throws Exception {
		Set<Long> existingIds = scanExistingIds(warsFolder, "war_", ".yml");
		Set<Long> cacheIds = new HashSet<>();

		for (War w : wars) {
			long warId = w.getUniqueId();
			cacheIds.add(warId);

			YamlConfiguration cfg = new YamlConfiguration();
			cfg.set("id", warId);
			cfg.set("name", w.getName());
			cfg.set("displayName", w.getDisplayName());
			cfg.set("description", w.getDescription());
			cfg.set("prize", w.getPrize());
			cfg.set("startedAt", w.getStartedAt());

			File out = new File(warsFolder, "war_" + warId + ".yml");
			cfg.save(out);


			deleteJunctionFiles(warId);
			for (long regionId : w.getRegionIds()) {
				YamlConfiguration junc = new YamlConfiguration();
				junc.set("warId", warId);
				junc.set("regionId", regionId);
				File juncOut = new File(warRegionsFolder, "war_region_" + warId + "_" + regionId + ".yml");
				junc.save(juncOut);
			}
		}

		existingIds.removeAll(cacheIds);
		for (long staleId : existingIds) {
			new File(warsFolder, "war_" + staleId + ".yml").delete();
			deleteJunctionFiles(staleId);
		}
	}

	private void deleteJunctionFiles(long warId) {
		File[] files = warRegionsFolder.listFiles((dir, name) -> name.startsWith("war_region_" + warId + "_") && name.endsWith(".yml"));
		if (files != null) {
			for (File f : files) f.delete();
		}
	}


	private Set<Long> scanExistingIds(File folder, String prefix, String suffix) {
		Set<Long> ids = new HashSet<>();
		File[] files = folder.listFiles((dir, name) -> name.startsWith(prefix) && name.endsWith(suffix));
		if (files == null) return ids;
		for (File file : files) {
			try {
				String num = file.getName().replace(prefix, "").replace(suffix, "");
				ids.add(Long.parseLong(num));
			} catch (NumberFormatException ignored) {
			}
		}
		return ids;
	}

	@Override
	public long getLatency() {
		List<File> folders = List.of(
				regionsFolder, regionMembersFolder, regionChunksFolder, regionLogsFolder,
				regionRatesFolder, regionInvitesFolder, regionBannedPlayersFolder,
				warsFolder, warRegionsFolder, subAreasFolder, levelsFolder
		);

		long startTime = System.currentTimeMillis();
		for (File folder : folders) {
			File[] files = folder.listFiles();
			if (files != null) {
				@SuppressWarnings("unused")
				int count = files.length;
			}
		}
		long endTime = System.currentTimeMillis();

		return endTime - startTime;
	}

	@Override
	public void closeConnection() throws Exception {
	}
}