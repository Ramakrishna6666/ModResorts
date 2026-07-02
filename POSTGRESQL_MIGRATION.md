# PostgreSQL Migration Guide for ModResorts Application

## Overview
This document describes the migration of the ModResorts application from SQL Server to PostgreSQL 16.

## Migration Summary

### Changes Made

#### 1. Package Dependencies (pom.xml)
- **Added**: PostgreSQL JDBC Driver (org.postgresql:postgresql:42.7.1)
- **Purpose**: Enable PostgreSQL database connectivity

#### 2. Database Configuration
- **Created**: `src/main/resources/database.properties`
- **Contains**: PostgreSQL connection parameters, pool settings, and timeouts
- **Default Connection**: jdbc:postgresql://localhost:5432/modresorts

#### 3. DataSource Configuration
- **Created**: `PostgreSQLDataSourceConfig.java`
- **Purpose**: Centralized PostgreSQL DataSource management
- **Features**:
  - Singleton pattern for DataSource
  - Property-based configuration
  - Connection pooling support
  - SSL mode configuration
  - Connection timeout management

#### 4. Data Access Layer Updates
- **Modified**: `ModResortsCustomerInformation.java`
- **Changes**:
  - Updated SQL query to PostgreSQL syntax (lowercase identifiers)
  - Changed column references from uppercase to lowercase
  - Added schema qualification (public.customer)
  - Integrated PostgreSQL DataSource configuration
  - Added @PostConstruct initialization

#### 5. Database Schema
- **Created**: `V1__create_customer_table.sql`
- **Contains**:
  - PostgreSQL-compatible table definitions
  - Sample data for testing
  - Indexes for performance
  - Triggers for automatic timestamp updates

## PostgreSQL-Specific Changes

### SQL Syntax Differences

#### Table and Column Names
- **SQL Server**: Case-insensitive by default (INFO, Customer)
- **PostgreSQL**: Case-sensitive when quoted, lowercase by default
- **Migration**: Changed all references to lowercase (info, customer)

#### Schema Qualification
- **SQL Server**: dbo schema default
- **PostgreSQL**: public schema default
- **Migration**: Explicitly use public.customer

#### Data Types
- **SQL Server IDENTITY** → **PostgreSQL SERIAL**
- **SQL Server DATETIME** → **PostgreSQL TIMESTAMP**
- **SQL Server VARCHAR(MAX)** → **PostgreSQL TEXT**

### Connection String Format

#### SQL Server Format
```
jdbc:sqlserver://localhost:1433;databaseName=modresorts;user=sa;password=password
```

#### PostgreSQL Format
```
jdbc:postgresql://localhost:5432/modresorts?user=postgres&password=postgres
```

## Configuration Files

### database.properties
Location: `src/main/resources/database.properties`

Key properties:
- `db.url`: JDBC connection URL
- `db.username`: Database username
- `db.password`: Database password
- `db.schema`: Default schema (public)
- `db.pool.*`: Connection pool settings

### Customization
To customize the database connection:
1. Edit `database.properties`
2. Update host, port, database name, credentials
3. Adjust pool sizes based on application load
4. Configure SSL if required

## Database Setup

### Prerequisites
1. PostgreSQL 16 installed and running
2. Database user with appropriate permissions
3. Network access to PostgreSQL server

### Setup Steps

#### 1. Create Database
```sql
CREATE DATABASE modresorts
    WITH 
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8';
```

#### 2. Run Migration Script
```bash
psql -U postgres -d modresorts -f src/main/resources/db/migration/V1__create_customer_table.sql
```

#### 3. Verify Setup
```sql
-- Connect to database
\c modresorts

-- List tables
\dt public.*

-- Check data
SELECT * FROM public.customer;
```

## Application Server Configuration

### WebSphere Liberty / Open Liberty
Add to `server.xml`:

```xml
<dataSource id="ModResortsDataSource" jndiName="jdbc/ModResortsJndi">
    <jdbcDriver libraryRef="PostgreSQLLib"/>
    <properties.postgresql 
        serverName="localhost" 
        portNumber="5432"
        databaseName="modresorts"
        user="postgres"
        password="postgres"
        currentSchema="public"/>
</dataSource>

<library id="PostgreSQLLib">
    <fileset dir="${server.config.dir}/lib" includes="postgresql-42.7.1.jar"/>
</library>
```

