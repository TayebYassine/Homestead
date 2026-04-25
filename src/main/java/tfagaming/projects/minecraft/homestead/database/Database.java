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
		return Homestead.regionsCache.getLatency() + Homestead.subAreasCache.getLatency() + Homestead.warsCache.getLatency() + Homestead.levelsCache.getLatency();
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

		Homestead.regionsCache.putAll(instance.importRegions());
		Homestead.regionMemberCache.putAll(instance.importRegionMembers());
		Homestead.regionChunkCache.putAll(instance.importRegionChunks());
		Homestead.regionLogCache.putAll(instance.importRegionLogs());
		Homestead.regionInviteCache.putAll(instance.importRegionInvites());
		Homestead.regionBanCache.putAll(instance.importRegionBannedPlayers());
		Homestead.regionRateCache.putAll(instance.importRegionRates());
		Homestead.subAreasCache.putAll(instance.importSubAreas());
		Homestead.warsCache.putAll(instance.importWars());
		Homestead.levelsCache.putAll(instance.importLevels());
	}

	public void exportFromCache() throws Exception {
		if (instance == null) {
			throw new IllegalStateException("Instance is null");
		}

		instance.exportRegions(Homestead.regionsCache.getAll());
		instance.exportRegionMembers(Homestead.regionMemberCache.getAll());
		instance.exportRegionChunks(Homestead.regionChunkCache.getAll());
		instance.exportRegionLogs(Homestead.regionLogCache.getAll());
		instance.exportRegionInvites(Homestead.regionInviteCache.getAll());
		instance.exportRegionBannedPlayers(Homestead.regionBanCache.getAll());
		instance.exportRegionRates(Homestead.regionRateCache.getAll());
		instance.exportSubAreas(Homestead.subAreasCache.getAll());
		instance.exportWars(Homestead.warsCache.getAll());
		instance.exportLevels(Homestead.levelsCache.getAll());
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