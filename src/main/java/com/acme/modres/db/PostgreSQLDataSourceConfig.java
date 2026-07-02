package com.acme.modres.db;

import javax.sql.DataSource;
import org.postgresql.ds.PGSimpleDataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * PostgreSQL DataSource Configuration Helper
 * Provides centralized database connection configuration for PostgreSQL
 */
public class PostgreSQLDataSourceConfig {
    
    private static final String DB_PROPERTIES_FILE = "database.properties";
    private static DataSource dataSource;
    
    /**
     * Get configured PostgreSQL DataSource
     * @return DataSource configured for PostgreSQL
     */
    public static DataSource getDataSource() {
        if (dataSource == null) {
            synchronized (PostgreSQLDataSourceConfig.class) {
                if (dataSource == null) {
                    dataSource = createDataSource();
                }
            }
        }
        return dataSource;
    }
    
    /**
     * Create PostgreSQL DataSource from properties file
     * @return Configured PGSimpleDataSource
     */
    private static DataSource createDataSource() {
        Properties props = loadProperties();
        
        PGSimpleDataSource pgDataSource = new PGSimpleDataSource();
        
        // Parse JDBC URL to extract host, port, and database
        String jdbcUrl = props.getProperty("db.url", "jdbc:postgresql://localhost:5432/modresorts");
        parseAndSetConnectionParams(pgDataSource, jdbcUrl);
        
        // Set authentication
        pgDataSource.setUser(props.getProperty("db.username", "postgres"));
        pgDataSource.setPassword(props.getProperty("db.password", "postgres"));
        
        // Set schema
        String schema = props.getProperty("db.schema", "public");
        pgDataSource.setCurrentSchema(schema);
        
        // Set SSL mode
        String sslMode = props.getProperty("db.ssl.mode", "prefer");
        pgDataSource.setSslMode(sslMode);
        
        // Set connection timeout (convert from milliseconds to seconds)
        String timeoutStr = props.getProperty("db.connection.timeout", "30000");
        int timeoutSeconds = Integer.parseInt(timeoutStr) / 1000;
        pgDataSource.setConnectTimeout(timeoutSeconds);
        
        // Set socket timeout
        String socketTimeoutStr = props.getProperty("db.socket.timeout", "30000");
        int socketTimeoutSeconds = Integer.parseInt(socketTimeoutStr) / 1000;
        pgDataSource.setSocketTimeout(socketTimeoutSeconds);
        
        return pgDataSource;
    }
    
    /**
     * Parse JDBC URL and set connection parameters
     * Format: jdbc:postgresql://host:port/database
     */
    private static void parseAndSetConnectionParams(PGSimpleDataSource dataSource, String jdbcUrl) {
        try {
            // Remove jdbc:postgresql:// prefix
            String urlWithoutPrefix = jdbcUrl.substring("jdbc:postgresql://".length());
            
            // Split by / to separate host:port from database
            String[] parts = urlWithoutPrefix.split("/");
            String hostPort = parts[0];
            String database = parts.length > 1 ? parts[1] : "modresorts";
            
            // Split host and port
            String[] hostPortParts = hostPort.split(":");
            String host = hostPortParts[0];
            int port = hostPortParts.length > 1 ? Integer.parseInt(hostPortParts[1]) : 5432;
            
            dataSource.setServerNames(new String[]{host});
            dataSource.setPortNumbers(new int[]{port});
            dataSource.setDatabaseName(database);
            
        } catch (Exception e) {
            // Use defaults if parsing fails
            dataSource.setServerNames(new String[]{"localhost"});
            dataSource.setPortNumbers(new int[]{5432});
            dataSource.setDatabaseName("modresorts");
        }
    }
    
    /**
     * Load database properties from classpath
     * @return Properties object with database configuration
     */
    private static Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream input = PostgreSQLDataSourceConfig.class
                .getClassLoader()
                .getResourceAsStream(DB_PROPERTIES_FILE)) {
            
            if (input != null) {
                props.load(input);
            } else {
                System.err.println("Unable to find " + DB_PROPERTIES_FILE + ", using defaults");
            }
        } catch (IOException e) {
            System.err.println("Error loading database properties: " + e.getMessage());
            e.printStackTrace();
        }
        return props;
    }
    
    /**
     * Test database connection
     * @return true if connection successful, false otherwise
     */
    public static boolean testConnection() {
        try {
            DataSource ds = getDataSource();
            ds.getConnection().close();
            return true;
        } catch (Exception e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            return false;
        }
    }
}
