package com.acme.modres;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UpperServletTest {

    private UpperServlet servlet;
    
    @Mock
    private HttpServletRequest request;
    
    @Mock
    private HttpServletResponse response;
    
    private StringWriter stringWriter;
    private PrintWriter writer;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        servlet = new UpperServlet();
        stringWriter = new StringWriter();
        writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
    }

    @Test
    void testDoGet_shouldSetContentType() throws Exception {
        when(request.getParameter("input")).thenReturn("test");
        servlet.doGet(request, response);
        verify(response).setContentType("text/html");
    }

    @Test
    void testDoGet_withInput_shouldConvertToUpperCase() throws Exception {
        when(request.getParameter("input")).thenReturn("hello");
        servlet.doGet(request, response);
        writer.flush();
        assertTrue(stringWriter.toString().contains("HELLO"));
    }

    @Test
    void testDoGet_withNullInput_shouldHandleGracefully() throws Exception {
        when(request.getParameter("input")).thenReturn(null);
        servlet.doGet(request, response);
        writer.flush();
        assertNotNull(stringWriter.toString());
    }

    @Test
    void testDoGet_withEmptyInput_shouldHandleGracefully() throws Exception {
        when(request.getParameter("input")).thenReturn("");
        servlet.doGet(request, response);
        writer.flush();
        assertNotNull(stringWriter.toString());
    }

    @Test
    void testDoGet_shouldEscapeHtml() throws Exception {
        when(request.getParameter("input")).thenReturn("<script>alert('xss')</script>");
        servlet.doGet(request, response);
        writer.flush();
        String output = stringWriter.toString();
        // Should escape HTML entities
        assertFalse(output.contains("<script>"));
    }

    @Test
    void testDoGet_shouldNotThrowException() {
        when(request.getParameter("input")).thenReturn("test");
        assertDoesNotThrow(() -> servlet.doGet(request, response));
    }

    @Test
    void testDoGet_shouldGetWriter() throws Exception {
        when(request.getParameter("input")).thenReturn("test");
        servlet.doGet(request, response);
        verify(response, atLeastOnce()).getWriter();
    }

    @Test
    void testDoGet_withSpecialCharacters_shouldHandleCorrectly() throws Exception {
        when(request.getParameter("input")).thenReturn("test@#$%");
        servlet.doGet(request, response);
        writer.flush();
        assertTrue(stringWriter.toString().contains("TEST"));
    }
}
