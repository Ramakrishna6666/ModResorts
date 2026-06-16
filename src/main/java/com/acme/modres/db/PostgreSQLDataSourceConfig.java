package com.acme.modres.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * PostgreSQL Database Configuration Manager.
 * Provides connection pooling using HikariCP for optimal performance.
 * This class follows the singleton pattern for centralized database configuration.
 */
public class PostgreSQLDataSourceConfig {
    private static final Logger LOGGER = Logger.getLogger(PostgreSQLDataSourceConfig.class.getName());
    private static final String DB_PROPERTIES_FILE = "database.properties";
    
    private static HikariDataSource dataSource;
    private static volatile PostgreSQLDataSourceConfig instance;
    
    private PostgreSQLDataSourceConfig() {
        initializeDataSource();
    }
    
    /**
     * Gets the singleton instance of the configuration manager.
     * Thread-safe double-checked locking implementation.
     * 
     * @return PostgreSQLDataSourceConfig instance
     */
    public static PostgreSQLDataSourceConfig getInstance() {
        if (instance == null) {
            synchronized (PostgreSQLDataSourceConfig.class) {
                if (instance == null) {
                    instance = new PostgreSQLDataSourceConfig();
                }
            }
        }
        return instance;
    }
    
    /**
     * Initializes the HikariCP connection pool with PostgreSQL configuration.
     */
    private void initializeDataSource() {
        try {
            Properties props = loadDatabaseProperties();
            
            HikariConfig config = new HikariConfig();
            
            // Basic connection properties
            config.setJdbcUrl(props.getProperty("db.url", "jdbc:postgresql://localhost:5432/modresorts"));
            config.setUsername(props.getProperty("db.username", "postgres"));
            config.setPassword(props.getProperty("db.password", ""));
            config.setDriverClassName(props.getProperty("db.driver", "org.postgresql.Driver"));
            
            // Connection pool settings
            config.setMaximumPoolSize(Integer.parseInt(props.getProperty("db.pool.maximumPoolSize", "10")));
            config.setMinimumIdle(Integer.parseInt(props.getProperty("db.pool.minimumIdle", "5")));
            config.setConnectionTimeout(Long.parseLong(props.getProperty("db.pool.connectionTimeout", "30000")));
            config.setIdleTimeout(Long.parseLong(props.getProperty("db.pool.idleTimeout", "600000")));
            config.setMaxLifetime(Long.parseLong(props.getProperty("db.pool.maxLifetime", "1800000")));
            
            // PostgreSQL specific optimizations
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");
            config.addDataSourceProperty("reWriteBatchedInserts", "true");
            
            // Application name for PostgreSQL monitoring
            config.addDataSourceProperty("ApplicationName", 
                props.getProperty("db.application.name", "ModResorts"));
            
            // Schema configuration
            String schema = props.getProperty("db.postgresql.schema", "public");
            config.setSchema(schema);
            
            // Connection validation
            config.setConnectionTestQuery("SELECT 1");
            config.setValidationTimeout(5000);
            
            // Pool name for monitoring
            config.setPoolName("ModResortsPostgreSQLPool");
            
            // Auto-commit setting
            config.setAutoCommit(Boolean.parseBoolean(
                props.getProperty("db.connection.autoCommit", "false")));
            
            dataSource = new HikariDataSource(config);
            
            LOGGER.log(Level.INFO, "PostgreSQL connection pool initialized successfully");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize PostgreSQL connection pool", e);
            throw new RuntimeException("Database configuration failed", e);
        }
    }
    
    /**
     * Loads database properties from the configuration file.
     * 
     * @return Properties object containing database configuration
     * @throws IOException if properties file cannot be loaded
     */
    private Properties loadDatabaseProperties() throws IOException {
        Properties props = new Properties();
        
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream(DB_PROPERTIES_FILE)) {
            
            if (input == null) {
                LOGGER.log(Level.WARNING, 
                    "Database properties file not found, using defaults");
                return props;
            }
            
            props.load(input);
            LOGGER.log(Level.INFO, "Database properties loaded successfully");
            
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, 
                "Error loading database properties, using defaults", e);
        }
        
        return props;
    }
    
    /**
     * Gets the configured DataSource for database connections.
     * 
     * @return HikariDataSource instance
     */
    public DataSource getDataSource() {
        if (dataSource == null || dataSource.isClosed()) {
            throw new IllegalStateException("DataSource is not initialized or has been closed");
        }
        return dataSource;
    }
    
    /**
     * Closes the connection pool and releases all resources.
     * Should be called during application shutdown.
     */
    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            LOGGER.log(Level.INFO, "PostgreSQL connection pool closed successfully");
        }
    }
    
    /**
     * Gets connection pool statistics for monitoring.
     * 
     * @return String containing pool statistics
     */
    public String getPoolStats() {
        if (dataSource != null && !dataSource.isClosed()) {
            return String.format(
                "Pool Stats - Active: %d, Idle: %d, Total: %d, Waiting: %d",
                dataSource.getHikariPoolMXBean().getActiveConnections(),
                dataSource.getHikariPoolMXBean().getIdleConnections(),
                dataSource.getHikariPoolMXBean().getTotalConnections(),
                dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection()
            );
        }
        return "Pool not initialized";
    }
}
