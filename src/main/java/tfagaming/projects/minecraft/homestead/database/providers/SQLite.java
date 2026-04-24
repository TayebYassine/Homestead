package tfagaming.projects.minecraft.homestead.database.providers;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.models.*;
import tfagaming.projects.minecraft.homestead.models.serialize.SeBlock;
import tfagaming.projects.minecraft.homestead.models.serialize.SeLocation;
import tfagaming.projects.minecraft.homestead.models.serialize.SeRent;

import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

public final class SQLite implements Provider {

	private static final String JDBC_URL = "jdbc:sqlite:";
	private static final Logger LOG = Logger.getLogger("Homestead");

	private final Connection connection;

	public SQLite(String path) throws ClassNotFoundException, SQLException {
		Class.forName("org.sqlite.JDBC");
		this.connection = DriverManager.getConnection(JDBC_URL + path);

		connection.createStatement().execute("PRAGMA journal_mode=WAL");
		prepareTables();
	}

	@Override
	public void prepareTables() throws SQLException {
		if (legacyTablesExist()) {
			LOG.warning("[Database] Legacy database structure detected, running one-time migration...");
			try {
				migrateFromLegacy();
				LOG.info("[Database] Database migration completed successfully.");
			} catch (SQLException e) {
				LOG.severe("[Database] Migration failed, old tables have not been dropped. Error: " + e.getMessage());
				throw e;
			}
		} else {

			createNewTables();
		}
	}

	private boolean legacyTablesExist() throws SQLException {
		return tableExists("regions") && columnExists("regions", "chunks");
	}

