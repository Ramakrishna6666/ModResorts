package com.acme.modres.db;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Cloud-ready Customer Information Service
 * Migrated from EJB 2.x to Spring Boot microservice
 * Uses try-with-resources for proper resource management
 */
@Service
public class ModResortsCustomerInformation {
  
  private static final Logger logger = Logger.getLogger(ModResortsCustomerInformation.class.getName());
  private static final String SELECT_CUSTOMERS_QUERY = "SELECT INFO FROM CUSTOMER";

  @Autowired(required = false)
  private DataSource dataSource;

  /**
   * Get customer information with proper resource management
   * Uses try-with-resources to prevent resource leaks in cloud environments
   */
  public ArrayList<String> getCustomerInformation() {
    ArrayList<String> customerInfo = new ArrayList<>();
    
    if (dataSource == null) {
      logger.warning("DataSource not configured. Returning empty customer list.");
      return customerInfo;
    }

    // Use try-with-resources to ensure all resources are properly closed
    try (Connection conn = dataSource.getConnection();
         PreparedStatement stmt = conn.prepareStatement(SELECT_CUSTOMERS_QUERY);
         ResultSet rs = stmt.executeQuery()) {

      // Process the results
      while (rs.next()) {
        String info = rs.getString("INFO");
        customerInfo.add(info);
      }

    } catch (SQLException e) {
      logger.severe("Database error retrieving customer information: " + e.getMessage());
      e.printStackTrace();
    }
    
    return customerInfo;
  }
}
