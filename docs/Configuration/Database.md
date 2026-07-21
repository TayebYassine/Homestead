# Database

A database stores all region data — claims, members, bans, logs, and settings. Homestead includes a built-in caching system that improves performance for region lookups.

## Supported Providers

| Provider | Recommended For |
|:---------|:----------------|
| **SQLite** (default) | Small servers (~100 players), no setup required |
| **MySQL** | Large servers (~1,000+ players) |
| **MariaDB** | Large servers (~1,000+ players) |
| **PostgreSQL** | Large servers (~1,000+ players) |
| **MongoDB** | Medium servers (~500 players) |
| **YAML** | Small/private servers (~50 players), human-readable files |

## Configuration

Set the provider in `config.yml`:

```yaml
database:
  provider: "sqlite"  # sqlite, mysql, mariadb, postgresql, mongodb, yaml
```

### SQLite

The default — no setup needed. Database file is saved to the server directory.

```yaml
database:
  provider: "sqlite"
  sqlite: "homestead_data.db"
```

### MySQL / MariaDB / PostgreSQL

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

### MongoDB

```yaml
database:
  provider: "mongodb"
  mongodb:
    uri: "mongodb://localhost:27017"  # Or Atlas SRV URI
    database: "homestead"
    collection_prefix: "hs_"
```

### YAML

!!! warning "Not Recommended for Production"

    YAML is human-readable but slow. Only use for testing or tiny private servers.

```yaml
database:
  provider: "yaml"
```

## Cache System

Homestead caches all data in memory for fast lookups. Modified data is periodically written back to the database.

```yaml
cache-interval: 30  # Seconds between cache flushes
```

**Recommendations:**

| Players | Interval |
|:--------|:---------|
| <20 | 30s |
| 20-50 | 60s |
| 50-100 | 120-180s |
| 100+ | 300s |

## Changing Providers

To switch databases without losing data:

1. Configure the **new** provider in `config.yml` (but keep `provider` set to the old one)
2. Run `/hsadmin reload`
3. Run `/hsadmin export [new-provider]`
4. Wait for the export to complete
5. Stop the server
6. Update `provider` to the new value in `config.yml`
7. Start the server

Read more: [Database Migration](Database Migration.md)
