package tfagaming.projects.minecraft.homestead.database.providers;

import org.bukkit.OfflinePlayer;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.logs.Logger;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.War;
import tfagaming.projects.minecraft.homestead.structure.serializable.*;
import tfagaming.projects.minecraft.homestead.tools.java.ListUtils;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class MariaDB {
	private final String TABLE_PREFIX;
	private static final String JDBC_URL = "jdbc:mariadb://";
	private Connection connection;

	public MariaDB(String username, String password, String host, int port, String database, String tablePrefix) {
		this(username, password, host, port, database, tablePrefix, false);
	}

	public MariaDB(String username, String password, String host, int port, String database, String tablePrefix, boolean handleError) {
		TABLE_PREFIX = tablePrefix.replaceAll("[^A-Za-z0-9_]", "");

		try {
			Class.forName("org.mariadb.jdbc.Driver");

			String connectionUrl = JDBC_URL + host + ":" + port + "/" + database
					+ "?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC";
			this.connection = DriverManager.getConnection(connectionUrl, username, password);

			Logger.info("MariaDB database connection established.");

			createTablesIfNotExists();
			createWarsTableIfNotExists();
		} catch (ClassNotFoundException e) {
			Logger.error("MariaDB JDBC Driver not found.");
			e.printStackTrace();

			if (!handleError) Homestead.getInstance().endInstance();
		} catch (SQLException e) {
			Logger.error("Unable to establish connection to MariaDB.");
			e.printStackTrace();

			if (!handleError) Homestead.getInstance().endInstance();
		}
	}

	public void createTablesIfNotExists() {
		String sql = """
				    CREATE TABLE IF NOT EXISTS `%sregions` (
				        id              CHAR(36) PRIMARY KEY,
				        display_name    TEXT NOT NULL,
				        name            TEXT NOT NULL,
				        description     TEXT NOT NULL,
				        owner_id        CHAR(36) NOT NULL,
				        location        TEXT,
				        created_at      BIGINT NOT NULL,
				        player_flags    BIGINT NOT NULL,
				        world_flags     BIGINT NOT NULL,
				        bank            DOUBLE PRECISION NOT NULL,
				        map_color       INT NOT NULL,
				        chunks          TEXT NOT NULL,
				        members         TEXT NOT NULL,
				        rates           TEXT NOT NULL,
				        invited_players TEXT NOT NULL,
				        banned_players  TEXT NOT NULL,
				        sub_areas       TEXT NOT NULL,
				        logs            TEXT NOT NULL,
				        rent            TEXT,
				        upkeep_at       BIGINT NOT NULL,
				        taxes_amount    DOUBLE NOT NULL,
				        weather         INT NOT NULL,
				        time            INT NOT NULL,
				        welcome_sign    TEXT,
				        icon            TEXT
				    )
				""".formatted(TABLE_PREFIX);
		try (Statement stmt = connection.createStatement()) {
			stmt.execute(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void importRegions() {
		final String sql = "SELECT * FROM " + TABLE_PREFIX + "regions";
		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery(sql)) {

			Homestead.regionsCache.clear();

			while (rs.next()) {
				UUID id = UUID.fromString(rs.getString("id"));
				String displayName = rs.getString("display_name");
				String name = rs.getString("name");
				String description = rs.getString("description");
				OfflinePlayer owner = Homestead.getInstance()
						.getOfflinePlayerSync(UUID.fromString(rs.getString("owner_id")));

				SerializableLocation location =
						Optional.ofNullable(rs.getString("location"))
								.map(SerializableLocation::fromString).orElse(null);

				long createdAt = rs.getLong("created_at");
				long playerFlags = rs.getLong("player_flags");
				long worldFlags = rs.getLong("world_flags");
				double bank = rs.getDouble("bank");
				int mapColor = rs.getInt("map_color");

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

				List<OfflinePlayer> invitedPlayers = !rs.getString("invited_players").isEmpty()
						? Arrays.stream(rs.getString("invited_players").split("§"))
						.map((uuidString) -> Homestead.getInstance()
								.getOfflinePlayerSync(UUID.fromString(uuidString)))
						.collect(Collectors.toList())
						: new ArrayList<>();

				List<SerializableBannedPlayer> bannedPlayers = !rs.getString("banned_players").isEmpty()
						? Arrays.stream(rs.getString("banned_players")
								.split("§"))
						.map(SerializableBannedPlayer::fromString)
						.collect(Collectors.toList())
						: new ArrayList<>();

				List<SerializableLog> logs = !rs.getString("logs").isEmpty()
						? Arrays.stream(rs.getString("logs").split("µ"))
						.map(SerializableLog::fromString).collect(Collectors.toList())
						: new ArrayList<>();

				List<SerializableSubArea> subAreas = !rs.getString("sub_areas").isEmpty()
						? Arrays.stream(rs.getString("sub_areas").split("§"))
						.map(SerializableSubArea::fromString).collect(Collectors.toList())
						: new ArrayList<>();

				SerializableRent rent = SerializableRent.fromString(rs.getString("rent"));

				long upkeepAt = rs.getLong("upkeep_at");
				double taxesAmount = rs.getDouble("taxes_amount");
				int weather = rs.getInt("weather");
				int time = rs.getInt("time");

				SerializableLocation welcomeSign =
						Optional.ofNullable(rs.getString("welcome_sign"))
								.map(SerializableLocation::fromString).orElse(null);
				String icon = rs.getString("icon");

				if (owner == null) continue;

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
				region.setSubAreas(subAreas);
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
			Logger.error("Unable to import regions from MariaDB.");
			e.printStackTrace();
		}
		Logger.info("Imported " + Homestead.regionsCache.size() + " regions from MariaDB.");
	}

	public void createWarsTableIfNotExists() {
		String sql = """
                CREATE TABLE IF NOT EXISTS `%swars` (
                    id          CHAR(36) PRIMARY KEY,
                    display_name TEXT NOT NULL,
                    name        TEXT NOT NULL,
                    description TEXT NOT NULL,
                    regions     TEXT NOT NULL,
                    prize       DOUBLE PRECISION NOT NULL,
                    started_at  BIGINT NOT NULL
                )
                """.formatted(TABLE_PREFIX);

		try (Statement stmt = connection.createStatement()) {
			stmt.execute(sql);
			Logger.info("Wars table created/verified in MariaDB.");
		} catch (SQLException e) {
			Logger.error("Unable to create wars table in MariaDB.");
			e.printStackTrace();
		}
	}

	public void importWars() {
		final String sql = "SELECT * FROM " + TABLE_PREFIX + "wars";
		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery(sql)) {

			Homestead.warsCache.clear();

			while (rs.next()) {
				UUID id = UUID.fromString(rs.getString("id"));
				String displayName = rs.getString("display_name");
				String name = rs.getString("name");
				String description = rs.getString("description");

				List<UUID> regions = !rs.getString("regions").isEmpty()
						? Arrays.stream(rs.getString("regions").split("§"))
						.map(UUID::fromString).collect(Collectors.toList())
						: new ArrayList<>();

				double prize = rs.getDouble("prize");
				long startedAt = rs.getLong("started_at");

				War war = new War(name, regions);
				war.id = id;
				war.displayName = displayName;
				war.description = description;
				war.prize = prize;
				war.startedAt = startedAt;

				Homestead.warsCache.putOrUpdate(war);
			}
		} catch (SQLException e) {
			Logger.error("Unable to import wars from MariaDB.");
			e.printStackTrace();
		}
		Logger.info("Imported " + Homestead.warsCache.size() + " wars from MariaDB.");
	}

	public void exportRegions() {
		Set<UUID> dbRegionIds = new HashSet<>();
		final String selectSql = "SELECT id FROM " + TABLE_PREFIX + "regions";
		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery(selectSql)) {
			while (rs.next()) {
				dbRegionIds.add(UUID.fromString(rs.getString("id")));
			}
		} catch (SQLException e) {
			Logger.error("Unable to fetch region IDs from MariaDB.");
			e.printStackTrace();
			return;
		}

		final String upsertSql = """
				    INSERT INTO `%sregions` (
				        id, display_name, name, description, owner_id, location, created_at,
				        player_flags, world_flags, bank, map_color, chunks, members, rates,
				        invited_players, banned_players, sub_areas, logs, rent, upkeep_at,
				        taxes_amount, weather, time, welcome_sign, icon
				    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
				    ON DUPLICATE KEY UPDATE
				        display_name = VALUES(display_name),
				        name = VALUES(name),
				        description = VALUES(description),
				        owner_id = VALUES(owner_id),
				        location = VALUES(location),
				        created_at = VALUES(created_at),
				        player_flags = VALUES(player_flags),
				        world_flags = VALUES(world_flags),
				        bank = VALUES(bank),
				        map_color = VALUES(map_color),
				        chunks = VALUES(chunks),
				        members = VALUES(members),
				        rates = VALUES(rates),
				        invited_players = VALUES(invited_players),
				        banned_players = VALUES(banned_players),
				        sub_areas = VALUES(sub_areas),
				        logs = VALUES(logs),
				        rent = VALUES(rent),
				        upkeep_at = VALUES(upkeep_at),
				        taxes_amount = VALUES(taxes_amount),
				        weather = VALUES(weather),
				        time = VALUES(time),
				        welcome_sign = VALUES(welcome_sign),
				        icon = VALUES(icon)
				""".formatted(TABLE_PREFIX);

		final String deleteSql = "DELETE FROM " + TABLE_PREFIX + "regions WHERE id = ?";

		try (PreparedStatement upsertStmt = connection.prepareStatement(upsertSql);
			 PreparedStatement deleteStmt = connection.prepareStatement(deleteSql)) {

			Set<UUID> cacheRegionIds = new HashSet<>();

			for (Region region : Homestead.regionsCache.getAll()) {
				UUID regionId = region.id;
				cacheRegionIds.add(regionId);

				upsertStmt.setString(1, regionId.toString());
				upsertStmt.setString(2, region.displayName);
				upsertStmt.setString(3, region.name);
				upsertStmt.setString(4, region.description);
				upsertStmt.setString(5, region.getOwnerId().toString());
				upsertStmt.setString(6, region.location == null ? null : region.location.toString());
				upsertStmt.setLong(7, region.createdAt);
				upsertStmt.setLong(8, region.playerFlags);
				upsertStmt.setLong(9, region.worldFlags);
				upsertStmt.setDouble(10, region.bank);
				upsertStmt.setInt(11, region.mapColor);

				String chunksStr = String.join("§",
						region.chunks.stream().map(SerializableChunk::toString).collect(Collectors.toList()));
				String membersStr = String.join("§",
						region.members.stream().map(SerializableMember::toString).collect(Collectors.toList()));
				String ratesStr = String.join("§",
						region.rates.stream().map(SerializableRate::toString).collect(Collectors.toList()));
				String invitedStr = String.join("§",
						region.getInvitedPlayers().stream().map(OfflinePlayer::getUniqueId)
								.map(UUID::toString).collect(Collectors.toList()));
				String bannedStr = String.join("§",
						region.bannedPlayers.stream().map(SerializableBannedPlayer::toString)
								.collect(Collectors.toList()));
				String logsStr = String.join("µ",
						region.logs.stream().map(SerializableLog::toString).collect(Collectors.toList()));
				String subAreasStr = String.join("§",
						region.subAreas.stream().map(SerializableSubArea::toString).collect(Collectors.toList()));

				upsertStmt.setString(12, chunksStr);
				upsertStmt.setString(13, membersStr);
				upsertStmt.setString(14, ratesStr);
				upsertStmt.setString(15, invitedStr);
				upsertStmt.setString(16, bannedStr);
				upsertStmt.setString(17, subAreasStr);
				upsertStmt.setString(18, logsStr);

				upsertStmt.setString(19, region.rent == null ? null : region.rent.toString());
				upsertStmt.setLong(20, region.upkeepAt);
				upsertStmt.setDouble(21, region.taxesAmount);
				upsertStmt.setInt(22, region.weather);
				upsertStmt.setInt(23, region.time);
				upsertStmt.setString(24, region.welcomeSign == null ? null : region.welcomeSign.toString());
				upsertStmt.setString(25, region.icon);

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
						+ " regions from MariaDB.");
			}
		} catch (SQLException e) {
			Logger.error("Unable to export regions to MariaDB.");
			e.printStackTrace();
		}
	}

	public void exportWars() {
		Set<UUID> dbWarIds = new HashSet<>();
		final String selectSql = "SELECT id FROM " + TABLE_PREFIX + "wars";
		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery(selectSql)) {
			while (rs.next()) {
				dbWarIds.add(UUID.fromString(rs.getString("id")));
			}
		} catch (SQLException e) {
			Logger.error("Unable to fetch war IDs from MariaDB.");
			e.printStackTrace();
			return;
		}

		final String upsertSql = """
        INSERT INTO `%swars` (
            id, display_name, name, description, regions, prize, started_at
        ) VALUES (?, ?, ?, ?, ?, ?, ?)
        ON DUPLICATE KEY UPDATE
            display_name = VALUES(display_name),
            name = VALUES(name),
            description = VALUES(description),
            regions = VALUES(regions),
            prize = VALUES(prize),
            started_at = VALUES(started_at)
        """.formatted(TABLE_PREFIX);

		final String deleteSql = "DELETE FROM " + TABLE_PREFIX + "wars WHERE id = ?";

		try (PreparedStatement upsertStmt = connection.prepareStatement(upsertSql);
			 PreparedStatement deleteStmt = connection.prepareStatement(deleteSql)) {

			Set<UUID> cacheWarIds = new HashSet<>();

			for (War war : Homestead.warsCache.getAll()) {
				UUID warId = war.id;
				cacheWarIds.add(warId);

				String regionsStr = String.join("§",
						war.regions.stream().map(UUID::toString).collect(Collectors.toList()));

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
				Logger.info("Exported " + cacheWarIds.size() + " wars and deleted "
						+ dbWarIds.size() + " wars from MariaDB.");
			}
		} catch (SQLException e) {
			Logger.error("Unable to export wars to MariaDB.");
			e.printStackTrace();
		}
	}

	public void closeConnection() {
		try {
			if (connection != null && !connection.isClosed()) {
				connection.close();
				Logger.warning("MariaDB connection has been closed.");
			}
		} catch (SQLException e) {
			Logger.error("Unable to close MariaDB connection.");
			e.printStackTrace();
		}
	}

	public long getLatency() {
		long before = System.currentTimeMillis();

		String sql = "SELECT 1 FROM `%sregions` LIMIT 1".formatted(TABLE_PREFIX);

		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery(sql)) {
			while (rs.next()) {

			}
		} catch (SQLException e) {
			return -1L;
		}

		return System.currentTimeMillis() - before;
	}
}
