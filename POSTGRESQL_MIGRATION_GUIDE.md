# PostgreSQL Migration Guide for ModResorts Application

## Overview
This document describes the migration of the ModResorts application from SQL Server to PostgreSQL 16.

## Migration Summary

### Changes Made

#### 1. Package Dependencies (pom.xml)
**Added:**
- `org.postgresql:postgresql:42.7.1` - PostgreSQL JDBC Driver
- `com.zaxxer:HikariCP:5.1.0` - Connection pooling

**Rationale:** PostgreSQL requires its own JDBC driver, and HikariCP provides efficient connection pooling optimized for PostgreSQL.

#### 2. Database Configuration
**Created:** `src/main/resources/database.properties`
- PostgreSQL connection string format: `jdbc:postgresql://localhost:5432/modresorts`
- Connection pool settings optimized for PostgreSQL
- Schema configuration set to `public` (PostgreSQL default)

**Key Differences from SQL Server:**
- Port: 5432 (PostgreSQL) vs 1433 (SQL Server)
- Driver: `org.postgresql.Driver` vs `com.microsoft.sqlserver.jdbc.SQLServerDriver`
- URL format: `jdbc:postgresql://` vs `jdbc:sqlserver://`

#### 3. DataSource Configuration
**Created:** `PostgreSQLDataSourceConfig.java`
- Singleton EJB for application-wide DataSource management
- HikariCP connection pooling configuration
- PostgreSQL-specific optimizations:
  - Prepared statement caching
  - Server-side prepared statements
  - Optimized pool sizes

#### 4. SQL Query Updates
**File:** `ModResortsCustomerInformation.java`

**Changes:**
```sql
-- Before (SQL Server)
SELECT INFO FROM CUSTOMER

-- After (PostgreSQL)
SELECT info FROM public.customer
```

**Key Changes:**
- Column names: `INFO` → `info` (PostgreSQL uses lowercase by default)
- Schema prefix: Added `public.` for explicit schema reference
- Case sensitivity: PostgreSQL is case-sensitive for quoted identifiers

**Code Improvements:**
- Replaced manual resource management with try-with-resources
- Added proper logging
- Improved error handling
- Added null checks for DataSource

#### 5. Database Schema
**Created:** `postgresql-init.sql`

**PostgreSQL-specific features:**
- `SERIAL` type for auto-incrementing primary keys (vs `IDENTITY` in SQL Server)
- `VARCHAR` instead of `NVARCHAR`
- `TIMESTAMP` for date/time (vs `DATETIME2` in SQL Server)
- Triggers and functions for automatic timestamp updates
- Proper indexing for performance

## PostgreSQL vs SQL Server Key Differences

### 1. Data Types
| SQL Server | PostgreSQL | Notes |
|------------|------------|-------|
| `INT IDENTITY` | `SERIAL` or `BIGSERIAL` | Auto-incrementing integers |
| `NVARCHAR(n)` | `VARCHAR(n)` | PostgreSQL uses UTF-8 by default |
| `DATETIME2` | `TIMESTAMP` | Date and time storage |
| `BIT` | `BOOLEAN` | Boolean values |
| `UNIQUEIDENTIFIER` | `UUID` | Unique identifiers |

### 2. Case Sensitivity
- **SQL Server:** Case-insensitive by default (depends on collation)
- **PostgreSQL:** Case-sensitive for quoted identifiers, converts unquoted to lowercase

### 3. Schema
- **SQL Server:** Default schema is `dbo`
- **PostgreSQL:** Default schema is `public`

### 4. String Concatenation
- **SQL Server:** `+` operator or `CONCAT()`
- **PostgreSQL:** `||` operator or `CONCAT()`

### 5. Date Functions
| SQL Server | PostgreSQL |
|------------|------------|
| `GETDATE()` | `CURRENT_TIMESTAMP` or `NOW()` |
| `DATEPART()` | `EXTRACT()` |
| `DATEDIFF()` | `AGE()` or date arithmetic |

### 6. Pagination
| SQL Server | PostgreSQL |
|------------|------------|
| `OFFSET n ROWS FETCH NEXT m ROWS ONLY` | `LIMIT m OFFSET n` |

### 7. Top N Records
| SQL Server | PostgreSQL |
|------------|------------|
| `SELECT TOP n` | `SELECT ... LIMIT n` |

## Setup Instructions

