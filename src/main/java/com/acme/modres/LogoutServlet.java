package com.acme.modres;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Updated for Java 21 compatibility:
 * - Removed IBM WebSphere-specific com.ibm.websphere.security.WSSecurityHelper API
 *   which is not available outside WebSphere runtime.
 * - Replaced with standard Java EE session invalidation and cookie clearing.
 */
@WebServlet({ "/logout" })
public class LogoutServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  @Override
  protected void doGet(HttpServletRequest request,
      HttpServletResponse response) throws IOException {

    try {
      // Invalidate the HTTP session (replaces WSSecurityHelper.revokeSSOCookies)
      HttpSession session = request.getSession(false);
      if (session != null) {
        session.invalidate();
      }

      // Clear any SSO/authentication cookies
      Cookie[] cookies = request.getCookies();
      if (cookies != null) {
        for (Cookie cookie : cookies) {
          cookie.setValue("");
          cookie.setPath("/");
          cookie.setMaxAge(0);
          response.addCookie(cookie);
        }
      }
    } catch (Exception e) {
      System.err.println("[ERROR] Error logging out");
      e.printStackTrace();
    }

    response.sendRedirect("login.jsp");
  }
}
