# Docker Setup for PostgreSQL - ModResorts Application

## Quick Start with Docker

This guide helps you set up PostgreSQL 16 using Docker for the ModResorts application.

## Prerequisites

- Docker installed (version 20.10+)
- Docker Compose installed (version 2.0+)

### Install Docker

**Ubuntu/Debian:**
```bash
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER
```

**macOS:**
```bash
brew install --cask docker
```

**Windows:**
Download Docker Desktop from https://www.docker.com/products/docker-desktop

## Starting PostgreSQL with Docker Compose

### 1. Start Services
```bash
# Start PostgreSQL and pgAdmin
docker-compose up -d

# Check if containers are running
docker-compose ps
```

Expected output:
```
NAME                    STATUS              PORTS
modresorts-postgres     Up (healthy)        0.0.0.0:5432->5432/tcp
modresorts-pgadmin      Up                  0.0.0.0:5050->80/tcp
```

### 2. Verify PostgreSQL is Running
```bash
# Check PostgreSQL logs
docker-compose logs postgres

# Test connection
docker exec -it modresorts-postgres psql -U postgres -d modresorts -c "SELECT version();"
```

### 3. Initialize Database Schema
The schema is automatically initialized from `src/main/resources/db/postgresql/schema.sql` when the container starts for the first time.

Verify the schema:
```bash
docker exec -it modresorts-postgres psql -U postgres -d modresorts -c "\dt"
```

### 4. Verify Sample Data
```bash
docker exec -it modresorts-postgres psql -U postgres -d modresorts -c "SELECT * FROM customer;"
```

## Accessing PostgreSQL

### Option 1: Command Line (psql)
```bash
# Connect to PostgreSQL
docker exec -it modresorts-postgres psql -U postgres -d modresorts

# Inside psql:
\dt              # List tables
\d customer      # Describe customer table
SELECT * FROM customer;
\q               # Quit
```

### Option 2: pgAdmin Web Interface
1. Open browser: http://localhost:5050
2. Login credentials:
   - Email: `admin@modresorts.com`
   - Password: `admin`
3. Add server:
   - Name: `ModResorts PostgreSQL`
   - Host: `postgres` (container name)
   - Port: `5432`
   - Database: `modresorts`
   - Username: `postgres`
   - Password: `postgres`

### Option 3: From Your Application
Update connection string in `PostgreSQLDataSourceConfig.java`:
```java
config.setJdbcUrl("jdbc:postgresql://localhost:5432/modresorts");
config.setUsername("postgres");
config.setPassword("postgres");
```

## Docker Compose Commands

### Start Services
```bash
# Start in background
docker-compose up -d

# Start with logs
docker-compose up

# Start only PostgreSQL (without pgAdmin)
docker-compose up -d postgres
```

### Stop Services
```bash
# Stop all services
docker-compose stop

# Stop and remove containers
docker-compose down

# Stop and remove containers + volumes (WARNING: deletes data)
docker-compose down -v
```

### View Logs
```bash
# All services
docker-compose logs

# Follow logs
docker-compose logs -f

# PostgreSQL only
docker-compose logs postgres

# Last 100 lines
docker-compose logs --tail=100
```

### Restart Services
```bash
# Restart all
docker-compose restart

# Restart PostgreSQL only
docker-compose restart postgres
```

## Database Management

### Backup Database
```bash
# Backup to SQL file
docker exec modresorts-postgres pg_dump -U postgres modresorts > backup.sql

# Backup to compressed file
docker exec modresorts-postgres pg_dump -U postgres modresorts | gzip > backup.sql.gz
```

### Restore Database
```bash
# Restore from SQL file
docker exec -i modresorts-postgres psql -U postgres modresorts < backup.sql

# Restore from compressed file
gunzip -c backup.sql.gz | docker exec -i modresorts-postgres psql -U postgres modresorts
```

### Reset Database
```bash
# Drop and recreate database
docker exec -it modresorts-postgres psql -U postgres -c "DROP DATABASE IF EXISTS modresorts;"
docker exec -it modresorts-postgres psql -U postgres -c "CREATE DATABASE modresorts;"

# Reinitialize schema
docker exec -i modresorts-postgres psql -U postgres modresorts < src/main/resources/db/postgresql/schema.sql
```

### Execute SQL Scripts
```bash
# Execute SQL file
docker exec -i modresorts-postgres psql -U postgres modresorts < your-script.sql

# Execute SQL command
docker exec -it modresorts-postgres psql -U postgres -d modresorts -c "SELECT COUNT(*) FROM customer;"
```

## Troubleshooting

### Container Won't Start
```bash
# Check logs
docker-compose logs postgres

# Check if port 5432 is already in use
sudo netstat -plnt | grep 5432
# or
sudo lsof -i :5432

# If port is in use, stop the conflicting service or change port in docker-compose.yml
```

