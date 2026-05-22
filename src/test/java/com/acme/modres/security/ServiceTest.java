package com.acme.modres.security;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ServiceTest {

    private Service service;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() {
        service = new Service();
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    void testConstructor() {
        assertNotNull(service);
    }

    @Test
    void testOperation_executesSuccessfully() {
        assertDoesNotThrow(() -> service.operation());
    }

    @Test
    void testOperation_printsMessage() {
        service.operation();
        
        String output = outContent.toString();
        assertTrue(output.contains("Operation is executed"));
    }

    @Test
    void testOperation_withNullSecurityManager() {
        System.setSecurityManager(null);
        
        assertDoesNotThrow(() -> service.operation());
    }

    @Test
    void testOperationConstant() {
        assertEquals("my-operation", Service.OPERATION);
    }

    @Test
    void testOperationConstant_isNotNull() {
        assertNotNull(Service.OPERATION);
    }

    @Test
    void testOperationConstant_isNotEmpty() {
        assertFalse(Service.OPERATION.isEmpty());
    }

    @Test
    void testMultipleOperationCalls() {
        service.operation();
        service.operation();
        
        String output = outContent.toString();
        int count = output.split("Operation is executed").length - 1;
        assertEquals(2, count);
    }

    @Test
    void testServiceInstantiation() {
        Service service1 = new Service();
        Service service2 = new Service();
        
        assertNotNull(service1);
        assertNotNull(service2);
        assertNotSame(service1, service2);
    }

    @Test
    void testClassIsPublic() {
        assertTrue(java.lang.reflect.Modifier.isPublic(Service.class.getModifiers()));
    }

    @Test
    void testPackage() {
        assertEquals("com.acme.modres.security", service.getClass().getPackage().getName());
    }
}
