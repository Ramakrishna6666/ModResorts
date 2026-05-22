package com.acme.modres;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class FirstFilterTest {

    private FirstFilter filter;

    @Mock
    private FilterConfig filterConfig;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain chain;

    private StringWriter stringWriter;
    private PrintWriter writer;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        filter = new FirstFilter();
        stringWriter = new StringWriter();
        writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
    }

    @Test
    void testInit() throws ServletException {
        assertDoesNotThrow(() -> filter.init(filterConfig));
    }

    @Test
    void testDoFilter_withUserParameter() throws IOException, ServletException {
        when(request.getParameter("user")).thenReturn("testUser");
        
        filter.doFilter(request, response, chain);
        
        verify(response).setContentType("text/plain");
        verify(chain).doFilter(request, response);
        writer.flush();
        assertTrue(stringWriter.toString().contains("Welcome testUser"));
    }

    @Test
    void testDoFilter_withNullUserParameter() throws IOException, ServletException {
        when(request.getParameter("user")).thenReturn(null);
        
        filter.doFilter(request, response, chain);
        
        verify(response).setContentType("text/plain");
        verify(chain).doFilter(request, response);
        writer.flush();
        assertTrue(stringWriter.toString().contains("Welcome defaultUser"));
    }

    @Test
    void testDoFilter_withEmptyUserParameter() throws IOException, ServletException {
        when(request.getParameter("user")).thenReturn("");
        
        filter.doFilter(request, response, chain);
        
        verify(response).setContentType("text/plain");
        verify(chain).doFilter(request, response);
    }

    @Test
    void testDoFilter_chainIsCalled() throws IOException, ServletException {
        when(request.getParameter("user")).thenReturn("testUser");
        
        filter.doFilter(request, response, chain);
        
        verify(chain, times(1)).doFilter(request, response);
    }

    @Test
    void testDestroy() {
        assertDoesNotThrow(() -> filter.destroy());
    }

    @Test
    void testFilterConstructor() {
        assertNotNull(filter);
    }
}
