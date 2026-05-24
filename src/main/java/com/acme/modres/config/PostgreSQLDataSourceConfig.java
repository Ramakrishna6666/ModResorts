package com.acme.modres.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import javax.sql.DataSource;

/**
 * PostgreSQL DataSource Configuration using HikariCP connection pool.
 * This configuration provides optimized connection pooling for PostgreSQL database.
 */
@Singleton
@Startup
public class PostgreSQLDataSourceConfig {
    
    private HikariDataSource dataSource;
    
    @PostConstruct
    public void init() {
        HikariConfig config = new HikariConfig();
        
        // PostgreSQL connection configuration
        // Update these values based on your PostgreSQL server configuration
        config.setJdbcUrl("jdbc:postgresql://localhost:5432/modresorts");
        config.setUsername("postgres");
        config.setPassword("postgres");
        config.setDriverClassName("org.postgresql.Driver");
        
        // Connection pool settings optimized for PostgreSQL
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000); // 30 seconds
        config.setIdleTimeout(600000); // 10 minutes
        config.setMaxLifetime(1800000); // 30 minutes
        
        // PostgreSQL specific optimizations
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        
        // Connection validation
        config.setConnectionTestQuery("SELECT 1");
        config.setValidationTimeout(5000);
        
        // Pool name for monitoring
        config.setPoolName("ModResortsPostgreSQLPool");
        
        this.dataSource = new HikariDataSource(config);
    }
    
    public DataSource getDataSource() {
        return dataSource;
    }
    
    @PreDestroy
    public void destroy() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
