package com.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import de.codecentric.boot.admin.server.config.EnableAdminServer;

/**
 * Spring Boot Admin Server for the QMA microservices.
 *
 * Discovers all services through Eureka — no manual client registration needed.
 * Every service that registers with Eureka AND exposes /actuator/** is
 * automatically shown in the Admin UI with:
 *   - Live health status and uptime
 *   - Environment properties and config
 *   - JVM metrics (heap, threads, GC)
 *   - HTTP request traces
 *   - Log level management at runtime
 *   - Logfile tail
 *
 * Admin UI:  http://localhost:8090  (user: admin / password: see application.properties)
 * Eureka:    http://localhost:8761
 */
@SpringBootApplication
@EnableAdminServer
@EnableDiscoveryClient
public class AdminServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(AdminServerApplication.class, args);
    }
}
