package com.acme.modres.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;

/**
 * PostgreSQL Database Utilities
 * Provides helper methods for PostgreSQL-specific operations
 */
public class PostgreSQLUtils {
    private static final Logger logger = Logger.getLogger(PostgreSQLUtils.class.getName());
    
    /**
     * Tests database connectivity
     * 
     * @param dataSource The DataSource to test
     * @return true if connection is successful, false otherwise
     */
    public static boolean testConnection(DataSource dataSource) {
        if (dataSource == null) {
            logger.warning("DataSource is null");
            return false;
        }
        
        try (Connection conn = dataSource.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                logger.info("Database connection test successful");
                return true;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database connection test failed", e);
        }
        
        return false;
    }
    
    /**
     * Gets PostgreSQL version information
     * 
     * @param dataSource The DataSource to query
     * @return PostgreSQL version string, or null if error
     */
    public static String getPostgreSQLVersion(DataSource dataSource) {
        String version = null;
        String query = "SELECT version()";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                version = rs.getString(1);
                logger.log(Level.INFO, "PostgreSQL Version: {0}", version);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting PostgreSQL version", e);
        }
        
        return version;
    }
    
    /**
     * Gets current database name
     * 
     * @param dataSource The DataSource to query
     * @return Database name, or null if error
     */
    public static String getCurrentDatabase(DataSource dataSource) {
        String dbName = null;
        String query = "SELECT current_database()";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                dbName = rs.getString(1);
                logger.log(Level.INFO, "Current Database: {0}", dbName);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting current database", e);
        }
        
        return dbName;
    }
    
    /**
     * Gets current schema
     * 
     * @param dataSource The DataSource to query
     * @return Schema name, or null if error
     */
    public static String getCurrentSchema(DataSource dataSource) {
        String schema = null;
        String query = "SELECT current_schema()";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                schema = rs.getString(1);
                logger.log(Level.INFO, "Current Schema: {0}", schema);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting current schema", e);
        }
        
        return schema;
    }
    
    /**
     * Checks if a table exists in the database
     * 
     * @param dataSource The DataSource to query
     * @param tableName The table name to check (case-sensitive)
     * @param schema The schema name (default: public)
     * @return true if table exists, false otherwise
     */
    public static boolean tableExists(DataSource dataSource, String tableName, String schema) {
        if (schema == null || schema.isEmpty()) {
            schema = "public";
        }
        
        String query = "SELECT EXISTS (" +
                      "SELECT FROM information_schema.tables " +
                      "WHERE table_schema = ? AND table_name = ?" +
                      ")";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, schema);
            stmt.setString(2, tableName);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    boolean exists = rs.getBoolean(1);
                    logger.log(Level.INFO, "Table {0}.{1} exists: {2}", 
                              new Object[]{schema, tableName, exists});
                    return exists;
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error checking table existence", e);
        }
        
        return false;
    }
    
    /**
     * Gets table row count
     * 
     * @param dataSource The DataSource to query
     * @param tableName The table name
     * @param schema The schema name (default: public)
     * @return Row count, or -1 if error
     */
    public static long getTableRowCount(DataSource dataSource, String tableName, String schema) {
        if (schema == null || schema.isEmpty()) {
            schema = "public";
        }
        
        // Note: Using format() for table name - be careful with SQL injection
        // In production, validate table name against whitelist
        String query = String.format("SELECT COUNT(*) FROM %s.%s", schema, tableName);
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                long count = rs.getLong(1);
                logger.log(Level.INFO, "Table {0}.{1} row count: {2}", 
                          new Object[]{schema, tableName, count});
                return count;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting table row count", e);
        }
        
        return -1;
    }
    
    /**
     * Executes VACUUM on a table to reclaim storage
     * Note: Cannot be executed within a transaction
     * 
     * @param dataSource The DataSource to use
     * @param tableName The table name
     * @param schema The schema name (default: public)
     * @return true if successful, false otherwise
     */
    public static boolean vacuumTable(DataSource dataSource, String tableName, String schema) {
        if (schema == null || schema.isEmpty()) {
            schema = "public";
        }
        
        String sql = String.format("VACUUM %s.%s", schema, tableName);
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // VACUUM cannot run inside a transaction block
            conn.setAutoCommit(true);
            stmt.execute();
            logger.log(Level.INFO, "VACUUM executed on {0}.{1}", 
                      new Object[]{schema, tableName});
            return true;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error executing VACUUM", e);
        }
        
        return false;
    }
    
    /**
     * Executes ANALYZE on a table to update statistics
     * 
     * @param dataSource The DataSource to use
     * @param tableName The table name
     * @param schema The schema name (default: public)
     * @return true if successful, false otherwise
     */
    public static boolean analyzeTable(DataSource dataSource, String tableName, String schema) {
        if (schema == null || schema.isEmpty()) {
            schema = "public";
        }
        
        String sql = String.format("ANALYZE %s.%s", schema, tableName);
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.execute();
            logger.log(Level.INFO, "ANALYZE executed on {0}.{1}", 
                      new Object[]{schema, tableName});
            return true;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error executing ANALYZE", e);
        }
        
        return false;
    }
    
    /**
     * Gets database connection information
     * 
     * @param dataSource The DataSource to query
     * @return Connection info string, or null if error
     */
    public static String getConnectionInfo(DataSource dataSource) {
        StringBuilder info = new StringBuilder();
        
        try (Connection conn = dataSource.getConnection()) {
            info.append("Database Product: ").append(conn.getMetaData().getDatabaseProductName()).append("\n");
            info.append("Database Version: ").append(conn.getMetaData().getDatabaseProductVersion()).append("\n");
            info.append("Driver Name: ").append(conn.getMetaData().getDriverName()).append("\n");
            info.append("Driver Version: ").append(conn.getMetaData().getDriverVersion()).append("\n");
            info.append("JDBC Version: ").append(conn.getMetaData().getJDBCMajorVersion())
                .append(".").append(conn.getMetaData().getJDBCMinorVersion()).append("\n");
            info.append("URL: ").append(conn.getMetaData().getURL()).append("\n");
            info.append("Username: ").append(conn.getMetaData().getUserName()).append("\n");
            
            logger.info("Connection Info:\n" + info.toString());
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting connection info", e);
            return null;
        }
        
        return info.toString();
    }
    
    /**
     * Escapes a string for use in PostgreSQL queries
     * Note: Use PreparedStatement instead when possible
     * 
     * @param input The string to escape
     * @return Escaped string
     */
    public static String escapeString(String input) {
        if (input == null) {
            return null;
        }
        // Replace single quotes with two single quotes (PostgreSQL standard)
        return input.replace("'", "''");
    }
    
    /**
     * Converts a table/column name to PostgreSQL lowercase convention
     * 
     * @param name The name to convert
     * @return Lowercase name
     */
    public static String toPostgreSQLName(String name) {
        if (name == null) {
            return null;
        }
        return name.toLowerCase();
    }
    
    /**
     * Converts camelCase to snake_case (PostgreSQL convention)
     * 
     * @param camelCase The camelCase string
     * @return snake_case string
     */
    public static String camelToSnakeCase(String camelCase) {
        if (camelCase == null || camelCase.isEmpty()) {
            return camelCase;
        }
        
        return camelCase
            .replaceAll("([a-z])([A-Z]+)", "$1_$2")
            .toLowerCase();
    }
    
    /**
     * Converts snake_case to camelCase
     * 
     * @param snakeCase The snake_case string
     * @return camelCase string
     */
    public static String snakeToCamelCase(String snakeCase) {
        if (snakeCase == null || snakeCase.isEmpty()) {
            return snakeCase;
        }
        
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = false;
        
        for (char c : snakeCase.toCharArray()) {
            if (c == '_') {
                capitalizeNext = true;
            } else {
                if (capitalizeNext) {
                    result.append(Character.toUpperCase(c));
                    capitalizeNext = false;
                } else {
                    result.append(c);
                }
            }
        }
        
        return result.toString();
    }
}
