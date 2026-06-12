# Database Migration - SQL Server to PostgreSQL

## Quick Start

This application has been migrated from SQL Server to PostgreSQL 16. Follow these steps to set up and run the application.

## Prerequisites

- PostgreSQL 16 installed and running
- Java 21 or higher
- Maven 3.8 or higher
- Jakarta EE 10 compatible application server (WildFly, Payara, Open Liberty, etc.)

## Setup Steps

### 1. Database Setup

```bash
# Start PostgreSQL service
sudo systemctl start postgresql

# Create database and initialize schema
psql -U postgres -f src/main/resources/postgresql-init.sql
```

### 2. Configure Database Connection

Edit `src/main/resources/database.properties`:

```properties
db.url=jdbc:postgresql://localhost:5432/modresorts
db.username=postgres
db.password=your_password
```

### 3. Build Application

```bash
mvn clean package
```

### 4. Deploy

Deploy the generated WAR file (`target/modresorts-2.0.0.war`) to your application server.

## Configuration Options

### Option 1: Programmatic DataSource (Default)

The application uses `PostgreSQLDataSourceConfig` with HikariCP connection pooling. This is configured via `database.properties`.

**Pros:**
- No application server configuration needed
- Portable across different servers
- Easy to configure and test

**Cons:**
- Connection pool managed by application
- Requires restart to change configuration

### Option 2: JNDI DataSource

For production environments, configure JNDI DataSource in your application server.

1. See `src/main/resources/jndi-datasource-examples.xml` for server-specific configurations
2. Uncomment the `@Resource` annotation in `ModResortsCustomerInformation.java`:

```java
@Resource(lookup = "jdbc/ModResortsJndi")
private DataSource dataSource;
```

3. Comment out the `@Inject` line for `PostgreSQLDataSourceConfig`

**Pros:**
- Centralized connection pool management
- Can be changed without redeployment
- Better monitoring and management

**Cons:**
- Server-specific configuration
- Requires application server setup

## Migration Changes Summary

### 1. Dependencies Added
- PostgreSQL JDBC Driver (42.7.1)
- HikariCP Connection Pool (5.1.0)

### 2. Code Changes
- Updated SQL queries for PostgreSQL syntax
- Changed column names to lowercase (PostgreSQL convention)
- Added explicit schema references (`public.customer`)
- Implemented try-with-resources for better resource management
- Added comprehensive logging

### 3. Database Schema Changes
- Table names: lowercase with underscores (snake_case)
- Column names: lowercase
- Auto-increment: `SERIAL` instead of `IDENTITY`
- Timestamps: `TIMESTAMP` instead of `DATETIME2`

## Key Differences: SQL Server vs PostgreSQL

| Feature | SQL Server | PostgreSQL |
|---------|------------|------------|
| **Port** | 1433 | 5432 |
| **Default Schema** | dbo | public |
| **Case Sensitivity** | Insensitive | Sensitive (quoted) |
| **Auto-increment** | IDENTITY | SERIAL |
| **String Type** | NVARCHAR | VARCHAR (UTF-8) |
| **Date/Time** | DATETIME2 | TIMESTAMP |
| **Top N** | SELECT TOP n | LIMIT n |
| **Pagination** | OFFSET/FETCH | LIMIT/OFFSET |

## Testing

### Verify Database Connection

```bash
# Connect to database
psql -U postgres -d modresorts

# Check tables
\dt

# Query data
SELECT * FROM customer;
```

### Test Application

```bash
# Access welcome page
curl http://localhost:8080/modresorts/welcome

# Check weather service
curl http://localhost:8080/modresorts/weather?selectedCity=Paris
```

## Troubleshooting

### Connection Issues

**Problem:** Cannot connect to PostgreSQL

**Solutions:**
1. Verify PostgreSQL is running: `sudo systemctl status postgresql`
2. Check connection settings in `database.properties`
3. Verify firewall allows port 5432
4. Check `pg_hba.conf` for authentication settings

### Authentication Errors

**Problem:** Password authentication failed

**Solutions:**
1. Verify credentials in configuration
2. Reset password: `ALTER USER postgres PASSWORD 'newpassword';`
3. Check `pg_hba.conf` authentication method

### Case Sensitivity Issues

**Problem:** Column not found errors

**Solutions:**
- PostgreSQL converts unquoted identifiers to lowercase
- Use lowercase column names in queries
- Or quote identifiers: `"ColumnName"`

## Performance Tuning

### Connection Pool Settings

Adjust in `database.properties`:

```properties
# For high-traffic applications
db.pool.maximumPoolSize=50
db.pool.minimumIdle=10

# For low-traffic applications
db.pool.maximumPoolSize=10
db.pool.minimumIdle=5
```

### PostgreSQL Configuration

Edit `postgresql.conf`:

```conf
# Memory
shared_buffers = 256MB
effective_cache_size = 1GB

# Connections
max_connections = 100

# Performance
random_page_cost = 1.1  # For SSD
```

## Monitoring

### Check Connection Pool Status

```java
// Add to your monitoring endpoint
HikariPoolMXBean poolProxy = dataSource.getHikariPoolMXBean();
int activeConnections = poolProxy.getActiveConnections();
int idleConnections = poolProxy.getIdleConnections();
int totalConnections = poolProxy.getTotalConnections();
```

### PostgreSQL Monitoring

```sql
-- Active connections
SELECT * FROM pg_stat_activity WHERE datname = 'modresorts';

-- Database size
SELECT pg_size_pretty(pg_database_size('modresorts'));

-- Table statistics
SELECT * FROM pg_stat_user_tables WHERE schemaname = 'public';
```

## Backup and Recovery

### Backup Database

```bash
# Full backup
pg_dump -U postgres modresorts > modresorts_backup.sql

# Compressed backup
pg_dump -U postgres modresorts | gzip > modresorts_backup.sql.gz
```

### Restore Database

```bash
# Restore from backup
psql -U postgres modresorts < modresorts_backup.sql

# Restore from compressed backup
gunzip -c modresorts_backup.sql.gz | psql -U postgres modresorts
```

## Docker Setup (Optional)

For local development:

```bash
# Start PostgreSQL in Docker
docker run -d \
  --name modresorts-postgres \
  -e POSTGRES_DB=modresorts \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  -v $(pwd)/src/main/resources/postgresql-init.sql:/docker-entrypoint-initdb.d/init.sql \
  postgres:16-alpine

# Check logs
docker logs modresorts-postgres

# Connect to database
docker exec -it modresorts-postgres psql -U postgres -d modresorts
```

## Additional Resources

- [PostgreSQL Migration Guide](POSTGRESQL_MIGRATION_GUIDE.md) - Detailed migration documentation
- [JNDI Configuration Examples](src/main/resources/jndi-datasource-examples.xml) - Server-specific configurations
- [Database Initialization Script](src/main/resources/postgresql-init.sql) - Schema and sample data

## Support

For issues or questions:
1. Check application logs
2. Check PostgreSQL logs: `/var/log/postgresql/`
3. Review migration guide
4. Check PostgreSQL documentation: https://www.postgresql.org/docs/16/

## Version Information

- **Application Version:** 2.0.0
- **PostgreSQL Version:** 16
- **JDBC Driver Version:** 42.7.1
- **Java Version:** 21
- **Jakarta EE Version:** 10
