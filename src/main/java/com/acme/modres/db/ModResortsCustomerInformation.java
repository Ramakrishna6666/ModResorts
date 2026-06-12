package com.acme.modres.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.annotation.Resource;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;
import javax.sql.DataSource;

/**
 * Customer Information Service
 * PostgreSQL-compatible implementation with proper resource management
 */
@Singleton
@Startup
public class ModResortsCustomerInformation {
  private static final Logger logger = Logger.getLogger(ModResortsCustomerInformation.class.getName());
  
  // PostgreSQL-compatible query with explicit schema and lowercase table names
  // Following PostgreSQL naming conventions (snake_case)
  private static final String SELECT_CUSTOMERS_QUERY = "SELECT info FROM public.customer";

  // Option 1: Use JNDI DataSource (commented out for demo)
  // @Resource(lookup = "jdbc/ModResortsJndi")
  // private DataSource dataSource;
  
  // Option 2: Use injected PostgreSQL DataSource configuration
  @Inject
  private PostgreSQLDataSourceConfig dataSourceConfig;

  /**
   * Retrieves customer information from PostgreSQL database
   * Uses try-with-resources for automatic resource management
   * 
   * @return ArrayList of customer information strings
   */
  public ArrayList<String> getCustomerInformation() {
    ArrayList<String> customerInfo = new ArrayList<>();
    
    // Get DataSource - either from JNDI or from configuration
    DataSource ds = getDataSource();
    
    if (ds == null) {
      logger.warning("DataSource is not available. Returning empty customer list.");
      return customerInfo;
    }

    // Use try-with-resources for automatic resource cleanup
    // This is PostgreSQL-compatible and follows best practices
    try (Connection conn = ds.getConnection();
         PreparedStatement stmt = conn.prepareStatement(SELECT_CUSTOMERS_QUERY);
         ResultSet rs = stmt.executeQuery()) {

      // Process the results
      // PostgreSQL column names are case-sensitive when quoted, 
      // but lowercase by default
      while (rs.next()) {
        String info = rs.getString("info");
        customerInfo.add(info);
      }
      
      logger.log(Level.INFO, "Retrieved {0} customer records from PostgreSQL", customerInfo.size());

    } catch (SQLException e) {
      logger.log(Level.SEVERE, "Error retrieving customer information from PostgreSQL", e);
      // In production, consider throwing a custom exception or handling appropriately
    }
    
    return customerInfo;
  }
  
  /**
   * Gets the appropriate DataSource
   * Priority: JNDI DataSource > Injected Configuration
   */
  private DataSource getDataSource() {
    // Uncomment when JNDI is configured
    // if (dataSource != null) {
    //   return dataSource;
    // }
    
    if (dataSourceConfig != null) {
      return dataSourceConfig.getDataSource();
    }
    
    return null;
  }
}
