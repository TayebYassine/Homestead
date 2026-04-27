package tfagaming.projects.minecraft.homestead.database;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.database.providers.*;
import tfagaming.projects.minecraft.homestead.logs.Logger;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.ConfigFile;

import java.io.IOException;
import java.sql.SQLException;

public final class Database {
	private final Driver provider;
	private Provider instance;

	public Database(Driver provider) throws ClassNotFoundException, SQLException, IOException {
		this.provider = provider;

		this.instance = switch (provider) {
			case POSTGRESQL ->
					new PostgreSQL(Resources.<ConfigFile>get(ResourceType.Config).getString("database.postgresql.username"),
							Resources.<ConfigFile>get(ResourceType.Config).getString("database.postgresql.password"),
							Resources.<ConfigFile>get(ResourceType.Config).getString("database.postgresql.host"),
							Resources.<ConfigFile>get(ResourceType.Config).getInt("database.postgresql.port"),
							Resources.<ConfigFile>get(ResourceType.Config).getString("database.postgresql.database"),
							Resources.<ConfigFile>get(ResourceType.Config).getString("database.postgresql.table_prefix"),
							Resources.<ConfigFile>get(ResourceType.Config).getString("database.postgresql.jdbc_url_parameters"));
			case MARIADB ->
					new MariaDB(Resources.<ConfigFile>get(ResourceType.Config).getString("database.mariadb.username"),
							Resources.<ConfigFile>get(ResourceType.Config).getString("database.mariadb.password"),
							Resources.<ConfigFile>get(ResourceType.Config).getString("database.mariadb.host"),
							Resources.<ConfigFile>get(ResourceType.Config).getInt("database.mariadb.port"),
							Resources.<ConfigFile>get(ResourceType.Config).getString("database.mariadb.database"),
							Resources.<ConfigFile>get(ResourceType.Config).getString("database.mariadb.table_prefix"),
							Resources.<ConfigFile>get(ResourceType.Config).getString("database.mariadb.jdbc_url_parameters"));
			case MYSQL -> new MySQL(Resources.<ConfigFile>get(ResourceType.Config).getString("database.mysql.username"),
					Resources.<ConfigFile>get(ResourceType.Config).getString("database.mysql.password"),
					Resources.<ConfigFile>get(ResourceType.Config).getString("database.mysql.host"),
					Resources.<ConfigFile>get(ResourceType.Config).getInt("database.mysql.port"),
					Resources.<ConfigFile>get(ResourceType.Config).getString("database.mysql.database"),
					Resources.<ConfigFile>get(ResourceType.Config).getString("database.mysql.table_prefix"),
					Resources.<ConfigFile>get(ResourceType.Config).getString("database.mysql.jdbc_url_parameters"));
			case MONGODB -> new MongoDB(
					Resources.<ConfigFile>get(ResourceType.Config).getString("database.mongodb.uri"),
					Resources.<ConfigFile>get(ResourceType.Config).getString("database.mongodb.database"),
					Resources.<ConfigFile>get(ResourceType.Config).getString("database.mongodb.collection_prefix"));
			case SQLITE -> new SQLite(Resources.<ConfigFile>get(ResourceType.Config).getString("database.sqlite"));
			case YAML -> new YAML(Homestead.getInstance().getDataFolder());
		};
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

	public <T extends Provider> T getInstance() {
		if (instance == null) {
			return null;
		}

		return (T) instance;
	}

	public void importToCache() throws Exception {
		if (instance == null) {
			throw new IllegalStateException("Instance is null");
		}

		Homestead.REGION_CACHE.putAll(instance.importRegions());
		Homestead.MEMBER_CACHE.putAll(instance.importRegionMembers());
		Homestead.CHUNK_CACHE.putAll(instance.importRegionChunks());
		Homestead.LOG_CACHE.putAll(instance.importRegionLogs());
		Homestead.INVITE_CACHE.putAll(instance.importRegionInvites());
		Homestead.BAN_CACHE.putAll(instance.importRegionBannedPlayers());
		Homestead.RATE_CACHE.putAll(instance.importRegionRates());
		Homestead.SUBAREA_CACHE.putAll(instance.importSubAreas());
		Homestead.WAR_CACHE.putAll(instance.importWars());
		Homestead.LEVEL_CACHE.putAll(instance.importLevels());
	}

	public void exportFromCache() throws Exception {
		if (instance == null) {
			throw new IllegalStateException("Instance is null");
		}

		instance.exportRegions(Homestead.REGION_CACHE.getAll());
		instance.exportRegionMembers(Homestead.MEMBER_CACHE.getAll());
		instance.exportRegionChunks(Homestead.CHUNK_CACHE.getAll());
		instance.exportRegionLogs(Homestead.LOG_CACHE.getAll());
		instance.exportRegionInvites(Homestead.INVITE_CACHE.getAll());
		instance.exportRegionBannedPlayers(Homestead.BAN_CACHE.getAll());
		instance.exportRegionRates(Homestead.RATE_CACHE.getAll());
		instance.exportSubAreas(Homestead.SUBAREA_CACHE.getAll());
		instance.exportWars(Homestead.WAR_CACHE.getAll());
		instance.exportLevels(Homestead.LEVEL_CACHE.getAll());
	}

	public void closeConnection() throws Exception {
		this.instance.closeConnection();
		this.instance = null;

		Logger.warning("Connection closed for " + provider.toString() + ".");
	}

	public long getLatency() {
		if (instance == null) {
			throw new IllegalStateException("Instance is null");
		}

		return this.instance.getLatency();
	}
}