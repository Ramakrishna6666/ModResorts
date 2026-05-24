package com.acme.modres;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SecondFilterTest {

    private SecondFilter filter;
    
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
        filter = new SecondFilter();
        stringWriter = new StringWriter();
        writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
    }

    @Test
    void testInit_shouldNotThrowException() {
        assertDoesNotThrow(() -> filter.init(filterConfig));
    }

    @Test
    void testDoFilter_shouldSetContentType() throws Exception {
        BufferedReader reader = new BufferedReader(new StringReader("test content"));
        when(request.getReader()).thenReturn(reader);
        filter.doFilter(request, response, filterChain);
        verify(response).setContentType("text/plain");
    }

    @Test
    void testDoFilter_shouldReadRequestContent() throws Exception {
        BufferedReader reader = new BufferedReader(new StringReader("Hello"));
        when(request.getReader()).thenReturn(reader);
        filter.doFilter(request, response, filterChain);
        writer.flush();
        assertTrue(stringWriter.toString().contains("Hello"));
    }

    @Test
    void testDoFilter_shouldAppendToOurSite() throws Exception {
        BufferedReader reader = new BufferedReader(new StringReader("Welcome"));
        when(request.getReader()).thenReturn(reader);
        filter.doFilter(request, response, filterChain);
        writer.flush();
        assertTrue(stringWriter.toString().contains("to our site!"));
    }

    @Test
    void testDoFilter_shouldCallFilterChain() throws Exception {
        BufferedReader reader = new BufferedReader(new StringReader("test"));
        when(request.getReader()).thenReturn(reader);
        filter.doFilter(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDestroy_shouldNotThrowException() {
        assertDoesNotThrow(() -> filter.destroy());
    }

    @Test
    void testDoFilter_withEmptyContent_shouldHandleGracefully() throws Exception {
        BufferedReader reader = new BufferedReader(new StringReader(""));
        when(request.getReader()).thenReturn(reader);
        assertDoesNotThrow(() -> filter.doFilter(request, response, filterChain));
    }

    @Test
    void testDoFilter_shouldGetWriter() throws Exception {
        BufferedReader reader = new BufferedReader(new StringReader("test"));
        when(request.getReader()).thenReturn(reader);
        filter.doFilter(request, response, filterChain);
        verify(response, atLeastOnce()).getWriter();
    }

    @Test
    void testDoFilter_withMultilineContent_shouldConcatenate() throws Exception {
        BufferedReader reader = new BufferedReader(new StringReader("Line1\nLine2\nLine3"));
        when(request.getReader()).thenReturn(reader);
        filter.doFilter(request, response, filterChain);
        writer.flush();
        String output = stringWriter.toString();
        assertTrue(output.contains("Line1Line2Line3"));
    }
}
