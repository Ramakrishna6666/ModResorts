package com.acme.modres;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Cookie;

import java.io.IOException;

@WebServlet({ "/logout" })
public class LogoutServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  @Override
  protected void doGet(HttpServletRequest request,
      HttpServletResponse response) throws IOException {

    try {
      // Replaced IBM WebSphere-specific WSSecurityHelper.revokeSSOCookies()
      // with standard Jakarta Servlet session invalidation and cookie clearing.
      // This provides equivalent logout functionality without IBM-specific dependencies.

      // Invalidate the HTTP session
      HttpSession session = request.getSession(false);
      if (session != null) {
        session.invalidate();
      }

      // Clear any SSO/LTPA cookies by setting them to expire immediately
      Cookie[] cookies = request.getCookies();
      if (cookies != null) {
        for (Cookie cookie : cookies) {
          Cookie expiredCookie = new Cookie(cookie.getName(), "");
          expiredCookie.setMaxAge(0);
          expiredCookie.setPath("/");
          expiredCookie.setHttpOnly(true);
          response.addCookie(expiredCookie);
        }
      }
    } catch (Exception e) {
      System.err.println("[ERROR] Error logging out");
      e.printStackTrace();
    }

    response.sendRedirect("login.jsp");
  }
}
