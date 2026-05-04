package com.acme.modres;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Cloud-native logout servlet using standard session management.
 * Migrated from WebSphere-specific WSSecurityHelper to standard HttpSession.
 * Session state is externalized to Amazon ElastiCache (Redis) via Spring Session.
 */
@WebServlet({ "/logout" })
public class LogoutServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  @Override
  protected void doGet(HttpServletRequest request,
      HttpServletResponse response) throws IOException {

    try {
      // Use standard servlet session invalidation
      // Session state is managed by Spring Session with Redis backend
      HttpSession session = request.getSession(false);
      if (session != null) {
        session.invalidate();
      }
      
      System.out.println("[INFO] User logged out successfully");
    } catch (Exception e) {
      System.err.println("[ERROR] Error logging out: " + e.getMessage());
      e.printStackTrace();
    }

    response.sendRedirect("login.jsp");
  }
}
