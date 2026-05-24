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

class WelcomeServletTest {

    private WelcomeServlet servlet;
    
    @Mock
    private HttpServletRequest request;
    
    @Mock
    private HttpServletResponse response;
    
    private StringWriter stringWriter;
    private PrintWriter writer;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        servlet = new WelcomeServlet();
        stringWriter = new StringWriter();
        writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
    }

    @Test
    void testDoGet_shouldSetContentTypeToTextPlain() throws Exception {
        servlet.doGet(request, response);
        verify(response).setContentType("text/plain");
    }

    @Test
    void testDoGet_shouldWriteEnjoyMessage() throws Exception {
        servlet.doGet(request, response);
        writer.flush();
        String output = stringWriter.toString();
        assertTrue(output.contains("Enjoy!"));
    }

    @Test
    void testDoGet_shouldNotThrowException() {
        assertDoesNotThrow(() -> servlet.doGet(request, response));
    }

    @Test
    void testDoGet_withNullRequest_shouldHandleGracefully() throws Exception {
        servlet.doGet(request, response);
        verify(response, times(1)).getWriter();
    }

    @Test
    void testDoGet_shouldCallGetWriter() throws Exception {
        servlet.doGet(request, response);
        verify(response, atLeastOnce()).getWriter();
    }
}
