package me.tayebyassine.homestead.database;

public enum Driver {
	POSTGRESQL("PostgreSQL"),
	MARIADB("MariaDB"),
	MYSQL("MySQL"),
	SQLITE("SQLite"),
	;

	private final String name;

	Driver(String name) {
		this.name = name;
	}

	public static Driver parse(String provider) {
		return switch (provider.toLowerCase()) {
			case "postgresql" -> POSTGRESQL;
			case "mariadb" -> MARIADB;
			case "mysql" -> MYSQL;
			case "sqlite" -> SQLITE;
			default -> null;
		};
	}

	@Override
	public String toString() {
		return this.name;
	}
}
