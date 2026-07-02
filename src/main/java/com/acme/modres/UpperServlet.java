package com.acme.modres;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
    // Replace WebSphere-specific ResponseUtils.encodeDataString with standard HTML encoding
    newStr = htmlEncode(newStr);

    PrintWriter out = response.getWriter();
    out.print("<br/><b>upper case input " + newStr + "</b>");
  }
  
  /**
   * HTML encode a string to prevent XSS attacks
   * Replaces WebSphere-specific ResponseUtils.encodeDataString
   */
  private String htmlEncode(String input) {
    if (input == null) {
      return null;
    }
    StringBuilder encoded = new StringBuilder(input.length());
    for (int i = 0; i < input.length(); i++) {
      char c = input.charAt(i);
      switch (c) {
        case '<':
          encoded.append("&lt;");
          break;
        case '>':
          encoded.append("&gt;");
          break;
        case '&':
          encoded.append("&amp;");
          break;
        case '"':
          encoded.append("&quot;");
          break;
        case '\'':
          encoded.append("&#x27;");
          break;
        case '/':
          encoded.append("&#x2F;");
          break;
        default:
          encoded.append(c);
      }
    }
    return encoded.toString();
  }
}
