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

class AvailabilityCheckerServletTest {

    private AvailabilityCheckerServlet servlet;
    
    @Mock
    private HttpServletRequest request;
    
    @Mock
    private HttpServletResponse response;
    
    private StringWriter stringWriter;
    private PrintWriter writer;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        servlet = new AvailabilityCheckerServlet();
        stringWriter = new StringWriter();
        writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
    }

    @Test
    void testInit_shouldNotThrowException() {
        assertDoesNotThrow(() -> servlet.init());
    }

    @Test
    void testDoGet_withValidDate_shouldNotThrowException() throws Exception {
        servlet.init();
        when(request.getParameter("date")).thenReturn("01/15/2024");
        assertDoesNotThrow(() -> servlet.doGet(request, response));
    }

    @Test
    void testDoGet_withInvalidDate_shouldHandleGracefully() throws Exception {
        servlet.init();
        when(request.getParameter("date")).thenReturn("invalid-date");
        assertDoesNotThrow(() -> servlet.doGet(request, response));
    }

    @Test
    void testDoGet_withNullDate_shouldHandleGracefully() throws Exception {
        servlet.init();
        when(request.getParameter("date")).thenReturn(null);
        assertDoesNotThrow(() -> servlet.doGet(request, response));
    }

    @Test
    void testDoGet_shouldSetContentType() throws Exception {
        servlet.init();
        when(request.getParameter("date")).thenReturn("01/15/2024");
        servlet.doGet(request, response);
        verify(response).setContentType("application/json");
    }

    @Test
    void testDoGet_shouldSetCharacterEncoding() throws Exception {
        servlet.init();
        when(request.getParameter("date")).thenReturn("01/15/2024");
        servlet.doGet(request, response);
        verify(response).setCharacterEncoding("UTF-8");
    }

    @Test
    void testDoPost_shouldCallDoGet() throws Exception {
        servlet.init();
        AvailabilityCheckerServlet spyServlet = spy(servlet);
        when(request.getParameter("date")).thenReturn("01/15/2024");
        doNothing().when(spyServlet).doGet(request, response);
        spyServlet.doPost(request, response);
        verify(spyServlet).doGet(request, response);
    }

    @Test
    void testExportRevervations_shouldReturnInteger() {
        servlet.init();
        int result = servlet.exportRevervations("01/15/2024");
        assertTrue(result == 0 || result == -1);
    }

    @Test
    void testExportRevervations_withNullDate_shouldHandleGracefully() {
        servlet.init();
        assertDoesNotThrow(() -> servlet.exportRevervations(null));
    }

    @Test
    void testDoGet_shouldWriteJsonResponse() throws Exception {
        servlet.init();
        when(request.getParameter("date")).thenReturn("01/15/2024");
        servlet.doGet(request, response);
        writer.flush();
        String output = stringWriter.toString();
        assertTrue(output.contains("availability"));
    }
}
