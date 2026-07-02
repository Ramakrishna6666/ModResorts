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
    void testHandleException_withException() {
        Exception exception = new Exception("Test exception");
        String errorMsg = "Error occurred";
        
        assertThrows(ServletException.class, () -> {
            ExceptionHandler.handleException(exception, errorMsg, logger);
        });
    }

    @Test
    void testHandleException_withNullException() {
        String errorMsg = "Error occurred";
        
        assertThrows(ServletException.class, () -> {
            ExceptionHandler.handleException(null, errorMsg, logger);
        });
    }

    @Test
    void testHandleException_withEmptyMessage() {
        Exception exception = new Exception("Test exception");
        String errorMsg = "";
        
        assertThrows(ServletException.class, () -> {
            ExceptionHandler.handleException(exception, errorMsg, logger);
        });
    }

    @Test
    void testHandleException_throwsServletException() {
        Exception exception = new Exception("Test exception");
        String errorMsg = "Error occurred";
        
        ServletException thrown = assertThrows(ServletException.class, () -> {
            ExceptionHandler.handleException(exception, errorMsg, logger);
        });
        
        assertNotNull(thrown);
        assertEquals(errorMsg, thrown.getMessage());
    }

    @Test
    void testHandleException_withNullExceptionThrowsServletException() {
        String errorMsg = "Error occurred";
        
        ServletException thrown = assertThrows(ServletException.class, () -> {
            ExceptionHandler.handleException(null, errorMsg, logger);
        });
        
        assertNotNull(thrown);
        assertEquals(errorMsg, thrown.getMessage());
    }

    @Test
    void testHandleException_withCause() {
        Exception exception = new Exception("Test exception");
        String errorMsg = "Error occurred";
        
        ServletException thrown = assertThrows(ServletException.class, () -> {
            ExceptionHandler.handleException(exception, errorMsg, logger);
        });
        
        assertNotNull(thrown.getCause());
    }

    @Test
    void testHandleException_withNullMessage() {
        Exception exception = new Exception("Test exception");
        
        assertThrows(ServletException.class, () -> {
            ExceptionHandler.handleException(exception, null, logger);
        });
    }
}
