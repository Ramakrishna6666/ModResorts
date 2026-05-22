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
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        customerInfo = new ModResortsCustomerInformation();
        customerInfo.setDataSource(dataSource);
        
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
    }

    @Test
    void testGetCustomerInformation_withResults() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("INFO")).thenReturn("Customer1", "Customer2");
        
        ArrayList<String> result = customerInfo.getCustomerInformation();
        
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Customer1", result.get(0));
        assertEquals("Customer2", result.get(1));
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
    void testGetCustomerInformation_withNullDataSource() {
        customerInfo.setDataSource(null);
        
        assertThrows(NullPointerException.class, () -> {
            customerInfo.getCustomerInformation();
        });
    }

    @Test
    void testSetDataSource() {
        DataSource newDataSource = mock(DataSource.class);
        customerInfo.setDataSource(newDataSource);
        assertNotNull(customerInfo);
    }

    @Test
    void testGetCustomerInformation_closesResources() throws SQLException {
        when(resultSet.next()).thenReturn(false);
        
        customerInfo.getCustomerInformation();
        
        verify(resultSet).close();
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    void testConstructor() {
        assertNotNull(new ModResortsCustomerInformation());
    }
}
