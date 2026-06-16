package com.acme.modres.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;

/**
 * Utility class for testing PostgreSQL database connectivity and configuration.
 * Provides diagnostic methods to verify database setup and connection health.
 */
public class DatabaseConnectionTest {
    private static final Logger LOGGER = Logger.getLogger(DatabaseConnectionTest.class.getName());
    
    private final DataSource dataSource;
    
    /**
     * Constructor with DataSource.
     * 
     * @param dataSource the DataSource to test
     */
    public DatabaseConnectionTest(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    /**
     * Performs a comprehensive database connection test.
     * 
     * @return true if all tests pass, false otherwise
     */
    public boolean runAllTests() {
        LOGGER.info("=== Starting PostgreSQL Database Connection Tests ===");
        
        boolean allTestsPassed = true;
        
        allTestsPassed &= testBasicConnection();
        allTestsPassed &= testDatabaseMetadata();
        allTestsPassed &= testTableExists("customer");
        allTestsPassed &= testSimpleQuery();
        allTestsPassed &= testTransactionSupport();
        
        if (allTestsPassed) {
            LOGGER.info("=== All Database Tests PASSED ===");
        } else {
            LOGGER.warning("=== Some Database Tests FAILED ===");
        }
        
        return allTestsPassed;
    }
    
    /**
     * Tests basic database connectivity.
     * 
     * @return true if connection successful, false otherwise
     */
    public boolean testBasicConnection() {
        LOGGER.info("Test 1: Basic Connection Test");
        
        try (Connection conn = dataSource.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                LOGGER.info("✓ Basic connection test PASSED");
                LOGGER.info("  - Connection established successfully");
                LOGGER.info("  - Connection class: " + conn.getClass().getName());
                return true;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "✗ Basic connection test FAILED", e);
        }
        
        return false;
    }
    
