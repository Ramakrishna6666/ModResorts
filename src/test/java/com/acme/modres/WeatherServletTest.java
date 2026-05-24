package com.acme.modres;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletOutputStream;
import com.acme.modres.db.ModResortsCustomerInformation;
import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WeatherServletTest {

    private WeatherServlet servlet;
    
    @Mock
    private HttpServletRequest request;
    
    @Mock
    private HttpServletResponse response;
    
    @Mock
    private ModResortsCustomerInformation customerInfo;
    
    @Mock
    private ServletOutputStream outputStream;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        servlet = new WeatherServlet();
        when(response.getOutputStream()).thenReturn(outputStream);
    }

    @Test
    void testInit_shouldNotThrowException() {
        assertDoesNotThrow(() -> servlet.init());
    }

    @Test
    void testDestroy_shouldNotThrowException() {
        servlet.init();
        assertDoesNotThrow(() -> servlet.destroy());
    }

    @Test
    void testDoGet_withValidCity_shouldNotThrowException() throws Exception {
        when(request.getParameter("selectedCity")).thenReturn("Paris");
        assertDoesNotThrow(() -> servlet.doGet(request, response));
    }

    @Test
    void testDoGet_withNullCity_shouldHandleGracefully() throws Exception {
        when(request.getParameter("selectedCity")).thenReturn(null);
        assertDoesNotThrow(() -> servlet.doGet(request, response));
    }

    @Test
    void testDoPost_shouldCallDoGet() throws Exception {
        WeatherServlet spyServlet = spy(servlet);
        when(request.getParameter("selectedCity")).thenReturn("Paris");
        doNothing().when(spyServlet).doGet(request, response);
        spyServlet.doPost(request, response);
        verify(spyServlet).doGet(request, response);
    }

    @Test
    void testDoGet_withParis_shouldProcessRequest() throws Exception {
        when(request.getParameter("selectedCity")).thenReturn("Paris");
        assertDoesNotThrow(() -> servlet.doGet(request, response));
    }

    @Test
    void testDoGet_withLasVegas_shouldProcessRequest() throws Exception {
        when(request.getParameter("selectedCity")).thenReturn("Las_Vegas");
        assertDoesNotThrow(() -> servlet.doGet(request, response));
    }

    @Test
    void testDoGet_withInvalidCity_shouldHandleError() throws Exception {
        when(request.getParameter("selectedCity")).thenReturn("InvalidCity");
        assertThrows(Exception.class, () -> servlet.doGet(request, response));
    }
}
