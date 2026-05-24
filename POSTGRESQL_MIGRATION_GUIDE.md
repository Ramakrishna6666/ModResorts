# PostgreSQL Migration Guide for ModResorts Application

## Overview
This guide documents the migration of the ModResorts application from SQL Server to PostgreSQL 16.

## Changes Made

### 1. Package Dependencies (pom.xml)
**Added PostgreSQL dependencies:**
- `org.postgresql:postgresql:42.7.1` - PostgreSQL JDBC Driver
- `com.zaxxer:HikariCP:5.1.0` - High-performance connection pool

### 2. Database Configuration
**Created new configuration class:**
- `PostgreSQLDataSourceConfig.java` - Manages PostgreSQL connection pool using HikariCP
- Optimized connection pool settings for PostgreSQL
- Automatic connection validation and lifecycle management

### 3. Data Access Layer Updates
**Updated `ModResortsCustomerInformation.java`:**
- Migrated from SQL Server to PostgreSQL
- Implemented try-with-resources for automatic resource management
- Added proper logging using Java Util Logging
- Updated SQL query to use PostgreSQL naming conventions (lowercase)
- Added connection test method for health checks

### 4. Database Schema
**Created PostgreSQL schema script:**
- `src/main/resources/db/postgresql/schema.sql`
- Uses PostgreSQL-specific features:
  - SERIAL for auto-incrementing primary keys
  - TIMESTAMP WITH TIME ZONE for proper timezone handling
  - Automatic triggers for updated_at timestamp
  - Proper indexing for performance
  - Sample data for testing

### 5. Configuration Files
**Created `application.properties`:**
- Centralized database configuration
- Connection pool settings
- PostgreSQL-specific parameters

## PostgreSQL Setup Instructions

### Prerequisites
1. Install PostgreSQL 16 on your system
2. Ensure PostgreSQL service is running

### Database Setup

#### Step 1: Create Database
```bash
# Connect to PostgreSQL as superuser
psql -U postgres

# Create database
CREATE DATABASE modresorts;

# Create application user (optional but recommended)
CREATE USER modresorts_user WITH PASSWORD 'your_secure_password';

# Grant privileges
GRANT ALL PRIVILEGES ON DATABASE modresorts TO modresorts_user;

# Exit psql
\q
```

#### Step 2: Initialize Schema
```bash
# Run the schema script
psql -U postgres -d modresorts -f src/main/resources/db/postgresql/schema.sql
```

#### Step 3: Verify Setup
```bash
# Connect to database
psql -U postgres -d modresorts

# Check tables
\dt

# Verify data
SELECT * FROM customer;

# Exit
\q
```

### Configuration Updates

#### Update Database Connection
Edit `src/main/java/com/acme/modres/config/PostgreSQLDataSourceConfig.java`:

```java
config.setJdbcUrl("jdbc:postgresql://your-host:5432/modresorts");
config.setUsername("your-username");
config.setPassword("your-password");
```

Or update `src/main/resources/application.properties`:

```properties
db.postgresql.url=jdbc:postgresql://your-host:5432/modresorts
db.postgresql.username=your-username
db.postgresql.password=your-password
```

## Key Differences: SQL Server vs PostgreSQL

### 1. Naming Conventions
- **SQL Server**: Case-insensitive by default, often uses UPPERCASE
- **PostgreSQL**: Case-sensitive, converts unquoted identifiers to lowercase
- **Solution**: Use lowercase table/column names or quote identifiers

### 2. Data Types
| SQL Server | PostgreSQL |
|------------|------------|
| INT IDENTITY | SERIAL or IDENTITY |
| DATETIME | TIMESTAMP |
| NVARCHAR | VARCHAR or TEXT |
| BIT | BOOLEAN |

### 3. Auto-Increment
- **SQL Server**: `INT IDENTITY(1,1)`
- **PostgreSQL**: `SERIAL` or `GENERATED ALWAYS AS IDENTITY`

### 4. Date/Time Functions
- **SQL Server**: `GETDATE()`, `DATEADD()`, `DATEDIFF()`
- **PostgreSQL**: `CURRENT_TIMESTAMP`, `+ INTERVAL`, `AGE()`

### 5. String Concatenation
- **SQL Server**: `+` operator or `CONCAT()`
- **PostgreSQL**: `||` operator or `CONCAT()`

### 6. TOP vs LIMIT
- **SQL Server**: `SELECT TOP 10 * FROM table`
- **PostgreSQL**: `SELECT * FROM table LIMIT 10`

## Connection Pool Configuration

### HikariCP Settings Explained

```java
// Maximum number of connections in the pool
config.setMaximumPoolSize(10);

// Minimum number of idle connections
config.setMinimumIdle(2);

// Maximum time to wait for connection (30 seconds)
config.setConnectionTimeout(30000);

// Maximum time a connection can sit idle (10 minutes)
config.setIdleTimeout(600000);

// Maximum lifetime of a connection (30 minutes)
config.setMaxLifetime(1800000);
```

### Tuning Recommendations
- **Small applications**: maxPoolSize = 5-10
- **Medium applications**: maxPoolSize = 10-20
- **Large applications**: maxPoolSize = 20-50

Formula: `connections = ((core_count * 2) + effective_spindle_count)`

## Testing the Migration

