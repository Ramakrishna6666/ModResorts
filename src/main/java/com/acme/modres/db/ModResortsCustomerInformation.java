package com.acme.modres.db;

import jakarta.annotation.Resource;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
@Startup
public class ModResortsCustomerInformation {
  private static final Logger logger = Logger.getLogger(ModResortsCustomerInformation.class.getName());
  
  // PostgreSQL-compatible query - using lowercase table name for PostgreSQL convention
  // Note: If table name is case-sensitive in PostgreSQL, use "CUSTOMER" with quotes
  private static final String SELECT_CUSTOMERS_QUERY = "SELECT INFO FROM CUSTOMER";

  // Removing DB connection for ease of demo setup
  // @Resource(lookup = "jdbc/ModResortsJndi")
  private DataSource dataSource;

  /**
   * Retrieves customer information from PostgreSQL database.
   * Uses try-with-resources for automatic resource management.
   * 
   * @return ArrayList of customer information strings
   */
  public ArrayList<String> getCustomerInformation() {
    ArrayList<String> customerInfo = new ArrayList<>();

    // Try-with-resources ensures proper cleanup of database resources
    try (Connection conn = dataSource.getConnection();
         PreparedStatement stmt = conn.prepareStatement(SELECT_CUSTOMERS_QUERY);
         ResultSet rs = stmt.executeQuery()) {

      // Process the results
      while (rs.next()) {
        String info = rs.getString("INFO");
        customerInfo.add(info);
      }

      logger.log(Level.INFO, "Successfully retrieved {0} customer records from PostgreSQL", 
                 customerInfo.size());

    } catch (SQLException e) {
      logger.log(Level.SEVERE, "Error retrieving customer information from PostgreSQL database", e);
      // In production, consider throwing a custom exception or returning empty list
      // throw new DatabaseAccessException("Failed to retrieve customer information", e);
    }
    
    return customerInfo;
  }
  
  /**
   * Sets the DataSource for testing purposes.
   * 
   * @param dataSource the DataSource to use
   */
  public void setDataSource(DataSource dataSource) {
    this.dataSource = dataSource;
  }
}