	private boolean tableExists(String name) throws SQLException {
		try (PreparedStatement ps = connection.prepareStatement(
				"SELECT 1 FROM sqlite_master WHERE type='table' AND name=?")) {
			ps.setString(1, name);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next();
			}
		}
	}

	private boolean columnExists(String table, String column) throws SQLException {
		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery("PRAGMA table_info(" + table + ")")) {
			while (rs.next()) {
				if (column.equalsIgnoreCase(rs.getString("name"))) return true;
			}
		}
		return false;
	}

	private void createNewTables() throws SQLException {
		String[] ddl = {

				"""
            CREATE TABLE IF NOT EXISTS regions (
                id          INTEGER PRIMARY KEY,
                name        TEXT    NOT NULL,
                displayName TEXT,
                description TEXT,
                ownerId     TEXT    NOT NULL,
                location    TEXT,
                playerFlags INTEGER NOT NULL DEFAULT 0,
                worldFlags  INTEGER NOT NULL DEFAULT 0,
                taxes       REAL    NOT NULL DEFAULT 0.0,
                bank        REAL    NOT NULL DEFAULT 0.0,
                mapColor    INTEGER NOT NULL DEFAULT 0,
                mapIcon     TEXT,
                rent        TEXT,
                weather     INTEGER NOT NULL DEFAULT 0,
                time        INTEGER NOT NULL DEFAULT 0,
                welcomeSign TEXT,
                upkeepAt    INTEGER NOT NULL DEFAULT 0,
                createdAt   INTEGER NOT NULL
            )
            """,


				"""
            CREATE TABLE IF NOT EXISTS region_members (
                id           INTEGER PRIMARY KEY,
                playerId     TEXT    NOT NULL,
                linkageType  INTEGER NOT NULL,
                regionId     INTEGER NOT NULL DEFAULT -1,
                subAreaId    INTEGER NOT NULL DEFAULT -1,
                playerFlags  INTEGER NOT NULL DEFAULT 0,
                controlFlags INTEGER NOT NULL DEFAULT 0,
                joinedAt     INTEGER NOT NULL,
                taxesAt      INTEGER NOT NULL DEFAULT 0
            )
            """,


				"""
            CREATE TABLE IF NOT EXISTS region_chunks (
                id          INTEGER PRIMARY KEY,
                regionId    INTEGER NOT NULL,
                worldId     TEXT    NOT NULL,
                x           INTEGER NOT NULL,
                z           INTEGER NOT NULL,
                claimedAt   INTEGER NOT NULL,
                forceLoaded INTEGER NOT NULL DEFAULT 0
            )
            """,


				"""
            CREATE TABLE IF NOT EXISTS region_logs (
                id       INTEGER PRIMARY KEY,
                regionId INTEGER NOT NULL,
                author   TEXT    NOT NULL,
                message  TEXT    NOT NULL,
                sentAt   INTEGER NOT NULL,
                read     INTEGER NOT NULL DEFAULT 0
            )
            """,


				"""
            CREATE TABLE IF NOT EXISTS region_rates (
                id       INTEGER PRIMARY KEY,
                regionId INTEGER NOT NULL,
                playerId TEXT    NOT NULL,
                rate     INTEGER NOT NULL,
                ratedAt  INTEGER NOT NULL
            )
            """,


				"""
            CREATE TABLE IF NOT EXISTS region_invites (
                id        INTEGER PRIMARY KEY,
                regionId  INTEGER NOT NULL,
                playerId  TEXT    NOT NULL,
                invitedAt INTEGER NOT NULL
            )
            """,


				"""
            CREATE TABLE IF NOT EXISTS region_banned_players (
                id       INTEGER PRIMARY KEY,
                regionId INTEGER NOT NULL,
                playerId TEXT    NOT NULL,
                reason   TEXT,
                bannedAt INTEGER NOT NULL
            )
            """,


				"""
            CREATE TABLE IF NOT EXISTS subareas (
                id          INTEGER PRIMARY KEY,
                regionId    INTEGER NOT NULL,
                name        TEXT    NOT NULL,
                worldId     TEXT    NOT NULL,
                point1      TEXT    NOT NULL,
                point2      TEXT    NOT NULL,
                playerFlags INTEGER NOT NULL DEFAULT 0,
                rent        TEXT,
                createdAt   INTEGER NOT NULL
            )
            """,


				"""
            CREATE TABLE IF NOT EXISTS levels (
                id              INTEGER PRIMARY KEY,
                regionId        INTEGER NOT NULL,
                level           INTEGER NOT NULL DEFAULT 0,
                experience      INTEGER NOT NULL DEFAULT 0,
                totalExperience INTEGER NOT NULL DEFAULT 0,
                createdAt       INTEGER NOT NULL
            )
            """,


				"""
            CREATE TABLE IF NOT EXISTS wars (
                id          INTEGER PRIMARY KEY,
                name        TEXT    NOT NULL,
                displayName TEXT,
                description TEXT,
                prize       REAL    NOT NULL DEFAULT 0.0,
                startedAt   INTEGER NOT NULL
            )
            """,


				"""
            CREATE TABLE IF NOT EXISTS war_regions (
                warId    INTEGER NOT NULL,
                regionId INTEGER NOT NULL,
                PRIMARY KEY (warId, regionId)
            )
            """
		};

		try (Statement stmt = connection.createStatement()) {
			for (String sql : ddl) {
				stmt.executeUpdate(sql.strip());
			}
		}
	}

	private void migrateFromLegacy() throws SQLException {

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


		if (tableExists("regions")) {
			try (Statement stmt = connection.createStatement();
				 ResultSet rs = stmt.executeQuery("SELECT * FROM regions")) {

				while (rs.next()) {
					String oldId = rs.getString("id");
					try {
						long newId = Homestead.SNOWFLAKE.nextId();
						regionIdMap.put(oldId, newId);

						UUID ownerId = UUID.fromString(rs.getString("ownerId"));
						long createdAt = rs.getLong("createdAt");

						Region region = new Region(newId, rs.getString("name"), ownerId, createdAt);
						region.setDisplayName(rs.getString("displayName"));
						region.setDescription(rs.getString("description"));
						region.setPlayerFlags(rs.getLong("playerFlags"));
						region.setWorldFlags(rs.getLong("worldFlags"));
						region.setBank(rs.getDouble("bank"));
						region.setMapColor(rs.getInt("mapColor"));
						region.setUpkeepAt(rs.getLong("upkeepAt"));
						region.setTaxes(rs.getDouble("taxesAmount"));
						region.setWeather(rs.getInt("weather"));
						region.setTime(rs.getInt("time"));
						region.setMapIcon(rs.getString("icon"));

						String locStr = rs.getString("location");
						if (LegacyParsers.isNotBlank(locStr))
							region.setLocation(SeLocation.deserialize(locStr));

						String wsStr = rs.getString("welcomeSign");
						if (LegacyParsers.isNotBlank(wsStr))
							region.setWelcomeSign(SeLocation.deserialize(wsStr));

						String rentStr = rs.getString("rent");
						if (LegacyParsers.isNotBlank(rentStr))
							region.setRent(SeRent.deserialize(rentStr));

						newRegions.add(region);

						LegacyParsers.splitAndParse(rs.getString("chunks"), "§", part -> {
							RegionChunk c = LegacyParsers.parseLegacyChunk(newId, part);
							if (c != null) newChunks.add(c);
						});

						LegacyParsers.splitAndParse(rs.getString("members"), "§", part -> {
							RegionMember m = LegacyParsers.parseLegacyMember(newId, RegionMember.LinkageType.REGION, part);
							if (m != null) newMembers.add(m);
						});

						LegacyParsers.splitAndParse(rs.getString("rates"), "§", part -> {
							RegionRate r = LegacyParsers.parseLegacyRate(newId, part);
							if (r != null) newRates.add(r);
						});

						LegacyParsers.splitAndParse(rs.getString("invitedPlayers"), "§", part -> {
							try {
								UUID pid = UUID.fromString(part.trim());
								newInvites.add(new RegionInvite(
										Homestead.SNOWFLAKE.nextId(), newId, pid, createdAt));
							} catch (IllegalArgumentException ignored) {
							}
						});

						LegacyParsers.splitAndParse(rs.getString("bannedPlayers"), "§", part -> {
							RegionBannedPlayer b = LegacyParsers.parseLegacyBannedPlayer(newId, part);
							if (b != null) newBanned.add(b);
						});

						LegacyParsers.splitAndParse(rs.getString("logs"), "µ", part -> {
							RegionLog l = LegacyParsers.parseLegacyLog(newId, part);
							if (l != null) newLogs.add(l);
						});
					} catch (Exception e) {
						LOG.warning("[Database] Skipping region " + oldId + " during migration: " + e.getMessage());
					}
				}
			}
		}

		if (tableExists("subareas")) {
			try (Statement stmt = connection.createStatement();
				 ResultSet rs = stmt.executeQuery("SELECT * FROM subareas")) {

				while (rs.next()) {
					String oldId = rs.getString("id");
					String oldRegionId = rs.getString("regionId");
					try {
						Long newRegionId = regionIdMap.get(oldRegionId);
						if (newRegionId == null) {
							LOG.warning("[Database] SubArea " + oldId
									+ " references unknown region " + oldRegionId + ", skipping.");
							continue;
						}

						long newSubAreaId = Homestead.SNOWFLAKE.nextId();
						subAreaIdMap.put(oldId, newSubAreaId);

						UUID worldId = LegacyParsers.resolveWorldUUID(rs.getString("worldName"));
						if (worldId == null) {
							LOG.warning("[Database] SubArea " + oldId
									+ " references an unresolvable world, skipping.");
							continue;
						}

						SeBlock point1 = LegacyParsers.parseLegacyBlock(worldId, rs.getString("point1"));
						SeBlock point2 = LegacyParsers.parseLegacyBlock(worldId, rs.getString("point2"));
						if (point1 == null || point2 == null) {
							LOG.warning("[Database] SubArea " + oldId
									+ " has invalid block coordinates, skipping.");
							continue;
						}

						String rentStr = rs.getString("rent");
						SeRent rent = LegacyParsers.isNotBlank(rentStr) ? SeRent.deserialize(rentStr) : null;

						SubArea subArea = new SubArea(
								newSubAreaId, newRegionId, rs.getString("name"),
								worldId, point1, point2,
								rs.getLong("playerFlags"), rent, rs.getLong("createdAt"));
						newSubAreas.add(subArea);

						LegacyParsers.splitAndParse(rs.getString("members"), "§", part -> {
							RegionMember m = LegacyParsers.parseLegacyMember(
									newSubAreaId, RegionMember.LinkageType.SUBAREA, part);
							if (m != null) newMembers.add(m);
						});

					} catch (Exception e) {
						LOG.warning("[Database] Skipping subarea " + oldId + " during migration: " + e.getMessage());
					}
				}
			}
		}

		if (tableExists("levels")) {
			try (Statement stmt = connection.createStatement();
				 ResultSet rs = stmt.executeQuery("SELECT * FROM levels")) {

				while (rs.next()) {
					String oldRegionId = rs.getString("regionId");
					try {
						Long newRegionId = regionIdMap.get(oldRegionId);
						if (newRegionId == null) {
							LOG.warning("[Database] Level references unknown region "
									+ oldRegionId + ", skipping.");
							continue;
						}
						Level lvl = new Level(
								Homestead.SNOWFLAKE.nextId(), newRegionId,
								rs.getInt("level"),
								rs.getLong("experience"),
								rs.getLong("totalExperience"),
								rs.getLong("createdAt"));
						newLevels.add(lvl);
					} catch (Exception e) {
						LOG.warning("[Database] Skipping level for region "
								+ oldRegionId + ": " + e.getMessage());
					}
				}
			}
		}

		if (tableExists("wars")) {
			try (Statement stmt = connection.createStatement();
				 ResultSet rs = stmt.executeQuery("SELECT * FROM wars")) {

				while (rs.next()) {
					String oldWarId = rs.getString("id");
					try {
						long newWarId = Homestead.SNOWFLAKE.nextId();


						List<Long> mappedRegionIds = new ArrayList<>();
						LegacyParsers.splitAndParse(rs.getString("regions"), "§", raw -> {
							Long mapped = regionIdMap.get(raw.trim());
							if (mapped != null) mappedRegionIds.add(mapped);
						});

						War war = new War(
								newWarId,
								rs.getString("name"),
								rs.getString("displayName"),
								rs.getString("description"),
								mappedRegionIds,
								rs.getDouble("prize"),
								rs.getLong("startedAt"));
						newWars.add(war);
						warRegionMap.put(newWarId, mappedRegionIds);

					} catch (Exception e) {
						LOG.warning("[Database] Skipping war " + oldWarId
								+ " during migration: " + e.getMessage());
					}
				}
			}
		}

		connection.setAutoCommit(false);
		try {
			try (Statement stmt = connection.createStatement()) {
				stmt.executeUpdate("DROP TABLE IF EXISTS regions");
				stmt.executeUpdate("DROP TABLE IF EXISTS subareas");
				stmt.executeUpdate("DROP TABLE IF EXISTS levels");
				stmt.executeUpdate("DROP TABLE IF EXISTS wars");
			}

			createNewTables();

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

			connection.commit();

			LOG.info(String.format(
					"[Database] Migrated: %d regions, %d members, %d chunks, %d logs, " +
							"%d rates, %d invites, %d bans, %d subareas, %d levels, %d wars.",
					newRegions.size(), newMembers.size(), newChunks.size(), newLogs.size(),
					newRates.size(), newInvites.size(), newBanned.size(),
					newSubAreas.size(), newLevels.size(), newWars.size()));

		} catch (SQLException e) {
			connection.rollback();
			throw e;
		} finally {
			connection.setAutoCommit(true);
		}
	}

	private void batchInsertRegions(List<Region> rows) throws SQLException {
		if (rows.isEmpty()) return;
		String sql =
				"INSERT OR IGNORE INTO regions " +
						"(id,name,displayName,description,ownerId,location,playerFlags,worldFlags," +
						" taxes,bank,mapColor,mapIcon,rent,weather,time,welcomeSign,upkeepAt,createdAt) " +
						"VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			for (Region r : rows) {
				ps.setLong(1, r.getUniqueId());
				ps.setString(2, r.getName());
				ps.setString(3, r.getDisplayName());
				ps.setString(4, r.getDescription());
				ps.setString(5, r.getOwnerId().toString());
				ps.setString(6, r.getLocation() != null ? r.getLocation().serialize() : null);
				ps.setLong(7, r.getPlayerFlags());
				ps.setLong(8, r.getWorldFlags());
				ps.setDouble(9, r.getTaxes());
				ps.setDouble(10, r.getBank());
				ps.setInt(11, r.getMapColor());
				ps.setString(12, r.getMapIcon());
				ps.setString(13, r.getRent() != null ? r.getRent().serialize() : null);
				ps.setInt(14, r.getWeather());
				ps.setInt(15, r.getTime());
				ps.setString(16, r.getWelcomeSign() != null ? r.getWelcomeSign().serialize() : null);
				ps.setLong(17, r.getUpkeepAt());
				ps.setLong(18, r.getCreatedAt());
				ps.addBatch();
			}
			ps.executeBatch();
		}
	}

	private void batchInsertRegionMembers(List<RegionMember> rows) throws SQLException {
		if (rows.isEmpty()) return;
		String sql =
				"INSERT OR IGNORE INTO region_members " +
						"(id,playerId,linkageType,regionId,subAreaId,playerFlags,controlFlags,joinedAt,taxesAt) " +
						"VALUES (?,?,?,?,?,?,?,?,?)";
		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			for (RegionMember m : rows) {
				ps.setLong(1, m.getUniqueId());
				ps.setString(2, m.getPlayerId().toString());
				ps.setInt(3, m.getLinkageType().getValue());
				ps.setLong(4, m.getRegionId());
				ps.setLong(5, m.getSubAreaId());
				ps.setLong(6, m.getPlayerFlags());
				ps.setLong(7, m.getControlFlags());
				ps.setLong(8, m.getJoinedAt());
				ps.setLong(9, m.getTaxesAt());
				ps.addBatch();
			}
			ps.executeBatch();
		}
	}

	private void batchInsertRegionChunks(List<RegionChunk> rows) throws SQLException {
		if (rows.isEmpty()) return;
		String sql =
				"INSERT OR IGNORE INTO region_chunks " +
						"(id,regionId,worldId,x,z,claimedAt,forceLoaded) VALUES (?,?,?,?,?,?,?)";
		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			for (RegionChunk c : rows) {
				ps.setLong(1, c.getUniqueId());
				ps.setLong(2, c.getRegionId());
				ps.setString(3, c.getWorldId().toString());
				ps.setInt(4, c.getX());
				ps.setInt(5, c.getZ());
				ps.setLong(6, c.getClaimedAt());
				ps.setInt(7, c.isForceLoaded() ? 1 : 0);
				ps.addBatch();
			}
			ps.executeBatch();
		}
	}

	private void batchInsertRegionLogs(List<RegionLog> rows) throws SQLException {
		if (rows.isEmpty()) return;
		String sql =
				"INSERT OR IGNORE INTO region_logs " +
						"(id,regionId,author,message,sentAt,read) VALUES (?,?,?,?,?,?)";
		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			for (RegionLog l : rows) {
				ps.setLong(1, l.getUniqueId());
				ps.setLong(2, l.getRegionId());
				ps.setString(3, l.getAuthor());
				ps.setString(4, l.getMessage());
				ps.setLong(5, l.getSentAt());
				ps.setInt(6, l.isRead() ? 1 : 0);
				ps.addBatch();
			}
			ps.executeBatch();
		}
	}

	private void batchInsertRegionRates(List<RegionRate> rows) throws SQLException {
		if (rows.isEmpty()) return;
		String sql =
				"INSERT OR IGNORE INTO region_rates " +
						"(id,regionId,playerId,rate,ratedAt) VALUES (?,?,?,?,?)";
		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			for (RegionRate r : rows) {
				ps.setLong(1, r.getUniqueId());
				ps.setLong(2, r.getRegionId());
				ps.setString(3, r.getPlayerId().toString());
				ps.setInt(4, r.getRate());
				ps.setLong(5, r.getRatedAt());
				ps.addBatch();
			}
			ps.executeBatch();
		}
	}

	private void batchInsertRegionInvites(List<RegionInvite> rows) throws SQLException {
		if (rows.isEmpty()) return;
		String sql =
				"INSERT OR IGNORE INTO region_invites " +
						"(id,regionId,playerId,invitedAt) VALUES (?,?,?,?)";
		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			for (RegionInvite i : rows) {
				ps.setLong(1, i.getUniqueId());
				ps.setLong(2, i.getRegionId());
				ps.setString(3, i.getPlayerId().toString());
				ps.setLong(4, i.getInvitedAt());
				ps.addBatch();
			}
			ps.executeBatch();
		}
	}

	private void batchInsertRegionBannedPlayers(List<RegionBannedPlayer> rows) throws SQLException {
		if (rows.isEmpty()) return;
		String sql =
				"INSERT OR IGNORE INTO region_banned_players " +
						"(id,regionId,playerId,reason,bannedAt) VALUES (?,?,?,?,?)";
		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			for (RegionBannedPlayer b : rows) {
				ps.setLong(1, b.getUniqueId());
				ps.setLong(2, b.getRegionId());
				ps.setString(3, b.getPlayerId().toString());
				ps.setString(4, b.getReason());
				ps.setLong(5, b.getBannedAt());
				ps.addBatch();
			}
			ps.executeBatch();
		}
	}

	private void batchInsertSubAreas(List<SubArea> rows) throws SQLException {
		if (rows.isEmpty()) return;
		String sql =
				"INSERT OR IGNORE INTO subareas " +
						"(id,regionId,name,worldId,point1,point2,playerFlags,rent,createdAt) VALUES (?,?,?,?,?,?,?,?,?)";
		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			for (SubArea s : rows) {
				ps.setLong(1, s.getUniqueId());
				ps.setLong(2, s.getRegionId());
				ps.setString(3, s.getName());
				ps.setString(4, s.getWorldId().toString());
				ps.setString(5, s.getPoint1().serialize());
				ps.setString(6, s.getPoint2().serialize());
				ps.setLong(7, s.getPlayerFlags());
				ps.setString(8, s.getRent() != null ? s.getRent().serialize() : null);
				ps.setLong(9, s.getCreatedAt());
				ps.addBatch();
			}
			ps.executeBatch();
		}
	}

	private void batchInsertLevels(List<Level> rows) throws SQLException {
		if (rows.isEmpty()) return;
		String sql =
				"INSERT OR IGNORE INTO levels " +
						"(id,regionId,level,experience,totalExperience,createdAt) VALUES (?,?,?,?,?,?)";
		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			for (Level l : rows) {
				ps.setLong(1, l.getUniqueId());
				ps.setLong(2, l.getRegionId());
				ps.setInt(3, l.getLevel());
				ps.setLong(4, l.getExperience());
				ps.setLong(5, l.getTotalExperience());
				ps.setLong(6, l.getCreatedAt());
				ps.addBatch();
			}
			ps.executeBatch();
		}
	}

	private void batchInsertWars(List<War> rows) throws SQLException {
		if (rows.isEmpty()) return;
		String sql =
				"INSERT OR IGNORE INTO wars " +
						"(id,name,displayName,description,prize,startedAt) VALUES (?,?,?,?,?,?)";
		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			for (War w : rows) {
				ps.setLong(1, w.getUniqueId());
				ps.setString(2, w.getName());
				ps.setString(3, w.getDisplayName());
				ps.setString(4, w.getDescription());
				ps.setDouble(5, w.getPrize());
				ps.setLong(6, w.getStartedAt());
				ps.addBatch();
			}
			ps.executeBatch();
		}
	}

	private void batchInsertWarRegions(Map<Long, List<Long>> warRegionMap) throws SQLException {
		if (warRegionMap.isEmpty()) return;
		String sql = "INSERT OR IGNORE INTO war_regions (warId,regionId) VALUES (?,?)";
		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			for (Map.Entry<Long, List<Long>> entry : warRegionMap.entrySet()) {
				long warId = entry.getKey();
				for (long regionId : entry.getValue()) {
					ps.setLong(1, warId);
					ps.setLong(2, regionId);
					ps.addBatch();
				}
			}
			ps.executeBatch();
		}
	}

	@Override
	public List<Region> importRegions() throws SQLException {
		List<Region> list = new ArrayList<>();
		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery("SELECT * FROM regions")) {
			while (rs.next()) {
				long id = rs.getLong("id");
				UUID ownerId = UUID.fromString(rs.getString("ownerId"));
				long created = rs.getLong("createdAt");

				Region region = new Region(id, rs.getString("name"), ownerId, created);
				region.setDisplayName(rs.getString("displayName"));
				region.setDescription(rs.getString("description"));
				region.setPlayerFlags(rs.getLong("playerFlags"));
				region.setWorldFlags(rs.getLong("worldFlags"));
				region.setTaxes(rs.getDouble("taxes"));
				region.setBank(rs.getDouble("bank"));
				region.setMapColor(rs.getInt("mapColor"));
				region.setMapIcon(rs.getString("mapIcon"));
				region.setWeather(rs.getInt("weather"));
				region.setTime(rs.getInt("time"));
				region.setUpkeepAt(rs.getLong("upkeepAt"));

				String locStr = rs.getString("location");
				if (locStr != null) region.setLocation(SeLocation.deserialize(locStr));

				String wsStr = rs.getString("welcomeSign");
				if (wsStr != null) region.setWelcomeSign(SeLocation.deserialize(wsStr));

				String rentStr = rs.getString("rent");
				if (rentStr != null) region.setRent(SeRent.deserialize(rentStr));

				list.add(region);
			}
		}
		return list;
	}

	@Override
	public List<RegionMember> importRegionMembers() throws SQLException {
		List<RegionMember> list = new ArrayList<>();
		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery("SELECT * FROM region_members")) {
			while (rs.next()) {
				UUID playerId = UUID.fromString(rs.getString("playerId"));
				int typeVal = rs.getInt("linkageType");
				long regionId = rs.getLong("regionId");
				long subAreaId = rs.getLong("subAreaId");
				long playerFlags = rs.getLong("playerFlags");
				long ctrlFlags = rs.getLong("controlFlags");
				long joinedAt = rs.getLong("joinedAt");
				long taxesAt = rs.getLong("taxesAt");

				RegionMember.LinkageType type =
						typeVal == RegionMember.LinkageType.REGION.getValue()
								? RegionMember.LinkageType.REGION
								: RegionMember.LinkageType.SUBAREA;

				long linkageId = type == RegionMember.LinkageType.REGION ? regionId : subAreaId;
				RegionMember member = new RegionMember(playerId, type, linkageId);
				member.setPlayerFlags(playerFlags);
				member.setControlFlags(ctrlFlags);
				member.setJoinedAt(joinedAt);
				member.setTaxesAt(taxesAt);
				list.add(member);
			}
		}
		return list;
	}

	@Override
	public List<RegionChunk> importRegionChunks() throws SQLException {
		List<RegionChunk> list = new ArrayList<>();
		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery("SELECT * FROM region_chunks")) {
			while (rs.next()) {
				list.add(new RegionChunk(
						rs.getLong("id"),
						rs.getLong("regionId"),
						UUID.fromString(rs.getString("worldId")),
						rs.getInt("x"),
						rs.getInt("z"),
						rs.getLong("claimedAt"),
						rs.getInt("forceLoaded") == 1));
			}
		}
		return list;
	}

	@Override
	public List<RegionLog> importRegionLogs() throws SQLException {
		List<RegionLog> list = new ArrayList<>();
		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery("SELECT * FROM region_logs")) {
			while (rs.next()) {
				list.add(new RegionLog(
						rs.getLong("id"),
						rs.getLong("regionId"),
						rs.getString("author"),
						rs.getString("message"),
						rs.getLong("sentAt"),
						rs.getInt("read") == 1));
			}
		}
		return list;
	}

	@Override
	public List<RegionRate> importRegionRates() throws SQLException {
		List<RegionRate> list = new ArrayList<>();
		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery("SELECT * FROM region_rates")) {
			while (rs.next()) {
				list.add(new RegionRate(
						rs.getLong("id"),
						rs.getLong("regionId"),
						UUID.fromString(rs.getString("playerId")),
						rs.getInt("rate"),
						rs.getLong("ratedAt")));
			}
		}
		return list;
	}

	@Override
	public List<RegionInvite> importRegionInvites() throws SQLException {
		List<RegionInvite> list = new ArrayList<>();
		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery("SELECT * FROM region_invites")) {
			while (rs.next()) {
				list.add(new RegionInvite(
						rs.getLong("id"),
						rs.getLong("regionId"),
						UUID.fromString(rs.getString("playerId")),
						rs.getLong("invitedAt")));
			}
		}
		return list;
	}

	@Override
	public List<RegionBannedPlayer> importRegionBannedPlayers() throws SQLException {
		List<RegionBannedPlayer> list = new ArrayList<>();
		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery("SELECT * FROM region_banned_players")) {
			while (rs.next()) {
				list.add(new RegionBannedPlayer(
						rs.getLong("id"),
						rs.getLong("regionId"),
						UUID.fromString(rs.getString("playerId")),
						rs.getString("reason"),
						rs.getLong("bannedAt")));
			}
		}
		return list;
	}

	@Override
	public List<SubArea> importSubAreas() throws SQLException {
		List<SubArea> list = new ArrayList<>();
		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery("SELECT * FROM subareas")) {
			while (rs.next()) {
				UUID worldId = UUID.fromString(rs.getString("worldId"));
				SeBlock p1 = SeBlock.deserialize(rs.getString("point1"));
				SeBlock p2 = SeBlock.deserialize(rs.getString("point2"));
				if (p1 == null || p2 == null) continue;

				String rentStr = rs.getString("rent");
				SeRent rent = rentStr != null ? SeRent.deserialize(rentStr) : null;

				SubArea subArea = new SubArea(
						rs.getLong("id"),
						rs.getLong("regionId"),
						rs.getString("name"),
						worldId, p1, p2,
						rs.getLong("playerFlags"),
						rent,
						rs.getLong("createdAt"));
				list.add(subArea);
			}
		}
		return list;
	}

	@Override
	public List<Level> importLevels() throws SQLException {
		List<Level> list = new ArrayList<>();
		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery("SELECT * FROM levels")) {
			while (rs.next()) {
				Level lvl = new Level(
						rs.getLong("id"),
						rs.getLong("regionId"),
						rs.getInt("level"),
						rs.getLong("experience"),
						rs.getLong("totalExperience"),
						rs.getLong("createdAt"));
				list.add(lvl);
			}
		}
		return list;
	}

	@Override
	public List<War> importWars() throws SQLException {
		Map<Long, List<Long>> warRegions = new HashMap<>();
		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery("SELECT warId, regionId FROM war_regions")) {
			while (rs.next()) {
				warRegions
						.computeIfAbsent(rs.getLong("warId"), k -> new ArrayList<>())
						.add(rs.getLong("regionId"));
			}
		}

		List<War> list = new ArrayList<>();
		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery("SELECT * FROM wars")) {
			while (rs.next()) {
				long warId = rs.getLong("id");
				War war = new War(
						warId,
						rs.getString("name"),
						rs.getString("displayName"),
						rs.getString("description"),
						warRegions.getOrDefault(warId, new ArrayList<>()),
						rs.getDouble("prize"),
						rs.getLong("startedAt"));
				list.add(war);
			}
		}
		return list;
	}

	@Override
	public void exportRegions(List<Region> regions) throws SQLException {
		Set<Long> dbIds = loadLongIds("SELECT id FROM regions");
		String upsert =
				"INSERT OR REPLACE INTO regions " +
						"(id,name,displayName,description,ownerId,location,playerFlags,worldFlags," +
						" taxes,bank,mapColor,mapIcon,rent,weather,time,welcomeSign,upkeepAt,createdAt) " +
						"VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

		connection.setAutoCommit(false);
		try (PreparedStatement ups = connection.prepareStatement(upsert);
			 PreparedStatement del = connection.prepareStatement("DELETE FROM regions WHERE id=?")) {

			Set<Long> cacheIds = new HashSet<>();
			for (Region r : regions) {
				cacheIds.add(r.getUniqueId());
				ups.setLong(1, r.getUniqueId());
				ups.setString(2, r.getName());
				ups.setString(3, r.getDisplayName());
				ups.setString(4, r.getDescription());
				ups.setString(5, r.getOwnerId().toString());
				ups.setString(6, r.getLocation() != null ? r.getLocation().serialize() : null);
				ups.setLong(7, r.getPlayerFlags());
				ups.setLong(8, r.getWorldFlags());
				ups.setDouble(9, r.getTaxes());
				ups.setDouble(10, r.getBank());
				ups.setInt(11, r.getMapColor());
				ups.setString(12, r.getMapIcon());
				ups.setString(13, r.getRent() != null ? r.getRent().serialize() : null);
				ups.setInt(14, r.getWeather());
				ups.setInt(15, r.getTime());
				ups.setString(16, r.getWelcomeSign() != null ? r.getWelcomeSign().serialize() : null);
				ups.setLong(17, r.getUpkeepAt());
				ups.setLong(18, r.getCreatedAt());
				ups.addBatch();
			}
			ups.executeBatch();

			dbIds.removeAll(cacheIds);
			for (long id : dbIds) {
				del.setLong(1, id);
				del.addBatch();
			}
			del.executeBatch();

			connection.commit();
		} catch (SQLException e) {
			connection.rollback();
			throw e;
		} finally {
			connection.setAutoCommit(true);
		}
	}

	@Override
	public void exportRegionMembers(List<RegionMember> members) throws SQLException {
		Set<Long> dbIds = loadLongIds("SELECT id FROM region_members");
		String upsert =
				"INSERT OR REPLACE INTO region_members " +
						"(id,playerId,linkageType,regionId,subAreaId,playerFlags,controlFlags,joinedAt,taxesAt) " +
						"VALUES (?,?,?,?,?,?,?,?,?)";

		connection.setAutoCommit(false);
		try (PreparedStatement ups = connection.prepareStatement(upsert);
			 PreparedStatement del = connection.prepareStatement("DELETE FROM region_members WHERE id=?")) {

			Set<Long> cacheIds = new HashSet<>();
			for (RegionMember m : members) {
				cacheIds.add(m.getUniqueId());
				ups.setLong(1, m.getUniqueId());
				ups.setString(2, m.getPlayerId().toString());
				ups.setInt(3, m.getLinkageType().getValue());
				ups.setLong(4, m.getRegionId());
				ups.setLong(5, m.getSubAreaId());
				ups.setLong(6, m.getPlayerFlags());
				ups.setLong(7, m.getControlFlags());
				ups.setLong(8, m.getJoinedAt());
				ups.setLong(9, m.getTaxesAt());
				ups.addBatch();
			}
			ups.executeBatch();

			dbIds.removeAll(cacheIds);
			for (long id : dbIds) {
				del.setLong(1, id);
				del.addBatch();
			}
			del.executeBatch();

			connection.commit();
		} catch (SQLException e) {
			connection.rollback();
			throw e;
		} finally {
			connection.setAutoCommit(true);
		}
	}

	@Override
	public void exportRegionChunks(List<RegionChunk> chunks) throws SQLException {
		Set<Long> dbIds = loadLongIds("SELECT id FROM region_chunks");
		String upsert =
				"INSERT OR REPLACE INTO region_chunks " +
						"(id,regionId,worldId,x,z,claimedAt,forceLoaded) VALUES (?,?,?,?,?,?,?)";

		connection.setAutoCommit(false);
		try (PreparedStatement ups = connection.prepareStatement(upsert);
			 PreparedStatement del = connection.prepareStatement("DELETE FROM region_chunks WHERE id=?")) {

			Set<Long> cacheIds = new HashSet<>();
			for (RegionChunk c : chunks) {
				cacheIds.add(c.getUniqueId());
				ups.setLong(1, c.getUniqueId());
				ups.setLong(2, c.getRegionId());
				ups.setString(3, c.getWorldId().toString());
				ups.setInt(4, c.getX());
				ups.setInt(5, c.getZ());
				ups.setLong(6, c.getClaimedAt());
				ups.setInt(7, c.isForceLoaded() ? 1 : 0);
				ups.addBatch();
			}
			ups.executeBatch();

			dbIds.removeAll(cacheIds);
			for (long id : dbIds) {
				del.setLong(1, id);
				del.addBatch();
			}
			del.executeBatch();

			connection.commit();
		} catch (SQLException e) {
			connection.rollback();
			throw e;
		} finally {
			connection.setAutoCommit(true);
		}
	}

	@Override
	public void exportRegionLogs(List<RegionLog> logs) throws SQLException {
		Set<Long> dbIds = loadLongIds("SELECT id FROM region_logs");
		String upsert =
				"INSERT OR REPLACE INTO region_logs " +
						"(id,regionId,author,message,sentAt,read) VALUES (?,?,?,?,?,?)";

		connection.setAutoCommit(false);
		try (PreparedStatement ups = connection.prepareStatement(upsert);
			 PreparedStatement del = connection.prepareStatement("DELETE FROM region_logs WHERE id=?")) {

			Set<Long> cacheIds = new HashSet<>();
			for (RegionLog l : logs) {
				cacheIds.add(l.getUniqueId());
				ups.setLong(1, l.getUniqueId());
				ups.setLong(2, l.getRegionId());
				ups.setString(3, l.getAuthor());
				ups.setString(4, l.getMessage());
				ups.setLong(5, l.getSentAt());
				ups.setInt(6, l.isRead() ? 1 : 0);
				ups.addBatch();
			}
			ups.executeBatch();

			dbIds.removeAll(cacheIds);
			for (long id : dbIds) {
				del.setLong(1, id);
				del.addBatch();
			}
			del.executeBatch();

			connection.commit();
		} catch (SQLException e) {
			connection.rollback();
			throw e;
		} finally {
			connection.setAutoCommit(true);
		}
	}

	@Override
	public void exportRegionRates(List<RegionRate> rates) throws SQLException {
		Set<Long> dbIds = loadLongIds("SELECT id FROM region_rates");
		String upsert =
				"INSERT OR REPLACE INTO region_rates " +
						"(id,regionId,playerId,rate,ratedAt) VALUES (?,?,?,?,?)";

		connection.setAutoCommit(false);
		try (PreparedStatement ups = connection.prepareStatement(upsert);
			 PreparedStatement del = connection.prepareStatement("DELETE FROM region_rates WHERE id=?")) {

			Set<Long> cacheIds = new HashSet<>();
			for (RegionRate r : rates) {
				cacheIds.add(r.getUniqueId());
				ups.setLong(1, r.getUniqueId());
				ups.setLong(2, r.getRegionId());
				ups.setString(3, r.getPlayerId().toString());
				ups.setInt(4, r.getRate());
				ups.setLong(5, r.getRatedAt());
				ups.addBatch();
			}
			ups.executeBatch();

			dbIds.removeAll(cacheIds);
			for (long id : dbIds) {
				del.setLong(1, id);
				del.addBatch();
			}
			del.executeBatch();

			connection.commit();
		} catch (SQLException e) {
			connection.rollback();
			throw e;
		} finally {
			connection.setAutoCommit(true);
		}
	}

	@Override
	public void exportRegionInvites(List<RegionInvite> invites) throws SQLException {
		Set<Long> dbIds = loadLongIds("SELECT id FROM region_invites");
		String upsert =
				"INSERT OR REPLACE INTO region_invites " +
						"(id,regionId,playerId,invitedAt) VALUES (?,?,?,?)";

		connection.setAutoCommit(false);
		try (PreparedStatement ups = connection.prepareStatement(upsert);
			 PreparedStatement del = connection.prepareStatement("DELETE FROM region_invites WHERE id=?")) {

			Set<Long> cacheIds = new HashSet<>();
			for (RegionInvite i : invites) {
				cacheIds.add(i.getUniqueId());
				ups.setLong(1, i.getUniqueId());
				ups.setLong(2, i.getRegionId());
				ups.setString(3, i.getPlayerId().toString());
				ups.setLong(4, i.getInvitedAt());
				ups.addBatch();
			}
			ups.executeBatch();

			dbIds.removeAll(cacheIds);
			for (long id : dbIds) {
				del.setLong(1, id);
				del.addBatch();
			}
			del.executeBatch();

			connection.commit();
		} catch (SQLException e) {
			connection.rollback();
			throw e;
		} finally {
			connection.setAutoCommit(true);
		}
	}

	@Override
	public void exportRegionBannedPlayers(List<RegionBannedPlayer> bannedPlayers) throws SQLException {
		Set<Long> dbIds = loadLongIds("SELECT id FROM region_banned_players");
		String upsert =
				"INSERT OR REPLACE INTO region_banned_players " +
						"(id,regionId,playerId,reason,bannedAt) VALUES (?,?,?,?,?)";

		connection.setAutoCommit(false);
		try (PreparedStatement ups = connection.prepareStatement(upsert);
			 PreparedStatement del = connection.prepareStatement("DELETE FROM region_banned_players WHERE id=?")) {

			Set<Long> cacheIds = new HashSet<>();
			for (RegionBannedPlayer b : bannedPlayers) {
				cacheIds.add(b.getUniqueId());
				ups.setLong(1, b.getUniqueId());
				ups.setLong(2, b.getRegionId());
				ups.setString(3, b.getPlayerId().toString());
				ups.setString(4, b.getReason());
				ups.setLong(5, b.getBannedAt());
				ups.addBatch();
			}
			ups.executeBatch();

			dbIds.removeAll(cacheIds);
			for (long id : dbIds) {
				del.setLong(1, id);
				del.addBatch();
			}
			del.executeBatch();

			connection.commit();
		} catch (SQLException e) {
			connection.rollback();
			throw e;
		} finally {
			connection.setAutoCommit(true);
		}
	}

	@Override
	public void exportSubAreas(List<SubArea> subAreas) throws SQLException {
		Set<Long> dbIds = loadLongIds("SELECT id FROM subareas");
		String upsert =
				"INSERT OR REPLACE INTO subareas " +
						"(id,regionId,name,worldId,point1,point2,playerFlags,rent,createdAt) VALUES (?,?,?,?,?,?,?,?,?)";

		connection.setAutoCommit(false);
		try (PreparedStatement ups = connection.prepareStatement(upsert);
			 PreparedStatement del = connection.prepareStatement("DELETE FROM subareas WHERE id=?")) {

			Set<Long> cacheIds = new HashSet<>();
			for (SubArea s : subAreas) {
				cacheIds.add(s.getUniqueId());
				ups.setLong(1, s.getUniqueId());
				ups.setLong(2, s.getRegionId());
				ups.setString(3, s.getName());
				ups.setString(4, s.getWorldId().toString());
				ups.setString(5, s.getPoint1().serialize());
				ups.setString(6, s.getPoint2().serialize());
				ups.setLong(7, s.getPlayerFlags());
				ups.setString(8, s.getRent() != null ? s.getRent().serialize() : null);
				ups.setLong(9, s.getCreatedAt());
				ups.addBatch();
			}
			ups.executeBatch();

			dbIds.removeAll(cacheIds);
			for (long id : dbIds) {
				del.setLong(1, id);
				del.addBatch();
			}
			del.executeBatch();

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
		Set<Long> dbIds = loadLongIds("SELECT id FROM levels");
		String upsert =
				"INSERT OR REPLACE INTO levels " +
						"(id,regionId,level,experience,totalExperience,createdAt) VALUES (?,?,?,?,?,?)";

		connection.setAutoCommit(false);
		try (PreparedStatement ups = connection.prepareStatement(upsert);
			 PreparedStatement del = connection.prepareStatement("DELETE FROM levels WHERE id=?")) {

			Set<Long> cacheIds = new HashSet<>();
			for (Level l : levels) {
				cacheIds.add(l.getUniqueId());
				ups.setLong(1, l.getUniqueId());
				ups.setLong(2, l.getRegionId());
				ups.setInt(3, l.getLevel());
				ups.setLong(4, l.getExperience());
				ups.setLong(5, l.getTotalExperience());
				ups.setLong(6, l.getCreatedAt());
				ups.addBatch();
			}
			ups.executeBatch();

			dbIds.removeAll(cacheIds);
			for (long id : dbIds) {
				del.setLong(1, id);
				del.addBatch();
			}
			del.executeBatch();

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
		Set<Long> dbWarIds = loadLongIds("SELECT id FROM wars");
		String upsertWar = "INSERT OR REPLACE INTO wars (id,name,displayName,description,prize,startedAt) VALUES (?,?,?,?,?,?)";
		String deleteWar = "DELETE FROM wars WHERE id=?";
		String deleteJunc = "DELETE FROM war_regions WHERE warId=?";
		String insertJunc = "INSERT OR IGNORE INTO war_regions (warId,regionId) VALUES (?,?)";

		connection.setAutoCommit(false);
		try (PreparedStatement upsWar = connection.prepareStatement(upsertWar);
			 PreparedStatement delWar = connection.prepareStatement(deleteWar);
			 PreparedStatement delJunc = connection.prepareStatement(deleteJunc);
			 PreparedStatement insJunc = connection.prepareStatement(insertJunc)) {

			Set<Long> cacheIds = new HashSet<>();

			for (War w : wars) {
				long warId = w.getUniqueId();
				cacheIds.add(warId);

				upsWar.setLong(1, warId);
				upsWar.setString(2, w.getName());
				upsWar.setString(3, w.getDisplayName());
				upsWar.setString(4, w.getDescription());
				upsWar.setDouble(5, w.getPrize());
				upsWar.setLong(6, w.getStartedAt());
				upsWar.addBatch();

				delJunc.setLong(1, warId);
				delJunc.addBatch();

				for (long regionId : w.getRegionIds()) {
					insJunc.setLong(1, warId);
					insJunc.setLong(2, regionId);
					insJunc.addBatch();
				}
			}
			upsWar.executeBatch();
			delJunc.executeBatch();
			insJunc.executeBatch();

			dbWarIds.removeAll(cacheIds);
			for (long staleId : dbWarIds) {
				delWar.setLong(1, staleId);
				delWar.addBatch();
				delJunc.setLong(1, staleId);
				delJunc.addBatch();
			}
			delWar.executeBatch();
			delJunc.executeBatch();

			connection.commit();
		} catch (SQLException e) {
			connection.rollback();
			throw e;
		} finally {
			connection.setAutoCommit(true);
		}
	}

	private Set<Long> loadLongIds(String selectSql) throws SQLException {
		Set<Long> ids = new HashSet<>();
		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery(selectSql)) {
			while (rs.next()) ids.add(rs.getLong(1));
		}
		return ids;
	}

	@Override
	public long getLatency() {
		List<String> tables = List.of(
				"regions", "region_members", "region_chunks", "region_logs",
				"region_rates", "region_invites", "region_banned_players",
				"subareas", "levels", "wars", "war_regions");
		try (Statement stmt = connection.createStatement()) {
			long start = System.currentTimeMillis();
			for (String t : tables) stmt.executeQuery("SELECT 1 FROM " + t + " LIMIT 1");
			return System.currentTimeMillis() - start;
		} catch (SQLException ignored) {
			return -1L;
		}
	}

	@Override
	public void closeConnection() throws SQLException {
		if (connection != null && !connection.isClosed()) connection.close();
	}

	@FunctionalInterface
	private interface ThrowingConsumer<T> {
		void accept(T t) throws Exception;
	}
}