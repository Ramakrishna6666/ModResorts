package com.acme.modres.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;

/**
 * Utility class for PostgreSQL database operations.
 * Provides helper methods for database health checks, metadata retrieval, and common operations.
 */
public class PostgreSQLDatabaseUtil {
    
    private static final Logger LOGGER = Logger.getLogger(PostgreSQLDatabaseUtil.class.getName());
    
    /**
     * Tests database connection and returns connection information.
     * 
     * @param dataSource the DataSource to test
     * @return Map containing connection information
     */
    public static Map<String, String> getConnectionInfo(DataSource dataSource) {
        Map<String, String> info = new HashMap<>();
        
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            
            info.put("databaseProductName", metaData.getDatabaseProductName());
            info.put("databaseProductVersion", metaData.getDatabaseProductVersion());
            info.put("driverName", metaData.getDriverName());
            info.put("driverVersion", metaData.getDriverVersion());
            info.put("url", metaData.getURL());
            info.put("username", metaData.getUserName());
            info.put("isValid", String.valueOf(conn.isValid(5)));
            
            LOGGER.log(Level.INFO, "Successfully retrieved PostgreSQL connection info");
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving PostgreSQL connection info", e);
            info.put("error", e.getMessage());
        }
        
        return info;
    }
    
    /**
     * Checks if a table exists in the PostgreSQL database.
     * 
     * @param dataSource the DataSource
     * @param tableName the table name to check (case-sensitive)
     * @return true if table exists, false otherwise
     */
    public static boolean tableExists(DataSource dataSource, String tableName) {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            
            // PostgreSQL stores table names in lowercase by default
            try (ResultSet rs = metaData.getTables(null, "public", tableName.toLowerCase(), new String[]{"TABLE"})) {
                return rs.next();
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking if table exists: " + tableName, e);
            return false;
        }
    }
    
    /**
     * Gets the count of records in a table.
     * 
     * @param dataSource the DataSource
     * @param tableName the table name
     * @return count of records, or -1 if error
     */
    public static long getTableRowCount(DataSource dataSource, String tableName) {
        String query = "SELECT COUNT(*) FROM " + tableName;
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            if (rs.next()) {
                return rs.getLong(1);
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting row count for table: " + tableName, e);
        }
        
        return -1;
    }
    
    /**
     * Executes a health check query on PostgreSQL.
     * 
     * @param dataSource the DataSource
     * @return true if health check passes, false otherwise
     */
    public static boolean healthCheck(DataSource dataSource) {
        String healthCheckQuery = "SELECT 1";
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(healthCheckQuery)) {
            
            if (rs.next() && rs.getInt(1) == 1) {
                LOGGER.log(Level.INFO, "PostgreSQL health check passed");
                return true;
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "PostgreSQL health check failed", e);
        }
        
        return false;
    }
    
    /**
     * Gets PostgreSQL server version information.
     * 
     * @param dataSource the DataSource
     * @return version string, or null if error
     */
    public static String getPostgreSQLVersion(DataSource dataSource) {
        String versionQuery = "SELECT version()";
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(versionQuery)) {
            
            if (rs.next()) {
                return rs.getString(1);
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting PostgreSQL version", e);
        }
        
        return null;
    }
    
    /**
     * Gets current database name.
     * 
     * @param dataSource the DataSource
     * @return database name, or null if error
     */
    public static String getCurrentDatabase(DataSource dataSource) {
        String dbQuery = "SELECT current_database()";
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(dbQuery)) {
            
            if (rs.next()) {
                return rs.getString(1);
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting current database", e);
        }
        
        return null;
    }
    
    /**
     * Gets current schema name.
     * 
     * @param dataSource the DataSource
     * @return schema name, or null if error
     */
    public static String getCurrentSchema(DataSource dataSource) {
        String schemaQuery = "SELECT current_schema()";
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(schemaQuery)) {
            
            if (rs.next()) {
                return rs.getString(1);
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting current schema", e);
        }
        
        return null;
    }
    
    /**
     * Checks if PostgreSQL connection is using SSL.
     * 
     * @param dataSource the DataSource
     * @return true if SSL is enabled, false otherwise
     */
    public static boolean isSSLEnabled(DataSource dataSource) {
        String sslQuery = "SHOW ssl";
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sslQuery)) {
            
            if (rs.next()) {
                String sslStatus = rs.getString(1);
                return "on".equalsIgnoreCase(sslStatus);
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error checking SSL status", e);
        }
        
        return false;
    }
    
    /**
     * Gets comprehensive database statistics.
     * 
     * @param dataSource the DataSource
     * @return Map containing database statistics
     */
    public static Map<String, Object> getDatabaseStatistics(DataSource dataSource) {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("version", getPostgreSQLVersion(dataSource));
        stats.put("currentDatabase", getCurrentDatabase(dataSource));
        stats.put("currentSchema", getCurrentSchema(dataSource));
        stats.put("sslEnabled", isSSLEnabled(dataSource));
        stats.put("healthCheck", healthCheck(dataSource));
        stats.put("connectionInfo", getConnectionInfo(dataSource));
        
        return stats;
    }
}
