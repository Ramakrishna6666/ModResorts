package com.acme.modres;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Updated for Java 21 compatibility:
 * - Removed IBM WebSphere-specific com.ibm.websphere.servlet.response.ResponseUtils API
 *   which is not available outside WebSphere runtime.
 * - Replaced ResponseUtils.encodeDataString() with standard HTML entity encoding
 *   to prevent XSS vulnerabilities while maintaining the same functionality.
 */
@WebServlet("/resorts/upper")
public class UpperServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    response.setContentType("text/html");

    String originalStr = request.getParameter("input");
    if (originalStr == null) {
      originalStr = "";
    }

    String newStr = originalStr.toUpperCase();
    // Replaced IBM WebSphere ResponseUtils.encodeDataString() with standard HTML encoding
    newStr = encodeHtml(newStr);

    PrintWriter out = response.getWriter();
    out.print("<br/><b>upper case input " + newStr + "</b>");
  }

  /**
   * Encodes special HTML characters to prevent XSS.
   * Replaces the IBM WebSphere-specific ResponseUtils.encodeDataString() method.
   */
  private String encodeHtml(String input) {
    if (input == null) {
      return "";
    }
    return input
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#x27;");
  }
}
