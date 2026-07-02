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
      // Replace WebSphere-specific WSSecurityHelper.revokeSSOCookies with standard servlet logout
      // Invalidate the session
      HttpSession session = request.getSession(false);
      if (session != null) {
        session.invalidate();
      }
      
      // Clear authentication cookies
      Cookie[] cookies = request.getCookies();
      if (cookies != null) {
        for (Cookie cookie : cookies) {
          // Clear common authentication cookies
          if (cookie.getName().startsWith("JSESSIONID") || 
              cookie.getName().startsWith("LtpaToken") ||
              cookie.getName().startsWith("LtpaToken2")) {
            Cookie clearCookie = new Cookie(cookie.getName(), "");
            clearCookie.setMaxAge(0);
            clearCookie.setPath("/");
            response.addCookie(clearCookie);
          }
        }
      }
      
      // Logout using standard servlet API (if available)
      try {
        request.logout();
      } catch (Exception e) {
        // logout() may not be supported in all containers, ignore
      }
      
    } catch (Exception e) {
      System.err.println("[ERROR] Error logging out");
      e.printStackTrace();
    }

    response.sendRedirect("login.jsp");
  }
}
