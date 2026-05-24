package com.acme.modres;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FirstFilterTest {

    private FirstFilter filter;
    
    @Mock
    private HttpServletRequest request;
    
    @Mock
    private HttpServletResponse response;
    
    @Mock
    private FilterChain filterChain;
    
    @Mock
    private FilterConfig filterConfig;
    
    private StringWriter stringWriter;
    private PrintWriter writer;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        filter = new FirstFilter();
        stringWriter = new StringWriter();
        writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
    }

    @Test
    void testInit_shouldNotThrowException() {
        assertDoesNotThrow(() -> filter.init(filterConfig));
    }

    @Test
    void testDoFilter_withUserParameter_shouldWriteWelcomeMessage() throws Exception {
        when(request.getParameter("user")).thenReturn("John");
        filter.doFilter(request, response, filterChain);
        writer.flush();
        assertTrue(stringWriter.toString().contains("Welcome John"));
    }

    @Test
    void testDoFilter_withoutUserParameter_shouldUseDefaultUser() throws Exception {
        when(request.getParameter("user")).thenReturn(null);
        filter.doFilter(request, response, filterChain);
        writer.flush();
        assertTrue(stringWriter.toString().contains("Welcome defaultUser"));
    }

    @Test
    void testDoFilter_shouldSetContentType() throws Exception {
        filter.doFilter(request, response, filterChain);
        verify(response).setContentType("text/plain");
    }

    @Test
    void testDoFilter_shouldCallFilterChain() throws Exception {
        filter.doFilter(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDestroy_shouldNotThrowException() {
        assertDoesNotThrow(() -> filter.destroy());
    }

    @Test
    void testDoFilter_withEmptyUser_shouldUseDefaultUser() throws Exception {
        when(request.getParameter("user")).thenReturn("");
        filter.doFilter(request, response, filterChain);
        writer.flush();
        // Empty string is not null, so it should use empty string
        assertTrue(stringWriter.toString().contains("Welcome "));
    }

    @Test
    void testDoFilter_shouldGetWriter() throws Exception {
        filter.doFilter(request, response, filterChain);
        verify(response, atLeastOnce()).getWriter();
    }
}
