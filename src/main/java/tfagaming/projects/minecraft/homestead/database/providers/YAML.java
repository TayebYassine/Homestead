package tfagaming.projects.minecraft.homestead.database.providers;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import tfagaming.projects.minecraft.homestead.structure.Level;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.SubArea;
import tfagaming.projects.minecraft.homestead.structure.War;
import tfagaming.projects.minecraft.homestead.structure.serializable.*;
import tfagaming.projects.minecraft.homestead.tools.java.ListUtils;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public final class YAML implements Provider {
	private final File regionsFolder;
	private final File warsFolder;
	private final File subAreasFolder;
	private final File levelsFolder;

	public YAML(File dataFolder) throws IOException {
		this.regionsFolder = prepareDataFolder(dataFolder, "regions");
		this.warsFolder = prepareDataFolder(dataFolder, "wars");
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

		if (!dir.exists() && !dir.mkdir()) {
			throw new IOException("Unable to create '" + dirName + "' directory, path: " + dir.getAbsolutePath());
		}

		return dir;
	}

	@Override
	public List<Region> importRegions() throws Exception {
		List<Region> regions = new ArrayList<>();
		File[] regionFiles = regionsFolder
				.listFiles((dir, name) -> name.startsWith("region_") && name.endsWith(".yml"));

		if (regionFiles == null || regionFiles.length == 0) {
			return regions;
		}

		for (File file : regionFiles) {
			YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

			UUID id = UUID.fromString(Objects.requireNonNull(config.getString("id")));
			String displayName = config.getString("displayName");
			String name = config.getString("name");
			String description = config.getString("description");
			OfflinePlayer owner = tfagaming.projects.minecraft.homestead.Homestead.getInstance()
					.getOfflinePlayerSync(UUID.fromString(Objects.requireNonNull(config.getString("ownerId"))));
			SerializableLocation location = SerializableLocation.fromString(config.getString("location"));
			long createdAt = config.getLong("createdAt");
			long playerFlags = config.getLong("playerFlags");
			long worldFlags = config.getLong("worldFlags");
			double bank = config.getDouble("bank");
			int mapColor = config.getInt("mapColor");

			List<SerializableChunk> chunks = config.getStringList("chunks").stream()
					.map(SerializableChunk::fromString)
					.collect(Collectors.toList());

			List<SerializableMember> members = config.getStringList("members").stream()
					.map(SerializableMember::fromString)
					.collect(Collectors.toList());

			List<SerializableRate> rates = config.getStringList("rates").stream()
					.map(SerializableRate::fromString)
					.collect(Collectors.toList());

			List<OfflinePlayer> invitedPlayers = config.getStringList("invitedPlayers").stream()
					.map(uuidString -> tfagaming.projects.minecraft.homestead.Homestead.getInstance().getOfflinePlayerSync(UUID.fromString(uuidString)))
					.collect(Collectors.toList());

			List<SerializableBannedPlayer> bannedPlayers = config.getStringList("bannedPlayers").stream()
					.map(SerializableBannedPlayer::fromString)
					.collect(Collectors.toList());

			List<SerializableLog> logs = config.getStringList("logs").stream()
					.map(SerializableLog::fromString)
					.collect(Collectors.toList());

			SerializableRent rent = config.getString("rent") != null
					? SerializableRent.fromString(config.getString("rent"))
					: null;
			long upkeepAt = config.getLong("upkeepAt");
			double taxesAmount = config.getDouble("taxesAmount");
			int weather = config.getInt("weather");
			int time = config.getInt("time");
			SerializableLocation welcomeSign = config.getString("welcomeSign") != null
					? SerializableLocation.fromString(config.getString("welcomeSign"))
					: null;
			String icon = config.getString("icon") == null ? null : config.getString("icon");

			if (owner == null) {
				continue;
			}

			Region region = new Region(name, owner);

			region.setAutoUpdate(false);

			region.id = id;
			region.displayName = displayName;
			region.description = description;
			region.location = location;
			region.createdAt = createdAt;
			region.playerFlags = playerFlags;
			region.worldFlags = worldFlags;
			region.bank = bank;
			region.mapColor = mapColor;
			region.setChunks(chunks);
			region.setMembers(members);
			region.setRates(rates);
			region.setInvitedPlayers(ListUtils.removeNullElements(invitedPlayers));
			region.setBannedPlayers(bannedPlayers);
			region.setLogs(logs);
			region.rent = rent;
			region.upkeepAt = upkeepAt;
			region.taxesAmount = taxesAmount;
			region.weather = weather;
			region.time = time;
			region.welcomeSign = welcomeSign;
			region.icon = icon;

			region.setAutoUpdate(true);

			regions.add(region);
		}

		return regions;
	}

	@Override
	public List<War> importWars() throws Exception {
		List<War> wars = new ArrayList<>();
		File[] warFiles = warsFolder
				.listFiles((dir, name) -> name.startsWith("war_") && name.endsWith(".yml"));

		if (warFiles == null || warFiles.length == 0) {
			return wars;
		}

		for (File file : warFiles) {
			YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

			UUID id = UUID.fromString(Objects.requireNonNull(config.getString("id")));
			String displayName = config.getString("displayName");
			String name = config.getString("name");
			String description = config.getString("description");
			List<UUID> regions = config.getStringList("regions").stream()
					.map(UUID::fromString)
					.collect(Collectors.toList());
			double prize = config.getDouble("prize");
			long startedAt = config.getLong("startedAt");

			War war = new War(name, regions);
			war.id = id;
			war.displayName = displayName;
			war.description = description;
			war.prize = prize;
			war.startedAt = startedAt;

			wars.add(war);
		}

		return wars;
	}

	@Override
	public List<SubArea> importSubAreas() throws Exception {
		List<SubArea> subAreas = new ArrayList<>();
		File[] files = subAreasFolder.listFiles((dir, name) -> name.startsWith("subarea_") && name.endsWith(".yml"));

		if (files == null || files.length == 0) {
			return subAreas;
		}

		for (File file : files) {
			YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

			UUID id = UUID.fromString(Objects.requireNonNull(cfg.getString("id")));
			UUID regionId = UUID.fromString(Objects.requireNonNull(cfg.getString("regionId")));
			String name = cfg.getString("name");

			World world = resolveWorld(Objects.requireNonNull(cfg.getString("worldName")));
			if (world == null) continue;

			Block point1 = SubArea.parseBlockLocation(world, Objects.requireNonNull(cfg.getString("point1")));
			Block point2 = SubArea.parseBlockLocation(world, Objects.requireNonNull(cfg.getString("point2")));

			List<SerializableMember> members = cfg.getStringList("members").stream()
					.map(SerializableMember::fromString)
					.collect(Collectors.toList());

			long flags = cfg.getLong("flags");

			SerializableRent rent = cfg.getString("rent") != null
					? SerializableRent.fromString(cfg.getString("rent"))
					: null;

			long createdAt = cfg.getLong("createdAt");

			SubArea subArea = new SubArea(id, regionId, name, world.getUID(),
					point1, point2, members, flags, rent, createdAt);

			subAreas.add(subArea);
		}

		return subAreas;
	}

	@Override
	public List<Level> importLevels() throws Exception {
		List<Level> levels = new ArrayList<>();
		File[] files = levelsFolder.listFiles((d, n) -> n.startsWith("level_") && n.endsWith(".yml"));

		if (files == null || files.length == 0) {
			return levels;
		}

		for (File file : files) {
			YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
			UUID id = UUID.fromString(Objects.requireNonNull(cfg.getString("id")));
			UUID regionId = UUID.fromString(Objects.requireNonNull(cfg.getString("regionId")));
			int level = cfg.getInt("level");
			long xp = cfg.getLong("experience");
			long totalXp = cfg.getLong("totalExperience");
			long createdAt = cfg.getLong("createdAt");

			Level lvl = new Level(id, regionId, level, xp, totalXp, createdAt);
			levels.add(lvl);
		}

		return levels;
	}

	@Override
	public void exportRegions(List<Region> regions) throws Exception {
		Set<UUID> existingFiles = new HashSet<>();
		File[] regionFiles = regionsFolder
				.listFiles((dir, name) -> name.startsWith("region_") && name.endsWith(".yml"));
		if (regionFiles != null) {
			for (File file : regionFiles) {
				try {
					String uuidStr = file.getName().replace("region_", "").replace(".yml", "");
					existingFiles.add(UUID.fromString(uuidStr));
				} catch (IllegalArgumentException ignored) {
				}
			}
		}

		Set<UUID> cacheRegionIds = new HashSet<>();
		for (Region region : regions) {
			try {
				UUID regionId = region.id;
				cacheRegionIds.add(regionId);

				File regionFile = new File(regionsFolder, "region_" + regionId.toString() + ".yml");
				YamlConfiguration config = new YamlConfiguration();

				config.set("id", regionId.toString());
				config.set("displayName", region.displayName);
				config.set("name", region.name);
				config.set("description", region.description);
				config.set("ownerId", region.getOwnerId().toString());
				config.set("location", region.location != null ? region.location.toString() : null);
				config.set("createdAt", region.createdAt);
				config.set("playerFlags", region.playerFlags);
				config.set("worldFlags", region.worldFlags);
				config.set("bank", region.bank);
				config.set("mapColor", region.mapColor);

				config.set("chunks", region.chunks.stream()
						.map(SerializableChunk::toString)
						.collect(Collectors.toList()));

				config.set("members", region.members.stream()
						.map(SerializableMember::toString)
						.collect(Collectors.toList()));

				config.set("rates", region.rates.stream()
						.map(SerializableRate::toString)
						.collect(Collectors.toList()));

				config.set("invitedPlayers", region.getInvitedPlayers().stream()
						.map(OfflinePlayer::getUniqueId)
						.map(UUID::toString)
						.collect(Collectors.toList()));

				config.set("bannedPlayers", region.bannedPlayers.stream()
						.map(SerializableBannedPlayer::toString)
						.collect(Collectors.toList()));

				config.set("logs", region.logs.stream()
						.map(SerializableLog::toString)
						.collect(Collectors.toList()));

				config.set("rent", region.rent != null ? region.rent.toString() : null);
				config.set("upkeepAt", region.upkeepAt);
				config.set("taxesAmount", region.taxesAmount);
				config.set("weather", region.weather);
				config.set("time", region.time);
				config.set("welcomeSign", region.welcomeSign != null ? region.welcomeSign.toString() : null);
				config.set("icon", region.icon != null ? region.icon : null);

				config.save(regionFile);
			} catch (IOException e) {
				throw new SQLException("Failed to save region: " + e.getMessage(), e);
			}
		}

		existingFiles.removeAll(cacheRegionIds);
		for (UUID deletedId : existingFiles) {
			File toDelete = new File(regionsFolder, "region_" + deletedId.toString() + ".yml");
			toDelete.delete();
		}
	}

	@Override
	public void exportWars(List<War> wars) throws Exception {
		Set<UUID> existingFiles = new HashSet<>();
		File[] warFiles = warsFolder
				.listFiles((dir, name) -> name.startsWith("war_") && name.endsWith(".yml"));
		if (warFiles != null) {
			for (File file : warFiles) {
				try {
					String uuidStr = file.getName().replace("war_", "").replace(".yml", "");
					existingFiles.add(UUID.fromString(uuidStr));
				} catch (IllegalArgumentException ignored) {
				}
			}
		}

		Set<UUID> cacheWarIds = new HashSet<>();
		for (War war : wars) {
			try {
				UUID warId = war.id;
				cacheWarIds.add(warId);

				File warFile = new File(warsFolder, "war_" + warId.toString() + ".yml");
				YamlConfiguration config = new YamlConfiguration();

				config.set("id", warId.toString());
				config.set("displayName", war.displayName);
				config.set("name", war.name);
				config.set("description", war.description);
				config.set("regions", war.regions.stream()
						.map(UUID::toString)
						.collect(Collectors.toList()));
				config.set("prize", war.prize);
				config.set("startedAt", war.startedAt);

				config.save(warFile);
			} catch (IOException e) {
				throw new SQLException("Failed to save war: " + e.getMessage(), e);
			}
		}

		existingFiles.removeAll(cacheWarIds);
		for (UUID deletedId : existingFiles) {
			File toDelete = new File(warsFolder, "war_" + deletedId.toString() + ".yml");
			toDelete.delete();
		}
	}

	@Override
	public void exportSubAreas(List<SubArea> subareas) throws Exception {
		Set<UUID> existingFiles = new HashSet<>();
		File[] files = subAreasFolder.listFiles((dir, name) -> name.startsWith("subarea_") && name.endsWith(".yml"));
		if (files != null) {
			for (File file : files) {
				try {
					existingFiles.add(UUID.fromString(file.getName().replace("subarea_", "").replace(".yml", "")));
				} catch (IllegalArgumentException ignored) {
				}
			}
		}

		Set<UUID> cacheIds = new HashSet<>();
		for (SubArea sub : subareas) {
			try {
				UUID id = sub.id;
				cacheIds.add(id);

				YamlConfiguration cfg = new YamlConfiguration();
				cfg.set("id", id.toString());
				cfg.set("regionId", sub.regionId.toString());
				cfg.set("name", sub.name);
				cfg.set("worldName", sub.worldId.toString());
				cfg.set("point1", SubArea.toStringBlockLocation(sub.getWorld(), sub.point1));
				cfg.set("point2", SubArea.toStringBlockLocation(sub.getWorld(), sub.point2));
				cfg.set("members", sub.members.stream()
						.map(SerializableMember::toString)
						.collect(Collectors.toList()));
				cfg.set("flags", sub.flags);
				cfg.set("rent", sub.rent != null ? sub.rent.toString() : null);
				cfg.set("createdAt", sub.createdAt);

				File out = new File(subAreasFolder, "subarea_" + id + ".yml");
				cfg.save(out);
			} catch (IOException e) {
				throw new SQLException("Failed to save sub-area: " + e.getMessage(), e);
			}
		}

		existingFiles.removeAll(cacheIds);
		for (UUID deletedId : existingFiles) {
			File toDelete = new File(subAreasFolder, "subarea_" + deletedId + ".yml");
			toDelete.delete();
		}
	}

	@Override
	public void exportLevels(List<Level> levels) throws Exception {
		Set<UUID> existingFiles = new HashSet<>();
		File[] files = levelsFolder.listFiles((d, n) -> n.startsWith("level_") && n.endsWith(".yml"));
		if (files != null) {
			for (File f : files) {
				try {
					existingFiles.add(UUID.fromString(f.getName().replace("level_", "").replace(".yml", "")));
				} catch (IllegalArgumentException ignored) {
				}
			}
		}

		Set<UUID> cacheIds = new HashSet<>();
		for (Level lvl : levels) {
			try {
				UUID id = lvl.getUniqueId();
				cacheIds.add(id);

				YamlConfiguration cfg = new YamlConfiguration();
				cfg.set("id", id.toString());
				cfg.set("regionId", lvl.getRegionId().toString());
				cfg.set("level", lvl.getLevel());
				cfg.set("experience", lvl.getExperience());
				cfg.set("totalExperience", lvl.getTotalExperience());
				cfg.set("createdAt", lvl.getCreatedAt());

				File out = new File(levelsFolder, "level_" + id + ".yml");
				cfg.save(out);
			} catch (IOException e) {
				throw new SQLException("Failed to save level: " + e.getMessage(), e);
			}
		}

		existingFiles.removeAll(cacheIds);
		for (UUID deletedId : existingFiles) {
			File toDelete = new File(levelsFolder, "level_" + deletedId + ".yml");
			toDelete.delete();
		}
	}

	@Override
	public void prepareTables() throws Exception {
	}

	@Override
	public long getLatency() {
		List<File> folders = List.of(regionsFolder, warsFolder, subAreasFolder, levelsFolder);

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