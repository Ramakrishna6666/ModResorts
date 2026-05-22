package com.acme.modres.db;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Properties;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;

class PostgreSQLDataSourceConfigTest {

    @Test
    void testCreateDataSource_withDefaultProperties() {
        DataSource dataSource = PostgreSQLDataSourceConfig.createDataSource();
        
        assertNotNull(dataSource);
    }

    @Test
    void testCreateDataSource_withCustomProperties() {
        Properties props = new Properties();
        props.setProperty("db.url", "jdbc:postgresql://localhost:5432/testdb");
        props.setProperty("db.username", "testuser");
        props.setProperty("db.password", "testpass");
        props.setProperty("db.schema", "public");
        props.setProperty("db.postgresql.ssl", "false");
        props.setProperty("db.postgresql.sslmode", "prefer");
        props.setProperty("db.postgresql.connectTimeout", "10");
        props.setProperty("db.postgresql.socketTimeout", "30");
        props.setProperty("db.postgresql.tcpKeepAlive", "true");
        
        DataSource dataSource = PostgreSQLDataSourceConfig.createDataSource(props);
        
        assertNotNull(dataSource);
    }

    @Test
    void testCreateDataSource_withMinimalProperties() {
        Properties props = new Properties();
        props.setProperty("db.url", "jdbc:postgresql://localhost:5432/modresorts");
        props.setProperty("db.username", "postgres");
        props.setProperty("db.password", "postgres");
        
        DataSource dataSource = PostgreSQLDataSourceConfig.createDataSource(props);
        
        assertNotNull(dataSource);
    }

    @Test
    void testCreateDataSource_withDifferentPort() {
        Properties props = new Properties();
        props.setProperty("db.url", "jdbc:postgresql://localhost:5433/modresorts");
        props.setProperty("db.username", "postgres");
        props.setProperty("db.password", "postgres");
        
        DataSource dataSource = PostgreSQLDataSourceConfig.createDataSource(props);
        
        assertNotNull(dataSource);
    }

    @Test
    void testCreateDataSource_withSSLEnabled() {
        Properties props = new Properties();
        props.setProperty("db.url", "jdbc:postgresql://localhost:5432/modresorts");
        props.setProperty("db.username", "postgres");
        props.setProperty("db.password", "postgres");
        props.setProperty("db.postgresql.ssl", "true");
        props.setProperty("db.postgresql.sslmode", "require");
        
        DataSource dataSource = PostgreSQLDataSourceConfig.createDataSource(props);
        
        assertNotNull(dataSource);
    }

    @Test
    void testCreateDataSource_withCustomTimeouts() {
        Properties props = new Properties();
        props.setProperty("db.url", "jdbc:postgresql://localhost:5432/modresorts");
        props.setProperty("db.username", "postgres");
        props.setProperty("db.password", "postgres");
        props.setProperty("db.postgresql.connectTimeout", "20");
        props.setProperty("db.postgresql.socketTimeout", "60");
        
        DataSource dataSource = PostgreSQLDataSourceConfig.createDataSource(props);
        
        assertNotNull(dataSource);
    }

    @Test
    void testCreateDataSource_withInvalidUrl_throwsException() {
        Properties props = new Properties();
        props.setProperty("db.url", "invalid-url");
        props.setProperty("db.username", "postgres");
        props.setProperty("db.password", "postgres");
        
        assertThrows(RuntimeException.class, () -> {
            PostgreSQLDataSourceConfig.createDataSource(props);
        });
    }

    @Test
    void testCreateDataSource_withEmptyProperties() {
        Properties props = new Properties();
        
        assertThrows(RuntimeException.class, () -> {
            PostgreSQLDataSourceConfig.createDataSource(props);
        });
    }
}
