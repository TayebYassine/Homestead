# Setup Database

A database stores all region data for your server. Homestead includes a built-in caching system that improves performance when protecting and managing regions across your server.

## Supported Drivers

Choose the database provider that best fits your server size and technical setup:

| Provider         | Available?       | Recommended For                               |
|:-----------------|:-----------------|:----------------------------------------------|
| PostgreSQL       | :material-check: | Large servers (~1,000+ daily players)         |
| MariaDB          | :material-check: | Large servers (~1,000+ daily players)         |
| MySQL            | :material-check: | Large servers (~1,000+ daily players)         |
| MongoDB          | :material-check: | Medium servers (~500 daily players)           |
| SQLite (Default) | :material-check: | Small servers (~100 daily players)            |
| YAML             | :material-check: | Small/private servers (~50 players)           |

Change the selected provider in **config.yml**:

```yaml
database:
  # Provider: sqlite (default), mysql, mariadb, postgresql, mongodb, yaml
  # sqlite: Good for small-medium servers, no setup required
  # mysql/mariadb/postgresql: Good for large servers (100+ players)
  # mongodb: Good for large servers (requires subscription for tiers)
  # yaml: Human-readable, slowest (not recommended for production)
  provider: "sqlite"
```

## Which Provider Should I Use?

- **SQLite**: Best for most servers. It's the default option and requires no setup. Perfect if you're just starting out or running a small community server.
- **MySQL, MariaDB, PostgreSQL**: Choose one of these if you run a large server with many players. They offer better performance under heavy load and support advanced features.
- **YAML**: Simple file-based storage. Only recommended for very small friend servers or testing environments.

## How to Set up

### SQLite

This is the default provider selected when installing Homestead for the first time.

There is no complex configuration, you can change the file name in **config.yml**:

```yaml
database:
  # SQLite filename (only used if provider = "sqlite")
  sqlite: "homestead_data.db"
```

All SQLite database files are saved within Minecraft's server directory, not in the plugin's folder.

### MySQL, MariaDB, and PostgresSQL

To connect to a MySQL, MariaDB, or Postgres database, you must have the following details:
- `host`: The host address
- `port`: The port number
- `username`: The credentials, username
- `password`: The credentials, password
- `database`: The schema name

Other configuration if needed:
- `table_prefix`: The prefix for each table. If you want to have prefix for each table, don't forget to add an underscore (`_`) in the end, example prefix: `myserver_`.
- `jdbc_url_parameters`: URL parameters, such as **useSSL**, **allowPublicKeyRetrieval**... etc

```yaml

database:
  # PostgreSQL settings (only used if provider = "postgresql")
  postgresql:
    host: "localhost"
    port: 5432
    username: "USERNAME"
    password: "PASSWORD"
    database: "homestead_data"
    table_prefix: ""
    jdbc_url_parameters: ""

  # MariaDB settings (only used if provider = "mariadb")
  mariadb:
    host: "localhost"
    port: 3306
    username: "USERNAME"
    password: "PASSWORD"
    database: "homestead_data"
    table_prefix: ""
    jdbc_url_parameters: "?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC"

  # MySQL settings (only used if provider = "mysql")
  mysql:
    host: "localhost"
    port: 3306
    username: "USERNAME"
    password: "PASSWORD"
    database: "homestead_data"
    table_prefix: ""
    jdbc_url_parameters: ""
```

### MongoDB

MongoDB is an excellent provider, but it has many limitations for free tier users.

```yaml
database:
  # MongoDB settings (only used if provider = "mongodb")
  mongodb:
    uri: "mongodb://localhost:27017"  # Or Atlas SRV URI
    database: "homestead"
    collection_prefix: "hs_"
```

### YAML

YAML is not a database, but it exports readable data, and it's easy to edit. In general, we do not recommend
any person to edit any data to avoid corruption unless its urgent.

