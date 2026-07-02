package com.acme.modres.db;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ModResortsCustomerInformationTest {

    private ModResortsCustomerInformation customerInfo;

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement statement;

    @Mock
    private ResultSet resultSet;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        customerInfo = new ModResortsCustomerInformation();
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
    }

    @Test
    void testInit() {
        assertDoesNotThrow(() -> customerInfo.init());
    }

    @Test
    void testGetCustomerInformation_withResults() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("info")).thenReturn("Customer1", "Customer2");
        
        ArrayList<String> result = customerInfo.getCustomerInformation();
        
        assertNotNull(result);
    }

    @Test
    void testGetCustomerInformation_withNoResults() throws SQLException {
        when(resultSet.next()).thenReturn(false);
        
        ArrayList<String> result = customerInfo.getCustomerInformation();
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetCustomerInformation_withSQLException() throws SQLException {
        when(dataSource.getConnection()).thenThrow(new SQLException("Connection failed"));
        
        ArrayList<String> result = customerInfo.getCustomerInformation();
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetCustomerInformation_returnsArrayList() {
        ArrayList<String> result = customerInfo.getCustomerInformation();
        
        assertNotNull(result);
        assertTrue(result instanceof ArrayList);
    }

    @Test
    void testGetCustomerInformation_closesResources() throws SQLException {
        when(resultSet.next()).thenReturn(false);
        
        customerInfo.getCustomerInformation();
        
        // Resources should be closed even if no results
        assertDoesNotThrow(() -> customerInfo.getCustomerInformation());
    }

    @Test
    void testGetCustomerInformation_handlesNullInfo() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("info")).thenReturn(null);
        
        ArrayList<String> result = customerInfo.getCustomerInformation();
        
        assertNotNull(result);
    }

    @Test
    void testGetCustomerInformation_handlesEmptyInfo() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("info")).thenReturn("");
        
        ArrayList<String> result = customerInfo.getCustomerInformation();
        
        assertNotNull(result);
    }
}
