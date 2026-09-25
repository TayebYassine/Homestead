# Database

Every region Homestead knows about (claims, members, bans, logs, and settings) is stored in a database. Homestead also keeps a built-in cache in memory so region lookups are fast and the disk is not hit for every command. Two things therefore matter for performance and reliability: which provider you use, and how often the cache is flushed.

## Supported Providers

- **SQLite** (default): Stores everything in a single file on disk. No setup required, and recommended for small to medium servers.
- **MySQL**: A dedicated database server. Recommended for large servers (roughly 100+ players) or setups that already run MySQL.
- **MariaDB**: A MySQL-compatible alternative with the same configuration format, also recommended for large servers.
- **PostgreSQL**: Another dedicated option for large servers already invested in the PostgreSQL ecosystem.

## Configuration

The active provider is chosen in `config.yml`:

```yaml
database:
  provider: "sqlite"  # sqlite, mysql, mariadb, postgresql
```

### SQLite

SQLite is the default and needs nothing beyond a filename. The database file is written to the server's main directory rather than the plugin data folder, which makes backups as simple as copying one file.

```yaml
database:
  provider: "sqlite"
  sqlite: "homestead_data.db"
```

### MySQL / MariaDB / PostgreSQL

These providers suit advanced setups such as large SMPs, where many players generate constant writes or where you want the database hosted separately from the game server. Only the connection block matching `provider` is read, so you can prefill the others without effect.

```yaml
database:
  provider: "mysql"  # or "mariadb", "postgresql"
  mysql:
    host: "localhost"
    port: 3306
    username: "USERNAME"
    password: "PASSWORD"
    database: "homestead_data"
    table_prefix: ""           # e.g. "myserver_"
    jdbc_url_parameters: ""    # e.g. "?useSSL=false&serverTimezone=UTC"
```

- **`table_prefix`** — Prepended to every Homestead table name. Useful when several plugins share one database.
- **`jdbc_url_parameters`** — Extra JDBC connection parameters appended to the connection URL. The value must start with `?`.

!!! warning "Keep Credentials Private"

    The `password` field holds a real secret. Restrict access to `config.yml` and never commit it to a public repository.

## Cache System

Homestead holds all data in memory and writes changes back to the database on a timer. The timer is controlled by `cache-interval`:

```yaml
cache-interval: 30  # Seconds between cache flushes
```

- **Lower values** flush more often, which reduces the amount of data a hard crash can lose but costs more CPU.
- **Higher values** improve performance at the cost of a slightly larger window of unsaved progress.

If your server has a large player base (or you expect it to grow), raise the interval once to a value that matches your population and leave it there; repeatedly changing it provides no benefit.

Recommended intervals by player count:

- **Fewer than 20 players**: 30 seconds
- **20–50 players**: 60 seconds
- **50–100 players**: 120–180 seconds
- **100+ players**: 300 seconds

!!! info "Switching Providers"

    To move your data from one provider to another without losing anything, follow the export and switch procedure in [Database Migration](Database%20Migration.md).

