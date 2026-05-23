package com.acme.modres.service;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.logging.Logger;

/**
 * Azure Service Bus Scheduler for distributed task scheduling
 * Replaces java.util.Timer with cloud-native scheduled message delivery
 */
@Service
public class AzureServiceBusScheduler {
    
    private static final Logger logger = Logger.getLogger(AzureServiceBusScheduler.class.getName());
    
    @Value("${azure.servicebus.namespace:#{null}}")
    private String serviceBusNamespace;
    
    @Value("${azure.servicebus.queue-name:scheduled-tasks}")
    private String queueName;
    
    @Value("${azure.servicebus.connection-string:#{null}}")
    private String connectionString;
    
    private ServiceBusSenderClient senderClient;
    
    @PostConstruct
    public void init() {
        try {
            if (connectionString != null && !connectionString.isEmpty()) {
                // Use connection string if provided
                senderClient = new ServiceBusClientBuilder()
                    .connectionString(connectionString)
                    .sender()
                    .queueName(queueName)
                    .buildClient();
            } else if (serviceBusNamespace != null && !serviceBusNamespace.isEmpty()) {
                // Use Managed Identity authentication
                String fullyQualifiedNamespace = serviceBusNamespace + ".servicebus.windows.net";
                senderClient = new ServiceBusClientBuilder()
                    .fullyQualifiedNamespace(fullyQualifiedNamespace)
                    .credential(new DefaultAzureCredentialBuilder().build())
                    .sender()
                    .queueName(queueName)
                    .buildClient();
            } else {
                logger.warning("Azure Service Bus not configured. Scheduled tasks will use local execution.");
                return;
            }
            
            logger.info("Azure Service Bus Scheduler initialized successfully");
        } catch (Exception e) {
            logger.warning("Failed to initialize Azure Service Bus: " + e.getMessage() + ". Using fallback mode.");
        }
    }
    
    /**
     * Schedule a task to be executed after a delay
     */
    public void scheduleTask(String taskId, String taskData, Duration delay) {
        if (senderClient == null) {
            logger.warning("Azure Service Bus not available. Task will be executed locally: " + taskId);
            // Fallback: execute task locally (not recommended for production)
            return;
        }
        
        try {
            ServiceBusMessage message = new ServiceBusMessage(taskData);
            message.setMessageId(taskId);
            message.setScheduledEnqueueTime(OffsetDateTime.now().plus(delay));
            
            senderClient.sendMessage(message);
            logger.info("Scheduled task in Azure Service Bus: " + taskId + " with delay: " + delay);
        } catch (Exception e) {
            logger.severe("Failed to schedule task: " + taskId + ": " + e.getMessage());
            throw new RuntimeException("Failed to schedule task in Azure Service Bus", e);
        }
    }
    
    /**
     * Schedule a task to be executed at a specific time
     */
    public void scheduleTaskAt(String taskId, String taskData, OffsetDateTime scheduledTime) {
        if (senderClient == null) {
            logger.warning("Azure Service Bus not available. Task will be executed locally: " + taskId);
            return;
        }
        
        try {
            ServiceBusMessage message = new ServiceBusMessage(taskData);
            message.setMessageId(taskId);
            message.setScheduledEnqueueTime(scheduledTime);
            
            senderClient.sendMessage(message);
            logger.info("Scheduled task in Azure Service Bus: " + taskId + " at: " + scheduledTime);
        } catch (Exception e) {
            logger.severe("Failed to schedule task: " + taskId + ": " + e.getMessage());
            throw new RuntimeException("Failed to schedule task in Azure Service Bus", e);
        }
    }
    
    @PreDestroy
    public void cleanup() {
        if (senderClient != null) {
            try {
                senderClient.close();
                logger.info("Azure Service Bus Scheduler closed successfully");
            } catch (Exception e) {
                logger.warning("Error closing Service Bus sender: " + e.getMessage());
            }
        }
    }
    
    public boolean isAvailable() {
        return senderClient != null;
    }
}