### Apache Tomcat
Add to `context.xml`:

```xml
<Resource name="jdbc/ModResortsJndi" 
          auth="Container"
          type="javax.sql.DataSource"
          driverClassName="org.postgresql.Driver"
          url="jdbc:postgresql://localhost:5432/modresorts"
          username="postgres" 
          password="postgres"
          maxTotal="20" 
          maxIdle="10"
          maxWaitMillis="10000"/>
```

## Testing

### Connection Test
The `PostgreSQLDataSourceConfig` class includes a test method:

```java
boolean connected = PostgreSQLDataSourceConfig.testConnection();
if (connected) {
    System.out.println("PostgreSQL connection successful!");
} else {
    System.err.println("PostgreSQL connection failed!");
}
```

### Query Test
Test the customer information retrieval:

```java
ModResortsCustomerInformation customerInfo = new ModResortsCustomerInformation();
ArrayList<String> customers = customerInfo.getCustomerInformation();
System.out.println("Retrieved " + customers.size() + " customers");
```

## Troubleshooting

### Common Issues

#### 1. Connection Refused
- **Cause**: PostgreSQL not running or wrong host/port
- **Solution**: Verify PostgreSQL is running: `systemctl status postgresql`
- **Check**: Ensure port 5432 is open and accessible

#### 2. Authentication Failed
- **Cause**: Wrong username/password or pg_hba.conf restrictions
- **Solution**: Check credentials in database.properties
- **Check**: Review pg_hba.conf for authentication method

#### 3. Schema Not Found
- **Cause**: Schema not created or wrong search_path
- **Solution**: Ensure public schema exists
- **Check**: Run `SELECT current_schema();` in psql

#### 4. Table Not Found
- **Cause**: Migration script not executed
- **Solution**: Run V1__create_customer_table.sql
- **Check**: `\dt public.*` in psql

#### 5. Column Not Found
- **Cause**: Case sensitivity issues
- **Solution**: Use lowercase column names without quotes
- **Check**: Verify column names match table definition

## Performance Considerations

### Connection Pooling
- Default pool size: 20 connections
- Adjust based on application load
- Monitor connection usage

### Indexes
- Created on email and name columns
- Add additional indexes based on query patterns
- Use EXPLAIN ANALYZE to optimize queries

### Query Optimization
- Use prepared statements (already implemented)
- Avoid SELECT * queries
- Use appropriate WHERE clauses
- Consider materialized views for complex queries

## Security Recommendations

### Production Deployment
1. **Change default credentials**: Update username/password
2. **Enable SSL**: Set `db.ssl=true` and configure certificates
3. **Restrict network access**: Use firewall rules
4. **Use connection encryption**: Configure SSL mode to 'require'
5. **Implement least privilege**: Create application-specific database user
6. **Secure properties file**: Encrypt sensitive configuration

### Database User Setup
```sql
-- Create application user
CREATE USER modresorts_app WITH PASSWORD 'secure_password';

-- Grant minimal required permissions
GRANT CONNECT ON DATABASE modresorts TO modresorts_app;
GRANT USAGE ON SCHEMA public TO modresorts_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.customer TO modresorts_app;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO modresorts_app;
```

## Rollback Plan

If migration issues occur:

1. **Keep SQL Server configuration**: Maintain original configuration files
2. **Database backup**: Backup SQL Server database before migration
3. **Version control**: Commit changes separately for easy rollback
4. **Parallel running**: Run both databases during transition period
5. **Data sync**: Implement data synchronization if needed

## Next Steps

1. **Test thoroughly**: Verify all database operations
2. **Performance testing**: Load test with production-like data
3. **Monitor logs**: Check for SQL errors or warnings
4. **Update documentation**: Document any custom changes
5. **Train team**: Ensure team understands PostgreSQL differences

## Support Resources

- PostgreSQL Documentation: https://www.postgresql.org/docs/16/
- JDBC Driver Documentation: https://jdbc.postgresql.org/documentation/
- Migration Tools: pgLoader, AWS DMS, ora2pg (for reference)

## Version History

- **v1.0** - Initial PostgreSQL migration
  - Added PostgreSQL JDBC driver
  - Created DataSource configuration
  - Updated SQL queries for PostgreSQL compatibility
  - Created database schema migration script
