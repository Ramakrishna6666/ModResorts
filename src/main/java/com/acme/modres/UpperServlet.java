package com.acme.modres;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.util.HtmlUtils;

/**
 * Cloud-native servlet using standard encoding utilities.
 * Migrated from WebSphere-specific ResponseUtils to Spring's HtmlUtils.
 */
@WebServlet("/resorts/upper")
public class UpperServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    response.setContentType("text/html");
    response.setCharacterEncoding("UTF-8");

    String originalStr = request.getParameter("input");
    if (originalStr == null) {
      originalStr = "";
    }

    String newStr = originalStr.toUpperCase();
    // Use Spring's HtmlUtils for HTML encoding (cloud-native alternative to WebSphere ResponseUtils)
    newStr = HtmlUtils.htmlEscape(newStr);

    try (PrintWriter out = response.getWriter()) {
      out.print("<br/><b>upper case input " + newStr + "</b>");
    }
  }
}
