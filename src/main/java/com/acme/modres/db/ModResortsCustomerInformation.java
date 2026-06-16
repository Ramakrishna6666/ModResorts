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

/**
 * Singleton EJB for managing customer information from PostgreSQL database.
 * Modernized with try-with-resources for automatic resource management.
 */
@Singleton
@Startup
public class ModResortsCustomerInformation {
  private static final Logger LOGGER = Logger.getLogger(ModResortsCustomerInformation.class.getName());
  
  // PostgreSQL-compatible query using standard SQL
  private static final String SELECT_CUSTOMERS_QUERY = "SELECT info FROM customer";

  // DataSource injection for PostgreSQL connection pool
  // Configure in application server: jdbc/ModResortsJndi
  @Resource(lookup = "jdbc/ModResortsJndi")
  private DataSource dataSource;

  /**
   * Retrieves customer information from the database.
   * Uses try-with-resources for automatic connection management.
   * 
   * @return ArrayList of customer information strings
   */
  public ArrayList<String> getCustomerInformation() {
    ArrayList<String> customerInfo = new ArrayList<>();

    // Try-with-resources ensures automatic closing of Connection, PreparedStatement, and ResultSet
    try (Connection conn = dataSource.getConnection();
         PreparedStatement stmt = conn.prepareStatement(SELECT_CUSTOMERS_QUERY);
         ResultSet rs = stmt.executeQuery()) {

      // Process the results
      while (rs.next()) {
        String info = rs.getString("info");
        if (info != null) {
          customerInfo.add(info);
        }
      }
      
      LOGGER.log(Level.INFO, "Successfully retrieved {0} customer records", customerInfo.size());

    } catch (SQLException e) {
      LOGGER.log(Level.SEVERE, "Database error while retrieving customer information", e);
      // In production, consider throwing a custom exception or returning an empty list
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Unexpected error while retrieving customer information", e);
    }
    
    return customerInfo;
  }
  
  /**
   * Health check method to verify database connectivity.
   * 
   * @return true if database connection is available, false otherwise
   */
  public boolean isDatabaseAvailable() {
    try (Connection conn = dataSource.getConnection()) {
      return conn != null && !conn.isClosed();
    } catch (SQLException e) {
      LOGGER.log(Level.WARNING, "Database connection check failed", e);
      return false;
    }
  }
}
