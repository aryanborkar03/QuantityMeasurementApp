package com.app.qma;

import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableDiscoveryClient
public class QmaServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(QmaServiceApplication.class, args);
    }
}
