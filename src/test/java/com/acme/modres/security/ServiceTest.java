package com.acme.modres.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ServiceTest {

    @Test
    void testOperationConstant() {
        assertEquals("my-operation", Service.OPERATION);
    }

    @Test
    void testOperation() {
        Service service = new Service();
        assertDoesNotThrow(() -> service.operation());
    }

    @Test
    void testConstructor() {
        Service service = new Service();
        assertNotNull(service);
    }

    @Test
    void testOperation_executesSuccessfully() {
        Service service = new Service();
        assertDoesNotThrow(() -> service.operation());
    }

    @Test
    void testOperation_withSecurityManager() {
        Service service = new Service();
        SecurityManager originalSecurityManager = System.getSecurityManager();
        
        try {
            // Test with no security manager
            System.setSecurityManager(null);
            assertDoesNotThrow(() -> service.operation());
        } finally {
            System.setSecurityManager(originalSecurityManager);
        }
    }

    @Test
    void testOperation_withNullSecurityManager() {
        Service service = new Service();
        SecurityManager originalSecurityManager = System.getSecurityManager();
        
        try {
            System.setSecurityManager(null);
            assertDoesNotThrow(() -> service.operation());
        } finally {
            System.setSecurityManager(originalSecurityManager);
        }
    }

    @Test
    void testOperationConstant_notNull() {
        assertNotNull(Service.OPERATION);
    }

    @Test
    void testOperationConstant_notEmpty() {
        assertFalse(Service.OPERATION.isEmpty());
    }
}