### 1. Connection Test
```java
ModResortsCustomerInformation dao = new ModResortsCustomerInformation();
boolean connected = dao.testConnection();
System.out.println("PostgreSQL Connection: " + (connected ? "SUCCESS" : "FAILED"));
```

### 2. Data Retrieval Test
```java
ArrayList<String> customers = dao.getCustomerInformation();
System.out.println("Retrieved " + customers.size() + " customers");
customers.forEach(System.out::println);
```

### 3. Performance Testing
Monitor connection pool metrics:
- Active connections
- Idle connections
- Connection wait time
- Query execution time

## Troubleshooting

### Common Issues

#### 1. Connection Refused
**Error**: `Connection refused: connect`
**Solution**: 
- Verify PostgreSQL is running: `sudo systemctl status postgresql`
- Check PostgreSQL is listening on correct port: `netstat -an | grep 5432`
- Update `pg_hba.conf` to allow connections

#### 2. Authentication Failed
**Error**: `FATAL: password authentication failed`
**Solution**:
- Verify username and password
- Check `pg_hba.conf` authentication method
- Reset password if needed: `ALTER USER postgres PASSWORD 'newpassword';`

#### 3. Database Does Not Exist
**Error**: `FATAL: database "modresorts" does not exist`
**Solution**: Create the database using the setup instructions above

#### 4. Table Not Found
**Error**: `ERROR: relation "customer" does not exist`
**Solution**: 
- Run the schema.sql script
- Check if table name needs quotes: `"CUSTOMER"` vs `customer`

#### 5. Connection Pool Exhausted
**Error**: `Connection is not available, request timed out`
**Solution**:
- Increase `maximumPoolSize`
- Check for connection leaks (unclosed connections)
- Reduce `connectionTimeout` to fail faster

## Performance Optimization

### 1. Indexing
```sql
-- Add indexes for frequently queried columns
CREATE INDEX idx_customer_info ON customer USING gin(to_tsvector('english', info));
```

### 2. Query Optimization
```sql
-- Use EXPLAIN ANALYZE to understand query performance
EXPLAIN ANALYZE SELECT info FROM customer WHERE id = 1;
```

### 3. Connection Pooling
- Use HikariCP (already configured)
- Monitor pool metrics
- Tune pool size based on load

### 4. Prepared Statements
- Already implemented in the code
- Reduces parsing overhead
- Improves security (prevents SQL injection)

## Security Best Practices

### 1. Use Environment Variables
```java
String dbUrl = System.getenv("DB_URL");
String dbUser = System.getenv("DB_USER");
String dbPassword = System.getenv("DB_PASSWORD");
```

### 2. Use SSL Connections
```java
config.setJdbcUrl("jdbc:postgresql://localhost:5432/modresorts?ssl=true&sslmode=require");
```

### 3. Principle of Least Privilege
```sql
-- Create read-only user for reporting
CREATE USER readonly_user WITH PASSWORD 'password';
GRANT CONNECT ON DATABASE modresorts TO readonly_user;
GRANT USAGE ON SCHEMA public TO readonly_user;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO readonly_user;
```

## Monitoring and Maintenance

### 1. Connection Pool Monitoring
```java
HikariPoolMXBean poolMXBean = dataSource.getHikariPoolMXBean();
System.out.println("Active Connections: " + poolMXBean.getActiveConnections());
System.out.println("Idle Connections: " + poolMXBean.getIdleConnections());
System.out.println("Total Connections: " + poolMXBean.getTotalConnections());
```

### 2. PostgreSQL Monitoring
```sql
-- Check active connections
SELECT * FROM pg_stat_activity;

-- Check database size
SELECT pg_size_pretty(pg_database_size('modresorts'));

-- Check table sizes
SELECT schemaname, tablename, pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename))
FROM pg_tables WHERE schemaname = 'public';
```

### 3. Regular Maintenance
```sql
-- Vacuum and analyze tables
VACUUM ANALYZE customer;

-- Reindex if needed
REINDEX TABLE customer;
```

## Rollback Plan

If you need to rollback to SQL Server:

1. Keep the original SQL Server configuration
2. Comment out PostgreSQL dependencies in pom.xml
3. Restore original `ModResortsCustomerInformation.java`
4. Update connection strings back to SQL Server

## Next Steps

1. **Test thoroughly** in development environment
2. **Performance test** with production-like data volume
3. **Update monitoring** to track PostgreSQL metrics
4. **Train team** on PostgreSQL differences
5. **Plan production migration** with minimal downtime
6. **Backup strategy** for PostgreSQL database

## Support and Resources

- PostgreSQL Documentation: https://www.postgresql.org/docs/16/
- HikariCP Documentation: https://github.com/brettwooldridge/HikariCP
- JDBC Driver Documentation: https://jdbc.postgresql.org/documentation/

## Migration Checklist

- [x] Add PostgreSQL dependencies to pom.xml
- [x] Create PostgreSQL DataSource configuration
- [x] Update data access layer for PostgreSQL
- [x] Create database schema script
- [x] Add configuration properties
- [x] Document migration process
- [ ] Test database connection
- [ ] Test data retrieval
- [ ] Performance testing
- [ ] Security review
- [ ] Production deployment plan

## Conclusion

The ModResorts application has been successfully migrated from SQL Server to PostgreSQL 16. The migration includes:
- Modern connection pooling with HikariCP
- PostgreSQL-specific optimizations
- Proper resource management
- Comprehensive documentation

Follow the setup instructions above to complete the database configuration and begin testing.
