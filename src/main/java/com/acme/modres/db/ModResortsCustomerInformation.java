package com.acme.modres.db;

import com.acme.modres.config.PostgreSQLDataSourceConfig;
import jakarta.annotation.Resource;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Customer Information Data Access Object for PostgreSQL.
 * Updated to use PostgreSQL-specific best practices and connection pooling.
 */
@Singleton
@Startup
public class ModResortsCustomerInformation {
  
  private static final Logger LOGGER = Logger.getLogger(ModResortsCustomerInformation.class.getName());
  
  // PostgreSQL query - using lowercase table name (PostgreSQL convention)
  // Note: If your table name is uppercase, you need to quote it: "SELECT info FROM \"CUSTOMER\""
  private static final String SELECT_CUSTOMERS_QUERY = "SELECT info FROM customer";

  // Option 1: Use JNDI lookup (for application server managed datasource)
  // @Resource(lookup = "jdbc/ModResortsJndi")
  // private DataSource dataSource;
  
  // Option 2: Use injected PostgreSQL DataSource configuration
  @Inject
  private PostgreSQLDataSourceConfig dataSourceConfig;

  /**
   * Retrieves customer information from PostgreSQL database.
   * Uses try-with-resources for automatic resource management.
   * 
   * @return ArrayList of customer information strings
   */
  public ArrayList<String> getCustomerInformation() {
    ArrayList<String> customerInfo = new ArrayList<>();
    
    // Use try-with-resources for automatic resource cleanup
    try (Connection conn = getDataSource().getConnection();
         PreparedStatement stmt = conn.prepareStatement(SELECT_CUSTOMERS_QUERY);
         ResultSet rs = stmt.executeQuery()) {

      // Process the results
      while (rs.next()) {
        String info = rs.getString("info");
        customerInfo.add(info);
      }
      
      LOGGER.log(Level.INFO, "Successfully retrieved {0} customer records from PostgreSQL", 
                 customerInfo.size());

    } catch (SQLException e) {
      LOGGER.log(Level.SEVERE, "Error retrieving customer information from PostgreSQL", e);
      // In production, consider throwing a custom exception or returning empty list
      // throw new DataAccessException("Failed to retrieve customer information", e);
    }
    
    return customerInfo;
  }
  
  /**
   * Gets the DataSource - either from JNDI or from injected configuration.
   * 
   * @return DataSource instance
   */
  private DataSource getDataSource() {
    // If using JNDI (uncomment the @Resource annotation above)
    // return dataSource;
    
    // Using injected PostgreSQL configuration
    return dataSourceConfig.getDataSource();
  }
  
  /**
   * Test method to verify PostgreSQL connection.
   * 
   * @return true if connection is successful, false otherwise
   */
  public boolean testConnection() {
    try (Connection conn = getDataSource().getConnection()) {
      return conn.isValid(5); // 5 second timeout
    } catch (SQLException e) {
      LOGGER.log(Level.SEVERE, "PostgreSQL connection test failed", e);
      return false;
    }
  }
}
