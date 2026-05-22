package com.acme.modres;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class UpperServletTest {

    private UpperServlet servlet;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private StringWriter stringWriter;
    private PrintWriter writer;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        servlet = new UpperServlet();
        stringWriter = new StringWriter();
        writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
    }

    @Test
    void testDoGet_withValidInput_convertsToUpperCase() throws ServletException, IOException {
        when(request.getParameter("input")).thenReturn("hello world");
        
        servlet.doGet(request, response);
        
        verify(response).setContentType("text/html");
        writer.flush();
        String output = stringWriter.toString();
        assertTrue(output.contains("HELLO WORLD"));
    }

    @Test
    void testDoGet_withNullInput_handlesGracefully() throws ServletException, IOException {
        when(request.getParameter("input")).thenReturn(null);
        
        servlet.doGet(request, response);
        
        verify(response).setContentType("text/html");
    }

    @Test
    void testDoGet_withEmptyInput_returnsEmpty() throws ServletException, IOException {
        when(request.getParameter("input")).thenReturn("");
        
        servlet.doGet(request, response);
        
        verify(response).setContentType("text/html");
    }

    @Test
    void testDoGet_withSpecialCharacters_escapesHtml() throws ServletException, IOException {
        when(request.getParameter("input")).thenReturn("<script>alert('xss')</script>");
        
        servlet.doGet(request, response);
        
        verify(response).setContentType("text/html");
        writer.flush();
        String output = stringWriter.toString();
        assertFalse(output.contains("<script>"));
    }

    @Test
    void testDoGet_withMixedCase_convertsToUpperCase() throws ServletException, IOException {
        when(request.getParameter("input")).thenReturn("TeSt");
        
        servlet.doGet(request, response);
        
        verify(response).setContentType("text/html");
        writer.flush();
        assertTrue(stringWriter.toString().contains("TEST"));
    }

    @Test
    void testDoGet_withNumbers_preservesNumbers() throws ServletException, IOException {
        when(request.getParameter("input")).thenReturn("test123");
        
        servlet.doGet(request, response);
        
        verify(response).setContentType("text/html");
        writer.flush();
        assertTrue(stringWriter.toString().contains("TEST123"));
    }

    @Test
    void testSerialVersionUID() {
        assertNotNull(servlet);
    }
}
