package com.acme.modres;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// Replaced WebSphere-specific com.ibm.websphere.security.WSSecurityHelper with standard servlet session invalidation (blocker-1, blocker-6)

import java.io.IOException;

@WebServlet({ "/logout" })
public class LogoutServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  @Override
  protected void doGet(HttpServletRequest request,
      HttpServletResponse response) throws IOException {

    try {
      // Replaced WSSecurityHelper.revokeSSOCookies() with standard HttpSession invalidation
      // for container-native deployment on AWS ECS/EKS (blocker-1, blocker-6)
      if (request.getSession(false) != null) {
        request.getSession(false).invalidate();
      }
    } catch (Exception e) {
      System.err.println("[ERROR] Error logging out");
      e.printStackTrace();
    }

    response.sendRedirect("login.jsp");
  }
}
