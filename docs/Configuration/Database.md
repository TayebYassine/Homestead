# Database

A database is a way to store data for all regions on a server. Homestead provides a built-in caching system to improve the performance and speed for protecting and managing all regions.

## Providers

| Provider         |   Implemented?   | Notes                                            |
|:-----------------|:----------------:|:-------------------------------------------------|
| PostgreSQL       | :material-check: | For big servers (~1,000 players daily)           |
| MariaDB          | :material-check: | For big servers (~1,000 players daily)           |
| MySQL            | :material-check: | For big servers (~1,000 players daily)           |
| MongoDB          |   Upcoming...    | For medium servers (~500 players daily)          |
| SQLite (Default) | :material-check: | For small servers (~100 players daily)           |
| YAML             | :material-check: | For small and friends-only servers (~50 players) |

## Setup

By default, when you install Homestead, the selected provider is **SQLite**.

If you have never let other players use Homestead, or you used it for testing before public joining, and you want to change the database provider, change the current provider name to the provider you want to use. These are the valid provider names:

- PostgreSQL: `postgresql`
- MariaDB: `mariadb`
- MySQL: `mysql`
- SQLite: `sqlite`
- YAML: `yaml`

```yaml
database:
  provider: "sqlite"

  # PostgreSQL configuration
  postgresql:
    host: "localhost"
    port: 3306
    username: "USERNAME"
    password: "PASSWORD"
    database: "homestead_data"
    table_prefix: ""
    
  # MariaDB configuration
  mariadb:
    ...

  # MySQL configuration
  mysql:
    ...

  # SQLite configuration
  sqlite: "homestead_data.db"
```

## Migrating to another provider

1. Prepare your new provider's details (username, password...) in the **config.yml** file.
2. Save the changes and reload the configuration using the command: `/hsadmin reload`
3. Migrate the current data to the new provider using the command: `/hsadmin export [provider]`
4. Stop your server.
5. Change the provider to the provider you migrated to earlier in **config.yml**.
6. Start the server. You're good to go!
