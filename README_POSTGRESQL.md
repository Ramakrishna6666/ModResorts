# ModResorts PostgreSQL Migration - Quick Start Guide

## Overview
This application has been migrated from SQL Server to PostgreSQL 16. This guide provides quick setup instructions.

## Prerequisites
- Java 21
- Maven 3.8+
- PostgreSQL 16
- Application Server (e.g., WildFly, Tomcat, etc.)

## Quick Setup (5 Minutes)

### 1. Install PostgreSQL
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
# Start PostgreSQL service
sudo systemctl start postgresql  # Linux
brew services start postgresql@16  # macOS

# Create database
sudo -u postgres psql -c "CREATE DATABASE modresorts;"

# Run schema script
sudo -u postgres psql -d modresorts -f src/main/resources/db/postgresql/schema.sql
```

### 3. Configure Connection
Edit `src/main/java/com/acme/modres/config/PostgreSQLDataSourceConfig.java`:

```java
config.setJdbcUrl("jdbc:postgresql://localhost:5432/modresorts");
config.setUsername("postgres");
config.setPassword("your_password");  // Change this!
```

### 4. Build Application
```bash
mvn clean package
```

### 5. Deploy and Test
Deploy the generated WAR file to your application server, then test:

```bash
# Test database health
curl http://localhost:8080/modresorts/health/db

# Expected response:
{
  "status": "UP",
  "database": "PostgreSQL",
  "databaseVersion": "16.x",
  "customerTableExists": true,
  "customerRecordCount": 5
}
```

## What Changed?

### Files Added
1. **PostgreSQLDataSourceConfig.java** - Database connection configuration
2. **PostgreSQLDatabaseUtil.java** - Database utility methods
3. **DatabaseHealthServlet.java** - Health check endpoint
4. **schema.sql** - PostgreSQL database schema
5. **application.properties** - Configuration properties

### Files Modified
1. **pom.xml** - Added PostgreSQL and HikariCP dependencies
2. **ModResortsCustomerInformation.java** - Updated for PostgreSQL
3. **web.xml** - Added health check servlet mapping

### Dependencies Added
- PostgreSQL JDBC Driver (42.7.1)
- HikariCP Connection Pool (5.1.0)

## Testing

### 1. Test Database Connection
```bash
curl http://localhost:8080/modresorts/health/db
```

### 2. Test Customer Data Retrieval
The existing application endpoints should work with PostgreSQL:
```bash
curl http://localhost:8080/modresorts/welcome
```

### 3. Verify Data in PostgreSQL
```bash
psql -U postgres -d modresorts -c "SELECT * FROM customer;"
```

## Configuration Options

### Environment Variables (Recommended for Production)
```bash
export DB_URL="jdbc:postgresql://prod-server:5432/modresorts"
export DB_USER="modresorts_user"
export DB_PASSWORD="secure_password"
```

### Connection Pool Tuning
Edit `PostgreSQLDataSourceConfig.java`:
```java
config.setMaximumPoolSize(20);  // Increase for high load
config.setMinimumIdle(5);       // Increase for better response time
```

## Troubleshooting

### Issue: Connection Refused
```bash
# Check if PostgreSQL is running
sudo systemctl status postgresql

# Check if PostgreSQL is listening
sudo netstat -plnt | grep 5432
```

### Issue: Authentication Failed
```bash
# Reset PostgreSQL password
sudo -u postgres psql
ALTER USER postgres PASSWORD 'newpassword';
\q
```

### Issue: Database Not Found
```bash
# List databases
sudo -u postgres psql -c "\l"

# Create if missing
sudo -u postgres psql -c "CREATE DATABASE modresorts;"
```

## Production Deployment Checklist

- [ ] Change default PostgreSQL password
- [ ] Use environment variables for credentials
- [ ] Enable SSL for database connections
- [ ] Configure connection pool for expected load
- [ ] Set up database backups
- [ ] Configure monitoring and alerting
- [ ] Test failover scenarios
- [ ] Document rollback procedure
- [ ] Train operations team

## Monitoring

### Health Check Endpoint
```bash
# Check database health
curl http://localhost:8080/modresorts/health/db
```

### PostgreSQL Monitoring
```sql
-- Active connections
SELECT count(*) FROM pg_stat_activity;

-- Database size
SELECT pg_size_pretty(pg_database_size('modresorts'));

-- Table statistics
SELECT * FROM pg_stat_user_tables WHERE schemaname = 'public';
```

## Support

For detailed migration information, see:
- **POSTGRESQL_MIGRATION_GUIDE.md** - Complete migration documentation
- **schema.sql** - Database schema with comments
- **application.properties** - Configuration reference

## Key Differences from SQL Server

| Feature | SQL Server | PostgreSQL |
|---------|-----------|------------|
| Case Sensitivity | Case-insensitive | Case-sensitive (lowercase default) |
| Auto-increment | IDENTITY | SERIAL |
| Date/Time | GETDATE() | CURRENT_TIMESTAMP |
| String Concat | + | \|\| |
| Limit Results | TOP N | LIMIT N |

## Next Steps

1. Review **POSTGRESQL_MIGRATION_GUIDE.md** for detailed information
2. Test all application features thoroughly
3. Monitor performance and tune as needed
4. Plan production migration with minimal downtime
5. Set up backup and recovery procedures

## Quick Reference

### PostgreSQL Commands
```bash
# Connect to database
psql -U postgres -d modresorts

# List tables
\dt

# Describe table
\d customer

# Run query
SELECT * FROM customer;

# Exit
\q
```

### Maven Commands
```bash
# Clean and build
mvn clean package

# Run tests
mvn test

# Skip tests
mvn clean package -DskipTests
```

## Success Criteria

✅ PostgreSQL service is running  
✅ Database 'modresorts' exists  
✅ Schema is created with sample data  
✅ Application builds successfully  
✅ Health check returns "UP" status  
✅ Customer data can be retrieved  

---

**Migration Status**: ✅ Complete  
**PostgreSQL Version**: 16  
**Java Version**: 21  
**Last Updated**: 2024
