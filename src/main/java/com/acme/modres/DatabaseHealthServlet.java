package com.acme.modres;

import com.acme.modres.config.PostgreSQLDataSourceConfig;
import com.acme.modres.db.PostgreSQLDatabaseUtil;
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
 * Servlet for checking PostgreSQL database health and connection status.
 * Provides a REST endpoint for monitoring database connectivity.
 */
@WebServlet(name = "DatabaseHealthServlet", urlPatterns = {"/health/db"})
public class DatabaseHealthServlet extends HttpServlet {
    
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(DatabaseHealthServlet.class.getName());
    
    @Inject
    private PostgreSQLDataSourceConfig dataSourceConfig;
    
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        Map<String, Object> healthStatus = new HashMap<>();
        
        try {
            // Perform health check
            boolean isHealthy = PostgreSQLDatabaseUtil.healthCheck(dataSourceConfig.getDataSource());
            
            healthStatus.put("status", isHealthy ? "UP" : "DOWN");
            healthStatus.put("database", "PostgreSQL");
            healthStatus.put("timestamp", System.currentTimeMillis());
            
            if (isHealthy) {
                // Get additional database information
                Map<String, String> connectionInfo = 
                    PostgreSQLDatabaseUtil.getConnectionInfo(dataSourceConfig.getDataSource());
                
                healthStatus.put("databaseVersion", connectionInfo.get("databaseProductVersion"));
                healthStatus.put("currentDatabase", 
                    PostgreSQLDatabaseUtil.getCurrentDatabase(dataSourceConfig.getDataSource()));
                healthStatus.put("currentSchema", 
                    PostgreSQLDatabaseUtil.getCurrentSchema(dataSourceConfig.getDataSource()));
                healthStatus.put("sslEnabled", 
                    PostgreSQLDatabaseUtil.isSSLEnabled(dataSourceConfig.getDataSource()));
                
                // Check if customer table exists
                boolean customerTableExists = 
                    PostgreSQLDatabaseUtil.tableExists(dataSourceConfig.getDataSource(), "customer");
                healthStatus.put("customerTableExists", customerTableExists);
                
                if (customerTableExists) {
                    long rowCount = 
                        PostgreSQLDatabaseUtil.getTableRowCount(dataSourceConfig.getDataSource(), "customer");
                    healthStatus.put("customerRecordCount", rowCount);
                }
                
                response.setStatus(HttpServletResponse.SC_OK);
                LOGGER.log(Level.INFO, "Database health check: UP");
            } else {
                response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                LOGGER.log(Level.WARNING, "Database health check: DOWN");
            }
            
        } catch (Exception e) {
            healthStatus.put("status", "DOWN");
            healthStatus.put("error", e.getMessage());
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            LOGGER.log(Level.SEVERE, "Database health check failed with exception", e);
        }
        
        // Write JSON response
        try (PrintWriter out = response.getWriter()) {
            out.print(gson.toJson(healthStatus));
            out.flush();
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        doGet(request, response);
    }
}
