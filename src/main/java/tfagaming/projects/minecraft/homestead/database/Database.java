package tfagaming.projects.minecraft.homestead.database;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.database.providers.*;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.ConfigFile;
import tfagaming.projects.minecraft.homestead.structure.Level;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.SubArea;
import tfagaming.projects.minecraft.homestead.structure.War;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public final class Database {
	private final Driver provider;
	private Provider instance;

	public Database(Driver provider) throws ClassNotFoundException, SQLException, IOException {
		this.provider = provider;

		this.instance = switch (provider) {
			case POSTGRESQL -> new PostgreSQL(Resources.<ConfigFile>get(ResourceType.Config).getString("database.postgresql.username"),
						Resources.<ConfigFile>get(ResourceType.Config).getString("database.postgresql.password"),
						Resources.<ConfigFile>get(ResourceType.Config).getString("database.postgresql.host"),
						Resources.<ConfigFile>get(ResourceType.Config).getInt("database.postgresql.port"),
						Resources.<ConfigFile>get(ResourceType.Config).getString("database.postgresql.database"),
						Resources.<ConfigFile>get(ResourceType.Config).getString("database.postgresql.table_prefix"),
						Resources.<ConfigFile>get(ResourceType.Config).getString("database.mysql.jdbc_url_parameters"));
			case MARIADB -> new MariaDB(Resources.<ConfigFile>get(ResourceType.Config).getString("database.mariadb.username"),
						Resources.<ConfigFile>get(ResourceType.Config).getString("database.mariadb.password"),
						Resources.<ConfigFile>get(ResourceType.Config).getString("database.mariadb.host"),
						Resources.<ConfigFile>get(ResourceType.Config).getInt("database.mariadb.port"),
						Resources.<ConfigFile>get(ResourceType.Config).getString("database.mariadb.database"),
						Resources.<ConfigFile>get(ResourceType.Config).getString("database.mariadb.table_prefix"),
						Resources.<ConfigFile>get(ResourceType.Config).getString("database.mysql.jdbc_url_parameters"));
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

	public Driver getProvider() { return provider; }

	public void importToCache() throws Exception {
		if (instance == null) {
			throw new IllegalStateException("instance is null");
		}

		List<Region> regions = instance.importRegions();
		List<SubArea> subAreas = instance.importSubAreas();
		List<War> wars = instance.importWars();
		List<Level> levels = instance.importLevels();

		Homestead.regionsCache.putAll(regions);
		Homestead.subAreasCache.putAll(subAreas);
		Homestead.warsCache.putAll(wars);
		Homestead.levelsCache.putAll(levels);
	}

	public void exportFromCache() throws Exception {
		if (instance == null) {
			throw new IllegalStateException("instance is null");
		}

		List<Region> regions = Homestead.regionsCache.getAll();
		List<SubArea> subAreas = Homestead.subAreasCache.getAll();
		List<War> wars = Homestead.warsCache.getAll();
		List<Level> levels = Homestead.levelsCache.getAll();

		instance.exportRegions(regions);
		instance.exportSubAreas(subAreas);
		instance.exportWars(wars);
		instance.exportLevels(levels);
	}

	public void closeConnection() throws Exception {
		this.instance.closeConnection();
		this.instance = null;
	}

	public long getLatency() {
		if (instance == null) {
			throw new IllegalStateException("instance is null");
		}

		return this.instance.getLatency();
	}

	public static long getCacheLatency() {
		return Homestead.regionsCache.getLatency() + Homestead.subAreasCache.getLatency() +  Homestead.warsCache.getLatency() +  Homestead.levelsCache.getLatency();
	}
}