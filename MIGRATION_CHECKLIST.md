# PostgreSQL Migration Checklist

## Pre-Migration Phase

### Assessment
- [x] Identify all database-dependent code
- [x] Document current SQL Server configuration
- [x] Review SQL queries for SQL Server-specific syntax
- [x] Identify stored procedures and functions
- [x] Document data types used
- [x] Review connection string configurations

### Planning
- [x] Choose migration approach (programmatic vs JNDI)
- [x] Plan downtime window (if applicable)
- [x] Prepare rollback strategy
- [x] Set up test environment
- [x] Document migration steps

## Migration Phase

### 1. Environment Setup
- [ ] Install PostgreSQL 16
- [ ] Configure PostgreSQL server
- [ ] Create database and user accounts
- [ ] Configure pg_hba.conf for authentication
- [ ] Test PostgreSQL connectivity
- [ ] Set up backup strategy

### 2. Dependency Updates
- [x] Add PostgreSQL JDBC driver to pom.xml (42.7.1)
- [x] Add HikariCP for connection pooling (5.1.0)
- [x] Remove SQL Server dependencies (if any)
- [x] Update Maven build configuration
- [ ] Run `mvn clean install` to verify dependencies

### 3. Configuration Files
- [x] Create database.properties with PostgreSQL settings
- [x] Configure connection pool parameters
- [x] Set up JNDI configuration examples
- [x] Update environment-specific configurations
- [ ] Configure SSL/TLS for production (if needed)

### 4. Code Changes

#### DataSource Configuration
- [x] Create PostgreSQLDataSourceConfig class
- [x] Implement HikariCP connection pooling
- [x] Add lifecycle management (@PostConstruct, @PreDestroy)
- [x] Add error handling and logging

#### SQL Query Updates
- [x] Convert table names to lowercase
- [x] Convert column names to lowercase
- [x] Add explicit schema references (public.)
- [x] Update SQL Server-specific functions
- [x] Replace TOP with LIMIT
- [x] Update date/time functions
- [x] Fix string concatenation operators

#### Entity/Model Updates
- [x] Update ModResortsCustomerInformation class
- [x] Implement try-with-resources for resource management
- [x] Add proper exception handling
- [x] Update column name references
- [x] Add logging statements

#### Utility Classes
- [x] Create PostgreSQLUtils helper class
- [x] Add connection testing methods
- [x] Add table existence checks
- [x] Add naming convention converters
- [x] Add database maintenance utilities

### 5. Database Schema
- [x] Create PostgreSQL initialization script
- [x] Convert data types (IDENTITY → SERIAL, etc.)
- [x] Create tables with proper constraints
- [x] Add indexes for performance
- [x] Create triggers and functions
- [x] Insert sample data
- [ ] Run initialization script
- [ ] Verify schema creation

### 6. Data Migration (if applicable)
- [ ] Export data from SQL Server
- [ ] Transform data format if needed
- [ ] Import data to PostgreSQL
- [ ] Verify data integrity
- [ ] Check row counts match
- [ ] Validate data types

### 7. Testing

#### Unit Testing
- [ ] Test database connection
- [ ] Test CRUD operations
- [ ] Test transaction handling
- [ ] Test error scenarios
- [ ] Test connection pool behavior

#### Integration Testing
- [ ] Test application startup
- [ ] Test all database-dependent features
- [ ] Test concurrent access
- [ ] Test connection pool under load
- [ ] Test failover scenarios

#### Performance Testing
- [ ] Benchmark query performance
- [ ] Test connection pool efficiency
- [ ] Monitor memory usage
- [ ] Check for connection leaks
- [ ] Optimize slow queries

### 8. Documentation
- [x] Create migration guide
- [x] Document configuration options
- [x] Create README for database setup
- [x] Document troubleshooting steps
- [x] Create JNDI configuration examples
- [x] Document SQL syntax differences

## Post-Migration Phase

### Validation
- [ ] Verify all features work correctly
- [ ] Check application logs for errors
- [ ] Verify data integrity
- [ ] Test all user workflows
- [ ] Validate performance metrics

### Monitoring Setup
- [x] Create health check endpoint (/db-health)
- [ ] Set up database monitoring
- [ ] Configure connection pool monitoring
- [ ] Set up alerting for issues
- [ ] Monitor query performance

### Optimization
- [ ] Run ANALYZE on all tables
- [ ] Create additional indexes if needed
- [ ] Tune PostgreSQL configuration
- [ ] Optimize connection pool settings
- [ ] Review and optimize slow queries

### Production Deployment
- [ ] Back up SQL Server database
- [ ] Schedule maintenance window
- [ ] Deploy application to staging
- [ ] Run full test suite in staging
- [ ] Deploy to production
- [ ] Monitor application closely
- [ ] Verify all functionality

### Post-Deployment
- [ ] Monitor application performance
- [ ] Check error logs
- [ ] Verify database connections
- [ ] Monitor connection pool metrics
- [ ] Collect user feedback
- [ ] Document any issues and resolutions

## Rollback Plan

### If Issues Occur
- [ ] Stop application
- [ ] Restore SQL Server configuration
- [ ] Revert code changes
- [ ] Redeploy previous version
- [ ] Verify application functionality
- [ ] Document issues for analysis

## Maintenance Tasks

### Daily
- [ ] Monitor application logs
- [ ] Check database connections
- [ ] Review error rates
- [ ] Monitor performance metrics

### Weekly
- [ ] Run VACUUM on tables
- [ ] Run ANALYZE for statistics
- [ ] Review slow query log
- [ ] Check disk space usage
- [ ] Review connection pool metrics

### Monthly
- [ ] Full database backup
- [ ] Review and optimize indexes
- [ ] Update PostgreSQL if needed
- [ ] Review security settings
- [ ] Performance tuning review

## Success Criteria

### Functional
- [ ] All features work as expected
- [ ] No data loss or corruption
- [ ] All queries return correct results
- [ ] Transactions work properly
- [ ] Error handling works correctly

### Performance
- [ ] Response times meet SLA
- [ ] Database queries perform well
- [ ] Connection pool is efficient
- [ ] No connection leaks
- [ ] Memory usage is acceptable

### Operational
- [ ] Monitoring is in place
- [ ] Backups are working
- [ ] Documentation is complete
- [ ] Team is trained
- [ ] Support processes are ready

## Notes and Issues

### Known Issues
- None identified yet

### Lessons Learned
- Document lessons learned during migration
- Update this checklist based on experience
- Share knowledge with team

### Future Improvements
- Consider implementing read replicas
- Evaluate connection pooling alternatives
- Review query optimization opportunities
- Consider implementing caching layer

## Sign-off

- [ ] Development Team Lead: _________________ Date: _______
- [ ] Database Administrator: _________________ Date: _______
- [ ] QA Team Lead: _________________ Date: _______
- [ ] Operations Manager: _________________ Date: _______
- [ ] Project Manager: _________________ Date: _______

## References

- [PostgreSQL Migration Guide](POSTGRESQL_MIGRATION_GUIDE.md)
- [Database Setup README](DATABASE_MIGRATION_README.md)
- [JNDI Configuration Examples](src/main/resources/jndi-datasource-examples.xml)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/16/)
