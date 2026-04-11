package tfagaming.projects.minecraft.homestead.database.providers;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.structure.Level;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.SubArea;
import tfagaming.projects.minecraft.homestead.structure.War;
import tfagaming.projects.minecraft.homestead.structure.serializable.*;
import tfagaming.projects.minecraft.homestead.tools.java.ListUtils;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public final class MySQL implements Provider {
	private static final String JDBC_URL = "jdbc:mysql://";
	private final String TABLE_PREFIX;
	private final Connection connection;

	public MySQL(String username, String password, String host, int port, String database, String tablePrefix, String parameters) throws ClassNotFoundException, SQLException {
		TABLE_PREFIX = tablePrefix.replaceAll("[^A-Za-z0-9_]", "");

		Class.forName("com.mysql.cj.jdbc.Driver");

		String connectionUrl = JDBC_URL + host + ":" + port + "/" + database + parameters;
		this.connection = DriverManager.getConnection(connectionUrl, username, password);

		prepareTables();
	}

	private static World resolveWorld(String value) {
		if (value == null || value.isBlank()) return null;
		try {
			return Bukkit.getWorld(UUID.fromString(value.trim()));
		} catch (IllegalArgumentException ignored) {
			return Bukkit.getWorld(value.trim());
		}
	}

	@Override
	public List<Region> importRegions() throws SQLException {
		List<Region> regions = new ArrayList<>();
		String sql = "SELECT * FROM " + TABLE_PREFIX + "regions";

		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery(sql)) {

			while (rs.next()) {
				UUID id = UUID.fromString(rs.getString("id"));
				String displayName = rs.getString("displayName");
				String name = rs.getString("name");
				String description = rs.getString("description");
				OfflinePlayer owner = Homestead.getInstance()
						.getOfflinePlayerSync(UUID.fromString(rs.getString("ownerId")));
				SerializableLocation location = SerializableLocation
						.fromString(rs.getString("location"));
				long createdAt = rs.getLong("createdAt");
				long playerFlags = rs.getLong("playerFlags");
				long worldFlags = rs.getLong("worldFlags");
				double bank = rs.getDouble("bank");
				int mapColor = rs.getInt("mapColor");
				List<SerializableChunk> chunks = !rs.getString("chunks").isEmpty()
						? Arrays.stream(rs.getString("chunks").split("§"))
						.map(SerializableChunk::fromString).collect(Collectors.toList())
						: new ArrayList<>();

				List<SerializableMember> members = !rs.getString("members").isEmpty()
						? Arrays.stream(rs.getString("members").split("§"))
						.map(SerializableMember::fromString).collect(Collectors.toList())
						: new ArrayList<>();

				List<SerializableRate> rates = !rs.getString("rates").isEmpty()
						? Arrays.stream(rs.getString("rates").split("§"))
						.map(SerializableRate::fromString).collect(Collectors.toList())
						: new ArrayList<>();

				List<OfflinePlayer> invitedPlayers = !rs.getString("invitedPlayers").isEmpty()
						? Arrays.stream(rs.getString("invitedPlayers").split("§"))
						.map((uuidString) -> Homestead.getInstance()
								.getOfflinePlayerSync(UUID.fromString(uuidString)))
						.collect(Collectors.toList())
						: new ArrayList<>();

				List<SerializableBannedPlayer> bannedPlayers = !rs.getString("bannedPlayers").isEmpty()
						? Arrays.stream(rs.getString("bannedPlayers")
								.split("§"))
						.map(SerializableBannedPlayer::fromString)
						.collect(Collectors.toList())
						: new ArrayList<>();

				List<SerializableLog> logs = !rs.getString("logs").isEmpty()
						? Arrays.stream(rs.getString("logs").split("µ"))
						.map(SerializableLog::fromString).collect(Collectors.toList())
						: new ArrayList<>();

				SerializableRent rent = SerializableRent.fromString(rs.getString("rent"));

				long upkeepAt = rs.getLong("upkeepAt");
				double taxesAmount = rs.getDouble("taxesAmount");
				int weather = rs.getInt("weather");
				int time = rs.getInt("time");
				SerializableLocation welcomeSign = rs.getString("welcomeSign") == null ? null
						: SerializableLocation.fromString(rs.getString("welcomeSign"));
				String icon = rs.getString("icon") == null ? null : rs.getString("icon");

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
		}

		return regions;
	}

	@Override
	public List<War> importWars() throws SQLException {
		List<War> wars = new ArrayList<>();
		String sql = "SELECT * FROM " + TABLE_PREFIX + "wars";

		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery(sql)) {

			while (rs.next()) {
				UUID id = UUID.fromString(rs.getString("id"));
				String displayName = rs.getString("displayName");
				String name = rs.getString("name");
				String description = rs.getString("description");
				List<UUID> regions = !rs.getString("regions").isEmpty()
						? Arrays.stream(rs.getString("regions").split("§"))
						.map(UUID::fromString).collect(Collectors.toList())
						: new ArrayList<>();
				double prize = rs.getDouble("prize");
				long startedAt = rs.getLong("startedAt");

				War war = new War(name, regions);
				war.id = id;
				war.displayName = displayName;
				war.description = description;
				war.prize = prize;
				war.startedAt = startedAt;

				wars.add(war);
			}
		}

		return wars;
	}

	@Override
	public List<SubArea> importSubAreas() throws SQLException {
		List<SubArea> subAreas = new ArrayList<>();
		String sql = "SELECT * FROM " + TABLE_PREFIX + "subareas";

		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery(sql)) {

			while (rs.next()) {
				UUID id = UUID.fromString(rs.getString("id"));
				UUID regionId = UUID.fromString(rs.getString("regionId"));
				String name = rs.getString("name");

				World world = resolveWorld(rs.getString("worldName"));

				if (world == null) {
					continue;
				}

				Block point1 = SubArea.parseBlockLocation(world, rs.getString("point1"));
				Block point2 = SubArea.parseBlockLocation(world, rs.getString("point2"));

				List<SerializableMember> members =
						!rs.getString("members").isEmpty()
								? Arrays.stream(rs.getString("members").split("§"))
								.map(SerializableMember::fromString)
								.collect(Collectors.toList())
								: new ArrayList<>();

				long flags = rs.getLong("flags");

				SerializableRent rent = rs.getString("rent") != null ? SerializableRent.fromString(rs.getString("rent"))
						: null;

				long createdAt = rs.getLong("createdAt");

				SubArea subArea = new SubArea(id, regionId, name, world.getUID(),
						point1, point2, members, flags, rent, createdAt);

				subAreas.add(subArea);
			}
		}

		return subAreas;
	}

	@Override
	public List<Level> importLevels() throws SQLException {
		List<Level> levels = new ArrayList<>();
		String sql = "SELECT * FROM " + TABLE_PREFIX + "levels";

		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery(sql)) {

			while (rs.next()) {
				UUID id = UUID.fromString(rs.getString("id"));
				UUID regionId = UUID.fromString(rs.getString("regionId"));
				int level = rs.getInt("level");
				long xp = rs.getLong("experience");
				long totalXp = rs.getLong("totalExperience");
				long createdAt = rs.getLong("createdAt");

				Level lvl = new Level(id, regionId, level, xp, totalXp, createdAt);
				levels.add(lvl);
			}
		}

		return levels;
	}

	@Override
	public void exportRegions(List<Region> regions) throws SQLException {
		Set<UUID> dbRegionIds = new HashSet<>();
		String selectSql = "SELECT id FROM " + TABLE_PREFIX + "regions";

		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery(selectSql)) {
			while (rs.next()) {
				dbRegionIds.add(UUID.fromString(rs.getString("id")));
			}
		}

		String upsertSql = "INSERT INTO " + TABLE_PREFIX + "regions (" +
				"id, displayName, name, description, ownerId, location, createdAt, " +
				"playerFlags, worldFlags, bank, mapColor, chunks, members, rates, " +
				"invitedPlayers, bannedPlayers, logs, rent, upkeepAt, taxesAmount, weather, " +
				"time, welcomeSign, icon" +
				") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
				"ON DUPLICATE KEY UPDATE " +
				"displayName = VALUES(displayName), " +
				"name = VALUES(name), " +
				"description = VALUES(description), " +
				"ownerId = VALUES(ownerId), " +
				"location = VALUES(location), " +
				"createdAt = VALUES(createdAt), " +
				"playerFlags = VALUES(playerFlags), " +
				"worldFlags = VALUES(worldFlags), " +
				"bank = VALUES(bank), " +
				"mapColor = VALUES(mapColor), " +
				"chunks = VALUES(chunks), " +
				"members = VALUES(members), " +
				"rates = VALUES(rates), " +
				"invitedPlayers = VALUES(invitedPlayers), " +
				"bannedPlayers = VALUES(bannedPlayers), " +
				"logs = VALUES(logs), " +
				"rent = VALUES(rent), " +
				"upkeepAt = VALUES(upkeepAt), " +
				"taxesAmount = VALUES(taxesAmount), " +
				"weather = VALUES(weather), " +
				"time = VALUES(time), " +
				"welcomeSign = VALUES(welcomeSign), " +
				"icon = VALUES(icon)";

		String deleteSql = "DELETE FROM " + TABLE_PREFIX + "regions WHERE id = ?";

		connection.setAutoCommit(false);

		try (PreparedStatement upsertStmt = connection.prepareStatement(upsertSql);
			 PreparedStatement deleteStmt = connection.prepareStatement(deleteSql)) {
			Set<UUID> cacheRegionIds = new HashSet<>();

			for (Region region : regions) {
				UUID regionId = region.id;
				cacheRegionIds.add(regionId);

				String chunksStr = region.chunks.stream().map(SerializableChunk::toString).collect(Collectors.joining("§"));
				String membersStr = region.members.stream().map(SerializableMember::toString).collect(Collectors.joining("§"));
				String ratesStr = region.rates.stream().map(SerializableRate::toString).collect(Collectors.joining("§"));
				String invitedStr = region.getInvitedPlayers().stream().map(OfflinePlayer::getUniqueId)
						.map(UUID::toString).collect(Collectors.joining("§"));
				String bannedStr = region.bannedPlayers.stream().map(SerializableBannedPlayer::toString)
						.collect(Collectors.joining("§"));
				String logsStr = region.logs.stream().map(SerializableLog::toString).collect(Collectors.joining("µ"));

				upsertStmt.setString(1, regionId.toString());
				upsertStmt.setString(2, region.displayName);
				upsertStmt.setString(3, region.name);
				upsertStmt.setString(4, region.description);
				upsertStmt.setString(5, region.getOwner().getUniqueId().toString());
				upsertStmt.setString(6, region.location != null ? region.location.toString() : null);
				upsertStmt.setLong(7, region.createdAt);
				upsertStmt.setLong(8, region.playerFlags);
				upsertStmt.setLong(9, region.worldFlags);
				upsertStmt.setDouble(10, region.bank);
				upsertStmt.setInt(11, region.mapColor);
				upsertStmt.setString(12, chunksStr);
				upsertStmt.setString(13, membersStr);
				upsertStmt.setString(14, ratesStr);
				upsertStmt.setString(15, invitedStr);
				upsertStmt.setString(16, bannedStr);
				upsertStmt.setString(17, logsStr);
				upsertStmt.setString(18, region.rent != null ? region.rent.toString() : null);
				upsertStmt.setLong(19, region.upkeepAt);
				upsertStmt.setDouble(20, region.taxesAmount);
				upsertStmt.setInt(21, region.weather);
				upsertStmt.setInt(22, region.time);
				upsertStmt.setString(23,
						region.welcomeSign != null ? region.welcomeSign.toString() : null);
				upsertStmt.setString(24, region.icon != null ? region.icon : null);

				upsertStmt.addBatch();
			}

			upsertStmt.executeBatch();

			dbRegionIds.removeAll(cacheRegionIds);
			for (UUID deletedId : dbRegionIds) {
				deleteStmt.setString(1, deletedId.toString());
				deleteStmt.addBatch();
			}

			deleteStmt.executeBatch();

			connection.commit();
		} catch (SQLException e) {
			connection.rollback();
			throw e;
		} finally {
			connection.setAutoCommit(true);
		}
	}

	@Override
	public void exportWars(List<War> wars) throws SQLException {
		Set<UUID> dbWarIds = new HashSet<>();
		String selectSql = "SELECT id FROM " + TABLE_PREFIX + "wars";

		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery(selectSql)) {
			while (rs.next()) {
				dbWarIds.add(UUID.fromString(rs.getString("id")));
			}
		}

		String upsertSql = "INSERT INTO " + TABLE_PREFIX + "wars (" +
				"id, displayName, name, description, regions, prize, startedAt" +
				") VALUES (?, ?, ?, ?, ?, ?, ?) " +
				"ON DUPLICATE KEY UPDATE " +
				"displayName = VALUES(displayName), " +
				"name = VALUES(name), " +
				"description = VALUES(description), " +
				"regions = VALUES(regions), " +
				"prize = VALUES(prize), " +
				"startedAt = VALUES(startedAt)";

		String deleteSql = "DELETE FROM " + TABLE_PREFIX + "wars WHERE id = ?";

		connection.setAutoCommit(false);

		try (PreparedStatement upsertStmt = connection.prepareStatement(upsertSql);
			 PreparedStatement deleteStmt = connection.prepareStatement(deleteSql)) {
			Set<UUID> cacheWarIds = new HashSet<>();

			for (War war : wars) {
				UUID warId = war.id;
				cacheWarIds.add(warId);

				String regionsStr = war.regions.stream().map(UUID::toString).collect(Collectors.joining("§"));

				upsertStmt.setString(1, warId.toString());
				upsertStmt.setString(2, war.displayName);
				upsertStmt.setString(3, war.name);
				upsertStmt.setString(4, war.description);
				upsertStmt.setString(5, regionsStr);
				upsertStmt.setDouble(6, war.prize);
				upsertStmt.setLong(7, war.startedAt);

				upsertStmt.addBatch();
			}

			upsertStmt.executeBatch();

			dbWarIds.removeAll(cacheWarIds);
			for (UUID deletedId : dbWarIds) {
				deleteStmt.setString(1, deletedId.toString());
				deleteStmt.addBatch();
			}

			deleteStmt.executeBatch();

			connection.commit();
		} catch (SQLException e) {
			connection.rollback();
			throw e;
		} finally {
			connection.setAutoCommit(true);
		}
	}

	@Override
	public void exportSubAreas(List<SubArea> subareas) throws SQLException {
		Set<UUID> dbSubAreaIds = new HashSet<>();

		String selectSql = "SELECT id FROM " + TABLE_PREFIX + "subareas";

		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery(selectSql)) {
			while (rs.next()) dbSubAreaIds.add(UUID.fromString(rs.getString("id")));
		}

		String upsertSql =
				"INSERT INTO " + TABLE_PREFIX + "subareas " +
						"(id, regionId, name, worldName, point1, point2, members, flags, rent, createdAt) " +
						"VALUES (?,?,?,?,?,?,?,?,?,?) " +
						"ON DUPLICATE KEY UPDATE " +
						"regionId=VALUES(regionId), name=VALUES(name), worldName=VALUES(worldName), " +
						"point1=VALUES(point1), point2=VALUES(point2), members=VALUES(members), " +
						"flags=VALUES(flags), rent=VALUES(rent), createdAt=VALUES(createdAt)";

		String deleteSql = "DELETE FROM " + TABLE_PREFIX + "subareas WHERE id=?";

		connection.setAutoCommit(false);

		try (PreparedStatement upsertStmt = connection.prepareStatement(upsertSql);
			 PreparedStatement deleteStmt = connection.prepareStatement(deleteSql)) {

			Set<UUID> cacheSubAreaIds = new HashSet<>();

			for (SubArea subArea : subareas) {
				UUID subAreaId = subArea.id;
				cacheSubAreaIds.add(subAreaId);

				String membersStr = subArea.members.stream()
						.map(SerializableMember::toString)
						.collect(Collectors.joining("§"));

				upsertStmt.setString(1, subAreaId.toString());
				upsertStmt.setString(2, subArea.regionId.toString());
				upsertStmt.setString(3, subArea.name);
				upsertStmt.setString(4, subArea.worldId.toString());
				upsertStmt.setString(5, SubArea.toStringBlockLocation(subArea.getWorld(), subArea.point1));
				upsertStmt.setString(6, SubArea.toStringBlockLocation(subArea.getWorld(), subArea.point2));
				upsertStmt.setString(7, membersStr);
				upsertStmt.setLong(8, subArea.flags);
				upsertStmt.setString(9, subArea.rent != null ? subArea.rent.toString() : null);
				upsertStmt.setLong(10, subArea.createdAt);
				upsertStmt.addBatch();
			}

			upsertStmt.executeBatch();

			dbSubAreaIds.removeAll(cacheSubAreaIds);
			for (UUID deletedId : dbSubAreaIds) {
				deleteStmt.setString(1, deletedId.toString());
				deleteStmt.addBatch();
			}
			deleteStmt.executeBatch();

			connection.commit();
		} catch (SQLException e) {
			connection.rollback();
			throw e;
		} finally {
			connection.setAutoCommit(true);
		}
	}

	@Override
	public void exportLevels(List<Level> levels) throws SQLException {
		Set<UUID> dbIds = new HashSet<>();
		String selectSql = "SELECT id FROM " + TABLE_PREFIX + "levels";

		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery(selectSql)) {
			while (rs.next()) dbIds.add(UUID.fromString(rs.getString("id")));
		}

		String upsertSql = "INSERT INTO " + TABLE_PREFIX + "levels " +
				"(id, regionId, level, experience, totalExperience, createdAt) " +
				"VALUES (?, ?, ?, ?, ?, ?) " +
				"ON DUPLICATE KEY UPDATE " +
				"regionId = VALUES(regionId), " +
				"level = VALUES(level), " +
				"experience = VALUES(experience), " +
				"totalExperience = VALUES(totalExperience), " +
				"createdAt = VALUES(createdAt)";

		String deleteSql = "DELETE FROM " + TABLE_PREFIX + "levels WHERE id = ?";

		connection.setAutoCommit(false);

		try (PreparedStatement upsert = connection.prepareStatement(upsertSql);
			 PreparedStatement delete = connection.prepareStatement(deleteSql)) {

			Set<UUID> cacheIds = new HashSet<>();

			for (Level lvl : levels) {
				UUID lvlId = lvl.getUniqueId();
				cacheIds.add(lvlId);

				upsert.setString(1, lvlId.toString());
				upsert.setString(2, lvl.getRegionId().toString());
				upsert.setInt(3, lvl.getLevel());
				upsert.setLong(4, lvl.getExperience());
				upsert.setLong(5, lvl.getTotalExperience());
				upsert.setLong(6, lvl.getCreatedAt());
				upsert.addBatch();
			}

			upsert.executeBatch();

			dbIds.removeAll(cacheIds);
			for (UUID deletedId : dbIds) {
				delete.setString(1, deletedId.toString());
				delete.addBatch();
			}
			delete.executeBatch();

			connection.commit();
		} catch (SQLException e) {
			connection.rollback();
			throw e;
		} finally {
			connection.setAutoCommit(true);
		}
	}

	@Override
	public void prepareTables() throws SQLException {
		String sql1 = "CREATE TABLE IF NOT EXISTS " + TABLE_PREFIX + "regions (" +
				"id VARCHAR(36) PRIMARY KEY, " +
				"displayName TINYTEXT NOT NULL, " +
				"name TINYTEXT NOT NULL, " +
				"description MEDIUMTEXT NOT NULL, " +
				"ownerId TINYTEXT NOT NULL, " +
				"location MEDIUMTEXT, " +
				"createdAt BIGINT NOT NULL, " +
				"playerFlags BIGINT NOT NULL, " +
				"worldFlags BIGINT NOT NULL, " +
				"bank DOUBLE NOT NULL, " +
				"mapColor INT NOT NULL, " +
				"chunks LONGTEXT NOT NULL, " +
				"members LONGTEXT NOT NULL, " +
				"rates LONGTEXT NOT NULL, " +
				"invitedPlayers LONGTEXT NOT NULL, " +
				"bannedPlayers LONGTEXT NOT NULL, " +
				"logs LONGTEXT NOT NULL, " +
				"rent LONGTEXT, " +
				"upkeepAt BIGINT NOT NULL, " +
				"taxesAmount DOUBLE NOT NULL, " +
				"weather INT NOT NULL, " +
				"time INT NOT NULL, " +
				"welcomeSign MEDIUMTEXT," +
				"icon LONGTEXT" +
				")";

		String sql2 = "CREATE TABLE IF NOT EXISTS " + TABLE_PREFIX + "wars (" +
				"id VARCHAR(36) PRIMARY KEY, " +
				"displayName TINYTEXT NOT NULL, " +
				"name TINYTEXT NOT NULL, " +
				"description MEDIUMTEXT NOT NULL, " +
				"regions LONGTEXT NOT NULL, " +
				"prize DOUBLE NOT NULL, " +
				"startedAt BIGINT NOT NULL" +
				")";

		String sql3 = "CREATE TABLE IF NOT EXISTS " + TABLE_PREFIX + "subareas (" +
				"id VARCHAR(36) PRIMARY KEY, " +
				"regionId VARCHAR(36) NOT NULL, " +
				"name TINYTEXT NOT NULL, " +
				"worldName TINYTEXT NOT NULL, " +
				"point1 TINYTEXT NOT NULL, " +
				"point2 TINYTEXT NOT NULL, " +
				"members LONGTEXT NOT NULL, " +
				"flags BIGINT NOT NULL, " +
				"rent LONGTEXT, " +
				"createdAt BIGINT NOT NULL" +
				")";

		String sql4 = "CREATE TABLE IF NOT EXISTS " + TABLE_PREFIX + "levels (" +
				"id VARCHAR(36) PRIMARY KEY, " +
				"regionId VARCHAR(36) NOT NULL, " +
				"level INT NOT NULL, " +
				"experience BIGINT NOT NULL, " +
				"totalExperience BIGINT NOT NULL, " +
				"createdAt BIGINT NOT NULL" +
				")";

		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql1);
			stmt.executeUpdate(sql2);
			stmt.executeUpdate(sql3);
			stmt.executeUpdate(sql4);
		}

		TableSyncer.apply(this.connection, TABLE_PREFIX);
	}

	@Override
	public long getLatency() {
		List<String> tables = List.of(
				TABLE_PREFIX + "regions",
				TABLE_PREFIX + "wars",
				TABLE_PREFIX + "subareas",
				TABLE_PREFIX + "levels"
		);

		try (Statement stmt = connection.createStatement()) {
			long startTime = System.currentTimeMillis();

			for (String table : tables) {
				stmt.executeQuery("SELECT 1 FROM " + table + " LIMIT 1");
			}

			long endTime = System.currentTimeMillis();

			return endTime - startTime;
		} catch (SQLException ignored) {
			return -1L;
		}
	}

	@Override
	public void closeConnection() throws SQLException {
		if (connection != null && !connection.isClosed()) {
			connection.close();
		}
	}

	private static final class TableSyncer {
		public static void syncTable(Connection conn,
									 String tableName,
									 List<String> wantedColumns,
									 Map<String, String> wantedDefinitions) throws SQLException {

			Map<String, String> realCols = getRealColumns(conn, tableName);

			for (String existing : realCols.keySet()) {
				if (!wantedColumns.contains(existing)) {
					String sql = "ALTER TABLE `" + tableName + "` DROP COLUMN `" + existing + "`";
					try (Statement st = conn.createStatement()) {
						st.execute(sql);
					}
				}
			}

			for (int i = 0; i < wantedColumns.size(); i++) {
				String col = wantedColumns.get(i);
				if (!realCols.containsKey(col)) {
					String def = wantedDefinitions.get(col);

					if (def == null) {
						throw new IllegalArgumentException("No definition for column " + col);
					}

					String after = i == 0 ? "FIRST" : "AFTER `" + wantedColumns.get(i - 1) + "`";
					String sql = "ALTER TABLE `" + tableName + "` ADD COLUMN `" + col + "` " + def + " " + after;

					try (Statement st = conn.createStatement()) {
						st.execute(sql);
					}
				}
			}
		}

		private static Map<String, String> getRealColumns(Connection conn, String tableName) throws SQLException {
			Map<String, String> map = new LinkedHashMap<>();
			String sql = "SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_DEFAULT, EXTRA " +
					"FROM INFORMATION_SCHEMA.COLUMNS " +
					"WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? " +
					"ORDER BY ORDINAL_POSITION";
			try (PreparedStatement ps = conn.prepareStatement(sql)) {
				ps.setString(1, tableName);
				try (ResultSet rs = ps.executeQuery()) {
					while (rs.next()) {
						String name = rs.getString("COLUMN_NAME");
						String type = rs.getString("COLUMN_TYPE");
						String nullable = "YES".equals(rs.getString("IS_NULLABLE")) ? "NULL" : "NOT NULL";
						String defaultVal = rs.getString("COLUMN_DEFAULT");
						String extra = rs.getString("EXTRA");
						StringBuilder def = new StringBuilder(type).append(' ').append(nullable);
						if (defaultVal != null) def.append(" DEFAULT '").append(defaultVal).append('\'');
						if (extra != null && !extra.isEmpty()) def.append(' ').append(extra);
						map.put(name, def.toString());
					}
				}
			}
			return map;
		}

		public static void apply(Connection conn, String prefix) throws SQLException {
			List<String> wanted = Arrays.asList(
					"id", "displayName", "name", "description", "ownerId", "location",
					"createdAt", "playerFlags", "worldFlags", "bank", "mapColor",
					"chunks", "members", "rates", "invitedPlayers", "bannedPlayers",
					"logs", "rent", "upkeepAt", "taxesAmount", "weather", "time",
					"welcomeSign", "icon"
			);

			Map<String, String> def = new HashMap<>();
			def.put("id", "VARCHAR(36) PRIMARY KEY");
			def.put("displayName", "TINYTEXT NOT NULL");
			def.put("name", "TINYTEXT NOT NULL");
			def.put("description", "MEDIUMTEXT NOT NULL");
			def.put("ownerId", "TINYTEXT NOT NULL");
			def.put("location", "MEDIUMTEXT");
			def.put("createdAt", "BIGINT NOT NULL");
			def.put("playerFlags", "BIGINT NOT NULL");
			def.put("worldFlags", "BIGINT NOT NULL");
			def.put("bank", "DOUBLE NOT NULL");
			def.put("mapColor", "INT NOT NULL");
			def.put("chunks", "LONGTEXT NOT NULL");
			def.put("members", "LONGTEXT NOT NULL");
			def.put("rates", "LONGTEXT NOT NULL");
			def.put("invitedPlayers", "LONGTEXT NOT NULL");
			def.put("bannedPlayers", "LONGTEXT NOT NULL");
			def.put("logs", "LONGTEXT NOT NULL");
			def.put("rent", "LONGTEXT");
			def.put("upkeepAt", "BIGINT NOT NULL");
			def.put("taxesAmount", "DOUBLE NOT NULL");
			def.put("weather", "INT NOT NULL");
			def.put("time", "INT NOT NULL");
			def.put("welcomeSign", "MEDIUMTEXT");
			def.put("icon", "LONGTEXT");

			syncTable(conn, prefix + "regions", wanted, def);
		}
	}
}