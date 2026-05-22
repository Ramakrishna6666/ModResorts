# PostgreSQL Migration Guide for ModResorts Application

## Overview
This guide provides step-by-step instructions for migrating the ModResorts application from SQL Server to PostgreSQL 16.

## Migration Summary

### Changes Made

#### 1. Package Dependencies (pom.xml)
- **Added**: PostgreSQL JDBC Driver 42.7.3
  ```xml
  <dependency>
      <groupId>org.postgresql</groupId>
      <artifactId>postgresql</artifactId>
      <version>42.7.3</version>
  </dependency>
  ```

#### 2. Database Access Layer
- **Updated**: `ModResortsCustomerInformation.java`
  - Implemented try-with-resources for automatic resource management
  - Added proper logging for PostgreSQL operations
  - Improved error handling
  - Added setter method for DataSource injection (testing support)

#### 3. Configuration Files
- **Created**: `database.properties` - PostgreSQL connection configuration
  - JDBC URL format: `jdbc:postgresql://localhost:5432/modresorts`
  - Connection pool settings
  - PostgreSQL-specific SSL and timeout configurations
  - Schema settings (default: public)

#### 4. Helper Classes
- **Created**: `PostgreSQLDataSourceConfig.java`
  - Utility class for creating and configuring PostgreSQL DataSource
  - Loads configuration from properties file
  - Provides default values for missing properties
  - Parses JDBC URLs and configures PGSimpleDataSource

- **Created**: `PostgreSQLConnectionTest.java`
  - Connection testing utility
  - Database metadata retrieval
  - Table existence verification
  - Standalone test execution support

#### 5. Database Migration Scripts
- **Created**: `V1__create_customer_table.sql`
  - PostgreSQL-compatible table creation
  - Uses SERIAL for auto-increment primary key
  - Includes TIMESTAMP WITH TIME ZONE for proper timezone handling
  - Creates indexes for performance
  - Adds table and column comments
  - Includes sample data
  - Creates trigger for automatic timestamp updates

## Prerequisites

### 1. PostgreSQL Installation
- PostgreSQL 16 or higher installed and running
- Default port: 5432
- Accessible from application server

### 2. Database Setup
```sql
-- Create database
CREATE DATABASE modresorts;

-- Create user (optional)
CREATE USER modresorts_app WITH PASSWORD 'your_secure_password';

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE modresorts TO modresorts_app;
```

### 3. Application Server Configuration
For Jakarta EE application servers (e.g., WildFly, Open Liberty, Payara):

#### WildFly/JBoss Configuration
Add PostgreSQL module and datasource to `standalone.xml`:

```xml
<!-- Add PostgreSQL Driver -->
<driver name="postgresql" module="org.postgresql">
    <xa-datasource-class>org.postgresql.xa.PGXADataSource</xa-datasource-class>
</driver>

<!-- Add DataSource -->
<datasource jndi-name="java:jboss/datasources/ModResortsDS" 
            pool-name="ModResortsDS" 
            enabled="true" 
            use-java-context="true">
    <connection-url>jdbc:postgresql://localhost:5432/modresorts</connection-url>
    <driver>postgresql</driver>
    <security>
        <user-name>postgres</user-name>
        <password>postgres</password>
    </security>
    <validation>
        <valid-connection-checker class-name="org.jboss.jca.adapters.jdbc.extensions.postgres.PostgreSQLValidConnectionChecker"/>
        <exception-sorter class-name="org.jboss.jca.adapters.jdbc.extensions.postgres.PostgreSQLExceptionSorter"/>
    </validation>
</datasource>
```

#### Open Liberty Configuration
Add to `server.xml`:

```xml
<library id="PostgreSQLLib">
    <fileset dir="${server.config.dir}/postgresql" includes="postgresql-42.7.3.jar"/>
</library>

<dataSource id="ModResortsDS" jndiName="jdbc/ModResortsJndi">
    <jdbcDriver libraryRef="PostgreSQLLib"/>
    <properties.postgresql 
        serverName="localhost" 
        portNumber="5432" 
        databaseName="modresorts" 
        user="postgres" 
        password="postgres"/>
</dataSource>
```

## Migration Steps

### Step 1: Update Configuration
1. Update `database.properties` with your PostgreSQL connection details:
   ```properties
   db.url=jdbc:postgresql://your-host:5432/modresorts
   db.username=your_username
   db.password=your_password
   ```

### Step 2: Run Database Migration Script
Execute the migration script to create tables:
```bash
psql -U postgres -d modresorts -f src/main/resources/db/migration/V1__create_customer_table.sql
```

Or use a migration tool like Flyway or Liquibase.

### Step 3: Test Database Connection
Run the connection test utility:
```bash
mvn exec:java -Dexec.mainClass="com.acme.modres.db.PostgreSQLConnectionTest"
```

Expected output:
```
INFO: Successfully connected to PostgreSQL database
INFO: Database Product: PostgreSQL
INFO: Database Version: 16.x
INFO: Connection Test: PASSED
INFO: Query Test: PASSED
INFO: Table Existence Test: PASSED
```

### Step 4: Update Application Server Configuration
1. Deploy PostgreSQL JDBC driver to your application server
2. Configure JNDI datasource (see Prerequisites section)
3. Update `ModResortsCustomerInformation.java` to uncomment the @Resource annotation:
   ```java
   @Resource(lookup = "jdbc/ModResortsJndi")
   private DataSource dataSource;
   ```

