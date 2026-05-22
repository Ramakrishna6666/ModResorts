# Database Migration - SQL Server to PostgreSQL

## Quick Start Guide

This document provides a quick reference for the database migration from SQL Server to PostgreSQL 16.

## What Changed?

### 1. Dependencies (pom.xml)
✅ Added PostgreSQL JDBC Driver 42.7.3

### 2. Database Code (Java)
✅ Updated `ModResortsCustomerInformation.java` with try-with-resources
✅ Created `PostgreSQLDataSourceConfig.java` for DataSource configuration
✅ Created `PostgreSQLConnectionTest.java` for connection testing

### 3. Configuration Files
✅ Created `database.properties` with PostgreSQL settings
✅ Created SQL migration script `V1__create_customer_table.sql`

### 4. Documentation
✅ Created comprehensive `POSTGRESQL_MIGRATION_GUIDE.md`

## Quick Setup (3 Steps)

### Step 1: Install PostgreSQL
```bash
# Ubuntu/Debian
sudo apt-get install postgresql-16

# Start PostgreSQL
sudo systemctl start postgresql
```

### Step 2: Create Database
```bash
sudo -u postgres psql
```
```sql
CREATE DATABASE modresorts;
\q
```

### Step 3: Run Migration Script
```bash
psql -U postgres -d modresorts -f src/main/resources/db/migration/V1__create_customer_table.sql
```

## Test Connection
```bash
mvn exec:java -Dexec.mainClass="com.acme.modres.db.PostgreSQLConnectionTest"
```

## Configuration

Edit `src/main/resources/database.properties`:
```properties
db.url=jdbc:postgresql://localhost:5432/modresorts
db.username=postgres
db.password=postgres
```

## Application Server Setup

### For WildFly/JBoss
1. Add PostgreSQL module
2. Configure datasource in `standalone.xml`
3. Uncomment `@Resource` annotation in `ModResortsCustomerInformation.java`

### For Open Liberty
1. Add PostgreSQL library to `server.xml`
2. Configure datasource with JNDI name `jdbc/ModResortsJndi`
3. Uncomment `@Resource` annotation in `ModResortsCustomerInformation.java`

## Build and Deploy
```bash
mvn clean package
# Deploy target/modresorts-2.0.0.war to your application server
```

## Need More Details?

See `POSTGRESQL_MIGRATION_GUIDE.md` for:
- Detailed migration steps
- Troubleshooting guide
- Performance optimization
- Security considerations
- Complete configuration examples

## Files Modified/Created

### Modified Files
- `pom.xml` - Added PostgreSQL dependency
- `src/main/java/com/acme/modres/db/ModResortsCustomerInformation.java` - Improved resource management

### New Files
- `src/main/resources/database.properties` - PostgreSQL configuration
- `src/main/java/com/acme/modres/db/PostgreSQLDataSourceConfig.java` - DataSource helper
- `src/main/java/com/acme/modres/db/PostgreSQLConnectionTest.java` - Connection test utility
- `src/main/resources/db/migration/V1__create_customer_table.sql` - Database schema
- `POSTGRESQL_MIGRATION_GUIDE.md` - Comprehensive migration guide
- `DATABASE_MIGRATION_README.md` - This file

## Support

For issues or questions:
1. Check `POSTGRESQL_MIGRATION_GUIDE.md` troubleshooting section
2. Review PostgreSQL logs: `/var/log/postgresql/`
3. Check application server logs
4. Verify connection settings in `database.properties`

## Migration Status

✅ Package dependencies updated
✅ Database access layer modernized
✅ Configuration files created
✅ Migration scripts prepared
✅ Testing utilities provided
✅ Documentation completed

**Status**: Ready for deployment and testing
