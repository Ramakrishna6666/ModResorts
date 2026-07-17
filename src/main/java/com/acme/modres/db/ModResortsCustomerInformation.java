package com.acme.modres.db;

import org.springframework.stereotype.Service;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * blocker-8, blocker-9: Migrated from EJB 2.x (@Singleton, @Startup) to Spring Boot
 * using @Service annotation. Replaced EJB container-managed lifecycle with Spring-managed
 * bean. Uses Spring Data JPA-compatible DataSource injection pattern with AWS RDS.
 */
@Service
public class ModResortsCustomerInformation {
  private static final String SELECT_CUSTOMERS_QUERY = "SELECT INFO FROM CUSTOMER";

  // DataSource injected via Spring (configured to use AWS RDS via HikariCP connection pool)
  private DataSource dataSource;

  public ModResortsCustomerInformation() {
    // Default constructor for Spring instantiation
  }

  public ModResortsCustomerInformation(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public ArrayList<String> getCustomerInformation() {
    ArrayList<String> customerInfo = new ArrayList<>();

    if (dataSource == null) {
      return customerInfo;
    }

    // blocker-8, blocker-9: Use try-with-resources (Spring Boot / cloud-native pattern)
    // replacing EJB container-managed resource handling
    try (Connection conn = dataSource.getConnection();
         PreparedStatement stmt = conn.prepareStatement(SELECT_CUSTOMERS_QUERY);
         ResultSet rs = stmt.executeQuery()) {

      while (rs.next()) {
        String info = rs.getString("INFO");
        customerInfo.add(info);
      }

    } catch (SQLException e) {
      e.printStackTrace();
    }

    return customerInfo;
  }
}
