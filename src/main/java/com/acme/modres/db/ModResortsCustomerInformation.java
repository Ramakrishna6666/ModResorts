package com.acme.modres.db;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Cloud-native repository using Spring Data with HikariCP connection pooling.
 * Migrated from EJB 2.x to Spring Boot for better cloud compatibility.
 */
@Repository
public class ModResortsCustomerInformation {
  private static final String SELECT_CUSTOMERS_QUERY = "SELECT INFO FROM CUSTOMER";

  // Spring-managed DataSource with HikariCP connection pooling
  @Autowired
  private DataSource dataSource;

  public ArrayList<String> getCustomerInformation() {
    ArrayList<String> customerInfo = new ArrayList<>();

    // Use try-with-resources to ensure all database resources are properly closed
    try (Connection conn = dataSource.getConnection();
         PreparedStatement stmt = conn.prepareStatement(SELECT_CUSTOMERS_QUERY);
         ResultSet rs = stmt.executeQuery()) {

      // Process the results
      while (rs.next()) {
        String info = rs.getString("INFO");
        customerInfo.add(info);
      }

    } catch (SQLException e) {
      // Log the error and rethrow as runtime exception for Spring transaction management
      System.err.println("Database error while fetching customer information: " + e.getMessage());
      e.printStackTrace();
      throw new RuntimeException("Failed to retrieve customer information", e);
    }
    
    return customerInfo;
  }
}
