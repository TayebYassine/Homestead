package tfagaming.projects.minecraft.homestead.database.providers;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.logs.Logger;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.SubArea;
import tfagaming.projects.minecraft.homestead.structure.War;
import tfagaming.projects.minecraft.homestead.structure.serializable.*;
import tfagaming.projects.minecraft.homestead.tools.java.ListUtils;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class MySQL {
	private static final String JDBC_URL = "jdbc:mysql://";
	private final String TABLE_PREFIX;
	private final Connection connection;

	public MySQL(String username, String password, String host, int port, String database, String tablePrefix) throws ClassNotFoundException, SQLException {
		TABLE_PREFIX = tablePrefix.replaceAll("[^A-Za-z0-9_]", "");

		Class.forName("com.mysql.cj.jdbc.Driver");

		String connectionUrl = JDBC_URL + host + ":" + port + "/" + database;
		this.connection = DriverManager.getConnection(connectionUrl, username, password);

		Logger.info("New database connection established.");

		createTables();

		TableSyncer.apply(this.connection, TABLE_PREFIX);
	}

	private void createTables() throws SQLException {
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

		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql1);
			stmt.executeUpdate(sql2);
			stmt.executeUpdate(sql3);
		}
	}

	public void importRegions() {
		String sql = "SELECT * FROM " + TABLE_PREFIX + "regions";

		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery(sql)) {
			Homestead.regionsCache.clear();

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

				Homestead.regionsCache.putOrUpdate(region);
			}
		} catch (SQLException e) {
			Homestead.getInstance().endInstance(e);
			return;
		}

		Logger.info("Imported " + Homestead.regionsCache.size() + " regions.");
	}

	public void importWars() {
		String sql = "SELECT * FROM " + TABLE_PREFIX + "wars";

		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery(sql)) {
			Homestead.warsCache.clear();

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

				Homestead.warsCache.putOrUpdate(war);
			}
		} catch (SQLException e) {
			Homestead.getInstance().endInstance(e);
			return;
		}

		Logger.info("Imported " + Homestead.warsCache.size() + " wars.");
	}

	public void importSubAreas() {
		String sql = "SELECT * FROM " + TABLE_PREFIX + "subareas";
		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery(sql)) {

			Homestead.subAreasCache.clear();

			while (rs.next()) {
				UUID id = UUID.fromString(rs.getString("id"));
				UUID regionId = UUID.fromString(rs.getString("regionId"));
				String name = rs.getString("name");

				World world = Bukkit.getWorld(rs.getString("worldName"));

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

				SubArea subArea = new SubArea(id, regionId, name, world.getName(),
						point1, point2, members, flags, rent, createdAt);

				Homestead.subAreasCache.putOrUpdate(subArea);
			}
		} catch (SQLException e) {
			Homestead.getInstance().endInstance(e);
			return;
		}

		Logger.info("Imported " + Homestead.subAreasCache.size() + " sub-areas.");
	}

	public void exportRegions() {
		Set<UUID> dbRegionIds = new HashSet<>();
		String selectSql = "SELECT id FROM " + TABLE_PREFIX + "regions";

		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery(selectSql)) {
			while (rs.next()) {
				dbRegionIds.add(UUID.fromString(rs.getString("id")));
			}
		} catch (SQLException e) {
			Homestead.getInstance().endInstance(e);
			return;
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

		try (PreparedStatement upsertStmt = connection.prepareStatement(upsertSql);
			 PreparedStatement deleteStmt = connection.prepareStatement(deleteSql)) {
			Set<UUID> cacheRegionIds = new HashSet<>();

			for (Region region : Homestead.regionsCache.getAll()) {
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

			if (Homestead.config.isDebugEnabled()) {
				Logger.info("Exported " + cacheRegionIds.size() + " regions and deleted " + dbRegionIds.size()
						+ " regions.");
			}
		} catch (SQLException e) {
			Homestead.getInstance().endInstance(e);
		}
	}

	public void exportWars() {
		Set<UUID> dbWarIds = new HashSet<>();
		String selectSql = "SELECT id FROM " + TABLE_PREFIX + "wars";

		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery(selectSql)) {
			while (rs.next()) {
				dbWarIds.add(UUID.fromString(rs.getString("id")));
			}
		} catch (SQLException e) {
			Homestead.getInstance().endInstance(e);
			return;
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

		try (PreparedStatement upsertStmt = connection.prepareStatement(upsertSql);
			 PreparedStatement deleteStmt = connection.prepareStatement(deleteSql)) {
			Set<UUID> cacheWarIds = new HashSet<>();

			for (War war : Homestead.warsCache.getAll()) {
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

			if (Homestead.config.isDebugEnabled()) {
				Logger.info("Exported " + cacheWarIds.size() + " wars and deleted " + dbWarIds.size()
						+ " wars.");
			}
		} catch (SQLException e) {
			Homestead.getInstance().endInstance(e);
		}
	}

	public void exportSubAreas() {
		Set<UUID> dbSubAreaIds = new HashSet<>();

		String selectSql = "SELECT id FROM " + TABLE_PREFIX + "subareas";

		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery(selectSql)) {
			while (rs.next()) dbSubAreaIds.add(UUID.fromString(rs.getString("id")));
		} catch (SQLException e) {
			Homestead.getInstance().endInstance(e);
			return;
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

		try (PreparedStatement upsertStmt = connection.prepareStatement(upsertSql);
			 PreparedStatement deleteStmt = connection.prepareStatement(deleteSql)) {

			Set<UUID> cacheSubAreaIds = new HashSet<>();

			for (SubArea subArea : Homestead.subAreasCache.getAll()) {
				UUID subAreaId = subArea.id;
				cacheSubAreaIds.add(subAreaId);

				String membersStr = subArea.members.stream()
						.map(SerializableMember::toString)
						.collect(Collectors.joining("§"));

				upsertStmt.setString(1, subAreaId.toString());
				upsertStmt.setString(2, subArea.regionId.toString());
				upsertStmt.setString(3, subArea.name);
				upsertStmt.setString(4, subArea.worldName);
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

			if (Homestead.config.isDebugEnabled()) {
				Logger.info("Exported " + cacheSubAreaIds.size() + " sub-areas and deleted " +
						dbSubAreaIds.size() + " sub-areas.");
			}
		} catch (SQLException e) {
			Homestead.getInstance().endInstance(e);
		}
	}

	public void closeConnection() {
		try {
			if (connection != null && !connection.isClosed()) {
				connection.close();
				Logger.warning("Connection for MySQL has been closed.");
			}
		} catch (SQLException e) {
			Homestead.getInstance().endInstance(e);
		}
	}

	public long getLatency() {
		long before = System.currentTimeMillis();

		String sql = "SELECT * FROM " + TABLE_PREFIX + "regions";

		int count = 0;
		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery(sql)) {
			while (rs.next()) {
				count++;
			}
		} catch (SQLException e) {
			return -1L;
		}

		long after = System.currentTimeMillis();

		return after - before;
	}

	public static final class TableSyncer {
		public static void syncTable(Connection conn,
									 String tableName,
									 List<String> wantedColumns,
									 Map<String, String> wantedDefinitions) throws SQLException {

			Map<String, String> realCols = getRealColumns(conn, tableName);

			Logger.warning("Synchronizing columns... This might take a while.");

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

			Logger.info("Synchronization done!");
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
