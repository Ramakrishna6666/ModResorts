package com.acme.modres.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.logging.Logger;

import jakarta.servlet.ServletException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ExceptionHandlerTest {

    @Mock
    private Logger logger;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testHandleException_withException_throwsServletException() {
        Exception exception = new RuntimeException("Test exception");
        String errorMsg = "Error occurred";
        
        ServletException thrown = assertThrows(ServletException.class, () -> {
            ExceptionHandler.handleException(exception, errorMsg, logger);
        });
        
        assertEquals(errorMsg, thrown.getMessage());
        assertEquals(exception, thrown.getCause());
    }

    @Test
    void testHandleException_withNullException_throwsServletException() {
        String errorMsg = "Error occurred without exception";
        
        ServletException thrown = assertThrows(ServletException.class, () -> {
            ExceptionHandler.handleException(null, errorMsg, logger);
        });
        
        assertEquals(errorMsg, thrown.getMessage());
        assertNull(thrown.getCause());
    }

    @Test
    void testHandleException_withIOException() {
        Exception exception = new java.io.IOException("IO error");
        String errorMsg = "IO operation failed";
        
        ServletException thrown = assertThrows(ServletException.class, () -> {
            ExceptionHandler.handleException(exception, errorMsg, logger);
        });
        
        assertEquals(errorMsg, thrown.getMessage());
        assertTrue(thrown.getCause() instanceof java.io.IOException);
    }

    @Test
    void testHandleException_withSQLException() {
        Exception exception = new java.sql.SQLException("SQL error");
        String errorMsg = "Database operation failed";
        
        ServletException thrown = assertThrows(ServletException.class, () -> {
            ExceptionHandler.handleException(exception, errorMsg, logger);
        });
        
        assertEquals(errorMsg, thrown.getMessage());
        assertTrue(thrown.getCause() instanceof java.sql.SQLException);
    }

    @Test
    void testHandleException_withEmptyErrorMessage() {
        Exception exception = new RuntimeException("Test");
        String errorMsg = "";
        
        ServletException thrown = assertThrows(ServletException.class, () -> {
            ExceptionHandler.handleException(exception, errorMsg, logger);
        });
        
        assertEquals("", thrown.getMessage());
    }

    @Test
    void testHandleException_withNullErrorMessage() {
        Exception exception = new RuntimeException("Test");
        
        assertThrows(ServletException.class, () -> {
            ExceptionHandler.handleException(exception, null, logger);
        });
    }

    @Test
    void testConstructor() {
        assertDoesNotThrow(() -> new ExceptionHandler());
    }
}