### Step 5: Build and Deploy
```bash
mvn clean package
# Deploy the generated WAR file to your application server
```

### Step 6: Verify Application
1. Start the application server
2. Access the application: `http://localhost:8080/modresorts/`
3. Test customer information retrieval functionality

## SQL Compatibility Notes

### Query Compatibility
The existing SQL query is already PostgreSQL-compatible:
```sql
SELECT INFO FROM CUSTOMER
```

### Case Sensitivity
- PostgreSQL converts unquoted identifiers to lowercase
- If your table is created as `CUSTOMER` (uppercase), queries will work
- If case-sensitive names are needed, use double quotes: `"CUSTOMER"`

### Data Types
Common SQL Server to PostgreSQL type mappings:
- `INT IDENTITY` → `SERIAL` or `BIGSERIAL`
- `NVARCHAR(n)` → `VARCHAR(n)` or `TEXT`
- `DATETIME` → `TIMESTAMP` or `TIMESTAMP WITH TIME ZONE`
- `BIT` → `BOOLEAN`
- `UNIQUEIDENTIFIER` → `UUID`

### Functions
Common function differences:
- `GETDATE()` → `CURRENT_TIMESTAMP` or `NOW()`
- `ISNULL(x, y)` → `COALESCE(x, y)`
- `LEN(x)` → `LENGTH(x)`
- `SUBSTRING(x, start, length)` → `SUBSTRING(x FROM start FOR length)`

## Troubleshooting

### Connection Issues
1. **Error**: "Connection refused"
   - Verify PostgreSQL is running: `systemctl status postgresql`
   - Check PostgreSQL is listening on correct port: `netstat -an | grep 5432`
   - Verify `pg_hba.conf` allows connections from your host

2. **Error**: "password authentication failed"
   - Verify username and password in `database.properties`
   - Check `pg_hba.conf` authentication method
   - Reset password if needed: `ALTER USER postgres PASSWORD 'newpassword';`

3. **Error**: "database does not exist"
   - Create database: `CREATE DATABASE modresorts;`
   - Verify database name in connection URL

### Table Not Found
1. Check if table exists:
   ```sql
   SELECT * FROM information_schema.tables WHERE table_name = 'customer';
   ```

2. Run migration script if table doesn't exist

3. Verify schema: PostgreSQL default schema is `public`

### Performance Issues
1. Ensure indexes are created (included in migration script)
2. Analyze tables: `ANALYZE CUSTOMER;`
3. Check query execution plan: `EXPLAIN ANALYZE SELECT INFO FROM CUSTOMER;`
4. Adjust connection pool settings in `database.properties`

## Rollback Plan

If migration needs to be rolled back:

1. **Keep SQL Server configuration** as backup
2. **Document current state** before migration
3. **Test rollback procedure** in non-production environment

To rollback:
1. Restore SQL Server JDBC driver in pom.xml
2. Revert DataSource configuration to SQL Server
3. Update connection strings
4. Redeploy application

## Performance Optimization

### Connection Pooling
The application uses Jakarta EE container-managed connection pooling. Recommended settings:

```properties
db.pool.initialSize=5
db.pool.maxTotal=20
db.pool.maxIdle=10
db.pool.minIdle=5
```

### Query Optimization
1. Use prepared statements (already implemented)
2. Create indexes on frequently queried columns
3. Use connection pooling
4. Enable query logging for analysis:
   ```properties
   # In postgresql.conf
   log_statement = 'all'
   log_duration = on
   ```

### PostgreSQL Configuration
Recommended `postgresql.conf` settings for application workload:

```conf
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

## Security Considerations

### 1. Connection Security
- Use SSL/TLS for production: `db.postgresql.ssl=true`
- Configure `sslmode=require` or `sslmode=verify-full`
- Store credentials securely (use environment variables or secrets management)

### 2. Database Security
- Create dedicated application user with minimal privileges
- Use strong passwords
- Restrict network access in `pg_hba.conf`
- Enable audit logging

### 3. Application Security
- Never log passwords or sensitive data
- Use parameterized queries (already implemented)
- Implement proper error handling
- Validate and sanitize all inputs

## Testing Checklist

- [ ] PostgreSQL 16 installed and running
- [ ] Database and user created
- [ ] Migration script executed successfully
- [ ] Connection test passes
- [ ] Application server configured with PostgreSQL datasource
- [ ] Application builds without errors
- [ ] Application deploys successfully
- [ ] Customer information retrieval works
- [ ] No SQL errors in application logs
- [ ] Performance is acceptable
- [ ] Connection pooling is working
- [ ] Error handling works correctly

## Support and Resources

### PostgreSQL Documentation
- Official Documentation: https://www.postgresql.org/docs/16/
- JDBC Driver: https://jdbc.postgresql.org/documentation/

### Migration Tools
- Flyway: https://flywaydb.org/
- Liquibase: https://www.liquibase.org/

### Community Support
- PostgreSQL Mailing Lists: https://www.postgresql.org/list/
- Stack Overflow: https://stackoverflow.com/questions/tagged/postgresql

## Conclusion

This migration guide provides comprehensive instructions for migrating the ModResorts application from SQL Server to PostgreSQL 16. The changes are minimal due to the application's simple database usage, making the migration straightforward.

Key benefits of PostgreSQL:
- Open source and cost-effective
- Excellent performance and scalability
- Strong ACID compliance
- Rich feature set
- Active community support

For questions or issues, consult the troubleshooting section or reach out to your database administrator.