### Connection Refused
```bash
# Check if container is running
docker-compose ps

# Check if PostgreSQL is ready
docker exec modresorts-postgres pg_isready -U postgres

# Restart container
docker-compose restart postgres
```

### Permission Denied
```bash
# Fix volume permissions
sudo chown -R $USER:$USER postgres_data/

# Or remove volumes and recreate
docker-compose down -v
docker-compose up -d
```

### Database Not Initialized
```bash
# Check initialization logs
docker-compose logs postgres | grep "database system is ready"

# Manually run schema script
docker exec -i modresorts-postgres psql -U postgres modresorts < src/main/resources/db/postgresql/schema.sql
```

### Out of Memory
```bash
# Check container resources
docker stats modresorts-postgres

# Increase Docker memory limit in Docker Desktop settings
# Or add to docker-compose.yml:
# services:
#   postgres:
#     mem_limit: 2g
```

## Performance Tuning

### Monitor Performance
```bash
# Check active connections
docker exec -it modresorts-postgres psql -U postgres -d modresorts -c "SELECT count(*) FROM pg_stat_activity;"

# Check database size
docker exec -it modresorts-postgres psql -U postgres -d modresorts -c "SELECT pg_size_pretty(pg_database_size('modresorts'));"

# Check table sizes
docker exec -it modresorts-postgres psql -U postgres -d modresorts -c "SELECT schemaname, tablename, pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) FROM pg_tables WHERE schemaname = 'public';"
```

### Optimize PostgreSQL Configuration
Edit `docker-compose.yml` to add PostgreSQL configuration:
```yaml
services:
  postgres:
    command:
      - "postgres"
      - "-c"
      - "max_connections=200"
      - "-c"
      - "shared_buffers=256MB"
      - "-c"
      - "effective_cache_size=1GB"
      - "-c"
      - "maintenance_work_mem=64MB"
      - "-c"
      - "checkpoint_completion_target=0.9"
      - "-c"
      - "wal_buffers=16MB"
      - "-c"
      - "default_statistics_target=100"
```

## Production Considerations

### Security
```yaml
# Use secrets for passwords
services:
  postgres:
    environment:
      POSTGRES_PASSWORD_FILE: /run/secrets/postgres_password
    secrets:
      - postgres_password

secrets:
  postgres_password:
    file: ./secrets/postgres_password.txt
```

### Persistent Data
```bash
# Backup volumes regularly
docker run --rm -v modresorts_postgres_data:/data -v $(pwd):/backup alpine tar czf /backup/postgres_backup.tar.gz /data

# Restore volumes
docker run --rm -v modresorts_postgres_data:/data -v $(pwd):/backup alpine tar xzf /backup/postgres_backup.tar.gz -C /
```

### Health Checks
The docker-compose.yml includes health checks. Monitor them:
```bash
docker inspect modresorts-postgres | grep -A 10 Health
```

## Development Workflow

### 1. Start Development Environment
```bash
docker-compose up -d
```

### 2. Run Application
```bash
mvn clean package
# Deploy WAR to application server
```

### 3. Test Database Connection
```bash
curl http://localhost:8080/modresorts/health/db
```

### 4. Make Schema Changes
```bash
# Edit schema.sql
# Recreate database
docker-compose down -v
docker-compose up -d
```

### 5. Stop Development Environment
```bash
docker-compose stop
```

## Useful Commands Reference

```bash
# Start services
docker-compose up -d

# Stop services
docker-compose stop

# View logs
docker-compose logs -f postgres

# Connect to PostgreSQL
docker exec -it modresorts-postgres psql -U postgres -d modresorts

# Execute SQL
docker exec -it modresorts-postgres psql -U postgres -d modresorts -c "YOUR SQL HERE"

# Backup database
docker exec modresorts-postgres pg_dump -U postgres modresorts > backup.sql

# Restore database
docker exec -i modresorts-postgres psql -U postgres modresorts < backup.sql

# Check container status
docker-compose ps

# Remove everything (including data)
docker-compose down -v
```

## Next Steps

1. ✅ Start PostgreSQL with Docker Compose
2. ✅ Verify database initialization
3. ✅ Update application configuration
4. ✅ Build and deploy application
5. ✅ Test database connectivity
6. ✅ Run application tests

## Resources

- Docker Documentation: https://docs.docker.com/
- PostgreSQL Docker Image: https://hub.docker.com/_/postgres
- pgAdmin Documentation: https://www.pgadmin.org/docs/
- Docker Compose Documentation: https://docs.docker.com/compose/

---

**Quick Start Summary:**
```bash
# 1. Start PostgreSQL
docker-compose up -d

# 2. Verify it's running
docker-compose ps

# 3. Check database
docker exec -it modresorts-postgres psql -U postgres -d modresorts -c "SELECT * FROM customer;"

# 4. Access pgAdmin
# Open http://localhost:5050 in browser

# 5. Build and run your application
mvn clean package
```

That's it! Your PostgreSQL database is ready for development.
