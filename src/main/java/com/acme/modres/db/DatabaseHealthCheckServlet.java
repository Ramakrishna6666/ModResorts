package com.acme.modres.db;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Database Health Check Servlet
 * Provides health check endpoint for PostgreSQL database connectivity
 * 
 * Access: GET /db-health
 */
@WebServlet("/db-health")
public class DatabaseHealthCheckServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(DatabaseHealthCheckServlet.class.getName());
    
    @Inject
    private PostgreSQLDataSourceConfig dataSourceConfig;
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        logger.info("Database health check requested");
        
        Map<String, Object> healthStatus = new HashMap<>();
        boolean isHealthy = false;
        
        try {
            if (dataSourceConfig == null || dataSourceConfig.getDataSource() == null) {
                healthStatus.put("status", "DOWN");
                healthStatus.put("message", "DataSource not available");
                response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            } else {
                // Test connection
                boolean connectionOk = PostgreSQLUtils.testConnection(dataSourceConfig.getDataSource());
                
                if (connectionOk) {
                    healthStatus.put("status", "UP");
                    healthStatus.put("message", "Database connection successful");
                    
                    // Get additional info
                    String version = PostgreSQLUtils.getPostgreSQLVersion(dataSourceConfig.getDataSource());
                    String database = PostgreSQLUtils.getCurrentDatabase(dataSourceConfig.getDataSource());
                    String schema = PostgreSQLUtils.getCurrentSchema(dataSourceConfig.getDataSource());
                    
                    Map<String, String> details = new HashMap<>();
                    details.put("version", version);
                    details.put("database", database);
                    details.put("schema", schema);
                    
                    // Check if customer table exists
                    boolean customerTableExists = PostgreSQLUtils.tableExists(
                        dataSourceConfig.getDataSource(), "customer", "public");
                    details.put("customerTableExists", String.valueOf(customerTableExists));
                    
                    if (customerTableExists) {
                        long rowCount = PostgreSQLUtils.getTableRowCount(
                            dataSourceConfig.getDataSource(), "customer", "public");
                        details.put("customerRowCount", String.valueOf(rowCount));
                    }
                    
                    healthStatus.put("details", details);
                    response.setStatus(HttpServletResponse.SC_OK);
                    isHealthy = true;
                } else {
                    healthStatus.put("status", "DOWN");
                    healthStatus.put("message", "Database connection failed");
                    response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error during health check", e);
            healthStatus.put("status", "DOWN");
            healthStatus.put("message", "Health check error: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        
        // Add timestamp
        healthStatus.put("timestamp", System.currentTimeMillis());
        healthStatus.put("timestampReadable", new java.util.Date().toString());
        
        // Return JSON response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonResponse = gson.toJson(healthStatus);
        
        PrintWriter out = response.getWriter();
        out.print(jsonResponse);
        out.flush();
        
        logger.log(Level.INFO, "Health check completed. Status: {0}", 
                  isHealthy ? "HEALTHY" : "UNHEALTHY");
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        doGet(request, response);
    }
}
