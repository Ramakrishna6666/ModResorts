package com.acme.modres.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;

/**
 * Repository class for Customer data access operations.
 * Implements the Repository pattern for PostgreSQL database operations.
 * Uses modern Java practices with try-with-resources and Optional.
 */
public class CustomerRepository {
    private static final Logger LOGGER = Logger.getLogger(CustomerRepository.class.getName());
    
    private final DataSource dataSource;
    
    // PostgreSQL-compatible SQL queries with lowercase column names
    private static final String SELECT_ALL_CUSTOMERS = 
        "SELECT id, info, created_at, updated_at, is_active FROM customer WHERE is_active = true ORDER BY created_at DESC";
    
    private static final String SELECT_CUSTOMER_BY_ID = 
        "SELECT id, info, created_at, updated_at, is_active FROM customer WHERE id = ? AND is_active = true";
    
    private static final String INSERT_CUSTOMER = 
        "INSERT INTO customer (info, is_active) VALUES (?, true) RETURNING id";
    
    private static final String UPDATE_CUSTOMER = 
        "UPDATE customer SET info = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ? AND is_active = true";
    
    private static final String DELETE_CUSTOMER = 
        "UPDATE customer SET is_active = false, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
    
    private static final String SEARCH_CUSTOMERS = 
        "SELECT id, info, created_at, updated_at, is_active FROM customer " +
        "WHERE is_active = true AND info ILIKE ? ORDER BY created_at DESC";
    
    /**
     * Constructor with DataSource injection.
     * 
     * @param dataSource the DataSource to use for database connections
     */
    public CustomerRepository(DataSource dataSource) {
        if (dataSource == null) {
            throw new IllegalArgumentException("DataSource cannot be null");
        }
        this.dataSource = dataSource;
    }
    
    /**
     * Retrieves all active customers from the database.
     * 
     * @return List of customer information strings
     */
    public List<String> findAllCustomers() {
        List<String> customers = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL_CUSTOMERS);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                String info = rs.getString("info");
                if (info != null) {
                    customers.add(info);
                }
            }
            
            LOGGER.log(Level.INFO, "Retrieved {0} customers", customers.size());
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving all customers", e);
        }
        
        return customers;
    }
    
    /**
     * Finds a customer by ID.
     * 
     * @param customerId the customer ID to search for
     * @return Optional containing customer info if found, empty otherwise
     */
    public Optional<String> findCustomerById(int customerId) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_CUSTOMER_BY_ID)) {
            
            stmt.setInt(1, customerId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String info = rs.getString("info");
                    LOGGER.log(Level.INFO, "Found customer with ID: {0}", customerId);
                    return Optional.ofNullable(info);
                }
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding customer by ID: " + customerId, e);
        }
        
        return Optional.empty();
    }
    
    /**
     * Inserts a new customer into the database.
     * Uses PostgreSQL RETURNING clause to get the generated ID.
     * 
     * @param customerInfo the customer information to insert
     * @return Optional containing the generated customer ID if successful
     */
    public Optional<Integer> insertCustomer(String customerInfo) {
        if (customerInfo == null || customerInfo.trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "Cannot insert customer with null or empty info");
            return Optional.empty();
        }
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_CUSTOMER)) {
            
            stmt.setString(1, customerInfo);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int generatedId = rs.getInt(1);
                    LOGGER.log(Level.INFO, "Inserted customer with ID: {0}", generatedId);
                    return Optional.of(generatedId);
                }
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error inserting customer", e);
        }
        
        return Optional.empty();
    }
    
    /**
     * Updates an existing customer's information.
     * 
     * @param customerId the ID of the customer to update
     * @param customerInfo the new customer information
     * @return true if update was successful, false otherwise
     */
    public boolean updateCustomer(int customerId, String customerInfo) {
        if (customerInfo == null || customerInfo.trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "Cannot update customer with null or empty info");
            return false;
        }
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_CUSTOMER)) {
            
            stmt.setString(1, customerInfo);
            stmt.setInt(2, customerId);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                LOGGER.log(Level.INFO, "Updated customer with ID: {0}", customerId);
                return true;
            } else {
                LOGGER.log(Level.WARNING, "No customer found with ID: {0}", customerId);
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating customer with ID: " + customerId, e);
        }
        
        return false;
    }
    
    /**
     * Soft deletes a customer by setting is_active to false.
     * 
     * @param customerId the ID of the customer to delete
     * @return true if deletion was successful, false otherwise
     */
    public boolean deleteCustomer(int customerId) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_CUSTOMER)) {
            
            stmt.setInt(1, customerId);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                LOGGER.log(Level.INFO, "Deleted customer with ID: {0}", customerId);
                return true;
            } else {
                LOGGER.log(Level.WARNING, "No customer found with ID: {0}", customerId);
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting customer with ID: " + customerId, e);
        }
        
        return false;
    }
    
    /**
     * Searches for customers by keyword using PostgreSQL ILIKE (case-insensitive).
     * 
     * @param keyword the search keyword
     * @return List of matching customer information strings
     */
    public List<String> searchCustomers(String keyword) {
        List<String> customers = new ArrayList<>();
        
        if (keyword == null || keyword.trim().isEmpty()) {
            return customers;
        }
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SEARCH_CUSTOMERS)) {
            
            // PostgreSQL ILIKE pattern matching
            stmt.setString(1, "%" + keyword + "%");
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String info = rs.getString("info");
                    if (info != null) {
                        customers.add(info);
                    }
                }
            }
            
            LOGGER.log(Level.INFO, "Found {0} customers matching keyword: {1}", 
                new Object[]{customers.size(), keyword});
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error searching customers with keyword: " + keyword, e);
        }
        
        return customers;
    }
    
    /**
     * Gets the total count of active customers.
     * 
     * @return the number of active customers
     */
    public int getActiveCustomerCount() {
        String query = "SELECT COUNT(*) FROM customer WHERE is_active = true";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting customer count", e);
        }
        
        return 0;
    }
    
    /**
     * Executes a batch insert of multiple customers.
     * Uses PostgreSQL batch processing for better performance.
     * 
     * @param customerInfoList list of customer information to insert
     * @return number of successfully inserted customers
     */
    public int batchInsertCustomers(List<String> customerInfoList) {
        if (customerInfoList == null || customerInfoList.isEmpty()) {
            return 0;
        }
        
        int insertedCount = 0;
        String batchInsert = "INSERT INTO customer (info, is_active) VALUES (?, true)";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(batchInsert)) {
            
            conn.setAutoCommit(false);
            
            for (String info : customerInfoList) {
                if (info != null && !info.trim().isEmpty()) {
                    stmt.setString(1, info);
                    stmt.addBatch();
                }
            }
            
            int[] results = stmt.executeBatch();
            conn.commit();
            
            for (int result : results) {
                if (result > 0) {
                    insertedCount++;
                }
            }
            
            LOGGER.log(Level.INFO, "Batch inserted {0} customers", insertedCount);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error in batch insert", e);
        }
        
        return insertedCount;
    }
}
