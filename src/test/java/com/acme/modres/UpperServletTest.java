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
    void testDoGet_withValidInput() throws ServletException, IOException {
        when(request.getParameter("input")).thenReturn("hello");
        
        servlet.doGet(request, response);
        
        verify(response).setContentType("text/html");
        String output = stringWriter.toString();
        assertTrue(output.contains("HELLO"));
    }

    @Test
    void testDoGet_withNullInput() throws ServletException, IOException {
        when(request.getParameter("input")).thenReturn(null);
        
        servlet.doGet(request, response);
        
        verify(response).setContentType("text/html");
    }

    @Test
    void testDoGet_withEmptyInput() throws ServletException, IOException {
        when(request.getParameter("input")).thenReturn("");
        
        servlet.doGet(request, response);
        
        verify(response).setContentType("text/html");
    }

    @Test
    void testDoGet_withSpecialCharacters() throws ServletException, IOException {
        when(request.getParameter("input")).thenReturn("<script>alert('xss')</script>");
        
        servlet.doGet(request, response);
        
        verify(response).setContentType("text/html");
        String output = stringWriter.toString();
        assertTrue(output.contains("&lt;") || output.contains("&gt;"));
    }

    @Test
    void testDoGet_withMixedCase() throws ServletException, IOException {
        when(request.getParameter("input")).thenReturn("HeLLo WoRLd");
        
        servlet.doGet(request, response);
        
        String output = stringWriter.toString();
        assertTrue(output.contains("HELLO WORLD"));
    }

    @Test
    void testDoGet_withNumbers() throws ServletException, IOException {
        when(request.getParameter("input")).thenReturn("test123");
        
        servlet.doGet(request, response);
        
        String output = stringWriter.toString();
        assertTrue(output.contains("TEST123"));
    }

    @Test
    void testDoGet_setsContentType() throws ServletException, IOException {
        when(request.getParameter("input")).thenReturn("test");
        
        servlet.doGet(request, response);
        
        verify(response).setContentType("text/html");
    }

    @Test
    void testDoGet_htmlEncodesOutput() throws ServletException, IOException {
        when(request.getParameter("input")).thenReturn("<>&\"'/");
        
        servlet.doGet(request, response);
        
        String output = stringWriter.toString();
        assertFalse(output.contains("<script>"));
    }

    @Test
    void testDoGet_withWhitespace() throws ServletException, IOException {
        when(request.getParameter("input")).thenReturn("  hello  ");
        
        servlet.doGet(request, response);
        
        String output = stringWriter.toString();
        assertTrue(output.contains("HELLO"));
    }
}
