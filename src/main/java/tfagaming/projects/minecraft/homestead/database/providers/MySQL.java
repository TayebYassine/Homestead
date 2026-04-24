package tfagaming.projects.minecraft.homestead.database.providers;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.models.*;
import tfagaming.projects.minecraft.homestead.models.serialize.SeBlock;
import tfagaming.projects.minecraft.homestead.models.serialize.SeLocation;
import tfagaming.projects.minecraft.homestead.models.serialize.SeRent;

import java.sql.*;
import java.util.*;

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

	@Override
	public void prepareTables() throws SQLException {
		if (legacyTablesExist()) {
			migrateFromLegacy();
		} else {
			createNewTables();
		}
	}

	private boolean legacyTablesExist() throws SQLException {
		return tableExists(TABLE_PREFIX + "regions") && columnExists(TABLE_PREFIX + "regions", "chunks");
	}

	private boolean tableExists(String table) throws SQLException {
		String sql = "SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?";
		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setString(1, table);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next();
			}
		}
	}

	private boolean columnExists(String table, String column) throws SQLException {
		String sql = "SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?";
		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setString(1, table);
			ps.setString(2, column);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next();
			}
		}
	}

	private void createNewTables() throws SQLException {
		String[] ddl = {
				"""
						CREATE TABLE IF NOT EXISTS `%sregions` (
						    id          BIGINT PRIMARY KEY,
						    name        TEXT    NOT NULL,
						    displayName TEXT,
						    description TEXT,
						    ownerId     CHAR(36) NOT NULL,
						    location    TEXT,
						    playerFlags BIGINT NOT NULL DEFAULT 0,
						    worldFlags  BIGINT NOT NULL DEFAULT 0,
						    taxes       DOUBLE NOT NULL DEFAULT 0.0,
						    bank        DOUBLE NOT NULL DEFAULT 0.0,
						    mapColor    INT    NOT NULL DEFAULT 0,
						    mapIcon     TEXT,
						    rent        TEXT,
						    weather     INT    NOT NULL DEFAULT 0,
						    time        INT    NOT NULL DEFAULT 0,
						    welcomeSign TEXT,
						    upkeepAt    BIGINT NOT NULL DEFAULT 0,
						    createdAt   BIGINT NOT NULL
						)
						""".formatted(TABLE_PREFIX),
				"""
						CREATE TABLE IF NOT EXISTS `%sregion_members` (
						    id           BIGINT PRIMARY KEY,
						    playerId     CHAR(36) NOT NULL,
						    linkageType  INT NOT NULL,
						    regionId     BIGINT NOT NULL DEFAULT -1,
						    subAreaId    BIGINT NOT NULL DEFAULT -1,
						    playerFlags  BIGINT NOT NULL DEFAULT 0,
						    controlFlags BIGINT NOT NULL DEFAULT 0,
						    joinedAt     BIGINT NOT NULL,
						    taxesAt      BIGINT NOT NULL DEFAULT 0
						)
						""".formatted(TABLE_PREFIX),
				"""
						CREATE TABLE IF NOT EXISTS `%sregion_chunks` (
						    id          BIGINT PRIMARY KEY,
						    regionId    BIGINT NOT NULL,
						    worldId     CHAR(36) NOT NULL,
						    x           INT NOT NULL,
						    z           INT NOT NULL,
						    claimedAt   BIGINT NOT NULL,
						    forceLoaded TINYINT NOT NULL DEFAULT 0
						)
						""".formatted(TABLE_PREFIX),
				"""
						CREATE TABLE IF NOT EXISTS `%sregion_logs` (
						    id       BIGINT PRIMARY KEY,
						    regionId BIGINT NOT NULL,
						    author   TEXT   NOT NULL,
						    message  TEXT   NOT NULL,
						    sentAt   BIGINT NOT NULL,
						    `read`   TINYINT NOT NULL DEFAULT 0
						)
						""".formatted(TABLE_PREFIX),
				"""
						CREATE TABLE IF NOT EXISTS `%sregion_rates` (
						    id       BIGINT PRIMARY KEY,
						    regionId BIGINT NOT NULL,
						    playerId CHAR(36) NOT NULL,
						    rate     INT NOT NULL,
						    ratedAt  BIGINT NOT NULL
						)
						""".formatted(TABLE_PREFIX),
				"""
						CREATE TABLE IF NOT EXISTS `%sregion_invites` (
						    id        BIGINT PRIMARY KEY,
						    regionId  BIGINT NOT NULL,
						    playerId  CHAR(36) NOT NULL,
						    invitedAt BIGINT NOT NULL
						)
						""".formatted(TABLE_PREFIX),
				"""
						CREATE TABLE IF NOT EXISTS `%sregion_banned_players` (
						    id       BIGINT PRIMARY KEY,
						    regionId BIGINT NOT NULL,
						    playerId CHAR(36) NOT NULL,
						    reason   TEXT,
						    bannedAt BIGINT NOT NULL
						)
						""".formatted(TABLE_PREFIX),
				"""
						CREATE TABLE IF NOT EXISTS `%ssubareas` (
						    id          BIGINT PRIMARY KEY,
						    regionId    BIGINT NOT NULL,
						    name        TEXT   NOT NULL,
						    worldId     CHAR(36) NOT NULL,
						    point1      TEXT   NOT NULL,
						    point2      TEXT   NOT NULL,
						    playerFlags BIGINT NOT NULL DEFAULT 0,
						    rent        TEXT,
						    createdAt   BIGINT NOT NULL
						)
						""".formatted(TABLE_PREFIX),
				"""
						CREATE TABLE IF NOT EXISTS `%slevels` (
						    id              BIGINT PRIMARY KEY,
						    regionId        BIGINT NOT NULL,
						    level           INT NOT NULL DEFAULT 0,
						    experience      BIGINT NOT NULL DEFAULT 0,
						    totalExperience BIGINT NOT NULL DEFAULT 0,
						    createdAt       BIGINT NOT NULL
						)
						""".formatted(TABLE_PREFIX),
				"""
						CREATE TABLE IF NOT EXISTS `%swars` (
						    id          BIGINT PRIMARY KEY,
						    name        TEXT   NOT NULL,
						    displayName TEXT,
						    description TEXT,
						    prize       DOUBLE NOT NULL DEFAULT 0.0,
						    startedAt   BIGINT NOT NULL
						)
						""".formatted(TABLE_PREFIX),
				"""
						CREATE TABLE IF NOT EXISTS `%swar_regions` (
						    warId    BIGINT NOT NULL,
						    regionId BIGINT NOT NULL,
						    PRIMARY KEY (warId, regionId)
						)
						""".formatted(TABLE_PREFIX)
		};

		try (Statement stmt = connection.createStatement()) {
			for (String sql : ddl) stmt.executeUpdate(sql.strip());
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

		String legacyRegions = TABLE_PREFIX + "regions";
		String legacySubareas = TABLE_PREFIX + "subareas";
		String legacyLevels = TABLE_PREFIX + "levels";
		String legacyWars = TABLE_PREFIX + "wars";

		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery("SELECT * FROM `" + legacyRegions + "`")) {
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
					if (LegacyParsers.isNotBlank(locStr)) region.setLocation(SeLocation.deserialize(locStr));

					String wsStr = rs.getString("welcomeSign");
					if (LegacyParsers.isNotBlank(wsStr)) region.setWelcomeSign(SeLocation.deserialize(wsStr));

					String rentStr = rs.getString("rent");
					if (LegacyParsers.isNotBlank(rentStr)) region.setRent(SeRent.deserialize(rentStr));

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
							newInvites.add(new RegionInvite(Homestead.SNOWFLAKE.nextId(), newId, pid, createdAt));
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
				} catch (Exception ignored) {
				}
			}
		}

		if (tableExists(legacySubareas)) {
			try (Statement stmt = connection.createStatement();
				 ResultSet rs = stmt.executeQuery("SELECT * FROM `" + legacySubareas + "`")) {
				while (rs.next()) {
					String oldId = rs.getString("id");
					String oldRegionId = rs.getString("regionId");
					try {
						Long newRegionId = regionIdMap.get(oldRegionId);
						if (newRegionId == null) continue;

						long newSubAreaId = Homestead.SNOWFLAKE.nextId();
						subAreaIdMap.put(oldId, newSubAreaId);

						UUID worldId = LegacyParsers.resolveWorldUUID(rs.getString("worldName"));
						if (worldId == null) continue;

						var point1 = LegacyParsers.parseLegacyBlock(worldId, rs.getString("point1"));
						var point2 = LegacyParsers.parseLegacyBlock(worldId, rs.getString("point2"));
						if (point1 == null || point2 == null) continue;

						String rentStr = rs.getString("rent");
						SeRent rent = LegacyParsers.isNotBlank(rentStr) ? SeRent.deserialize(rentStr) : null;

						SubArea subArea = new SubArea(
								newSubAreaId,
								newRegionId,
								rs.getString("name"),
								worldId,
								point1,
								point2,
								rs.getLong("flags"),
								rent,
								rs.getLong("createdAt"));
						newSubAreas.add(subArea);

						LegacyParsers.splitAndParse(rs.getString("members"), "§", part -> {
							RegionMember m = LegacyParsers.parseLegacyMember(newSubAreaId, RegionMember.LinkageType.SUBAREA, part);
							if (m != null) newMembers.add(m);
						});
					} catch (Exception ignored) {
					}
				}
			}
		}

		if (tableExists(legacyLevels)) {
			try (Statement stmt = connection.createStatement();
				 ResultSet rs = stmt.executeQuery("SELECT * FROM `" + legacyLevels + "`")) {
				while (rs.next()) {
					String oldRegionId = rs.getString("regionId");
					try {
						Long newRegionId = regionIdMap.get(oldRegionId);
						if (newRegionId == null) continue;
						newLevels.add(new Level(
								Homestead.SNOWFLAKE.nextId(),
								newRegionId,
								rs.getInt("level"),
								rs.getLong("experience"),
								rs.getLong("totalExperience"),
								rs.getLong("createdAt")));
					} catch (Exception ignored) {
					}
				}
			}
		}

		if (tableExists(legacyWars)) {
			try (Statement stmt = connection.createStatement();
				 ResultSet rs = stmt.executeQuery("SELECT * FROM `" + legacyWars + "`")) {
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
					} catch (Exception ignored) {
					}
				}
			}
		}

		connection.setAutoCommit(false);
		try {
			try (Statement stmt = connection.createStatement()) {
				stmt.executeUpdate("DROP TABLE IF EXISTS `" + legacyRegions + "`");
				stmt.executeUpdate("DROP TABLE IF EXISTS `" + legacySubareas + "`");
				stmt.executeUpdate("DROP TABLE IF EXISTS `" + legacyLevels + "`");
				stmt.executeUpdate("DROP TABLE IF EXISTS `" + legacyWars + "`");
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
				"INSERT IGNORE INTO `" + TABLE_PREFIX + "regions` " +
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
				"INSERT IGNORE INTO `" + TABLE_PREFIX + "region_members` " +
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
				"INSERT IGNORE INTO `" + TABLE_PREFIX + "region_chunks` " +
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
				"INSERT IGNORE INTO `" + TABLE_PREFIX + "region_logs` " +
						"(id,regionId,author,message,sentAt,`read`) VALUES (?,?,?,?,?,?)";
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
				"INSERT IGNORE INTO `" + TABLE_PREFIX + "region_rates` " +
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
				"INSERT IGNORE INTO `" + TABLE_PREFIX + "region_invites` " +
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
				"INSERT IGNORE INTO `" + TABLE_PREFIX + "region_banned_players` " +
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
				"INSERT IGNORE INTO `" + TABLE_PREFIX + "subareas` " +
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
				"INSERT IGNORE INTO `" + TABLE_PREFIX + "levels` " +
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
				"INSERT IGNORE INTO `" + TABLE_PREFIX + "wars` " +
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
		String sql = "INSERT IGNORE INTO `" + TABLE_PREFIX + "war_regions` (warId,regionId) VALUES (?,?)";
		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			for (var entry : warRegionMap.entrySet()) {
				for (long regionId : entry.getValue()) {
					ps.setLong(1, entry.getKey());
					ps.setLong(2, regionId);
					ps.addBatch();
				}
			}
			ps.executeBatch();
		}
	}

	@Override
	public List<Region> importRegions() throws Exception {
		List<Region> list = new ArrayList<>();
		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery("SELECT * FROM `" + TABLE_PREFIX + "regions`")) {
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
	public List<RegionMember> importRegionMembers() throws Exception {
		List<RegionMember> list = new ArrayList<>();
		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery("SELECT * FROM `" + TABLE_PREFIX + "region_members`")) {
			while (rs.next()) {
				UUID playerId = UUID.fromString(rs.getString("playerId"));
				int typeVal = rs.getInt("linkageType");
				long regionId = rs.getLong("regionId");
				long subAreaId = rs.getLong("subAreaId");

				RegionMember.LinkageType type =
						typeVal == RegionMember.LinkageType.REGION.getValue()
								? RegionMember.LinkageType.REGION
								: RegionMember.LinkageType.SUBAREA;
				long linkageId = type == RegionMember.LinkageType.REGION ? regionId : subAreaId;

				RegionMember member = new RegionMember(playerId, type, linkageId);
				member.setPlayerFlags(rs.getLong("playerFlags"));
				member.setControlFlags(rs.getLong("controlFlags"));
				member.setJoinedAt(rs.getLong("joinedAt"));
				member.setTaxesAt(rs.getLong("taxesAt"));
				list.add(member);
			}
		}
		return list;
	}

	@Override
	public List<RegionChunk> importRegionChunks() throws Exception {
		List<RegionChunk> list = new ArrayList<>();
		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery("SELECT * FROM `" + TABLE_PREFIX + "region_chunks`")) {
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
	public List<RegionLog> importRegionLogs() throws Exception {
		List<RegionLog> list = new ArrayList<>();
		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery("SELECT * FROM `" + TABLE_PREFIX + "region_logs`")) {
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
	public List<RegionRate> importRegionRates() throws Exception {
		List<RegionRate> list = new ArrayList<>();
		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery("SELECT * FROM `" + TABLE_PREFIX + "region_rates`")) {
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
	public List<RegionInvite> importRegionInvites() throws Exception {
		List<RegionInvite> list = new ArrayList<>();
		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery("SELECT * FROM `" + TABLE_PREFIX + "region_invites`")) {
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
	public List<RegionBannedPlayer> importRegionBannedPlayers() throws Exception {
		List<RegionBannedPlayer> list = new ArrayList<>();
		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery("SELECT * FROM `" + TABLE_PREFIX + "region_banned_players`")) {
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
	public List<SubArea> importSubAreas() throws Exception {
		List<SubArea> list = new ArrayList<>();
		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery("SELECT * FROM `" + TABLE_PREFIX + "subareas`")) {
			while (rs.next()) {
				UUID worldId = UUID.fromString(rs.getString("worldId"));
				SeBlock p1 = SeBlock.deserialize(rs.getString("point1"));
				SeBlock p2 = SeBlock.deserialize(rs.getString("point2"));
				if (p1 == null || p2 == null) continue;
				String rentStr = rs.getString("rent");
				SeRent rent = rentStr != null ? SeRent.deserialize(rentStr) : null;
				list.add(new SubArea(
						rs.getLong("id"),
						rs.getLong("regionId"),
						rs.getString("name"),
						worldId, p1, p2,
						rs.getLong("playerFlags"),
						rent,
						rs.getLong("createdAt")));
			}
		}
		return list;
	}

	@Override
	public List<Level> importLevels() throws Exception {
		List<Level> list = new ArrayList<>();
		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery("SELECT * FROM `" + TABLE_PREFIX + "levels`")) {
			while (rs.next()) {
				list.add(new Level(
						rs.getLong("id"),
						rs.getLong("regionId"),
						rs.getInt("level"),
						rs.getLong("experience"),
						rs.getLong("totalExperience"),
						rs.getLong("createdAt")));
			}
		}
		return list;
	}

	@Override
	public List<War> importWars() throws Exception {
		Map<Long, List<Long>> warRegions = new HashMap<>();
		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery("SELECT warId, regionId FROM `" + TABLE_PREFIX + "war_regions`")) {
			while (rs.next()) {
				warRegions.computeIfAbsent(rs.getLong("warId"), k -> new ArrayList<>()).add(rs.getLong("regionId"));
			}
		}

		List<War> list = new ArrayList<>();
		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery("SELECT * FROM `" + TABLE_PREFIX + "wars`")) {
			while (rs.next()) {
				long warId = rs.getLong("id");
				list.add(new War(
						warId,
						rs.getString("name"),
						rs.getString("displayName"),
						rs.getString("description"),
						warRegions.getOrDefault(warId, new ArrayList<>()),
						rs.getDouble("prize"),
						rs.getLong("startedAt")));
			}
		}
		return list;
	}

	@Override
	public void exportRegions(List<Region> regions) throws Exception {
		upsertSimple(
				"SELECT id FROM `" + TABLE_PREFIX + "regions`",
				"INSERT INTO `" + TABLE_PREFIX + "regions` " +
						"(id,name,displayName,description,ownerId,location,playerFlags,worldFlags,taxes,bank,mapColor,mapIcon,rent,weather,time,welcomeSign,upkeepAt,createdAt) " +
						"VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) " +
						"ON DUPLICATE KEY UPDATE " +
						"name=VALUES(name),displayName=VALUES(displayName),description=VALUES(description),ownerId=VALUES(ownerId),location=VALUES(location)," +
						"playerFlags=VALUES(playerFlags),worldFlags=VALUES(worldFlags),taxes=VALUES(taxes),bank=VALUES(bank),mapColor=VALUES(mapColor)," +
						"mapIcon=VALUES(mapIcon),rent=VALUES(rent),weather=VALUES(weather),time=VALUES(time),welcomeSign=VALUES(welcomeSign),upkeepAt=VALUES(upkeepAt),createdAt=VALUES(createdAt)",
				"DELETE FROM `" + TABLE_PREFIX + "regions` WHERE id=?",
				regions,
				(ps, r) -> {
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
				});
	}

	@Override
	public void exportRegionMembers(List<RegionMember> members) throws Exception {
		upsertSimple(
				"SELECT id FROM `" + TABLE_PREFIX + "region_members`",
				"INSERT INTO `" + TABLE_PREFIX + "region_members` " +
						"(id,playerId,linkageType,regionId,subAreaId,playerFlags,controlFlags,joinedAt,taxesAt) VALUES (?,?,?,?,?,?,?,?,?) " +
						"ON DUPLICATE KEY UPDATE " +
						"playerId=VALUES(playerId),linkageType=VALUES(linkageType),regionId=VALUES(regionId),subAreaId=VALUES(subAreaId)," +
						"playerFlags=VALUES(playerFlags),controlFlags=VALUES(controlFlags),joinedAt=VALUES(joinedAt),taxesAt=VALUES(taxesAt)",
				"DELETE FROM `" + TABLE_PREFIX + "region_members` WHERE id=?",
				members,
				(ps, m) -> {
					ps.setLong(1, m.getUniqueId());
					ps.setString(2, m.getPlayerId().toString());
					ps.setInt(3, m.getLinkageType().getValue());
					ps.setLong(4, m.getRegionId());
					ps.setLong(5, m.getSubAreaId());
					ps.setLong(6, m.getPlayerFlags());
					ps.setLong(7, m.getControlFlags());
					ps.setLong(8, m.getJoinedAt());
					ps.setLong(9, m.getTaxesAt());
				});
	}

	@Override
	public void exportRegionChunks(List<RegionChunk> chunks) throws Exception {
		upsertSimple(
				"SELECT id FROM `" + TABLE_PREFIX + "region_chunks`",
				"INSERT INTO `" + TABLE_PREFIX + "region_chunks` " +
						"(id,regionId,worldId,x,z,claimedAt,forceLoaded) VALUES (?,?,?,?,?,?,?) " +
						"ON DUPLICATE KEY UPDATE " +
						"regionId=VALUES(regionId),worldId=VALUES(worldId),x=VALUES(x),z=VALUES(z),claimedAt=VALUES(claimedAt),forceLoaded=VALUES(forceLoaded)",
				"DELETE FROM `" + TABLE_PREFIX + "region_chunks` WHERE id=?",
				chunks,
				(ps, c) -> {
					ps.setLong(1, c.getUniqueId());
					ps.setLong(2, c.getRegionId());
					ps.setString(3, c.getWorldId().toString());
					ps.setInt(4, c.getX());
					ps.setInt(5, c.getZ());
					ps.setLong(6, c.getClaimedAt());
					ps.setInt(7, c.isForceLoaded() ? 1 : 0);
				});
	}

	@Override
	public void exportRegionLogs(List<RegionLog> logs) throws Exception {
		upsertSimple(
				"SELECT id FROM `" + TABLE_PREFIX + "region_logs`",
				"INSERT INTO `" + TABLE_PREFIX + "region_logs` " +
						"(id,regionId,author,message,sentAt,`read`) VALUES (?,?,?,?,?,?) " +
						"ON DUPLICATE KEY UPDATE " +
						"regionId=VALUES(regionId),author=VALUES(author),message=VALUES(message),sentAt=VALUES(sentAt),`read`=VALUES(`read`)",
				"DELETE FROM `" + TABLE_PREFIX + "region_logs` WHERE id=?",
				logs,
				(ps, l) -> {
					ps.setLong(1, l.getUniqueId());
					ps.setLong(2, l.getRegionId());
					ps.setString(3, l.getAuthor());
					ps.setString(4, l.getMessage());
					ps.setLong(5, l.getSentAt());
					ps.setInt(6, l.isRead() ? 1 : 0);
				});
	}

	@Override
	public void exportRegionRates(List<RegionRate> rates) throws Exception {
		upsertSimple(
				"SELECT id FROM `" + TABLE_PREFIX + "region_rates`",
				"INSERT INTO `" + TABLE_PREFIX + "region_rates` " +
						"(id,regionId,playerId,rate,ratedAt) VALUES (?,?,?,?,?) " +
						"ON DUPLICATE KEY UPDATE " +
						"regionId=VALUES(regionId),playerId=VALUES(playerId),rate=VALUES(rate),ratedAt=VALUES(ratedAt)",
				"DELETE FROM `" + TABLE_PREFIX + "region_rates` WHERE id=?",
				rates,
				(ps, r) -> {
					ps.setLong(1, r.getUniqueId());
					ps.setLong(2, r.getRegionId());
					ps.setString(3, r.getPlayerId().toString());
					ps.setInt(4, r.getRate());
					ps.setLong(5, r.getRatedAt());
				});
	}

	@Override
	public void exportRegionInvites(List<RegionInvite> invites) throws Exception {
		upsertSimple(
				"SELECT id FROM `" + TABLE_PREFIX + "region_invites`",
				"INSERT INTO `" + TABLE_PREFIX + "region_invites` " +
						"(id,regionId,playerId,invitedAt) VALUES (?,?,?,?) " +
						"ON DUPLICATE KEY UPDATE " +
						"regionId=VALUES(regionId),playerId=VALUES(playerId),invitedAt=VALUES(invitedAt)",
				"DELETE FROM `" + TABLE_PREFIX + "region_invites` WHERE id=?",
				invites,
				(ps, i) -> {
					ps.setLong(1, i.getUniqueId());
					ps.setLong(2, i.getRegionId());
					ps.setString(3, i.getPlayerId().toString());
					ps.setLong(4, i.getInvitedAt());
				});
	}

	@Override
	public void exportRegionBannedPlayers(List<RegionBannedPlayer> bannedPlayers) throws Exception {
		upsertSimple(
				"SELECT id FROM `" + TABLE_PREFIX + "region_banned_players`",
				"INSERT INTO `" + TABLE_PREFIX + "region_banned_players` " +
						"(id,regionId,playerId,reason,bannedAt) VALUES (?,?,?,?,?) " +
						"ON DUPLICATE KEY UPDATE " +
						"regionId=VALUES(regionId),playerId=VALUES(playerId),reason=VALUES(reason),bannedAt=VALUES(bannedAt)",
				"DELETE FROM `" + TABLE_PREFIX + "region_banned_players` WHERE id=?",
				bannedPlayers,
				(ps, b) -> {
					ps.setLong(1, b.getUniqueId());
					ps.setLong(2, b.getRegionId());
					ps.setString(3, b.getPlayerId().toString());
					ps.setString(4, b.getReason());
					ps.setLong(5, b.getBannedAt());
				});
	}

	@Override
	public void exportSubAreas(List<SubArea> subAreas) throws Exception {
		upsertSimple(
				"SELECT id FROM `" + TABLE_PREFIX + "subareas`",
				"INSERT INTO `" + TABLE_PREFIX + "subareas` " +
						"(id,regionId,name,worldId,point1,point2,playerFlags,rent,createdAt) VALUES (?,?,?,?,?,?,?,?,?) " +
						"ON DUPLICATE KEY UPDATE " +
						"regionId=VALUES(regionId),name=VALUES(name),worldId=VALUES(worldId),point1=VALUES(point1),point2=VALUES(point2)," +
						"playerFlags=VALUES(playerFlags),rent=VALUES(rent),createdAt=VALUES(createdAt)",
				"DELETE FROM `" + TABLE_PREFIX + "subareas` WHERE id=?",
				subAreas,
				(ps, s) -> {
					ps.setLong(1, s.getUniqueId());
					ps.setLong(2, s.getRegionId());
					ps.setString(3, s.getName());
					ps.setString(4, s.getWorldId().toString());
					ps.setString(5, s.getPoint1().serialize());
					ps.setString(6, s.getPoint2().serialize());
					ps.setLong(7, s.getPlayerFlags());
					ps.setString(8, s.getRent() != null ? s.getRent().serialize() : null);
					ps.setLong(9, s.getCreatedAt());
				});
	}

	@Override
	public void exportLevels(List<Level> levels) throws Exception {
		upsertSimple(
				"SELECT id FROM `" + TABLE_PREFIX + "levels`",
				"INSERT INTO `" + TABLE_PREFIX + "levels` " +
						"(id,regionId,level,experience,totalExperience,createdAt) VALUES (?,?,?,?,?,?) " +
						"ON DUPLICATE KEY UPDATE " +
						"regionId=VALUES(regionId),level=VALUES(level),experience=VALUES(experience),totalExperience=VALUES(totalExperience),createdAt=VALUES(createdAt)",
				"DELETE FROM `" + TABLE_PREFIX + "levels` WHERE id=?",
				levels,
				(ps, l) -> {
					ps.setLong(1, l.getUniqueId());
					ps.setLong(2, l.getRegionId());
					ps.setInt(3, l.getLevel());
					ps.setLong(4, l.getExperience());
					ps.setLong(5, l.getTotalExperience());
					ps.setLong(6, l.getCreatedAt());
				});
	}

	@Override
	public void exportWars(List<War> wars) throws Exception {
		Set<Long> dbWarIds = loadLongIds("SELECT id FROM `" + TABLE_PREFIX + "wars`");

		String upsertWar =
				"INSERT INTO `" + TABLE_PREFIX + "wars` (id,name,displayName,description,prize,startedAt) VALUES (?,?,?,?,?,?) " +
						"ON DUPLICATE KEY UPDATE " +
						"name=VALUES(name),displayName=VALUES(displayName),description=VALUES(description),prize=VALUES(prize),startedAt=VALUES(startedAt)";
		String deleteWar = "DELETE FROM `" + TABLE_PREFIX + "wars` WHERE id=?";
		String deleteJunc = "DELETE FROM `" + TABLE_PREFIX + "war_regions` WHERE warId=?";
		String insertJunc = "INSERT IGNORE INTO `" + TABLE_PREFIX + "war_regions` (warId,regionId) VALUES (?,?)";

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

	private <T> void upsertSimple(String selectIds, String upsertSql, String deleteSql, List<T> rows, Binder<T> binder) throws SQLException {
		Set<Long> dbIds = loadLongIds(selectIds);
		connection.setAutoCommit(false);
		try (PreparedStatement ups = connection.prepareStatement(upsertSql);
			 PreparedStatement del = connection.prepareStatement(deleteSql)) {
			Set<Long> cacheIds = new HashSet<>();
			for (T row : rows) {
				long id = (row instanceof Region r) ? r.getUniqueId()
						: (row instanceof RegionMember m) ? m.getUniqueId()
						: (row instanceof RegionChunk c) ? c.getUniqueId()
						: (row instanceof RegionLog l) ? l.getUniqueId()
						: (row instanceof RegionRate r) ? r.getUniqueId()
						: (row instanceof RegionInvite i) ? i.getUniqueId()
						: (row instanceof RegionBannedPlayer b) ? b.getUniqueId()
						: (row instanceof SubArea s) ? s.getUniqueId()
						: (row instanceof Level l) ? l.getUniqueId()
						: -1L;
				cacheIds.add(id);
				binder.bind(ups, row);
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
	public long getLatency() {
		List<String> tables = List.of(
				TABLE_PREFIX + "regions",
				TABLE_PREFIX + "region_members",
				TABLE_PREFIX + "region_chunks",
				TABLE_PREFIX + "region_logs",
				TABLE_PREFIX + "region_rates",
				TABLE_PREFIX + "region_invites",
				TABLE_PREFIX + "region_banned_players",
				TABLE_PREFIX + "subareas",
				TABLE_PREFIX + "levels",
				TABLE_PREFIX + "wars",
				TABLE_PREFIX + "war_regions"
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

	@FunctionalInterface
	private interface Binder<T> {
		void bind(PreparedStatement ps, T value) throws SQLException;
	}
}