package com.acme.modres.db;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class PostgreSQLConnectionTestTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private DatabaseMetaData metaData;

    @Mock
    private Statement statement;

    @Mock
    private ResultSet resultSet;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testTestConnection_withValidDataSource_returnsTrue() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isClosed()).thenReturn(false);
        when(connection.getMetaData()).thenReturn(metaData);
        when(metaData.getDatabaseProductName()).thenReturn("PostgreSQL");
        when(metaData.getDatabaseProductVersion()).thenReturn("14.0");
        when(metaData.getDriverName()).thenReturn("PostgreSQL JDBC Driver");
        when(metaData.getDriverVersion()).thenReturn("42.7.3");
        when(metaData.getJDBCMajorVersion()).thenReturn(4);
        when(metaData.getJDBCMinorVersion()).thenReturn(2);
        
        boolean result = PostgreSQLConnectionTest.testConnection(dataSource);
        
        assertTrue(result);
    }

    @Test
    void testTestConnection_withNullDataSource_returnsFalse() {
        boolean result = PostgreSQLConnectionTest.testConnection(null);
        
        assertFalse(result);
    }

    @Test
    void testTestConnection_withSQLException_returnsFalse() throws SQLException {
        when(dataSource.getConnection()).thenThrow(new SQLException("Connection failed"));
        
        boolean result = PostgreSQLConnectionTest.testConnection(dataSource);
        
        assertFalse(result);
    }

    @Test
    void testTestQuery_withValidDataSource_returnsTrue() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString(1)).thenReturn("PostgreSQL 14.0");
        when(resultSet.getString(2)).thenReturn("modresorts");
        when(resultSet.getString(3)).thenReturn("public");
        when(resultSet.getString(4)).thenReturn("postgres");
        
        boolean result = PostgreSQLConnectionTest.testQuery(dataSource);
        
        assertTrue(result);
    }

    @Test
    void testTestQuery_withNullDataSource_returnsFalse() {
        boolean result = PostgreSQLConnectionTest.testQuery(null);
        
        assertFalse(result);
    }

    @Test
    void testTestQuery_withSQLException_returnsFalse() throws SQLException {
        when(dataSource.getConnection()).thenThrow(new SQLException("Query failed"));
        
        boolean result = PostgreSQLConnectionTest.testQuery(dataSource);
        
        assertFalse(result);
    }

    @Test
    void testTableExists_withExistingTable_returnsTrue() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(metaData);
        when(metaData.getTables(isNull(), isNull(), eq("customer"), any())).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        
        boolean result = PostgreSQLConnectionTest.tableExists(dataSource, "CUSTOMER");
        
        assertTrue(result);
    }

    @Test
    void testTableExists_withNonExistingTable_returnsFalse() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(metaData);
        when(metaData.getTables(isNull(), isNull(), anyString(), any())).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);
        
        boolean result = PostgreSQLConnectionTest.tableExists(dataSource, "NONEXISTENT");
        
        assertFalse(result);
    }

    @Test
    void testTableExists_withNullDataSource_returnsFalse() {
        boolean result = PostgreSQLConnectionTest.tableExists(null, "CUSTOMER");
        
        assertFalse(result);
    }

    @Test
    void testTableExists_withNullTableName_returnsFalse() {
        boolean result = PostgreSQLConnectionTest.tableExists(dataSource, null);
        
        assertFalse(result);
    }

    @Test
    void testTableExists_withEmptyTableName_returnsFalse() {
        boolean result = PostgreSQLConnectionTest.tableExists(dataSource, "");
        
        assertFalse(result);
    }

    @Test
    void testTableExists_withSQLException_returnsFalse() throws SQLException {
        when(dataSource.getConnection()).thenThrow(new SQLException("Connection failed"));
        
        boolean result = PostgreSQLConnectionTest.tableExists(dataSource, "CUSTOMER");
        
        assertFalse(result);
    }
}
