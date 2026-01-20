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

public class SQLite {
	private static final String JDBC_URL = "jdbc:sqlite:";
	private final Connection connection;

	public SQLite(String dbFile) throws ClassNotFoundException, SQLException {
		Class.forName("org.sqlite.JDBC");

		this.connection = DriverManager.getConnection(JDBC_URL + dbFile);

		Logger.info("New SQLite database connection established.");

		createTables();
	}

	private void createTables() throws SQLException {
		String sql1 = "CREATE TABLE IF NOT EXISTS regions (" +
				"id TEXT PRIMARY KEY, " +
				"displayName TEXT NOT NULL, " +
				"name TEXT NOT NULL, " +
				"description TEXT NOT NULL, " +
				"ownerId TEXT NOT NULL, " +
				"location TEXT, " +
				"createdAt INTEGER NOT NULL, " +
				"playerFlags INTEGER NOT NULL, " +
				"worldFlags INTEGER NOT NULL, " +
				"bank REAL NOT NULL, " +
				"mapColor INTEGER NOT NULL, " +
				"chunks TEXT NOT NULL, " +
				"members TEXT NOT NULL, " +
				"rates TEXT NOT NULL, " +
				"invitedPlayers TEXT NOT NULL, " +
				"bannedPlayers TEXT NOT NULL, " +
				"subAreas TEXT NOT NULL, " + // NOTE: Sub-Areas in regions table is completely ignored
				"logs TEXT NOT NULL, " +
				"rent TEXT, " +
				"upkeepAt INTEGER NOT NULL, " +
				"taxesAmount REAL NOT NULL, " +
				"weather INTEGER NOT NULL, " +
				"time INTEGER NOT NULL, " +
				"welcomeSign TEXT," +
				"icon TEXT" +
				")";

		String sql2 = "CREATE TABLE IF NOT EXISTS wars (" +
				"id TEXT PRIMARY KEY, " +
				"displayName TEXT NOT NULL, " +
				"name TEXT NOT NULL, " +
				"description TEXT NOT NULL, " +
				"regions TEXT NOT NULL, " +
				"prize REAL NOT NULL, " +
				"startedAt INTEGER NOT NULL" +
				")";

		String sql3 = "CREATE TABLE IF NOT EXISTS subareas (" +
				"id TEXT PRIMARY KEY, " +
				"regionId TEXT NOT NULL, " +
				"name TEXT NOT NULL, " +
				"worldName TEXT NOT NULL, " +
				"point1 TEXT NOT NULL, " +
				"point2 TEXT NOT NULL, " +
				"members TEXT NOT NULL, " +
				"flags INTEGER NOT NULL, " +
				"createdAt INTEGER NOT NULL" +
				")";

		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql1);
			stmt.executeUpdate(sql2);
			stmt.executeUpdate(sql3);
		}
	}

	public void importRegions() {
		String sql = "SELECT * FROM regions";

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
						.map(SerializableChunk::fromString)
						.collect(Collectors.toList())
						: new ArrayList<>();

				List<SerializableMember> members = !rs.getString("members").isEmpty()
						? Arrays.stream(rs.getString("members").split("§"))
						.map(SerializableMember::fromString)
						.collect(Collectors.toList())
						: new ArrayList<>();

				List<SerializableRate> rates = !rs.getString("rates").isEmpty()
						? Arrays.stream(rs.getString("rates").split("§"))
						.map(SerializableRate::fromString)
						.collect(Collectors.toList())
						: new ArrayList<>();

				List<OfflinePlayer> invitedPlayers = !rs.getString("invitedPlayers").isEmpty()
						? Arrays.stream(rs.getString("invitedPlayers").split("§"))
						.map((uuidString) -> Homestead.getInstance()
								.getOfflinePlayerSync(UUID.fromString(
										uuidString)))
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
						.map(SerializableLog::fromString)
						.collect(Collectors.toList())
						: new ArrayList<>();

				rs.getString("subAreas"); // Ignored

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
		String sql = "SELECT * FROM wars";

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
		String sql = "SELECT * FROM subareas";

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

				List<SerializableMember> members = !rs.getString("members").isEmpty()
						? Arrays.stream(rs.getString("members").split("§"))
						.map(SerializableMember::fromString).toList()
						: new ArrayList<>();
				long flags = rs.getLong("flags");
				long createdAt = rs.getLong("createdAt");

				SubArea subArea = new SubArea(id, regionId, name, world.getName(), point1, point2, members, flags, createdAt);

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
		String selectSql = "SELECT id FROM regions";

		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery(selectSql)) {
			while (rs.next()) {
				dbRegionIds.add(UUID.fromString(rs.getString("id")));
			}
		} catch (SQLException e) {
			Homestead.getInstance().endInstance(e);
			return;
		}

		String upsertSql = "INSERT OR REPLACE INTO regions (" +
				"id, displayName, name, description, ownerId, location, createdAt, " +
				"playerFlags, worldFlags, bank, mapColor, chunks, members, rates, " +
				"invitedPlayers, bannedPlayers, subAreas, logs, rent, upkeepAt, taxesAmount, weather, " +
				"time, welcomeSign, icon" +
				") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		String deleteSql = "DELETE FROM regions WHERE id = ?";

		try (PreparedStatement upsertStmt = connection.prepareStatement(upsertSql);
			 PreparedStatement deleteStmt = connection.prepareStatement(deleteSql)) {
			Set<UUID> cacheRegionIds = new HashSet<>();

			for (Region region : Homestead.regionsCache.getAll()) {
				UUID regionId = region.id;
				cacheRegionIds.add(regionId);

				String chunksStr = region.chunks.stream().map(SerializableChunk::toString)
						.collect(Collectors.joining("§"));
				String membersStr = region.members.stream().map(SerializableMember::toString)
						.collect(Collectors.joining("§"));
				String ratesStr = region.rates.stream().map(SerializableRate::toString)
						.collect(Collectors.joining("§"));
				String invitedStr = region.getInvitedPlayers().stream().map(OfflinePlayer::getUniqueId)
						.map(UUID::toString).collect(Collectors.joining("§"));
				String bannedStr = region.bannedPlayers.stream().map(SerializableBannedPlayer::toString)
						.collect(Collectors.joining("§"));
				String logsStr = region.logs.stream().map(SerializableLog::toString)
						.collect(Collectors.joining("µ"));

				upsertStmt.setString(1, regionId.toString());
				upsertStmt.setString(2, region.displayName);
				upsertStmt.setString(3, region.name);
				upsertStmt.setString(4, region.description);
				upsertStmt.setString(5, region.getOwnerId().toString());
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
				upsertStmt.setString(17, ""); // Ignored
				upsertStmt.setString(18, logsStr);
				upsertStmt.setString(19, region.rent != null ? region.rent.toString() : null);
				upsertStmt.setLong(20, region.upkeepAt);
				upsertStmt.setDouble(21, region.taxesAmount);
				upsertStmt.setInt(22, region.weather);
				upsertStmt.setInt(23, region.time);
				upsertStmt.setString(24,
						region.welcomeSign != null ? region.welcomeSign.toString() : null);
				upsertStmt.setString(25, region.icon != null ? region.icon : null);

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
		String selectSql = "SELECT id FROM wars";

		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery(selectSql)) {
			while (rs.next()) {
				dbWarIds.add(UUID.fromString(rs.getString("id")));
			}
		} catch (SQLException e) {
			Homestead.getInstance().endInstance(e);
			return;
		}

		String upsertSql = "INSERT OR REPLACE INTO  wars (" +
				"id, displayName, name, description, regions, prize, startedAt" +
				") VALUES (?, ?, ?, ?, ?, ?, ?)";

		String deleteSql = "DELETE FROM wars WHERE id = ?";

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
		String selectSql = "SELECT id FROM subareas";

		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery(selectSql)) {
			while (rs.next()) {
				dbSubAreaIds.add(UUID.fromString(rs.getString("id")));
			}
		} catch (SQLException e) {
			Homestead.getInstance().endInstance(e);
			return;
		}

		String upsertSql = "INSERT OR REPLACE INTO subareas (" +
				"id, regionId, name, worldName, point1, point2, members, flags, createdAt" +
				") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

		String deleteSql = "DELETE FROM subareas WHERE id = ?";

		try (PreparedStatement upsertStmt = connection.prepareStatement(upsertSql);
			 PreparedStatement deleteStmt = connection.prepareStatement(deleteSql)) {
			Set<UUID> cacheSubAreaIds = new HashSet<>();

			for (SubArea subArea : Homestead.subAreasCache.getAll()) {
				UUID subAreaId = subArea.id;
				cacheSubAreaIds.add(subAreaId);

				World world = subArea.getWorld();

				String membersStr = subArea.members.stream().map(SerializableMember::toString).collect(Collectors.joining("§"));

				upsertStmt.setString(1, subAreaId.toString());
				upsertStmt.setString(2, subArea.regionId.toString());
				upsertStmt.setString(3, subArea.name);
				upsertStmt.setString(4, subArea.worldName);
				upsertStmt.setString(5, SubArea.toStringBlockLocation(world, subArea.point1));
				upsertStmt.setString(6, SubArea.toStringBlockLocation(world, subArea.point2));
				upsertStmt.setString(7, membersStr);
				upsertStmt.setLong(8, subArea.flags);
				upsertStmt.setLong(9, subArea.createdAt);

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
				Logger.info("Exported " + cacheSubAreaIds.size() + " sub-areas and deleted " + dbSubAreaIds.size()
						+ " sub-areas.");
			}
		} catch (SQLException e) {
			Homestead.getInstance().endInstance(e);
		}
	}

	public void closeConnection() {
		try {
			if (connection != null && !connection.isClosed()) {
				connection.close();
				Logger.warning("Connection for SQLite has been closed.");
			}
		} catch (SQLException e) {
			Homestead.getInstance().endInstance(e);
		}
	}

	public long getLatency() {
		long before = System.currentTimeMillis();

		String sql = "SELECT * FROM regions";

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
}
