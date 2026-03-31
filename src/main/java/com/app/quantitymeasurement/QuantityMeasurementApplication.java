package com.app.quantitymeasurement;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * QuantityMeasurementApplication
 *
 * Entry point for the Quantity Measurement Spring Boot application.
 *
 * {@code @SpringBootApplication} combines three annotations:
 * 
 *       {@code @Configuration} — marks this class as a source of bean definitions.
 *       {@code @EnableAutoConfiguration} — activates Spring Boot's auto-configuration
 *       based on classpath dependencies (H2, JPA, Tomcat, Jackson, Security).
 *       {@code @ComponentScan} — scans the package tree for {@code @Component},
 *       {@code @Service}, {@code @Repository}, and {@code @Controller} beans.
 *
 * {@code @OpenAPIDefinition} supplies the Swagger / OpenAPI metadata displayed at
 * {@code http://localhost:8080/swagger-ui.html} after the application starts.
 *
 * On startup, the embedded Tomcat server starts on port 8080, the H2 in-memory
 * database is initialised, and Spring Data JPA creates the schema automatically
 * from the entity annotations.
 */
@SpringBootApplication
@OpenAPIDefinition(
    info = @Info(
        title       = "Quantity Measurement API",
        version     = "17.0",
        description = "REST API for quantity measurement operations — comparison, conversion, " +
                      "addition, subtraction, and division across Length, Weight, Volume, " +
                      "and Temperature units."
    )
)
public class QuantityMeasurementApplication {

    /**
     * Bootstraps the Spring Boot application.
     *
     * @param args command-line arguments passed to the application
     */
    public static void main(String[] args) {
        SpringApplication.run(QuantityMeasurementApplication.class, args);
    }
}