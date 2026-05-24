package com.acme.modres;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Cookie;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LogoutServletTest {

    private LogoutServlet servlet;
    
    @Mock
    private HttpServletRequest request;
    
    @Mock
    private HttpServletResponse response;
    
    @Mock
    private HttpSession session;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        servlet = new LogoutServlet();
    }

    @Test
    void testDoGet_withSession_shouldInvalidateSession() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(request.getCookies()).thenReturn(null);
        servlet.doGet(request, response);
        verify(session).invalidate();
    }

    @Test
    void testDoGet_withoutSession_shouldNotThrowException() throws Exception {
        when(request.getSession(false)).thenReturn(null);
        when(request.getCookies()).thenReturn(null);
        assertDoesNotThrow(() -> servlet.doGet(request, response));
    }

    @Test
    void testDoGet_shouldRedirectToLoginPage() throws Exception {
        when(request.getSession(false)).thenReturn(null);
        when(request.getCookies()).thenReturn(null);
        servlet.doGet(request, response);
        verify(response).sendRedirect("login.jsp");
    }

    @Test
    void testDoGet_withCookies_shouldClearCookies() throws Exception {
        Cookie[] cookies = new Cookie[]{new Cookie("test", "value")};
        when(request.getSession(false)).thenReturn(null);
        when(request.getCookies()).thenReturn(cookies);
        servlet.doGet(request, response);
        verify(response, atLeastOnce()).addCookie(any(Cookie.class));
    }

    @Test
    void testDoGet_withNullCookies_shouldNotThrowException() throws Exception {
        when(request.getSession(false)).thenReturn(null);
        when(request.getCookies()).thenReturn(null);
        assertDoesNotThrow(() -> servlet.doGet(request, response));
    }

    @Test
    void testDoGet_shouldNotThrowException() {
        when(request.getSession(false)).thenReturn(null);
        when(request.getCookies()).thenReturn(null);
        assertDoesNotThrow(() -> servlet.doGet(request, response));
    }

    @Test
    void testDoGet_withException_shouldHandleGracefully() throws Exception {
        when(request.getSession(false)).thenThrow(new RuntimeException("Test exception"));
        when(request.getCookies()).thenReturn(null);
        assertDoesNotThrow(() -> servlet.doGet(request, response));
    }
}
