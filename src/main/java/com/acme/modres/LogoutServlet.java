package com.acme.modres;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet({ "/logout" })
public class LogoutServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  @Override
  protected void doGet(HttpServletRequest request,
      HttpServletResponse response) throws IOException {

    try {
      // Replace WebSphere-specific WSSecurityHelper with standard session invalidation
      HttpSession session = request.getSession(false);
      if (session != null) {
        session.invalidate();
      }
      
      // Clear authentication by removing cookies
      jakarta.servlet.http.Cookie[] cookies = request.getCookies();
      if (cookies != null) {
        for (jakarta.servlet.http.Cookie cookie : cookies) {
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
