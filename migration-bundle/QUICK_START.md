# Quick Start Guide - PostgreSQL Migration

## Prerequisites
- Docker and Docker Compose installed
- Java 21 JDK installed
- Maven 3.8+ installed
- Git (optional)

## Quick Setup (5 minutes)

### Step 1: Start PostgreSQL with Docker
```bash
cd migration-bundle
docker-compose up -d postgres
```

This will:
- Start PostgreSQL 16 on port 5432
- Create database `modresorts`
- Create user `modresorts_user` with password `modresorts_pass`
- Automatically run the migration script
- Configure optimal PostgreSQL settings

### Step 2: Verify Database is Running
```bash
docker-compose ps
docker-compose logs postgres
```

Expected output: `database system is ready to accept connections`

### Step 3: Update Configuration
Edit `src/main/resources/database.properties`:
```properties
db.url=jdbc:postgresql://localhost:5432/modresorts
db.username=modresorts_user
db.password=modresorts_pass
```

### Step 4: Build the Application
```bash
cd ..
mvn clean package
```

### Step 5: Test Database Connection
```bash
# Run the connection test
mvn exec:java -Dexec.mainClass="com.acme.modres.db.DatabaseConnectionTest"
```

Expected output: `All Database Tests PASSED`

## Optional: Use pgAdmin

### Start pgAdmin
```bash
cd migration-bundle
docker-compose up -d pgadmin
```

### Access pgAdmin
1. Open browser: http://localhost:5050
2. Login:
   - Email: `admin@modresorts.com`
   - Password: `admin`
3. Add Server:
   - Name: `ModResorts`
   - Host: `postgres` (or `localhost` if connecting from host)
   - Port: `5432`
   - Database: `modresorts`
   - Username: `modresorts_user`
   - Password: `modresorts_pass`

## Verify Migration

### Check Tables
```bash
docker exec -it modresorts-postgres psql -U modresorts_user -d modresorts -c "\dt"
```

### Check Data
```bash
docker exec -it modresorts-postgres psql -U modresorts_user -d modresorts -c "SELECT * FROM customer;"
```

### Check Indexes
```bash
docker exec -it modresorts-postgres psql -U modresorts_user -d modresorts -c "\di"
```

## Common Commands

### Stop Services
```bash
cd migration-bundle
docker-compose down
```

### Stop and Remove Data
```bash
docker-compose down -v
```

### View Logs
```bash
docker-compose logs -f postgres
```

### Backup Database
```bash
docker-compose run --rm postgres-backup
```

### Restore Database
```bash
docker exec -i modresorts-postgres pg_restore -U modresorts_user -d modresorts < backups/modresorts_YYYYMMDD_HHMMSS.backup
```

### Connect to PostgreSQL CLI
```bash
docker exec -it modresorts-postgres psql -U modresorts_user -d modresorts
```

## Troubleshooting

### Port Already in Use
If port 5432 is already in use, edit `docker-compose.yml`:
```yaml
ports:
  - "5433:5432"  # Change host port to 5433
```

Then update `database.properties`:
```properties
db.url=jdbc:postgresql://localhost:5433/modresorts
```

### Connection Refused
```bash
# Check if container is running
docker-compose ps

# Check logs
docker-compose logs postgres

# Restart services
docker-compose restart
```

### Permission Denied
```bash
# Fix volume permissions
sudo chown -R $(id -u):$(id -g) postgres_data/
```

## Production Deployment

For production, update the following:

1. **Change Passwords** in `docker-compose.yml`:
   ```yaml
   POSTGRES_PASSWORD: your_secure_password_here
   ```

2. **Enable SSL** in `database.properties`:
   ```properties
   db.postgresql.ssl=true
   db.postgresql.sslmode=require
   ```

3. **Configure Backups**:
   ```bash
   # Add to crontab
   0 2 * * * cd /path/to/migration-bundle && docker-compose run --rm postgres-backup
   ```

4. **Monitor Performance**:
   ```sql
   -- Enable pg_stat_statements
   CREATE EXTENSION IF NOT EXISTS pg_stat_statements;
   ```

## Next Steps

1. Deploy application to your Jakarta EE server (WildFly, Tomcat, etc.)
2. Configure JNDI DataSource (see POSTGRESQL_MIGRATION_README.md)
3. Run integration tests
4. Monitor performance
5. Set up automated backups

## Support

- PostgreSQL Docs: https://www.postgresql.org/docs/16/
- Docker Compose Docs: https://docs.docker.com/compose/
- HikariCP Docs: https://github.com/brettwooldridge/HikariCP

## Clean Up

To completely remove everything:
```bash
cd migration-bundle
docker-compose down -v
docker rmi postgres:16-alpine dpage/pgadmin4:latest
```
