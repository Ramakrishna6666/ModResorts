package com.acme.modres;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class WeatherServletTest {

    private WeatherServlet servlet;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private StringWriter stringWriter;
    private TestServletOutputStream outputStream;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        servlet = new WeatherServlet();
        stringWriter = new StringWriter();
        outputStream = new TestServletOutputStream();
        when(response.getOutputStream()).thenReturn(outputStream);
    }

    @Test
    void testInit() {
        assertDoesNotThrow(() -> servlet.init());
    }

    @Test
    void testDestroy() {
        servlet.init();
        assertDoesNotThrow(() -> servlet.destroy());
    }

    @Test
    void testDoGet_withValidCity() throws ServletException, IOException {
        when(request.getParameter("selectedCity")).thenReturn(Constants.PARIS);
        
        servlet.init();
        servlet.doGet(request, response);
        
        verify(response).setContentType("application/json");
    }

    @Test
    void testDoGet_withNullCity() throws ServletException, IOException {
        when(request.getParameter("selectedCity")).thenReturn(null);
        
        servlet.init();
        
        assertThrows(ServletException.class, () -> {
            servlet.doGet(request, response);
        });
    }

    @Test
    void testDoGet_withInvalidCity() throws ServletException, IOException {
        when(request.getParameter("selectedCity")).thenReturn("InvalidCity");
        
        servlet.init();
        
        assertThrows(ServletException.class, () -> {
            servlet.doGet(request, response);
        });
    }

    @Test
    void testDoGet_withParis() throws ServletException, IOException {
        when(request.getParameter("selectedCity")).thenReturn(Constants.PARIS);
        
        servlet.init();
        servlet.doGet(request, response);
        
        verify(response).setContentType("application/json");
    }

    @Test
    void testDoGet_withLasVegas() throws ServletException, IOException {
        when(request.getParameter("selectedCity")).thenReturn(Constants.LAS_VEGAS);
        
        servlet.init();
        servlet.doGet(request, response);
        
        verify(response).setContentType("application/json");
    }

    @Test
    void testDoGet_withSanFrancisco() throws ServletException, IOException {
        when(request.getParameter("selectedCity")).thenReturn(Constants.SAN_FRANCISCO);
        
        servlet.init();
        servlet.doGet(request, response);
        
        verify(response).setContentType("application/json");
    }

    @Test
    void testDoGet_withMiami() throws ServletException, IOException {
        when(request.getParameter("selectedCity")).thenReturn(Constants.MIAMI);
        
        servlet.init();
        servlet.doGet(request, response);
        
        verify(response).setContentType("application/json");
    }

    @Test
    void testDoGet_withCork() throws ServletException, IOException {
        when(request.getParameter("selectedCity")).thenReturn(Constants.CORK);
        
        servlet.init();
        servlet.doGet(request, response);
        
        verify(response).setContentType("application/json");
    }

    @Test
    void testDoGet_withBarcelona() throws ServletException, IOException {
        when(request.getParameter("selectedCity")).thenReturn(Constants.BARCELONA);
        
        servlet.init();
        servlet.doGet(request, response);
        
        verify(response).setContentType("application/json");
    }

    @Test
    void testDoPost_callsDoGet() throws ServletException, IOException {
        when(request.getParameter("selectedCity")).thenReturn(Constants.PARIS);
        
        servlet.init();
        servlet.doPost(request, response);
        
        verify(response).setContentType("application/json");
    }

    // Helper class for testing ServletOutputStream
    private static class TestServletOutputStream extends ServletOutputStream {
        private final StringWriter writer = new StringWriter();

        @Override
        public void write(int b) throws IOException {
            writer.write(b);
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {
        }

        public String getContent() {
            return writer.toString();
        }
    }
}
