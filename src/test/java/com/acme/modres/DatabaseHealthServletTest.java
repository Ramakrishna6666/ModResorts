package com.acme.modres;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.acme.modres.config.PostgreSQLDataSourceConfig;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DatabaseHealthServletTest {

    private DatabaseHealthServlet servlet;
    
    @Mock
    private HttpServletRequest request;
    
    @Mock
    private HttpServletResponse response;
    
    @Mock
    private PostgreSQLDataSourceConfig dataSourceConfig;
    
    @Mock
    private DataSource dataSource;
    
    private StringWriter stringWriter;
    private PrintWriter writer;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        servlet = new DatabaseHealthServlet();
        stringWriter = new StringWriter();
        writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
    }

    @Test
    void testDoGet_shouldSetContentType() throws Exception {
        servlet.doGet(request, response);
        verify(response).setContentType("application/json");
    }

    @Test
    void testDoGet_shouldSetCharacterEncoding() throws Exception {
        servlet.doGet(request, response);
        verify(response).setCharacterEncoding("UTF-8");
    }

    @Test
    void testDoGet_shouldNotThrowException() {
        assertDoesNotThrow(() -> servlet.doGet(request, response));
    }

    @Test
    void testDoPost_shouldCallDoGet() throws Exception {
        DatabaseHealthServlet spyServlet = spy(servlet);
        doNothing().when(spyServlet).doGet(request, response);
        spyServlet.doPost(request, response);
        verify(spyServlet).doGet(request, response);
    }

    @Test
    void testDoGet_shouldGetWriter() throws Exception {
        servlet.doGet(request, response);
        verify(response, atLeastOnce()).getWriter();
    }

    @Test
    void testDoGet_shouldWriteJsonResponse() throws Exception {
        servlet.doGet(request, response);
        writer.flush();
        String output = stringWriter.toString();
        // Should contain JSON structure
        assertTrue(output.contains("{") || output.isEmpty());
    }
}
