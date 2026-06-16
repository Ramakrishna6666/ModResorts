# PostgreSQL Migration Guide for ModResorts Application

## Overview
This document describes the database migration from a generic JDBC implementation to a PostgreSQL 16-optimized implementation with modern Java practices.

## Migration Summary

### What Changed

#### 1. **Dependencies Added (pom.xml)**
- **PostgreSQL JDBC Driver** (42.7.1): Official PostgreSQL driver for Java
- **HikariCP** (5.1.0): High-performance JDBC connection pool

#### 2. **Database Configuration**
- **New File**: `src/main/resources/database.properties`
  - PostgreSQL connection settings
  - Connection pool configuration
  - Schema and SSL settings
  - Query timeout configuration

#### 3. **Code Modernization**

##### ModResortsCustomerInformation.java
**Before:**
- Manual resource management with try-catch-finally
- No logging
- Basic error handling
- Commented-out DataSource injection

**After:**
- Try-with-resources for automatic resource management
- Comprehensive logging using java.util.logging
- Better error handling with specific exception types
- Active DataSource injection
- Added health check method
- PostgreSQL-compatible SQL (lowercase column names)

##### New Classes Created

**PostgreSQLDataSourceConfig.java**
- Singleton pattern for centralized database configuration
- HikariCP connection pool management
- Loads configuration from properties file
- Thread-safe implementation
- Connection pool monitoring and statistics
- Graceful shutdown handling

**CustomerRepository.java**
- Repository pattern implementation
- Full CRUD operations
- Modern Java practices (Optional, try-with-resources)
- PostgreSQL-specific features:
  - ILIKE for case-insensitive search
  - RETURNING clause for insert operations
  - Batch processing support
- Soft delete implementation
- Search functionality

#### 4. **Database Schema**
- **New File**: `migration-bundle/postgresql_migration.sql`
  - Complete PostgreSQL schema definition
  - Customer table with proper data types
  - Indexes for performance optimization
  - Full-text search index
  - Automatic timestamp update trigger
  - Sample data for testing
  - Views for common queries
  - Performance optimization commands

## PostgreSQL-Specific Features Implemented

### 1. **Data Types**
- `SERIAL` for auto-incrementing primary keys
- `TEXT` for variable-length strings
- `TIMESTAMP WITH TIME ZONE` for timezone-aware timestamps
- `BOOLEAN` for true/false flags

### 2. **Indexes**
- B-tree indexes for standard queries
- GIN index for full-text search
- Partial indexes for filtered queries

### 3. **Triggers and Functions**
- Automatic `updated_at` timestamp update
- PL/pgSQL function implementation

### 4. **Query Optimizations**
- `ILIKE` for case-insensitive pattern matching
- `RETURNING` clause to get generated IDs
- Batch insert with `reWriteBatchedInserts`
- Prepared statement caching

### 5. **Connection Pool Optimizations**
- HikariCP with PostgreSQL-specific settings
- Prepared statement caching
- Server-side prepared statements
- Batch rewrite for better performance

## Configuration

### Database Connection Properties

Edit `src/main/resources/database.properties`:

```properties
# Basic Connection
db.url=jdbc:postgresql://localhost:5432/modresorts
db.username=modresorts_user
db.password=your_secure_password

# Connection Pool
db.pool.maximumPoolSize=10
db.pool.minimumIdle=5
db.pool.connectionTimeout=30000

# PostgreSQL Settings
db.postgresql.schema=public
db.postgresql.ssl=false
```

### Application Server Configuration (Jakarta EE)

Configure JNDI DataSource in your application server:

**For WildFly/JBoss:**
```xml
<datasource jndi-name="java:/jdbc/ModResortsJndi" pool-name="ModResortsPool">
    <connection-url>jdbc:postgresql://localhost:5432/modresorts</connection-url>
    <driver>postgresql</driver>
    <security>
        <user-name>modresorts_user</user-name>
        <password>your_password</password>
    </security>
</datasource>
```

**For Tomcat (context.xml):**
```xml
<Resource name="jdbc/ModResortsJndi" 
          auth="Container"
          type="javax.sql.DataSource"
          driverClassName="org.postgresql.Driver"
          url="jdbc:postgresql://localhost:5432/modresorts"
          username="modresorts_user" 
          password="your_password"
          maxTotal="20" 
          maxIdle="10"
          maxWaitMillis="10000"/>
```

## Migration Steps

### 1. **Install PostgreSQL 16**
```bash
# Ubuntu/Debian
sudo apt-get update
sudo apt-get install postgresql-16

# Start PostgreSQL service
sudo systemctl start postgresql
sudo systemctl enable postgresql
```

### 2. **Create Database and User**
```sql
-- Connect as postgres user
sudo -u postgres psql

-- Create database
CREATE DATABASE modresorts;

-- Create user
CREATE USER modresorts_user WITH PASSWORD 'your_secure_password';

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE modresorts TO modresorts_user;

-- Connect to the database
\c modresorts

-- Grant schema privileges
GRANT ALL ON SCHEMA public TO modresorts_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO modresorts_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO modresorts_user;
```

### 3. **Run Migration Script**
```bash
# Execute the migration SQL script
psql -U modresorts_user -d modresorts -f migration-bundle/postgresql_migration.sql
```

