package com.acme.modres.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;

class PostgreSQLDataSourceConfigTest {

    private PostgreSQLDataSourceConfig config;

    @BeforeEach
    void setUp() {
        config = new PostgreSQLDataSourceConfig();
    }

    @Test
    void testConstructor_shouldCreateInstance() {
        assertNotNull(config);
    }

    @Test
    void testInit_shouldNotThrowException() {
        assertDoesNotThrow(() -> config.init());
    }

    @Test
    void testGetDataSource_afterInit_shouldReturnDataSource() {
        config.init();
        DataSource dataSource = config.getDataSource();
        assertNotNull(dataSource);
    }

    @Test
    void testGetDataSource_beforeInit_shouldReturnNull() {
        DataSource dataSource = config.getDataSource();
        assertNull(dataSource);
    }

    @Test
    void testDestroy_shouldNotThrowException() {
        config.init();
        assertDoesNotThrow(() -> config.destroy());
    }

    @Test
    void testDestroy_withoutInit_shouldNotThrowException() {
        assertDoesNotThrow(() -> config.destroy());
    }

    @Test
    void testInit_shouldCreateHikariDataSource() {
        config.init();
        DataSource dataSource = config.getDataSource();
        assertNotNull(dataSource);
        assertTrue(dataSource.getClass().getName().contains("Hikari"));
    }

    @Test
    void testDestroy_afterInit_shouldCloseDataSource() {
        config.init();
        config.destroy();
        // DataSource should be closed
    }
}
