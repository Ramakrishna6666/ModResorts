package com.acme.modres;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

// WebSphere specific API removed for Java 21 compatibility
// import com.ibm.websphere.security.WSSecurityHelper;

import java.io.IOException;

@WebServlet({ "/logout" })
public class LogoutServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  @Override
  protected void doGet(HttpServletRequest request,
      HttpServletResponse response) throws IOException {

    try {
      // Replaced WebSphere-specific logout with standard servlet session invalidation
      HttpSession session = request.getSession(false);
      if (session != null) {
        session.invalidate();
      }
      // WSSecurityHelper.revokeSSOCookies(request, response);
    } catch (Exception e) {
      System.err.println("[ERROR] Error logging out");
      e.printStackTrace();
    }

    response.sendRedirect("login.jsp");
  }
}