### 4. **Update Application Configuration**
- Update `database.properties` with your connection details
- Configure JNDI DataSource in your application server
- Update any environment-specific settings

### 5. **Build and Deploy**
```bash
# Build the application
mvn clean package

# Deploy the WAR file to your application server
# Copy target/modresorts-2.0.0.war to your server's deployment directory
```

## Testing

### Verify Database Connection
```java
// Use the health check method
ModResortsCustomerInformation customerInfo = new ModResortsCustomerInformation();
boolean isAvailable = customerInfo.isDatabaseAvailable();
System.out.println("Database available: " + isAvailable);
```

### Test CRUD Operations
```java
// Using the repository
DataSource ds = PostgreSQLDataSourceConfig.getInstance().getDataSource();
CustomerRepository repo = new CustomerRepository(ds);

// Insert
Optional<Integer> id = repo.insertCustomer("Test Customer");

// Read
List<String> customers = repo.findAllCustomers();

// Update
repo.updateCustomer(id.get(), "Updated Customer Info");

// Search
List<String> results = repo.searchCustomers("Test");

// Delete
repo.deleteCustomer(id.get());
```

### Monitor Connection Pool
```java
PostgreSQLDataSourceConfig config = PostgreSQLDataSourceConfig.getInstance();
String stats = config.getPoolStats();
System.out.println(stats);
```

## Performance Tuning

### PostgreSQL Configuration
Edit `postgresql.conf`:

```conf
# Connection Settings
max_connections = 100
shared_buffers = 256MB
effective_cache_size = 1GB
maintenance_work_mem = 64MB
checkpoint_completion_target = 0.9
wal_buffers = 16MB
default_statistics_target = 100
random_page_cost = 1.1
effective_io_concurrency = 200
work_mem = 4MB
min_wal_size = 1GB
max_wal_size = 4GB
```

### Application Tuning
- Adjust connection pool size based on load
- Use batch operations for bulk inserts
- Enable prepared statement caching
- Monitor slow queries with pg_stat_statements

## Monitoring

### Database Monitoring
```sql
-- Active connections
SELECT * FROM pg_stat_activity WHERE datname = 'modresorts';

-- Table statistics
SELECT * FROM pg_stat_user_tables WHERE schemaname = 'public';

-- Index usage
SELECT * FROM pg_stat_user_indexes WHERE schemaname = 'public';

-- Slow queries (requires pg_stat_statements extension)
SELECT query, mean_exec_time, calls 
FROM pg_stat_statements 
ORDER BY mean_exec_time DESC 
LIMIT 10;
```

### Application Monitoring
- Monitor HikariCP metrics via JMX
- Log slow queries (configure in database.properties)
- Track connection pool statistics

## Troubleshooting

### Common Issues

#### Connection Refused
```
Error: Connection refused. Check that the hostname and port are correct
```
**Solution:** Verify PostgreSQL is running and listening on the correct port
```bash
sudo systemctl status postgresql
sudo netstat -plnt | grep 5432
```

#### Authentication Failed
```
Error: FATAL: password authentication failed for user "modresorts_user"
```
**Solution:** Check pg_hba.conf and ensure user credentials are correct

#### Too Many Connections
```
Error: FATAL: sorry, too many clients already
```
**Solution:** Increase max_connections in postgresql.conf or reduce pool size

#### Slow Queries
**Solution:** 
- Run ANALYZE on tables
- Check index usage
- Review query execution plans with EXPLAIN ANALYZE

## Rollback Procedure

If you need to rollback the migration:

1. **Database Rollback:**
```sql
-- Run the rollback section from postgresql_migration.sql
DROP VIEW IF EXISTS active_customers;
DROP TRIGGER IF EXISTS trigger_update_customer_timestamp ON customer;
DROP FUNCTION IF EXISTS update_updated_at_column();
DROP TABLE IF EXISTS customer CASCADE;
```

2. **Code Rollback:**
- Revert to previous version from Git
- Remove PostgreSQL dependencies from pom.xml
- Restore original database configuration

## Best Practices

1. **Always use connection pooling** (HikariCP is configured)
2. **Use try-with-resources** for automatic resource management
3. **Implement proper error handling** with logging
4. **Use prepared statements** to prevent SQL injection
5. **Enable SSL** for production environments
6. **Regular backups** using pg_dump
7. **Monitor performance** with pg_stat_statements
8. **Use transactions** for data consistency
9. **Implement retry logic** for transient failures
10. **Keep PostgreSQL updated** for security and performance

## Additional Resources

- [PostgreSQL 16 Documentation](https://www.postgresql.org/docs/16/)
- [HikariCP Configuration](https://github.com/brettwooldridge/HikariCP)
- [PostgreSQL JDBC Driver](https://jdbc.postgresql.org/)
- [PostgreSQL Performance Tuning](https://wiki.postgresql.org/wiki/Performance_Optimization)

## Support

For issues or questions:
1. Check PostgreSQL logs: `/var/log/postgresql/`
2. Check application logs
3. Review this documentation
4. Consult PostgreSQL community forums

---

**Migration Date:** 2024
**PostgreSQL Version:** 16
**Java Version:** 21
**Jakarta EE Version:** 10
