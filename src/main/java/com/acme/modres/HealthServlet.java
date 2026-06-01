package com.acme.modres;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Health check endpoint for container orchestration platforms (AWS ECS/EKS).
 * Responds to GET /health with HTTP 200 and a JSON status payload.
 * This endpoint is used by load balancers and container health probes.
 */
@WebServlet({ "/health" })
public class HealthServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    response.setStatus(HttpServletResponse.SC_OK);

    PrintWriter out = response.getWriter();
    out.print("{\"status\":\"UP\",\"application\":\"modresorts\"}");
    out.flush();
  }
}
