package com.acme.modres.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import jakarta.servlet.ServletException;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionHandlerTest {

    private Logger logger;

    @BeforeEach
    void setUp() {
        logger = Logger.getLogger(ExceptionHandlerTest.class.getName());
    }

    @Test
    void testHandleException_withNullException_shouldThrowServletException() {
        assertThrows(ServletException.class, () -> {
            ExceptionHandler.handleException(null, "Error message", logger);
        });
    }

    @Test
    void testHandleException_withException_shouldThrowServletException() {
        Exception ex = new Exception("Test exception");
        assertThrows(ServletException.class, () -> {
            ExceptionHandler.handleException(ex, "Error occurred", logger);
        });
    }

    @Test
    void testHandleException_withNullException_shouldIncludeMessage() {
        try {
            ExceptionHandler.handleException(null, "Custom error", logger);
            fail("Should have thrown ServletException");
        } catch (ServletException e) {
            assertEquals("Custom error", e.getMessage());
        }
    }

    @Test
    void testHandleException_withException_shouldIncludeMessage() {
        Exception ex = new Exception("Original exception");
        try {
            ExceptionHandler.handleException(ex, "Wrapped error", logger);
            fail("Should have thrown ServletException");
        } catch (ServletException e) {
            assertEquals("Wrapped error", e.getMessage());
        }
    }

    @Test
    void testHandleException_withException_shouldIncludeCause() {
        Exception ex = new Exception("Original exception");
        try {
            ExceptionHandler.handleException(ex, "Wrapped error", logger);
            fail("Should have thrown ServletException");
        } catch (ServletException e) {
            assertEquals(ex, e.getCause());
        }
    }

    @Test
    void testHandleException_withNullLogger_shouldNotThrowNullPointer() {
        Exception ex = new Exception("Test");
        assertThrows(ServletException.class, () -> {
            ExceptionHandler.handleException(ex, "Error", null);
        });
    }

    @Test
    void testHandleException_withEmptyMessage_shouldThrowException() {
        assertThrows(ServletException.class, () -> {
            ExceptionHandler.handleException(null, "", logger);
        });
    }
}
