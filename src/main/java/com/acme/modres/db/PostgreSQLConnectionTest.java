package com.acme.modres.db;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * PostgreSQL Connection Test Utility
 * Provides methods to test and validate PostgreSQL database connectivity
 */
public class PostgreSQLConnectionTest {
    private static final Logger logger = Logger.getLogger(PostgreSQLConnectionTest.class.getName());
    
    /**
     * Tests the PostgreSQL database connection
     * 
     * @param dataSource the DataSource to test
     * @return true if connection is successful, false otherwise
     */
    public static boolean testConnection(DataSource dataSource) {
        if (dataSource == null) {
            logger.log(Level.SEVERE, "DataSource is null, cannot test connection");
            return false;
        }
        
        try (Connection conn = dataSource.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                logger.log(Level.INFO, "Successfully connected to PostgreSQL database");
                
                // Get database metadata
                DatabaseMetaData metaData = conn.getMetaData();
                logger.log(Level.INFO, "Database Product: {0}", metaData.getDatabaseProductName());
                logger.log(Level.INFO, "Database Version: {0}", metaData.getDatabaseProductVersion());
                logger.log(Level.INFO, "Driver Name: {0}", metaData.getDriverName());
                logger.log(Level.INFO, "Driver Version: {0}", metaData.getDriverVersion());
                logger.log(Level.INFO, "JDBC Version: {0}.{1}", 
                          new Object[]{metaData.getJDBCMajorVersion(), metaData.getJDBCMinorVersion()});
                
                return true;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to connect to PostgreSQL database", e);
            logger.log(Level.SEVERE, "SQL State: {0}", e.getSQLState());
            logger.log(Level.SEVERE, "Error Code: {0}", e.getErrorCode());
        }
        
        return false;
    }
    
    /**
     * Tests a simple query execution
     * 
     * @param dataSource the DataSource to use
     * @return true if query executes successfully, false otherwise
     */
    public static boolean testQuery(DataSource dataSource) {
        if (dataSource == null) {
            logger.log(Level.SEVERE, "DataSource is null, cannot test query");
            return false;
        }
        
        String testQuery = "SELECT version(), current_database(), current_schema(), current_user";
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(testQuery)) {
            
            if (rs.next()) {
                logger.log(Level.INFO, "PostgreSQL Version: {0}", rs.getString(1));
                logger.log(Level.INFO, "Current Database: {0}", rs.getString(2));
                logger.log(Level.INFO, "Current Schema: {0}", rs.getString(3));
                logger.log(Level.INFO, "Current User: {0}", rs.getString(4));
                return true;
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to execute test query", e);
        }
        
        return false;
    }
    
    /**
     * Checks if a specific table exists in the database
     * 
     * @param dataSource the DataSource to use
     * @param tableName the name of the table to check
     * @return true if table exists, false otherwise
     */
    public static boolean tableExists(DataSource dataSource, String tableName) {
        if (dataSource == null || tableName == null || tableName.trim().isEmpty()) {
            logger.log(Level.SEVERE, "Invalid parameters for table existence check");
            return false;
        }
        
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            
            // PostgreSQL stores unquoted identifiers in lowercase
            String tableNameLower = tableName.toLowerCase();
            String tableNameUpper = tableName.toUpperCase();
            
            // Check for table in current schema
            try (ResultSet rs = metaData.getTables(null, null, tableNameLower, new String[]{"TABLE"})) {
                if (rs.next()) {
                    logger.log(Level.INFO, "Table {0} exists in database", tableName);
                    return true;
                }
            }
            
            // Try uppercase if lowercase didn't work
            try (ResultSet rs = metaData.getTables(null, null, tableNameUpper, new String[]{"TABLE"})) {
                if (rs.next()) {
                    logger.log(Level.INFO, "Table {0} exists in database", tableName);
                    return true;
                }
            }
            
            logger.log(Level.WARNING, "Table {0} does not exist in database", tableName);
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error checking table existence", e);
        }
        
        return false;
    }
    
    /**
     * Main method for standalone testing
     * 
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        logger.log(Level.INFO, "Starting PostgreSQL Connection Test...");
        
        try {
            DataSource dataSource = PostgreSQLDataSourceConfig.createDataSource();
            
            // Test connection
            boolean connectionSuccess = testConnection(dataSource);
            logger.log(Level.INFO, "Connection Test: {0}", connectionSuccess ? "PASSED" : "FAILED");
            
            if (connectionSuccess) {
                // Test query
                boolean querySuccess = testQuery(dataSource);
                logger.log(Level.INFO, "Query Test: {0}", querySuccess ? "PASSED" : "FAILED");
                
                // Check if CUSTOMER table exists
                boolean tableExists = tableExists(dataSource, "CUSTOMER");
                logger.log(Level.INFO, "Table Existence Test: {0}", tableExists ? "PASSED" : "FAILED");
            }
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error during PostgreSQL connection test", e);
        }
        
        logger.log(Level.INFO, "PostgreSQL Connection Test completed");
    }
}
