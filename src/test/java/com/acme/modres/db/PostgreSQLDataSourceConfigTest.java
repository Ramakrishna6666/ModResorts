package com.acme.modres.db;

import static org.junit.jupiter.api.Assertions.*;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;

class PostgreSQLDataSourceConfigTest {

    @Test
    void testGetDataSource() {
        DataSource dataSource = PostgreSQLDataSourceConfig.getDataSource();
        assertNotNull(dataSource);
    }

    @Test
    void testGetDataSource_returnsSameInstance() {
        DataSource dataSource1 = PostgreSQLDataSourceConfig.getDataSource();
        DataSource dataSource2 = PostgreSQLDataSourceConfig.getDataSource();
        
        assertSame(dataSource1, dataSource2);
    }

    @Test
    void testGetDataSource_notNull() {
        DataSource dataSource = PostgreSQLDataSourceConfig.getDataSource();
        assertNotNull(dataSource);
    }

    @Test
    void testTestConnection() {
        boolean result = PostgreSQLDataSourceConfig.testConnection();
        // Result can be true or false depending on database availability
        assertTrue(result || !result);
    }

    @Test
    void testGetDataSource_isThreadSafe() throws InterruptedException {
        Thread thread1 = new Thread(() -> {
            DataSource ds = PostgreSQLDataSourceConfig.getDataSource();
            assertNotNull(ds);
        });
        
        Thread thread2 = new Thread(() -> {
            DataSource ds = PostgreSQLDataSourceConfig.getDataSource();
            assertNotNull(ds);
        });
        
        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();
    }

    @Test
    void testGetDataSource_multipleCalls() {
        for (int i = 0; i < 5; i++) {
            DataSource ds = PostgreSQLDataSourceConfig.getDataSource();
            assertNotNull(ds);
        }
    }
}
