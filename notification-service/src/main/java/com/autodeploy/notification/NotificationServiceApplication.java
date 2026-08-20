package com.autodeploy.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Notification Service — real-time streaming and email alerts.
 *
 * <p>Responsibilities:</p>
 * <ul>
 *   <li>Subscribes to Redis Pub/Sub channels for build and deployment log lines</li>
 *   <li>Exposes WebSocket endpoint for browsers to receive live log streams</li>
 *   <li>Exposes SSE endpoint as an alternative to WebSocket</li>
 *   <li>Consumes {@code deployment.completed/failed} events from RabbitMQ</li>
 *   <li>Sends optional email notifications on deploy success/failure</li>
 * </ul>
 *
 * <p>Runs on port 8086.</p>
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableAsync
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
