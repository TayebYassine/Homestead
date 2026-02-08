# Database

A database stores all region data for your server. Homestead includes a built-in caching system that improves performance when protecting and managing regions across your server.

## Supported Database Providers

Choose the database provider that best fits your server size and technical setup:

| Provider         | Status             | Recommended For                               |
|:-----------------|:-------------------|:----------------------------------------------|
| PostgreSQL       | :material-check:   | Large servers (~1,000+ daily players)         |
| MariaDB          | :material-check:   | Large servers (~1,000+ daily players)         |
| MySQL            | :material-check:   | Large servers (~1,000+ daily players)         |
| MongoDB          | 5.0.1.0            | Medium servers (~500 daily players)           |
| SQLite (Default) | :material-check:   | Small servers (~100 daily players)            |
| YAML             | :material-check:   | Small/private servers (~50 players)           |

### Which Provider Should I Use?

- **SQLite**: Best for most servers. It's the default option and requires no setup. Perfect if you're just starting out or running a small community server.
- **MySQL, MariaDB, PostgreSQL**: Choose one of these if you run a large server with many players. They offer better performance under heavy load and support advanced features.
- **YAML**: Simple file-based storage. Only recommended for very small friend servers or testing environments.

## Initial Setup

By default, Homestead uses **SQLite** when first installed. For most servers, this works perfectly and requires no configuration.

### Using SQLite (Default)

If you're happy with SQLite, you don't need to change anything. The database file will be created automatically in the server folder with the name **homestead_data.db**.

### Switching to a Different Provider

!!! warning "Before Public Use"

    If you want to change database providers, do it **before** allowing players to create regions. Changing providers on an active server requires migration (see below).

To switch providers, change the `provider` value in `config.yml` to one of these options:

- PostgreSQL: `postgresql`
- MariaDB: `mariadb`
- MySQL: `mysql`
- SQLite: `sqlite`
- YAML: `yaml`

#### Example Configuration

```yaml
database:
  provider: "sqlite"

  # PostgreSQL configuration
  postgresql:
    host: "localhost"
    port: 5432
    username: "your_username"
    password: "your_password"
    database: "homestead_data"
    table_prefix: ""
    
  # MariaDB configuration
  mariadb:
    host: "localhost"
    port: 3306
    username: "your_username"
    password: "your_password"
    database: "homestead_data"
    table_prefix: ""

  # MySQL configuration
  mysql:
    host: "localhost"
    port: 3306
    username: "your_username"
    password: "your_password"
    database: "homestead_data"
    table_prefix: ""

  # SQLite configuration
  sqlite: "homestead_data.db"
```

### Configuration Tips

**For MySQL, MariaDB, PostgreSQL:**

1. Create a new database on your database server (e.g., `homestead_data`)
2. Create a user with full permissions on that database
3. Fill in the connection details in `config.yml`
4. Make sure the port is correct:
    - MySQL/MariaDB: Usually `3306`
    - PostgreSQL: Usually `5432`

**For SQLite:**

Simply specify the filename. The file will be created in the server folder.

**For YAML:**

No configuration needed. Data will be stored in YAML files in the plugin folder.

## Migrating to a Different Provider

Already have an active server with regions? Follow these steps to safely migrate your data:

### Migration Steps

1. **Prepare the new provider** by filling in its connection details in `config.yml`
2. **Save your changes** and reload the configuration:
   ```
   /hsadmin reload
   ```
3. **Export your data** to the new provider:
   ```
   /hsadmin export [provider]
   ```
   Replace `[provider]` with your target provider (e.g., `MySQL`, `PostgreSQL`)
4. **Wait for the export to complete**. Watch the console for progress messages.
5. **Stop your server** completely.
6. **Update `config.yml`** to change the `provider` value to your new provider.
7. **Start your server**. Homestead will now use the new database!
