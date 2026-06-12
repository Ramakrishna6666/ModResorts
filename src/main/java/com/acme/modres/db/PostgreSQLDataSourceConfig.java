package com.acme.modres.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * PostgreSQL DataSource Configuration
 * Provides connection pooling using HikariCP for PostgreSQL database
 */
@Singleton
@Startup
public class PostgreSQLDataSourceConfig {
    private static final Logger logger = Logger.getLogger(PostgreSQLDataSourceConfig.class.getName());
    private static final String DB_PROPERTIES_FILE = "database.properties";
    
    private HikariDataSource dataSource;
    
    @PostConstruct
    public void init() {
        try {
            Properties props = loadDatabaseProperties();
            HikariConfig config = new HikariConfig();
            
            // Basic connection properties
            config.setJdbcUrl(props.getProperty("db.url"));
            config.setUsername(props.getProperty("db.username"));
            config.setPassword(props.getProperty("db.password"));
            config.setDriverClassName(props.getProperty("db.driver"));
            
            // Connection pool settings
            config.setMaximumPoolSize(Integer.parseInt(props.getProperty("db.pool.maximumPoolSize", "10")));
            config.setMinimumIdle(Integer.parseInt(props.getProperty("db.pool.minimumIdle", "5")));
            config.setConnectionTimeout(Long.parseLong(props.getProperty("db.pool.connectionTimeout", "30000")));
            config.setIdleTimeout(Long.parseLong(props.getProperty("db.pool.idleTimeout", "600000")));
            config.setMaxLifetime(Long.parseLong(props.getProperty("db.pool.maxLifetime", "1800000")));
            
            // PostgreSQL-specific optimizations
            config.addDataSourceProperty("cachePrepStmts", props.getProperty("db.pool.dataSource.cachePrepStmts", "true"));
            config.addDataSourceProperty("prepStmtCacheSize", props.getProperty("db.pool.dataSource.prepStmtCacheSize", "250"));
            config.addDataSourceProperty("prepStmtCacheSqlLimit", props.getProperty("db.pool.dataSource.prepStmtCacheSqlLimit", "2048"));
            config.addDataSourceProperty("useServerPrepStmts", props.getProperty("db.pool.dataSource.useServerPrepStmts", "true"));
            
            // Pool name for monitoring
            config.setPoolName("ModResortsPostgreSQLPool");
            
            // Create the datasource
            dataSource = new HikariDataSource(config);
            
            logger.info("PostgreSQL DataSource initialized successfully");
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to initialize PostgreSQL DataSource", e);
            throw new RuntimeException("Failed to initialize PostgreSQL DataSource", e);
        }
    }
    
    @PreDestroy
    public void destroy() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("PostgreSQL DataSource closed successfully");
        }
    }
    
    public DataSource getDataSource() {
        return dataSource;
    }
    
    private Properties loadDatabaseProperties() throws IOException {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(DB_PROPERTIES_FILE)) {
            if (input == null) {
                throw new IOException("Unable to find " + DB_PROPERTIES_FILE);
            }
            props.load(input);
        }
        return props;
    }
}
