package com.acme.modres;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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
    void testDoGet_withValidSession() throws IOException {
        when(request.getSession(false)).thenReturn(session);
        
        servlet.doGet(request, response);
        
        verify(session).invalidate();
        verify(response).sendRedirect("login.jsp");
    }

    @Test
    void testDoGet_withNullSession() throws IOException {
        when(request.getSession(false)).thenReturn(null);
        
        servlet.doGet(request, response);
        
        verify(response).sendRedirect("login.jsp");
    }

    @Test
    void testDoGet_withCookies() throws IOException {
        Cookie[] cookies = new Cookie[] {
            new Cookie("JSESSIONID", "test123"),
            new Cookie("LtpaToken", "token123"),
            new Cookie("LtpaToken2", "token456")
        };
        when(request.getSession(false)).thenReturn(session);
        when(request.getCookies()).thenReturn(cookies);
        
        servlet.doGet(request, response);
        
        verify(response, atLeastOnce()).addCookie(any(Cookie.class));
        verify(response).sendRedirect("login.jsp");
    }

    @Test
    void testDoGet_withNoCookies() throws IOException {
        when(request.getSession(false)).thenReturn(session);
        when(request.getCookies()).thenReturn(null);
        
        servlet.doGet(request, response);
        
        verify(session).invalidate();
        verify(response).sendRedirect("login.jsp");
    }

    @Test
    void testDoGet_redirectsToLoginPage() throws IOException {
        when(request.getSession(false)).thenReturn(null);
        
        servlet.doGet(request, response);
        
        verify(response).sendRedirect("login.jsp");
    }

    @Test
    void testDoGet_clearsJSESSIONIDCookie() throws IOException {
        Cookie jsessionCookie = new Cookie("JSESSIONID", "test123");
        when(request.getCookies()).thenReturn(new Cookie[] { jsessionCookie });
        when(request.getSession(false)).thenReturn(session);
        
        servlet.doGet(request, response);
        
        verify(response, atLeastOnce()).addCookie(any(Cookie.class));
    }

    @Test
    void testDoGet_clearsLtpaTokenCookie() throws IOException {
        Cookie ltpaCookie = new Cookie("LtpaToken", "token123");
        when(request.getCookies()).thenReturn(new Cookie[] { ltpaCookie });
        when(request.getSession(false)).thenReturn(session);
        
        servlet.doGet(request, response);
        
        verify(response, atLeastOnce()).addCookie(any(Cookie.class));
    }

    @Test
    void testDoGet_handlesLogoutException() throws IOException, ServletException {
        when(request.getSession(false)).thenReturn(session);
        doThrow(new ServletException("Logout failed")).when(request).logout();
        
        assertDoesNotThrow(() -> servlet.doGet(request, response));
        verify(response).sendRedirect("login.jsp");
    }
}
