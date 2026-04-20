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

import static tfagaming.projects.minecraft.homestead.tools.minecraft.subareas.SubAreaUtility.*;

public final class PostgreSQL implements Provider {
	private static final String JDBC_URL = "jdbc:postgresql://";
	private final String TABLE_PREFIX;
	private final Connection connection;

	public PostgreSQL(String username, String password, String host, int port, String database, String tablePrefix, String parameters) throws ClassNotFoundException, SQLException {
		TABLE_PREFIX = tablePrefix.replaceAll("[^A-Za-z0-9_]", "");

		Class.forName("org.postgresql.Driver");

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
				UUID id = (UUID) rs.getObject("id");
				String displayName = rs.getString("display_name");
				String name = rs.getString("name");
				String description = rs.getString("description");
				OfflinePlayer owner = Homestead.getInstance()
						.getOfflinePlayerSync((UUID) rs.getObject("owner_id"));
				SerializableLocation location = SerializableLocation.fromString(rs.getString("location"));
				long createdAt = rs.getLong("created_at");
				long playerFlags = rs.getLong("player_flags");
				long worldFlags = rs.getLong("world_flags");
				double bank = rs.getDouble("bank");
				int mapColor = rs.getInt("map_color");

				// Handle array types
				List<SerializableChunk> chunks = Arrays.stream((String[]) rs.getArray("chunks").getArray())
						.map(SerializableChunk::fromString)
						.collect(Collectors.toList());

				List<SerializableMember> members = Arrays.stream((String[]) rs.getArray("members").getArray())
						.map(SerializableMember::fromString)
						.collect(Collectors.toList());

				List<SerializableRate> rates = Arrays.stream((String[]) rs.getArray("rates").getArray())
						.map(SerializableRate::fromString)
						.collect(Collectors.toList());

				List<OfflinePlayer> invitedPlayers = Arrays.stream((UUID[]) rs.getArray("invited_players").getArray())
						.map(uuid -> Homestead.getInstance().getOfflinePlayerSync(uuid))
						.collect(Collectors.toList());

				List<SerializableBannedPlayer> bannedPlayers = Arrays
						.stream((String[]) rs.getArray("banned_players").getArray())
						.map(SerializableBannedPlayer::fromString)
						.collect(Collectors.toList());

				List<SerializableLog> logs = Arrays.stream((String[]) rs.getArray("logs").getArray())
						.map(SerializableLog::fromString)
						.collect(Collectors.toList());

				SerializableRent rent = rs.getString("rent") != null ? SerializableRent.fromString(rs.getString("rent"))
						: null;
				long upkeepAt = rs.getLong("upkeep_at");
				double taxesAmount = rs.getDouble("taxes_amount");
				int weather = rs.getInt("weather");
				int time = rs.getInt("time");
				SerializableLocation welcomeSign = rs.getString("welcome_sign") != null
						? SerializableLocation.fromString(rs.getString("welcome_sign"))
						: null;
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
				List<UUID> regions = Arrays.stream((String[]) rs.getArray("regions").getArray())
						.map(UUID::fromString)
						.collect(Collectors.toList());
				double prize = rs.getDouble("prize");
				long startedAt = rs.getLong("started_at");

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
		final String sql = "SELECT * FROM " + TABLE_PREFIX + "subareas";

		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery(sql)) {

			while (rs.next()) {
				UUID id = (UUID) rs.getObject("id");
				UUID regionId = (UUID) rs.getObject("region_id");
				String name = rs.getString("name");

				World world = resolveWorld(rs.getString("world_name"));

				if (world == null) {
					continue;
				}

				Block point1 = parseBlockLocation(world, rs.getString("point1"));
				Block point2 = parseBlockLocation(world, rs.getString("point2"));

				List<SerializableMember> members =
						Arrays.stream((String[]) rs.getArray("members").getArray())
								.map(SerializableMember::fromString)
								.collect(Collectors.toList());

				long flags = rs.getLong("flags");

				SerializableRent rent = SerializableRent.fromString(rs.getString("rent"));

				long createdAt = rs.getLong("created_at");

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
		final String sql = "SELECT * FROM " + TABLE_PREFIX + "levels";

		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery(sql)) {

			while (rs.next()) {
				UUID id = (UUID) rs.getObject("id");
				UUID regionId = (UUID) rs.getObject("region_id");
				int level = rs.getInt("level");
				long xp = rs.getLong("experience");
				long totalXp = rs.getLong("total_experience");
				long createdAt = rs.getLong("created_at");

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
				dbRegionIds.add((UUID) rs.getObject("id"));
			}
		}

		String upsertSql = "INSERT INTO " + TABLE_PREFIX + "regions (" +
				"id, display_name, name, description, owner_id, location, created_at, " +
				"player_flags, world_flags, bank, map_color, chunks, members, rates, " +
				"invited_players, banned_players, logs, rent, upkeep_at, taxes_amount, weather, " +
				"time, welcome_sign, icon" +
				") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
				"ON CONFLICT (id) DO UPDATE SET " +
				"display_name = EXCLUDED.display_name, " +
				"name = EXCLUDED.name, " +
				"description = EXCLUDED.description, " +
				"owner_id = EXCLUDED.owner_id, " +
				"location = EXCLUDED.location, " +
				"created_at = EXCLUDED.created_at, " +
				"player_flags = EXCLUDED.player_flags, " +
				"world_flags = EXCLUDED.world_flags, " +
				"bank = EXCLUDED.bank, " +
				"map_color = EXCLUDED.map_color, " +
				"chunks = EXCLUDED.chunks, " +
				"members = EXCLUDED.members, " +
				"rates = EXCLUDED.rates, " +
				"invited_players = EXCLUDED.invited_players, " +
				"banned_players = EXCLUDED.banned_players, " +
				"logs = EXCLUDED.logs, " +
				"rent = EXCLUDED.rent, " +
				"upkeep_at = EXCLUDED.upkeep_at, " +
				"taxes_amount = EXCLUDED.taxes_amount, " +
				"weather = EXCLUDED.weather, " +
				"time = EXCLUDED.time, " +
				"welcome_sign = EXCLUDED.welcome_sign," +
				"icon = EXCLUDED.icon";

		String deleteSql = "DELETE FROM " + TABLE_PREFIX + "regions WHERE id = ?";

		connection.setAutoCommit(false);

		try (PreparedStatement upsertStmt = connection.prepareStatement(upsertSql);
			 PreparedStatement deleteStmt = connection.prepareStatement(deleteSql)) {
			Set<UUID> cacheRegionIds = new HashSet<>();

			for (Region region : regions) {
				UUID regionId = region.id;
				cacheRegionIds.add(regionId);

				// Convert lists to arrays for PostgreSQL
				String[] chunksArray = region.chunks.stream()
						.map(SerializableChunk::toString)
						.toArray(String[]::new);

				String[] membersArray = region.members.stream()
						.map(SerializableMember::toString)
						.toArray(String[]::new);

				String[] ratesArray = region.rates.stream()
						.map(SerializableRate::toString)
						.toArray(String[]::new);

				UUID[] invitedArray = region.getInvitedPlayers().stream()
						.map(OfflinePlayer::getUniqueId)
						.toArray(UUID[]::new);

				String[] bannedArray = region.bannedPlayers.stream()
						.map(SerializableBannedPlayer::toString)
						.toArray(String[]::new);

				String[] logsArray = region.logs.stream()
						.map(SerializableLog::toString)
						.toArray(String[]::new);

				upsertStmt.setObject(1, regionId);
				upsertStmt.setString(2, region.displayName);
				upsertStmt.setString(3, region.name);
				upsertStmt.setString(4, region.description);
				upsertStmt.setObject(5, region.getOwnerId());
				upsertStmt.setString(6, region.location != null ? region.location.toString() : null);
				upsertStmt.setLong(7, region.createdAt);
				upsertStmt.setLong(8, region.playerFlags);
				upsertStmt.setLong(9, region.worldFlags);
				upsertStmt.setDouble(10, region.bank);
				upsertStmt.setInt(11, region.mapColor);
				upsertStmt.setArray(12, connection.createArrayOf("text", chunksArray));
				upsertStmt.setArray(13, connection.createArrayOf("text", membersArray));
				upsertStmt.setArray(14, connection.createArrayOf("text", ratesArray));
				upsertStmt.setArray(15, connection.createArrayOf("uuid", invitedArray));
				upsertStmt.setArray(16, connection.createArrayOf("text", bannedArray));
				upsertStmt.setArray(17, connection.createArrayOf("text", logsArray));
				upsertStmt.setString(18, region.rent != null ? region.rent.toString() : null);
				upsertStmt.setLong(19, region.upkeepAt);
				upsertStmt.setDouble(20, region.taxesAmount);
				upsertStmt.setInt(21, region.weather);
				upsertStmt.setInt(22, region.time);
				upsertStmt.setString(23, region.welcomeSign != null ? region.welcomeSign.toString() : null);
				upsertStmt.setString(24, region.icon != null ? region.icon : null);

				upsertStmt.addBatch();
			}

			upsertStmt.executeBatch();

			dbRegionIds.removeAll(cacheRegionIds);
			for (UUID deletedId : dbRegionIds) {
				deleteStmt.setObject(1, deletedId);
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
				"id, displayName, name, description, regions, prize, started_at" +
				") VALUES (?, ?, ?, ?, ?, ?, ?) " +
				"ON CONFLICT (id) DO UPDATE SET " +
				"displayName = EXCLUDED.displayName, " +
				"name = EXCLUDED.name, " +
				"description = EXCLUDED.description, " +
				"regions = EXCLUDED.regions, " +
				"prize = EXCLUDED.prize, " +
				"started_at = EXCLUDED.started_at";

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
		final String selectSql = "SELECT id FROM " + TABLE_PREFIX + "subareas";

		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery(selectSql)) {
			while (rs.next()) dbSubAreaIds.add((UUID) rs.getObject("id"));
		}

		final String upsertSql = """
				INSERT INTO %ssubareas
				    (id, region_id, name, world_name, point1, point2, members, flags, rent, created_at)
				VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
				ON CONFLICT (id) DO UPDATE SET
				    region_id  = EXCLUDED.region_id,
				    name       = EXCLUDED.name,
				    world_name = EXCLUDED.world_name,
				    point1     = EXCLUDED.point1,
				    point2     = EXCLUDED.point2,
				    members    = EXCLUDED.members,
				    flags      = EXCLUDED.flags,
				    rent       = EXCLUDED.rent,
				    created_at = EXCLUDED.created_at
				""".formatted(TABLE_PREFIX);

		final String deleteSql = "DELETE FROM " + TABLE_PREFIX + "subareas WHERE id = ?";

		connection.setAutoCommit(false);

		try (PreparedStatement upsertStmt = connection.prepareStatement(upsertSql);
			 PreparedStatement deleteStmt = connection.prepareStatement(deleteSql)) {

			Set<UUID> cacheSubAreaIds = new HashSet<>();

			for (SubArea subArea : subareas) {
				UUID subAreaId = subArea.id;
				cacheSubAreaIds.add(subAreaId);

				String[] membersArray = subArea.members.stream()
						.map(SerializableMember::toString)
						.toArray(String[]::new);

				upsertStmt.setObject(1, subAreaId);
				upsertStmt.setObject(2, subArea.regionId);
				upsertStmt.setString(3, subArea.name);
				upsertStmt.setString(4, subArea.worldId.toString());
				upsertStmt.setString(5, toStringBlockLocation(subArea.getWorld(), subArea.point1));
				upsertStmt.setString(6, toStringBlockLocation(subArea.getWorld(), subArea.point2));
				upsertStmt.setArray(7, connection.createArrayOf("text", membersArray));
				upsertStmt.setLong(8, subArea.flags);
				upsertStmt.setString(9, subArea.rent != null ? subArea.rent.toString() : null);
				upsertStmt.setLong(10, subArea.createdAt);
				upsertStmt.addBatch();
			}
			upsertStmt.executeBatch();

			dbSubAreaIds.removeAll(cacheSubAreaIds);
			for (UUID deletedId : dbSubAreaIds) {
				deleteStmt.setObject(1, deletedId);
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
		final String selectSql = "SELECT id FROM " + TABLE_PREFIX + "levels";

		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery(selectSql)) {
			while (rs.next()) dbIds.add((UUID) rs.getObject("id"));
		}

		final String upsertSql = """
				INSERT INTO %slevels
				    (id, region_id, level, experience, total_experience, created_at)
				VALUES (?, ?, ?, ?, ?, ?)
				ON CONFLICT (id) DO UPDATE SET
				    region_id       = EXCLUDED.region_id,
				    level           = EXCLUDED.level,
				    experience      = EXCLUDED.experience,
				    total_experience = EXCLUDED.total_experience,
				    created_at      = EXCLUDED.created_at
				""".formatted(TABLE_PREFIX);

		final String deleteSql = "DELETE FROM " + TABLE_PREFIX + "levels WHERE id = ?";

		connection.setAutoCommit(false);

		try (PreparedStatement upsert = connection.prepareStatement(upsertSql);
			 PreparedStatement delete = connection.prepareStatement(deleteSql)) {

			Set<UUID> cacheIds = new HashSet<>();

			for (Level lvl : levels) {
				UUID lvlId = lvl.getUniqueId();
				cacheIds.add(lvlId);

				upsert.setObject(1, lvlId);
				upsert.setObject(2, lvl.getRegionId());
				upsert.setInt(3, lvl.getLevel());
				upsert.setLong(4, lvl.getExperience());
				upsert.setLong(5, lvl.getTotalExperience());
				upsert.setLong(6, lvl.getCreatedAt());
				upsert.addBatch();
			}

			upsert.executeBatch();

			dbIds.removeAll(cacheIds);
			for (UUID deletedId : dbIds) {
				delete.setObject(1, deletedId);
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
				"id UUID PRIMARY KEY, " +
				"display_name TEXT NOT NULL, " +
				"name TEXT NOT NULL, " +
				"description TEXT NOT NULL, " +
				"owner_id UUID NOT NULL, " +
				"location TEXT, " +
				"created_at BIGINT NOT NULL, " +
				"player_flags BIGINT NOT NULL, " +
				"world_flags BIGINT NOT NULL, " +
				"bank DOUBLE PRECISION NOT NULL, " +
				"map_color INTEGER NOT NULL, " +
				"chunks TEXT[] NOT NULL, " +
				"members TEXT[] NOT NULL, " +
				"rates TEXT[] NOT NULL, " +
				"invited_players UUID[] NOT NULL, " +
				"banned_players TEXT[] NOT NULL, " +
				"logs TEXT[] NOT NULL, " +
				"rent TEXT, " +
				"upkeep_at BIGINT NOT NULL, " +
				"taxes_amount DOUBLE PRECISION NOT NULL, " +
				"weather INTEGER NOT NULL, " +
				"time INTEGER NOT NULL, " +
				"welcome_sign TEXT," +
				"icon TEXT" +
				")";

		String sql2 = "CREATE TABLE IF NOT EXISTS " + TABLE_PREFIX + "wars (" +
				"id UUID PRIMARY KEY, " +
				"displayName TEXT NOT NULL, " +
				"name TEXT NOT NULL, " +
				"description TEXT NOT NULL, " +
				"regions TEXT[] NOT NULL, " +
				"prize DOUBLE PRECISION NOT NULL, " +
				"started_at BIGINT NOT NULL" +
				")";

		String sql3 = """
				CREATE TABLE IF NOT EXISTS %ssubareas (
				    id         UUID PRIMARY KEY,
				    region_id  UUID NOT NULL,
				    name       TEXT NOT NULL,
				    world_name TEXT NOT NULL,
				    point1     TEXT NOT NULL,
				    point2     TEXT NOT NULL,
				    members    TEXT[] NOT NULL,
				    flags      BIGINT NOT NULL,
				    rent       TEXT,
				    created_at BIGINT NOT NULL
				)
				""".formatted(TABLE_PREFIX);

		String sql4 = """
				CREATE TABLE IF NOT EXISTS %slevels (
				    id              UUID PRIMARY KEY,
				    region_id       UUID NOT NULL,
				    level           INTEGER NOT NULL,
				    experience      BIGINT NOT NULL,
				    total_experience BIGINT NOT NULL,
				    created_at      BIGINT NOT NULL
				)
				""".formatted(TABLE_PREFIX);

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
									 List<String> wantedCols,
									 Map<String, String> colDef) throws SQLException {

			Set<String> wanted = new LinkedHashSet<>(wantedCols);
			Set<String> real = getRealColumns(conn, tableName);

			for (String existing : real) {
				if (!wanted.contains(existing)) {
					String sql = "ALTER TABLE \"" + tableName + "\" DROP COLUMN IF EXISTS \"" + existing + "\"";
					try (Statement st = conn.createStatement()) {
						st.execute(sql);
					}
				}
			}

			for (String col : wanted) {
				if (!real.contains(col)) {
					String def = colDef.get(col);

					if (def == null) {
						throw new IllegalArgumentException("No definition for column " + col);
					}

					String sql = "ALTER TABLE \"" + tableName + "\" ADD COLUMN \"" + col + "\" " + def;

					try (Statement st = conn.createStatement()) {
						st.execute(sql);
					}
				}
			}
		}

		private static Set<String> getRealColumns(Connection conn, String table) throws SQLException {
			Set<String> set = new LinkedHashSet<>();
			String sql = "SELECT column_name " +
					"FROM information_schema.columns " +
					"WHERE table_schema = current_schema() AND table_name = ? " +
					"ORDER BY ordinal_position";
			try (PreparedStatement ps = conn.prepareStatement(sql)) {
				ps.setString(1, table);
				try (ResultSet rs = ps.executeQuery()) {
					while (rs.next()) set.add(rs.getString(1));
				}
			}
			return set;
		}

		public static void apply(Connection conn, String prefix) throws SQLException {
			String table = prefix + "regions";

			List<String> wanted = Arrays.asList(
					"id", "display_name", "name", "description", "owner_id", "location",
					"created_at", "player_flags", "world_flags", "bank", "map_color",
					"chunks", "members", "rates", "invited_players", "banned_players",
					"logs", "rent", "upkeep_at", "taxes_amount", "weather", "time",
					"welcome_sign", "icon"
			);

			Map<String, String> def = new HashMap<>();
			def.put("id", "UUID PRIMARY KEY");
			def.put("display_name", "TEXT NOT NULL");
			def.put("name", "TEXT NOT NULL");
			def.put("description", "TEXT NOT NULL");
			def.put("owner_id", "UUID NOT NULL");
			def.put("location", "TEXT");
			def.put("created_at", "BIGINT NOT NULL");
			def.put("player_flags", "BIGINT NOT NULL");
			def.put("world_flags", "BIGINT NOT NULL");
			def.put("bank", "DOUBLE PRECISION NOT NULL");
			def.put("map_color", "INTEGER NOT NULL");
			def.put("chunks", "TEXT[] NOT NULL");
			def.put("members", "TEXT[] NOT NULL");
			def.put("rates", "TEXT[] NOT NULL");
			def.put("invited_players", "UUID[] NOT NULL");
			def.put("banned_players", "TEXT[] NOT NULL");
			def.put("logs", "TEXT[] NOT NULL");
			def.put("rent", "TEXT");
			def.put("upkeep_at", "BIGINT NOT NULL");
			def.put("taxes_amount", "DOUBLE PRECISION NOT NULL");
			def.put("weather", "INTEGER NOT NULL");
			def.put("time", "INTEGER NOT NULL");
			def.put("welcome_sign", "TEXT");
			def.put("icon", "TEXT");

			syncTable(conn, table, wanted, def);
		}
	}
}