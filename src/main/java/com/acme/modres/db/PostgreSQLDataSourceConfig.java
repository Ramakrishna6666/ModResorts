package com.acme.modres.db;

import org.postgresql.ds.PGSimpleDataSource;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * PostgreSQL DataSource Configuration Helper
 * Provides utility methods to create and configure PostgreSQL DataSource
 */
public class PostgreSQLDataSourceConfig {
    private static final Logger logger = Logger.getLogger(PostgreSQLDataSourceConfig.class.getName());
    private static final String DB_PROPERTIES_FILE = "database.properties";
    
    /**
     * Creates a PostgreSQL DataSource from properties file
     * 
     * @return configured PostgreSQL DataSource
     */
    public static DataSource createDataSource() {
        Properties props = loadDatabaseProperties();
        return createDataSource(props);
    }
    
    /**
     * Creates a PostgreSQL DataSource with provided properties
     * 
     * @param props database properties
     * @return configured PostgreSQL DataSource
     */
    public static DataSource createDataSource(Properties props) {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        
        try {
            // Extract host and port from JDBC URL
            String jdbcUrl = props.getProperty("db.url", "jdbc:postgresql://localhost:5432/modresorts");
            String[] urlParts = parseJdbcUrl(jdbcUrl);
            
            dataSource.setServerNames(new String[]{urlParts[0]});
            dataSource.setPortNumbers(new int[]{Integer.parseInt(urlParts[1])});
            dataSource.setDatabaseName(urlParts[2]);
            
            // Set authentication
            dataSource.setUser(props.getProperty("db.username", "postgres"));
            dataSource.setPassword(props.getProperty("db.password", "postgres"));
            
            // PostgreSQL-specific settings
            dataSource.setSsl(Boolean.parseBoolean(props.getProperty("db.postgresql.ssl", "false")));
            dataSource.setSslMode(props.getProperty("db.postgresql.sslmode", "prefer"));
            dataSource.setConnectTimeout(Integer.parseInt(props.getProperty("db.postgresql.connectTimeout", "10")));
            dataSource.setSocketTimeout(Integer.parseInt(props.getProperty("db.postgresql.socketTimeout", "30")));
            dataSource.setTcpKeepAlive(Boolean.parseBoolean(props.getProperty("db.postgresql.tcpKeepAlive", "true")));
            
            // Set current schema
            dataSource.setCurrentSchema(props.getProperty("db.schema", "public"));
            
            logger.log(Level.INFO, "PostgreSQL DataSource configured successfully");
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error configuring PostgreSQL DataSource", e);
            throw new RuntimeException("Failed to configure PostgreSQL DataSource", e);
        }
        
        return dataSource;
    }
    
    /**
     * Parses JDBC URL to extract host, port, and database name
     * 
     * @param jdbcUrl JDBC connection URL
     * @return array containing [host, port, database]
     */
    private static String[] parseJdbcUrl(String jdbcUrl) {
        // Format: jdbc:postgresql://host:port/database
        String[] result = new String[3];
        
        try {
            String urlWithoutPrefix = jdbcUrl.substring("jdbc:postgresql://".length());
            String[] hostAndRest = urlWithoutPrefix.split("/");
            String[] hostAndPort = hostAndRest[0].split(":");
            
            result[0] = hostAndPort[0]; // host
            result[1] = hostAndPort.length > 1 ? hostAndPort[1] : "5432"; // port
            result[2] = hostAndRest.length > 1 ? hostAndRest[1].split("\\?")[0] : "modresorts"; // database
            
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error parsing JDBC URL, using defaults", e);
            result[0] = "localhost";
            result[1] = "5432";
            result[2] = "modresorts";
        }
        
        return result;
    }
    
    /**
     * Loads database properties from classpath
     * 
     * @return Properties object with database configuration
     */
    private static Properties loadDatabaseProperties() {
        Properties props = new Properties();
        
        try (InputStream input = PostgreSQLDataSourceConfig.class
                .getClassLoader()
                .getResourceAsStream(DB_PROPERTIES_FILE)) {
            
            if (input == null) {
                logger.log(Level.WARNING, "Unable to find {0}, using default values", DB_PROPERTIES_FILE);
                return getDefaultProperties();
            }
            
            props.load(input);
            logger.log(Level.INFO, "Database properties loaded successfully");
            
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error loading database properties, using defaults", e);
            return getDefaultProperties();
        }
        
        return props;
    }
    
    /**
     * Returns default database properties
     * 
     * @return Properties with default values
     */
    private static Properties getDefaultProperties() {
        Properties props = new Properties();
        props.setProperty("db.url", "jdbc:postgresql://localhost:5432/modresorts");
        props.setProperty("db.username", "postgres");
        props.setProperty("db.password", "postgres");
        props.setProperty("db.schema", "public");
        props.setProperty("db.postgresql.ssl", "false");
        props.setProperty("db.postgresql.sslmode", "prefer");
        props.setProperty("db.postgresql.connectTimeout", "10");
        props.setProperty("db.postgresql.socketTimeout", "30");
        props.setProperty("db.postgresql.tcpKeepAlive", "true");
        return props;
    }
}