    /**
     * Tests and displays database metadata.
     * 
     * @return true if metadata retrieval successful, false otherwise
     */
    public boolean testDatabaseMetadata() {
        LOGGER.info("Test 2: Database Metadata Test");
        
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            
            LOGGER.info("✓ Database metadata test PASSED");
            LOGGER.info("  - Database Product: " + metaData.getDatabaseProductName());
            LOGGER.info("  - Database Version: " + metaData.getDatabaseProductVersion());
            LOGGER.info("  - Driver Name: " + metaData.getDriverName());
            LOGGER.info("  - Driver Version: " + metaData.getDriverVersion());
            LOGGER.info("  - JDBC Version: " + metaData.getJDBCMajorVersion() + "." + 
                       metaData.getJDBCMinorVersion());
            LOGGER.info("  - URL: " + metaData.getURL());
            LOGGER.info("  - Username: " + metaData.getUserName());
            LOGGER.info("  - Supports Transactions: " + metaData.supportsTransactions());
            LOGGER.info("  - Supports Batch Updates: " + metaData.supportsBatchUpdates());
            
            return true;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "✗ Database metadata test FAILED", e);
            return false;
        }
    }
    
    /**
     * Tests if a specific table exists in the database.
     * 
     * @param tableName the name of the table to check
     * @return true if table exists, false otherwise
     */
    public boolean testTableExists(String tableName) {
        LOGGER.info("Test 3: Table Existence Test - " + tableName);
        
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            
            try (ResultSet rs = metaData.getTables(null, "public", tableName, new String[]{"TABLE"})) {
                if (rs.next()) {
                    LOGGER.info("✓ Table existence test PASSED");
                    LOGGER.info("  - Table '" + tableName + "' exists in schema 'public'");
                    
                    // Get column information
                    try (ResultSet columns = metaData.getColumns(null, "public", tableName, null)) {
                        LOGGER.info("  - Columns:");
                        while (columns.next()) {
                            String columnName = columns.getString("COLUMN_NAME");
                            String columnType = columns.getString("TYPE_NAME");
                            int columnSize = columns.getInt("COLUMN_SIZE");
                            String nullable = columns.getString("IS_NULLABLE");
                            
                            LOGGER.info(String.format("    • %s (%s, size: %d, nullable: %s)", 
                                columnName, columnType, columnSize, nullable));
                        }
                    }
                    
                    return true;
                } else {
                    LOGGER.warning("✗ Table existence test FAILED");
                    LOGGER.warning("  - Table '" + tableName + "' does not exist");
                    return false;
                }
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "✗ Table existence test FAILED", e);
            return false;
        }
    }
    
    /**
     * Tests a simple SELECT query.
     * 
     * @return true if query executes successfully, false otherwise
     */
    public boolean testSimpleQuery() {
        LOGGER.info("Test 4: Simple Query Test");
        
        String query = "SELECT COUNT(*) as count FROM customer";
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            if (rs.next()) {
                int count = rs.getInt("count");
                LOGGER.info("✓ Simple query test PASSED");
                LOGGER.info("  - Query executed successfully");
                LOGGER.info("  - Customer count: " + count);
                return true;
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "✗ Simple query test FAILED", e);
        }
        
        return false;
    }
    
    /**
     * Tests transaction support.
     * 
     * @return true if transactions work correctly, false otherwise
     */
    public boolean testTransactionSupport() {
        LOGGER.info("Test 5: Transaction Support Test");
        
        try (Connection conn = dataSource.getConnection()) {
            // Disable auto-commit to test transactions
            conn.setAutoCommit(false);
            
            try (Statement stmt = conn.createStatement()) {
                // Test insert
                stmt.executeUpdate(
                    "INSERT INTO customer (info) VALUES ('Transaction Test Customer')");
                
                // Rollback the transaction
                conn.rollback();
                
                LOGGER.info("✓ Transaction support test PASSED");
                LOGGER.info("  - Transaction rollback successful");
                LOGGER.info("  - Auto-commit disabled successfully");
                
                // Re-enable auto-commit
                conn.setAutoCommit(true);
                
                return true;
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "✗ Transaction support test FAILED", e);
            return false;
        }
    }
    
    /**
     * Tests PostgreSQL-specific features.
     * 
     * @return true if PostgreSQL features work, false otherwise
     */
    public boolean testPostgreSQLFeatures() {
        LOGGER.info("Test 6: PostgreSQL-Specific Features Test");
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Test ILIKE (case-insensitive pattern matching)
            try (ResultSet rs = stmt.executeQuery(
                    "SELECT COUNT(*) as count FROM customer WHERE info ILIKE '%test%'")) {
                if (rs.next()) {
                    LOGGER.info("✓ ILIKE operator works");
                }
            }
            
            // Test RETURNING clause
            try (ResultSet rs = stmt.executeQuery(
                    "INSERT INTO customer (info) VALUES ('Feature Test') RETURNING id")) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    LOGGER.info("✓ RETURNING clause works (generated ID: " + id + ")");
                    
                    // Clean up test data
                    stmt.executeUpdate("DELETE FROM customer WHERE id = " + id);
                }
            }
            
            // Test current_timestamp
            try (ResultSet rs = stmt.executeQuery("SELECT CURRENT_TIMESTAMP")) {
                if (rs.next()) {
                    LOGGER.info("✓ CURRENT_TIMESTAMP works: " + rs.getTimestamp(1));
                }
            }
            
            LOGGER.info("✓ PostgreSQL features test PASSED");
            return true;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "✗ PostgreSQL features test FAILED", e);
            return false;
        }
    }
    
    /**
     * Main method for standalone testing.
     * 
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        try {
            // Get DataSource from configuration
            DataSource dataSource = PostgreSQLDataSourceConfig.getInstance().getDataSource();
            
            // Create test instance
            DatabaseConnectionTest test = new DatabaseConnectionTest(dataSource);
            
            // Run all tests
            boolean success = test.runAllTests();
            
            // Run PostgreSQL-specific tests
            test.testPostgreSQLFeatures();
            
            // Print connection pool stats
            String poolStats = PostgreSQLDataSourceConfig.getInstance().getPoolStats();
            LOGGER.info("Connection Pool Statistics: " + poolStats);
            
            // Exit with appropriate code
            System.exit(success ? 0 : 1);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Test execution failed", e);
            System.exit(1);
        }
    }
}
