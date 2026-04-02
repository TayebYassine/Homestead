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
	private final Provider provider;
	private PostgreSQL postgreSQL;
	private MariaDB mariaDB;
	private MySQL mySQL;
	private SQLite sqLite;
	private YAML yaml;
	private MongoDB mongoDB;

	public Database(Provider provider) throws ClassNotFoundException, SQLException, IOException {
		this.provider = provider;

		Logger.warning("Attempting to connect to database... Provider: " + provider.toString());

		switch (provider) {
			case PostgreSQL:
				postgreSQL = new PostgreSQL(Resources.<ConfigFile>get(ResourceType.Config).getString("database.postgresql.username"),
						Resources.<ConfigFile>get(ResourceType.Config).getString("database.postgresql.password"),
						Resources.<ConfigFile>get(ResourceType.Config).getString("database.postgresql.host"),
						Resources.<ConfigFile>get(ResourceType.Config).getInt("database.postgresql.port"),
						Resources.<ConfigFile>get(ResourceType.Config).getString("database.postgresql.database"),
						Resources.<ConfigFile>get(ResourceType.Config).getString("database.postgresql.table_prefix"));
				break;
			case MariaDB:
				mariaDB = new MariaDB(Resources.<ConfigFile>get(ResourceType.Config).getString("database.mariadb.username"),
						Resources.<ConfigFile>get(ResourceType.Config).getString("database.mariadb.password"),
						Resources.<ConfigFile>get(ResourceType.Config).getString("database.mariadb.host"),
						Resources.<ConfigFile>get(ResourceType.Config).getInt("database.mariadb.port"),
						Resources.<ConfigFile>get(ResourceType.Config).getString("database.mariadb.database"),
						Resources.<ConfigFile>get(ResourceType.Config).getString("database.mariadb.table_prefix"));
				break;
			case MySQL:
				mySQL = new MySQL(Resources.<ConfigFile>get(ResourceType.Config).getString("database.mysql.username"),
						Resources.<ConfigFile>get(ResourceType.Config).getString("database.mysql.password"),
						Resources.<ConfigFile>get(ResourceType.Config).getString("database.mysql.host"),
						Resources.<ConfigFile>get(ResourceType.Config).getInt("database.mysql.port"),
						Resources.<ConfigFile>get(ResourceType.Config).getString("database.mysql.database"),
						Resources.<ConfigFile>get(ResourceType.Config).getString("database.mysql.table_prefix"));
				break;
			case MongoDB:
				mongoDB = new MongoDB(
						Resources.<ConfigFile>get(ResourceType.Config).getString("database.mongodb.uri"),
						Resources.<ConfigFile>get(ResourceType.Config).getString("database.mongodb.database"),
						Resources.<ConfigFile>get(ResourceType.Config).getString("database.mongodb.collection_prefix"));
				break;
			case SQLite:
				sqLite = new SQLite(Resources.<ConfigFile>get(ResourceType.Config).getString("database.sqlite"));
				break;
			case YAML:
				yaml = new YAML(Homestead.getInstance().getDataFolder());
				break;
			default:
				break;
		}
	}

	public static Provider parseProviderFromString(String provider) {
		return switch (provider.toLowerCase()) {
			case "postgresql" -> Provider.PostgreSQL;
			case "mariadb" -> Provider.MariaDB;
			case "mysql" -> Provider.MySQL;
			case "sqlite" -> Provider.SQLite;
			case "yaml" -> Provider.YAML;
			case "mongodb" -> Provider.MongoDB;
			default -> null;
		};
	}

	public String getSelectedProvider() {
		return provider.toString();
	}

	public void importRegions() {
		switch (provider) {
			case PostgreSQL:
				postgreSQL.importRegions();
				break;
			case MariaDB:
				mariaDB.importRegions();
				break;
			case MySQL:
				mySQL.importRegions();
				break;
			case SQLite:
				sqLite.importRegions();
				break;
			case YAML:
				yaml.importRegions();
				break;
			case MongoDB:
				mongoDB.importRegions();
				break;
			default:
				break;
		}
	}

	public void importWars() {
		switch (provider) {
			case PostgreSQL:
				postgreSQL.importWars();
				break;
			case MariaDB:
				mariaDB.importWars();
				break;
			case MySQL:
				mySQL.importWars();
				break;
			case SQLite:
				sqLite.importWars();
				break;
			case YAML:
				yaml.importWars();
				break;
			case MongoDB:
				mongoDB.importWars();
				break;
			default:
				break;
		}
	}

	public void importSubAreas() {
		switch (provider) {
			case PostgreSQL:
				postgreSQL.importSubAreas();
				break;
			case MariaDB:
				mariaDB.importSubAreas();
				break;
			case MySQL:
				mySQL.importSubAreas();
				break;
			case SQLite:
				sqLite.importSubAreas();
				break;
			case YAML:
				yaml.importSubAreas();
				break;
			case MongoDB:
				mongoDB.importSubAreas();
				break;
			default:
				break;
		}
	}

	public void importLevels() {
		switch (provider) {
			case PostgreSQL:
				postgreSQL.importLevels();
				break;
			case MariaDB:
				mariaDB.importLevels();
				break;
			case MySQL:
				mySQL.importLevels();
				break;
			case SQLite:
				sqLite.importLevels();
				break;
			case YAML:
				yaml.importLevels();
				break;
			case MongoDB:
				mongoDB.importLevels();
				break;
			default:
				break;
		}
	}

	public void exportRegions() {
		switch (provider) {
			case PostgreSQL:
				postgreSQL.exportRegions();
				break;
			case MariaDB:
				mariaDB.exportRegions();
				break;
			case MySQL:
				mySQL.exportRegions();
				break;
			case SQLite:
				sqLite.exportRegions();
				break;
			case YAML:
				yaml.exportRegions();
				break;
			case MongoDB:
				mongoDB.exportRegions();
				break;
			default:
				break;
		}
	}

	public void exportWars() {
		switch (provider) {
			case PostgreSQL:
				postgreSQL.exportWars();
				break;
			case MariaDB:
				mariaDB.exportWars();
				break;
			case MySQL:
				mySQL.exportWars();
				break;
			case SQLite:
				sqLite.exportWars();
				break;
			case YAML:
				yaml.exportWars();
				break;
			case MongoDB:
				mongoDB.exportWars();
				break;
			default:
				break;
		}
	}

	public void exportSubAreas() {
		switch (provider) {
			case PostgreSQL:
				postgreSQL.exportSubAreas();
				break;
			case MariaDB:
				mariaDB.exportSubAreas();
				break;
			case MySQL:
				mySQL.exportSubAreas();
				break;
			case SQLite:
				sqLite.exportSubAreas();
				break;
			case YAML:
				yaml.exportSubAreas();
				break;
			case MongoDB:
				mongoDB.exportSubAreas();
				break;
			default:
				break;
		}
	}

	public void exportLevels() {
		switch (provider) {
			case PostgreSQL:
				postgreSQL.exportLevels();
				break;
			case MariaDB:
				mariaDB.exportLevels();
				break;
			case MySQL:
				mySQL.exportLevels();
				break;
			case SQLite:
				sqLite.exportLevels();
				break;
			case YAML:
				yaml.exportLevels();
				break;
			case MongoDB:
				mongoDB.exportLevels();
				break;
			default:
				break;
		}
	}

	public void closeConnection() {
		switch (provider) {
			case PostgreSQL:
				postgreSQL.closeConnection();
				break;
			case MariaDB:
				mariaDB.closeConnection();
				break;
			case MySQL:
				mySQL.closeConnection();
				break;
			case SQLite:
				sqLite.closeConnection();
				break;
			case YAML:
				yaml.closeConnection();
				break;
			case MongoDB:
				mongoDB.closeConnection();
				break;
			default:
				break;
		}
	}

	public long getLatency() {
		return switch (provider) {
			case PostgreSQL -> postgreSQL.getLatency();
			case MariaDB -> mariaDB.getLatency();
			case MySQL -> mySQL.getLatency();
			case SQLite -> sqLite.getLatency();
			case YAML -> yaml.getLatency();
			case MongoDB -> mongoDB.getLatency();
		};
	}

	public enum Provider {
		PostgreSQL,
		MariaDB,
		MySQL,
		SQLite,
		YAML,
		MongoDB
	}
}