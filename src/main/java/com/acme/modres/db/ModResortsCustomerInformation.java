package com.acme.modres.db;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Replaced @Singleton with @Component for distributed caching compatibility.
 * State should be externalized to Amazon ElastiCache (Redis) for horizontal scaling.
 * Use environment variables for database configuration.
 */
@Component
public class ModResortsCustomerInformation {
  private static final String SELECT_CUSTOMERS_QUERY = "SELECT INFO FROM CUSTOMER";

  // Use environment variables for database configuration in containerized environments
  @Value("${DB_HOST:localhost}")
  private String dbHost;
  
  @Value("${DB_PORT:5432}")
  private String dbPort;
  
  @Value("${DB_NAME:modresorts}")
  private String dbName;
  
  @Value("${DB_USER:dbuser}")
  private String dbUser;
  
  @Value("${DB_PASSWORD:dbpassword}")
  private String dbPassword;

  // Removing DB connection for ease of demo setup
  // DataSource should be configured via Spring Boot externalized configuration
  private DataSource dataSource;

  @PostConstruct
  public void init() {
    // Initialize DataSource using environment variables
    // In production, use Spring Boot's DataSource auto-configuration with externalized properties
  }

  public ArrayList<String> getCustomerInformation() {
    Connection conn = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;
    ArrayList<String> customerInfo = new ArrayList<>();

    try {
      // Get a connection from the injected data source
      if (dataSource != null) {
        conn = dataSource.getConnection();
        // Create a prepared statement
        stmt = conn.prepareStatement(SELECT_CUSTOMERS_QUERY);
        // Execute the query
        rs = stmt.executeQuery();

        // Process the results
        while (rs.next()) {
          String info = rs.getString("INFO");
          customerInfo.add(info);
        }
      }

    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      // Close the result set, statement, and connection
      try {
        if (rs != null)
          rs.close();
        if (stmt != null)
          stmt.close();
        if (conn != null)
          conn.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
    return customerInfo;
  }
}
