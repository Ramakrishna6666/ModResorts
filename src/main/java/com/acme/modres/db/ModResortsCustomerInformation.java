package com.acme.modres.db;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Customer Information Data Access Object
 * Migrated to PostgreSQL from SQL Server
 */
@Singleton
@Startup
public class ModResortsCustomerInformation {
  
  // PostgreSQL-compatible query with explicit schema and lowercase table name
  // PostgreSQL uses lowercase identifiers by default unless quoted
  private static final String SELECT_CUSTOMERS_QUERY = "SELECT info FROM public.customer";

  // DataSource can be injected via JNDI or configured programmatically
  // For demo purposes, using programmatic configuration
  // @Resource(lookup = "jdbc/ModResortsJndi")
  private DataSource dataSource;
  
  /**
   * Initialize DataSource after construction
   * Uses PostgreSQL DataSource configuration
   */
  @PostConstruct
  public void init() {
    // Initialize PostgreSQL DataSource if not injected
    if (dataSource == null) {
      dataSource = PostgreSQLDataSourceConfig.getDataSource();
    }
  }

  /**
   * Retrieve customer information from PostgreSQL database
   * @return List of customer information strings
   */
  public ArrayList<String> getCustomerInformation() {
    Connection conn = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;
    ArrayList<String> customerInfo = new ArrayList<>();

    try {
      // Get a connection from the data source
      conn = dataSource.getConnection();
      
      // Create a prepared statement
      stmt = conn.prepareStatement(SELECT_CUSTOMERS_QUERY);
      
      // Execute the query
      rs = stmt.executeQuery();

      // Process the results
      // PostgreSQL column names are case-sensitive when quoted, lowercase by default
      while (rs.next()) {
        String info = rs.getString("info");
        customerInfo.add(info);
      }

    } catch (SQLException e) {
      System.err.println("Error retrieving customer information: " + e.getMessage());
      e.printStackTrace();
    } finally {
      // Close the result set, statement, and connection in reverse order
      try {
        if (rs != null)
          rs.close();
        if (stmt != null)
          stmt.close();
        if (conn != null)
          conn.close();
      } catch (SQLException e) {
        System.err.println("Error closing database resources: " + e.getMessage());
        e.printStackTrace();
      }
    }
    return customerInfo;
  }
}