### 1. Install PostgreSQL 16
```bash
# Ubuntu/Debian
sudo apt-get update
sudo apt-get install postgresql-16

# macOS (using Homebrew)
brew install postgresql@16

# Windows
# Download installer from https://www.postgresql.org/download/windows/
```

### 2. Create Database
```bash
# Connect to PostgreSQL
psql -U postgres

# Run initialization script
\i src/main/resources/postgresql-init.sql
```

### 3. Configure Connection
Edit `src/main/resources/database.properties`:
```properties
db.url=jdbc:postgresql://your-host:5432/modresorts
db.username=your-username
db.password=your-password
```

### 4. Deploy Application
```bash
mvn clean package
# Deploy the generated WAR file to your application server
```

## Testing

### 1. Verify Database Connection
```sql
-- Connect to database
psql -U postgres -d modresorts

-- Check tables
\dt

-- Query customer data
SELECT * FROM customer;
```

### 2. Test Application
```bash
# Access the application
curl http://localhost:8080/modresorts/welcome

# Test customer data retrieval (if endpoint exists)
curl http://localhost:8080/modresorts/customers
```

## Performance Tuning

### PostgreSQL Configuration
Edit `postgresql.conf`:
```conf
# Memory settings
shared_buffers = 256MB
effective_cache_size = 1GB
work_mem = 16MB

# Connection settings
max_connections = 100

# Query planner
random_page_cost = 1.1  # For SSD storage
```

### Connection Pool Settings
Adjust in `database.properties`:
```properties
db.pool.maximumPoolSize=20
db.pool.minimumIdle=10
```

## Troubleshooting

### Common Issues

#### 1. Connection Refused
**Error:** `Connection refused: connect`
**Solution:** 
- Verify PostgreSQL is running: `sudo systemctl status postgresql`
- Check `pg_hba.conf` for connection permissions
- Ensure firewall allows port 5432

#### 2. Authentication Failed
**Error:** `FATAL: password authentication failed`
**Solution:**
- Verify credentials in `database.properties`
- Check `pg_hba.conf` authentication method
- Reset password: `ALTER USER postgres PASSWORD 'newpassword';`

#### 3. Schema Not Found
**Error:** `ERROR: schema "public" does not exist`
**Solution:**
```sql
CREATE SCHEMA IF NOT EXISTS public;
GRANT ALL ON SCHEMA public TO postgres;
```

#### 4. Case Sensitivity Issues
**Error:** `ERROR: column "INFO" does not exist`
**Solution:** Use lowercase column names or quote identifiers:
```sql
-- Option 1: Use lowercase
SELECT info FROM customer;

-- Option 2: Quote identifiers
SELECT "INFO" FROM customer;
```

## Rollback Plan

If migration issues occur:

1. **Keep SQL Server database** as backup
2. **Document all changes** for easy reversal
3. **Test thoroughly** in staging environment
4. **Have rollback scripts** ready

### Rollback Steps
1. Restore original `pom.xml` (remove PostgreSQL dependencies)
2. Restore original `ModResortsCustomerInformation.java`
3. Reconfigure DataSource to SQL Server
4. Redeploy application

## Best Practices

### 1. Always Use Connection Pooling
- HikariCP is recommended for PostgreSQL
- Configure appropriate pool sizes based on load

### 2. Use Prepared Statements
- Prevents SQL injection
- Improves performance through statement caching

### 3. Explicit Schema References
- Use `public.table_name` for clarity
- Prevents ambiguity in multi-schema environments

### 4. Follow PostgreSQL Naming Conventions
- Use lowercase with underscores (snake_case)
- Avoid reserved keywords
- Keep names descriptive but concise

### 5. Regular Maintenance
```sql
-- Analyze tables for query optimization
ANALYZE;

-- Vacuum to reclaim storage
VACUUM;

-- Reindex if needed
REINDEX TABLE customer;
```

## Additional Resources

- [PostgreSQL Official Documentation](https://www.postgresql.org/docs/16/)
- [PostgreSQL JDBC Driver Documentation](https://jdbc.postgresql.org/documentation/)
- [HikariCP Configuration](https://github.com/brettwooldridge/HikariCP)
- [SQL Server to PostgreSQL Migration Guide](https://wiki.postgresql.org/wiki/Converting_from_other_Databases_to_PostgreSQL)

## Support

For issues or questions:
1. Check PostgreSQL logs: `/var/log/postgresql/`
2. Check application logs
3. Review this migration guide
4. Consult PostgreSQL community forums

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | 2024 | Initial PostgreSQL migration |
