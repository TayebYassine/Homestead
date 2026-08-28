package me.tayebyassine.homestead.database;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.jdbc.DataSourceConnectionSource;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.jdbc.db.MariaDbDatabaseType;
import com.j256.ormlite.jdbc.db.MysqlDatabaseType;
import com.j256.ormlite.jdbc.db.PostgresDatabaseType;
import com.j256.ormlite.jdbc.db.SqliteDatabaseType;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;
import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.database.entities.*;
import me.tayebyassine.homestead.logs.Logger;
import me.tayebyassine.homestead.models.*;
import me.tayebyassine.homestead.models.serialize.SeBlock;
import me.tayebyassine.homestead.models.serialize.SeLocation;
import me.tayebyassine.homestead.models.serialize.SeRent;
import me.tayebyassine.homestead.resources.ResourceType;
import me.tayebyassine.homestead.resources.Resources;
import me.tayebyassine.homestead.resources.files.ConfigFile;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class Database {

	private static final List<String> TABLES = List.of(
			"regions", "region_members", "region_chunks", "region_logs",
			"region_rates", "region_invites", "region_banned_players",
			"subareas", "levels", "wars", "war_regions");

	private final Driver provider;
	private final ConnectionSource connectionSource;
	private final String jdbcUrl;
	private final String username;
	private final String password;
	private final String tablePrefix;

	private final DatabaseTableConfig<RegionEntity> regionTableConfig;
	private final DatabaseTableConfig<RegionMemberEntity> regionMemberTableConfig;
	private final DatabaseTableConfig<RegionChunkEntity> regionChunkTableConfig;
	private final DatabaseTableConfig<RegionLogEntity> regionLogTableConfig;
	private final DatabaseTableConfig<RegionRateEntity> regionRateTableConfig;
	private final DatabaseTableConfig<RegionInviteEntity> regionInviteTableConfig;
	private final DatabaseTableConfig<RegionBanEntity> regionBanTableConfig;
	private final DatabaseTableConfig<SubAreaEntity> subAreaTableConfig;
	private final DatabaseTableConfig<LevelEntity> levelTableConfig;
	private final DatabaseTableConfig<WarEntity> warTableConfig;

	private final Dao<RegionEntity, Long> regionDao;
	private final Dao<RegionMemberEntity, Long> regionMemberDao;
	private final Dao<RegionChunkEntity, Long> regionChunkDao;
	private final Dao<RegionLogEntity, Long> regionLogDao;
	private final Dao<RegionRateEntity, Long> regionRateDao;
	private final Dao<RegionInviteEntity, Long> regionInviteDao;
	private final Dao<RegionBanEntity, Long> regionBanDao;
	private final Dao<SubAreaEntity, Long> subAreaDao;
	private final Dao<LevelEntity, Long> levelDao;
	private final Dao<WarEntity, Long> warDao;

	private boolean closed = false;

	public Database(Driver provider) throws Exception {
		this(provider,
				resolveJdbcUrl(provider),
				provider == Driver.SQLITE ? null : resolveUsername(provider),
				provider == Driver.SQLITE ? null : resolvePassword(provider),
				provider == Driver.SQLITE ? "" : resolveTablePrefix(provider));
	}

	Database(Driver provider, String jdbcUrl, String username, String password, String tablePrefix) throws Exception {
		this.provider = provider;
		this.jdbcUrl = jdbcUrl;
		this.username = username;
		this.password = password;
		this.tablePrefix = sanitizePrefix(tablePrefix);

		this.connectionSource = createConnectionSource();
		this.regionTableConfig = tableConfig(RegionEntity.class, "regions");
		this.regionMemberTableConfig = tableConfig(RegionMemberEntity.class, "region_members");
		this.regionChunkTableConfig = tableConfig(RegionChunkEntity.class, "region_chunks");
		this.regionLogTableConfig = tableConfig(RegionLogEntity.class, "region_logs");
		this.regionRateTableConfig = tableConfig(RegionRateEntity.class, "region_rates");
		this.regionInviteTableConfig = tableConfig(RegionInviteEntity.class, "region_invites");
		this.regionBanTableConfig = tableConfig(RegionBanEntity.class, "region_banned_players");
		this.subAreaTableConfig = tableConfig(SubAreaEntity.class, "subareas");
		this.levelTableConfig = tableConfig(LevelEntity.class, "levels");
		this.warTableConfig = tableConfig(WarEntity.class, "wars");

		this.regionDao = DaoManager.createDao(connectionSource, regionTableConfig);
		this.regionMemberDao = DaoManager.createDao(connectionSource, regionMemberTableConfig);
		this.regionChunkDao = DaoManager.createDao(connectionSource, regionChunkTableConfig);
		this.regionLogDao = DaoManager.createDao(connectionSource, regionLogTableConfig);
		this.regionRateDao = DaoManager.createDao(connectionSource, regionRateTableConfig);
		this.regionInviteDao = DaoManager.createDao(connectionSource, regionInviteTableConfig);
		this.regionBanDao = DaoManager.createDao(connectionSource, regionBanTableConfig);
		this.subAreaDao = DaoManager.createDao(connectionSource, subAreaTableConfig);
		this.levelDao = DaoManager.createDao(connectionSource, levelTableConfig);
		this.warDao = DaoManager.createDao(connectionSource, warTableConfig);

		prepareTables();
	}

	private ConnectionSource createConnectionSource() throws Exception {
		if (provider == Driver.SQLITE) {
			try {
				Class.forName("org.sqlite.JDBC");
			} catch (ClassNotFoundException e) {
				throw new SQLException("SQLite JDBC driver not found", e);
			}

			SQLiteConfig sqliteConfig = new SQLiteConfig();
			sqliteConfig.setJournalMode(SQLiteConfig.JournalMode.WAL);
			sqliteConfig.setBusyTimeout(30000);

			SQLiteDataSource dataSource = new SQLiteDataSource(sqliteConfig);
			dataSource.setUrl(jdbcUrl);

			return new DataSourceConnectionSource(dataSource, databaseType());
		}

		if (username == null || username.isBlank()) {
			return new JdbcConnectionSource(jdbcUrl, databaseType());
		}

		return new JdbcConnectionSource(jdbcUrl, username, password, databaseType());
	}

	private DatabaseType databaseType() {
		return switch (provider) {
			case POSTGRESQL -> new PostgresDatabaseType();
			case MARIADB -> new MariaDbDatabaseType();
			case MYSQL -> new MysqlDatabaseType();
			case SQLITE -> new SqliteDatabaseType();
		};
	}

	private String sanitizePrefix(String prefix) {
		return prefix == null ? "" : prefix.replaceAll("[^A-Za-z0-9_]", "");
	}

	private static String resolveJdbcUrl(Driver provider) {
		ConfigFile config = Resources.<ConfigFile>get(ResourceType.Config);

		return switch (provider) {
			case POSTGRESQL -> "jdbc:postgresql://" + config.getString("database.postgresql.host")
					+ ":" + config.getInt("database.postgresql.port")
					+ "/" + config.getString("database.postgresql.database")
					+ config.getString("database.postgresql.jdbc_url_parameters");
			case MARIADB -> "jdbc:mariadb://" + config.getString("database.mariadb.host")
					+ ":" + config.getInt("database.mariadb.port")
					+ "/" + config.getString("database.mariadb.database")
					+ config.getString("database.mariadb.jdbc_url_parameters");
			case MYSQL -> "jdbc:mysql://" + config.getString("database.mysql.host")
					+ ":" + config.getInt("database.mysql.port")
					+ "/" + config.getString("database.mysql.database")
					+ config.getString("database.mysql.jdbc_url_parameters");
			case SQLITE -> "jdbc:sqlite:" + config.getString("database.sqlite");
		};
	}

	private static String resolveUsername(Driver provider) {
		ConfigFile config = Resources.<ConfigFile>get(ResourceType.Config);

		return switch (provider) {
			case POSTGRESQL -> config.getString("database.postgresql.username");
			case MARIADB -> config.getString("database.mariadb.username");
			case MYSQL -> config.getString("database.mysql.username");
			case SQLITE -> null;
		};
	}

	private static String resolvePassword(Driver provider) {
		ConfigFile config = Resources.<ConfigFile>get(ResourceType.Config);

		return switch (provider) {
			case POSTGRESQL -> config.getString("database.postgresql.password");
			case MARIADB -> config.getString("database.mariadb.password");
			case MYSQL -> config.getString("database.mysql.password");
			case SQLITE -> null;
		};
	}

	private static String resolveTablePrefix(Driver provider) {
		ConfigFile config = Resources.<ConfigFile>get(ResourceType.Config);

		return switch (provider) {
			case POSTGRESQL -> config.getString("database.postgresql.table_prefix");
			case MARIADB -> config.getString("database.mariadb.table_prefix");
			case MYSQL -> config.getString("database.mysql.table_prefix");
			case SQLITE -> "";
		};
	}

	private <T> DatabaseTableConfig<T> tableConfig(Class<T> entityClass, String baseName) throws SQLException {
		DatabaseTableConfig<T> config = DatabaseTableConfig.fromClass(databaseType(), entityClass);
		if (!tablePrefix.isEmpty()) {
			config.setTableName(tablePrefix + baseName);
		}
		return config;
	}

	private String quote(String name) {
		StringBuilder sb = new StringBuilder();
		databaseType().appendEscapedEntityName(sb, name);
		return sb.toString();
	}

	private void prepareTables() throws SQLException {
		TableUtils.createTableIfNotExists(connectionSource, regionTableConfig);
		TableUtils.createTableIfNotExists(connectionSource, regionMemberTableConfig);
		TableUtils.createTableIfNotExists(connectionSource, regionChunkTableConfig);
		TableUtils.createTableIfNotExists(connectionSource, regionLogTableConfig);
		TableUtils.createTableIfNotExists(connectionSource, regionRateTableConfig);
		TableUtils.createTableIfNotExists(connectionSource, regionInviteTableConfig);
		TableUtils.createTableIfNotExists(connectionSource, regionBanTableConfig);
		TableUtils.createTableIfNotExists(connectionSource, subAreaTableConfig);
		TableUtils.createTableIfNotExists(connectionSource, levelTableConfig);
		TableUtils.createTableIfNotExists(connectionSource, warTableConfig);

		String warRegions = tablePrefix + "war_regions";
		try (Connection raw = rawConnection();
			 Statement stmt = raw.createStatement()) {
			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + quote(warRegions)
					+ " (" + quote("warId") + " BIGINT NOT NULL, "
					+ quote("regionId") + " BIGINT NOT NULL, "
					+ "PRIMARY KEY (" + quote("warId") + ", " + quote("regionId") + "))");
		}
	}

	private Connection rawConnection() throws SQLException {
		if (provider == Driver.SQLITE) {
			try {
				Class.forName("org.sqlite.JDBC");
			} catch (ClassNotFoundException e) {
				throw new SQLException("SQLite JDBC driver not found", e);
			}
		}
		return username == null || username.isBlank()
				? DriverManager.getConnection(jdbcUrl)
				: DriverManager.getConnection(jdbcUrl, username, password);
	}

	public static long getCacheLatency() {
		return Homestead.REGION_CACHE.getLatency()
				+ Homestead.SUBAREA_CACHE.getLatency()
				+ Homestead.WAR_CACHE.getLatency()
				+ Homestead.LEVEL_CACHE.getLatency()
				+ Homestead.MEMBER_CACHE.getLatency()
				+ Homestead.CHUNK_CACHE.getLatency()
				+ Homestead.BAN_CACHE.getLatency()
				+ Homestead.INVITE_CACHE.getLatency()
				+ Homestead.LOG_CACHE.getLatency()
				+ Homestead.RATE_CACHE.getLatency();
	}

	public Driver getProvider() {
		return provider;
	}

	public synchronized void importToCache() throws Exception {
		Homestead.REGION_CACHE.putAll(importRegions());
		Homestead.MEMBER_CACHE.putAll(importRegionMembers());

		List<RegionChunk> chunks = importRegionChunks();
		Homestead.CHUNK_CACHE.putAll(chunks);
		Homestead.REGION_INDEXED_CHUNK_CACHE.putAll(chunks);
		Homestead.POSITION_INDEXED_CHUNK_CACHE.putAll(chunks);

		Homestead.LOG_CACHE.putAll(importRegionLogs());
		Homestead.INVITE_CACHE.putAll(importRegionInvites());
		Homestead.BAN_CACHE.putAll(importRegionBannedPlayers());
		Homestead.RATE_CACHE.putAll(importRegionRates());
		Homestead.SUBAREA_CACHE.putAll(importSubAreas());
		Homestead.WAR_CACHE.putAll(importWars());
		Homestead.LEVEL_CACHE.putAll(importLevels());
	}

	public synchronized void exportFromCache() throws Exception {
		Logger.debug("Started exporting data from cache to the database...");

		exportRegions(Homestead.REGION_CACHE.getAll());
		exportRegionMembers(Homestead.MEMBER_CACHE.getAll());
		exportRegionChunks(Homestead.CHUNK_CACHE.getAll());
		exportRegionLogs(Homestead.LOG_CACHE.getAll());
		exportRegionInvites(Homestead.INVITE_CACHE.getAll());
		exportRegionBannedPlayers(Homestead.BAN_CACHE.getAll());
		exportRegionRates(Homestead.RATE_CACHE.getAll());
		exportSubAreas(Homestead.SUBAREA_CACHE.getAll());
		exportWars(Homestead.WAR_CACHE.getAll());
		exportLevels(Homestead.LEVEL_CACHE.getAll());

		Logger.debug("Done exporting data.");
	}

	public synchronized void closeConnection() throws Exception {
		if (closed) return;

		connectionSource.close();
		closed = true;

		Logger.warning("Connection closed for " + provider + ".");
	}

	public synchronized long getLatency() {
		if (closed) {
			return -1L;
		}

		String warRegions = tablePrefix + "war_regions";
		long start = System.currentTimeMillis();
		try (Connection raw = rawConnection();
			 Statement stmt = raw.createStatement()) {
			for (String table : TABLES) {
				String name = table.equals("war_regions") ? warRegions : tablePrefix + table;
				ResultSet rs = stmt.executeQuery("SELECT 1 FROM " + quote(name) + " LIMIT 1");
				rs.close();
			}
			return System.currentTimeMillis() - start;
		} catch (Exception ignored) {
			return -1L;
		}
	}

	private Set<Long> loadExistingIds(Dao<?, Long> dao) throws SQLException {
		QueryBuilder<?, Long> queryBuilder = dao.queryBuilder();
		queryBuilder.selectRaw(dao.getTableInfo().getIdField().getColumnName());
		Set<Long> ids = new HashSet<>();
		GenericRawResults<String[]> results = queryBuilder.queryRaw();
		try {
			for (String[] row : results) {
				ids.add(Long.parseLong(row[0]));
			}
		} finally {
			try {
				results.close();
			} catch (Exception ignored) {
			}
		}
		return ids;
	}

	private <T> void exportAll(Dao<T, Long> dao, List<T> entities, Set<Long> cacheIds) throws SQLException {
		for (T entity : entities) {
			dao.createOrUpdate(entity);
		}

		Set<Long> dbIds = loadExistingIds(dao);
		dbIds.removeAll(cacheIds);
		if (!dbIds.isEmpty()) {
			dao.deleteIds(dbIds);
		}
	}

	// Regions
	private List<Region> importRegions() throws SQLException {
		List<Region> list = new ArrayList<>();
		for (RegionEntity entity : regionDao.queryForAll()) {
			list.add(fromEntity(entity));
		}
		return list;
	}

	private void exportRegions(List<Region> regions) throws SQLException {
		List<RegionEntity> entities = new ArrayList<>(regions.size());
		Set<Long> cacheIds = new HashSet<>(regions.size());
		for (Region region : regions) {
			entities.add(toEntity(region));
			cacheIds.add(region.getUniqueId());
		}
		exportAll(regionDao, entities, cacheIds);
	}

	private RegionEntity toEntity(Region region) {
		RegionEntity entity = new RegionEntity();
		entity.id = region.getUniqueId();
		entity.name = region.getName();
		entity.displayName = region.getDisplayName();
		entity.description = region.getDescription();
		entity.ownerId = region.getOwnerId().toString();
		entity.location = region.getLocation() == null ? null : region.getLocation().serialize();
		entity.playerFlags = region.getPlayerFlags();
		entity.worldFlags = region.getWorldFlags();
		entity.taxes = region.getTaxes();
		entity.bank = region.getBank();
		entity.mapColor = region.getMapColor();
		entity.mapIcon = region.getMapIcon();
		entity.rent = region.getRent().serialize();
		entity.weather = region.getWeather();
		entity.time = region.getTime();
		entity.welcomeSign = region.getWelcomeSign() == null ? null : region.getWelcomeSign().serialize();
		entity.upkeepAt = region.getUpkeepAt();
		entity.createdAt = region.getCreatedAt();
		return entity;
	}

	private Region fromEntity(RegionEntity entity) {
		Region region = new Region(entity.id, entity.name, UUID.fromString(entity.ownerId), entity.createdAt);
		region.setDisplayName(entity.displayName);
		region.setDescription(entity.description);
		region.setLocation(parseSeLocation(entity.location));
		region.setPlayerFlags(entity.playerFlags);
		region.setWorldFlags(entity.worldFlags);
		region.setTaxes(entity.taxes);
		region.setBank(entity.bank);
		region.setMapColor(entity.mapColor);
		region.setMapIcon(entity.mapIcon);
		region.setRent(parseSeRent(entity.rent));
		region.setWeather(entity.weather);
		region.setTime(entity.time);
		region.setWelcomeSign(parseSeLocation(entity.welcomeSign));
		region.setUpkeepAt(entity.upkeepAt);
		return region;
	}

	// Region members
	private List<RegionMember> importRegionMembers() throws SQLException {
		List<RegionMember> list = new ArrayList<>();
		for (RegionMemberEntity entity : regionMemberDao.queryForAll()) {
			RegionMember.LinkageType type =
					entity.linkageType == RegionMember.LinkageType.REGION.getValue()
							? RegionMember.LinkageType.REGION
							: RegionMember.LinkageType.SUBAREA;
			long linkageId = type == RegionMember.LinkageType.REGION ? entity.regionId : entity.subAreaId;
			list.add(new RegionMember(entity.id, UUID.fromString(entity.playerId), type, linkageId,
					entity.playerFlags, entity.controlFlags, entity.taxesAt, entity.joinedAt));
		}
		return list;
	}

	private void exportRegionMembers(List<RegionMember> members) throws SQLException {
		List<RegionMemberEntity> entities = new ArrayList<>(members.size());
		Set<Long> cacheIds = new HashSet<>(members.size());
		for (RegionMember member : members) {
			RegionMemberEntity entity = new RegionMemberEntity();
			entity.id = member.getUniqueId();
			entity.playerId = member.getPlayerId().toString();
			entity.linkageType = member.getLinkageType().getValue();
			entity.regionId = member.getRegionId();
			entity.subAreaId = member.getSubAreaId();
			entity.playerFlags = member.getPlayerFlags();
			entity.controlFlags = member.getControlFlags();
			entity.joinedAt = member.getJoinedAt();
			entity.taxesAt = member.getTaxesAt();
			entities.add(entity);
			cacheIds.add(member.getUniqueId());
		}
		exportAll(regionMemberDao, entities, cacheIds);
	}

	// Region chunks
	private List<RegionChunk> importRegionChunks() throws SQLException {
		List<RegionChunk> list = new ArrayList<>();
		for (RegionChunkEntity entity : regionChunkDao.queryForAll()) {
			list.add(new RegionChunk(entity.id, entity.regionId, UUID.fromString(entity.worldId),
					entity.x, entity.z, entity.claimedAt, entity.forceLoaded));
		}
		return list;
	}

	private void exportRegionChunks(List<RegionChunk> chunks) throws SQLException {
		List<RegionChunkEntity> entities = new ArrayList<>(chunks.size());
		Set<Long> cacheIds = new HashSet<>(chunks.size());
		for (RegionChunk chunk : chunks) {
			RegionChunkEntity entity = new RegionChunkEntity();
			entity.id = chunk.getUniqueId();
			entity.regionId = chunk.getRegionId();
			entity.worldId = chunk.getWorldId().toString();
			entity.x = chunk.getX();
			entity.z = chunk.getZ();
			entity.claimedAt = chunk.getClaimedAt();
			entity.forceLoaded = chunk.isForceLoaded();
			entities.add(entity);
			cacheIds.add(chunk.getUniqueId());
		}
		exportAll(regionChunkDao, entities, cacheIds);
	}

	// Region logs
	private List<RegionLog> importRegionLogs() throws SQLException {
		List<RegionLog> list = new ArrayList<>();
		for (RegionLogEntity entity : regionLogDao.queryForAll()) {
			list.add(new RegionLog(entity.id, entity.regionId, entity.author,
					entity.message, entity.sentAt, entity.read));
		}
		return list;
	}

	private void exportRegionLogs(List<RegionLog> logs) throws SQLException {
		List<RegionLogEntity> entities = new ArrayList<>(logs.size());
		Set<Long> cacheIds = new HashSet<>(logs.size());
		for (RegionLog log : logs) {
			RegionLogEntity entity = new RegionLogEntity();
			entity.id = log.getUniqueId();
			entity.regionId = log.getRegionId();
			entity.author = log.getAuthor();
			entity.message = log.getMessage();
			entity.sentAt = log.getSentAt();
			entity.read = log.isRead();
			entities.add(entity);
			cacheIds.add(log.getUniqueId());
		}
		exportAll(regionLogDao, entities, cacheIds);
	}

	// Region rates
	private List<RegionRate> importRegionRates() throws SQLException {
		List<RegionRate> list = new ArrayList<>();
		for (RegionRateEntity entity : regionRateDao.queryForAll()) {
			list.add(new RegionRate(entity.id, entity.regionId, UUID.fromString(entity.playerId),
					entity.rate, entity.ratedAt));
		}
		return list;
	}

	private void exportRegionRates(List<RegionRate> rates) throws SQLException {
		List<RegionRateEntity> entities = new ArrayList<>(rates.size());
		Set<Long> cacheIds = new HashSet<>(rates.size());
		for (RegionRate rate : rates) {
			RegionRateEntity entity = new RegionRateEntity();
			entity.id = rate.getUniqueId();
			entity.regionId = rate.getRegionId();
			entity.playerId = rate.getPlayerId().toString();
			entity.rate = rate.getRate();
			entity.ratedAt = rate.getRatedAt();
			entities.add(entity);
			cacheIds.add(rate.getUniqueId());
		}
		exportAll(regionRateDao, entities, cacheIds);
	}

	// Region invites
	private List<RegionInvite> importRegionInvites() throws SQLException {
		List<RegionInvite> list = new ArrayList<>();
		for (RegionInviteEntity entity : regionInviteDao.queryForAll()) {
			list.add(new RegionInvite(entity.id, entity.regionId, UUID.fromString(entity.playerId),
					entity.invitedAt));
		}
		return list;
	}

	private void exportRegionInvites(List<RegionInvite> invites) throws SQLException {
		List<RegionInviteEntity> entities = new ArrayList<>(invites.size());
		Set<Long> cacheIds = new HashSet<>(invites.size());
		for (RegionInvite invite : invites) {
			RegionInviteEntity entity = new RegionInviteEntity();
			entity.id = invite.getUniqueId();
			entity.regionId = invite.getRegionId();
			entity.playerId = invite.getPlayerId().toString();
			entity.invitedAt = invite.getInvitedAt();
			entities.add(entity);
			cacheIds.add(invite.getUniqueId());
		}
		exportAll(regionInviteDao, entities, cacheIds);
	}

	// Region banned players
	private List<RegionBan> importRegionBannedPlayers() throws SQLException {
		List<RegionBan> list = new ArrayList<>();
		for (RegionBanEntity entity : regionBanDao.queryForAll()) {
			list.add(new RegionBan(entity.id, entity.regionId, UUID.fromString(entity.playerId),
					entity.reason, entity.bannedAt));
		}
		return list;
	}

	private void exportRegionBannedPlayers(List<RegionBan> bannedPlayers) throws SQLException {
		List<RegionBanEntity> entities = new ArrayList<>(bannedPlayers.size());
		Set<Long> cacheIds = new HashSet<>(bannedPlayers.size());
		for (RegionBan ban : bannedPlayers) {
			RegionBanEntity entity = new RegionBanEntity();
			entity.id = ban.getUniqueId();
			entity.regionId = ban.getRegionId();
			entity.playerId = ban.getPlayerId().toString();
			entity.reason = ban.getReason();
			entity.bannedAt = ban.getBannedAt();
			entities.add(entity);
			cacheIds.add(ban.getUniqueId());
		}
		exportAll(regionBanDao, entities, cacheIds);
	}

	// Sub-areas
	private List<SubArea> importSubAreas() throws SQLException {
		List<SubArea> list = new ArrayList<>();
		for (SubAreaEntity entity : subAreaDao.queryForAll()) {
			SeBlock point1 = SeBlock.deserialize(entity.point1);
			SeBlock point2 = SeBlock.deserialize(entity.point2);
			if (point1 == null || point2 == null) {
				continue;
			}
			list.add(new SubArea(entity.id, entity.regionId, entity.name,
					UUID.fromString(entity.worldId), point1, point2,
					entity.playerFlags, parseSeRent(entity.rent), entity.createdAt));
		}
		return list;
	}

	private void exportSubAreas(List<SubArea> subAreas) throws SQLException {
		List<SubAreaEntity> entities = new ArrayList<>(subAreas.size());
		Set<Long> cacheIds = new HashSet<>(subAreas.size());
		for (SubArea subArea : subAreas) {
			SubAreaEntity entity = new SubAreaEntity();
			entity.id = subArea.getUniqueId();
			entity.regionId = subArea.getRegionId();
			entity.name = subArea.getName();
			entity.worldId = subArea.getWorldId().toString();
			entity.point1 = subArea.getPoint1().serialize();
			entity.point2 = subArea.getPoint2().serialize();
			entity.playerFlags = subArea.getPlayerFlags();
			entity.rent = subArea.getRent().serialize();
			entity.createdAt = subArea.getCreatedAt();
			entities.add(entity);
			cacheIds.add(subArea.getUniqueId());
		}
		exportAll(subAreaDao, entities, cacheIds);
	}

	// Levels
	private List<Level> importLevels() throws SQLException {
		List<Level> list = new ArrayList<>();
		for (LevelEntity entity : levelDao.queryForAll()) {
			list.add(new Level(entity.id, entity.regionId, entity.level,
					entity.experience, entity.totalExperience, entity.createdAt));
		}
		return list;
	}

	private void exportLevels(List<Level> levels) throws SQLException {
		List<LevelEntity> entities = new ArrayList<>(levels.size());
		Set<Long> cacheIds = new HashSet<>(levels.size());
		for (Level level : levels) {
			LevelEntity entity = new LevelEntity();
			entity.id = level.getUniqueId();
			entity.regionId = level.getRegionId();
			entity.level = level.getLevel();
			entity.experience = level.getExperience();
			entity.totalExperience = level.getTotalExperience();
			entity.createdAt = level.getCreatedAt();
			entities.add(entity);
			cacheIds.add(level.getUniqueId());
		}
		exportAll(levelDao, entities, cacheIds);
	}

	// Wars
	private List<War> importWars() throws SQLException {
		Map<Long, List<Long>> warRegions = new java.util.HashMap<>();
		String warRegionsTable = tablePrefix + "war_regions";
		try (Connection raw = rawConnection();
			 Statement stmt = raw.createStatement();
			 ResultSet rs = stmt.executeQuery("SELECT " + quote("warId") + ", " + quote("regionId")
					 + " FROM " + quote(warRegionsTable))) {
			while (rs.next()) {
				warRegions.computeIfAbsent(rs.getLong("warId"), k -> new ArrayList<>())
						.add(rs.getLong("regionId"));
			}
		}

		List<War> list = new ArrayList<>();
		for (WarEntity entity : warDao.queryForAll()) {
			list.add(new War(entity.id, entity.name, entity.displayName, entity.description,
					warRegions.getOrDefault(entity.id, new ArrayList<>()),
					entity.prize, entity.startedAt));
		}
		return list;
	}

	private void exportWars(List<War> wars) throws Exception {
		List<WarEntity> entities = new ArrayList<>(wars.size());
		Set<Long> cacheIds = new HashSet<>(wars.size());
		for (War war : wars) {
			WarEntity entity = new WarEntity();
			entity.id = war.getUniqueId();
			entity.name = war.getName();
			entity.displayName = war.getDisplayName();
			entity.description = war.getDescription();
			entity.prize = war.getPrize();
			entity.startedAt = war.getStartedAt();
			entities.add(entity);
			cacheIds.add(war.getUniqueId());
		}

		Set<Long> dbWarIds = loadExistingIds(warDao);
		Set<Long> staleWarIds = new HashSet<>(dbWarIds);
		staleWarIds.removeAll(cacheIds);

		String warRegionsTable = tablePrefix + "war_regions";
		try (Connection raw = rawConnection()) {
			raw.setAutoCommit(false);
			try (PreparedStatement delete = raw.prepareStatement(
					"DELETE FROM " + quote(warRegionsTable) + " WHERE " + quote("warId") + "=?");
				 PreparedStatement insert = raw.prepareStatement(
						 "INSERT INTO " + quote(warRegionsTable) + " (" + quote("warId") + ", " + quote("regionId")
								 + ") VALUES (?,?)")) {
				for (War war : wars) {
					delete.setLong(1, war.getUniqueId());
					delete.addBatch();
					for (long regionId : war.getRegionIds()) {
						insert.setLong(1, war.getUniqueId());
						insert.setLong(2, regionId);
						insert.addBatch();
					}
				}
				for (long warId : staleWarIds) {
					delete.setLong(1, warId);
					delete.addBatch();
				}
				delete.executeBatch();
				insert.executeBatch();
				raw.commit();
			} catch (SQLException e) {
				raw.rollback();
				throw e;
			} finally {
				raw.setAutoCommit(true);
			}
		}

		exportAll(warDao, entities, cacheIds);
	}

	// Serialization helpers
	private static SeLocation parseSeLocation(String value) {
		return value == null || value.isBlank() ? null : SeLocation.deserialize(value);
	}

	private static SeRent parseSeRent(String value) {
		return value == null || value.isBlank() ? null : SeRent.deserialize(value);
	}
}