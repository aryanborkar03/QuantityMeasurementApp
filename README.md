# 📏 QuantityMeasurementApp

> A Java-based **Spring Boot REST application** developed using Test-Driven Development (TDD) to progressively design and evolve a multi-category quantity measurement system. The project emphasizes incremental development, clean object-oriented design, and continuous architectural refactoring to build a scalable, flexible, and maintainable domain model.

### 📖 Overview

- Modular **Spring Boot-based Java application** focused on modelling multi-category quantity measurements (length, weight, volume, and temperature) with full conversion and controlled arithmetic support.
- Organized around incremental Use Cases evolving from simple equality checks to a **scalable, capability-aware and layered (N-Tier) architecture**.
- Emphasizes clarity, consistency, and maintainable structure through **Test-Driven Development (TDD)** and continuous refactoring.

### ✅ Implemented Features

> _Features will be added here as Use Cases are implemented._

- 🧩 **UC1 – Feet Equality :**
  - Implements value-based equality for feet measurements using an overridden `equals()` method.
  - Establishes object equality semantics as the foundation for future unit comparisons.

- 🧩 **UC2 – Inches Equality :**
  - Extends value-based equality comparison to inches measurements using a dedicated `Inches` class.
  - Maintains independent unit validation while reinforcing equality behaviour across measurement types.

- 🧩 **UC3 – Generic Length :**
  - Refactors unit-specific classes into a unified `Length` abstraction using a `LengthUnit` enum.
  - Eliminates duplicated logic by applying the DRY principle while enabling cross-unit equality comparison.

- 🧩 **UC4 – Extended Unit Support :**
  - Adds Yards and Centimeters to the `LengthUnit` enum with appropriate conversion factors.
  - Demonstrates scalability of the generic design by enabling seamless cross-unit equality without introducing new classes.

- 🧩 **UC5 – Unit-to-Unit Conversion :**
  - Introduces explicit conversion operations between supported length units using centralized enum conversion factors.
  - Extends the `Length` API to convert measurements across units while preserving mathematical equivalence and precision.

- 🧩 **UC6 – Length Addition Operation :**
  - Introduces addition between length measurements with automatic unit normalization and conversion.
  - Returns a new immutable `Length` result expressed in the unit of the first operand while preserving mathematical accuracy.

- 🧩 **UC7 – Addition with Target Unit Specification :**
  - Extends length addition to allow explicit specification of the result unit independent of operand units.
  - Enhances API flexibility by enabling arithmetic results to be expressed in any supported unit while preserving immutability and precision.

- 🧩 **UC8 – Standalone Unit Refactor :**
  - Extracts `LengthUnit` into a standalone enum responsible for all unit conversion logic.
  - Improves architectural separation by delegating conversions to units, reducing coupling and enabling scalable support for future measurement categories.

- 🧩 **UC9 – Weight Measurement Support :**
  - Introduces a weight measurement category with `Weight` and `WeightUnit` supporting kilograms, grams, and pounds.
  - Enables equality, conversion, and addition operations for weight while preserving strict separation from length measurements and stabilizing the shared measurement architecture.

- 🧩 **UC10 – Generic Quantity Architecture :**
  - Introduces a generic `Quantity<U extends IMeasurable>` model enabling multiple measurement categories through a shared abstraction.
  - Eliminates category-specific duplication by unifying equality, conversion, and addition logic into a single scalable architecture.

- 🧩 **UC11 – Volume Measurement Support :**
  - Adds a new measurement category using `VolumeUnit` (Litre, Millilitre, Gallon) implemented through the generic `Quantity<U>` architecture.
  - Validates that new measurement types integrate without modifying existing quantity logic, proving true multi-category scalability.

- 🧩 **UC12 – Subtraction and Division Operations :**
  - Introduces subtraction between quantities with automatic cross-unit normalization while preserving immutability.
  - Adds division support producing a dimensionless ratio, enabling comparative analysis across measurements of the same category.

- 🧩 **UC13 – Centralized Arithmetic Logic (DRY Refactor) :**
  - Refactors addition, subtraction, and division to use a centralized arithmetic helper, eliminating duplicated validation and conversion logic.
  - Improves maintainability and scalability while preserving all existing behaviour and public APIs.

- 🧩 **UC14 – Temperature Measurement (Selective Arithmetic Support) :**
  - Introduces temperature measurements using `TemperatureUnit` integrated into the generic `Quantity<U>` architecture.
  - Supports equality comparison and unit conversion across Celsius, Fahrenheit, and Kelvin using non-linear conversion formulas.
  - Refactors `IMeasurable` with default capability validation to allow category-specific operation support.
  - Prevents unsupported arithmetic operations (addition, subtraction, division) through explicit validation and meaningful exceptions.
  - Demonstrates Interface Segregation and capability-based design while preserving backward compatibility for length, weight, and volume.

- 🧩 **UC15 – N-Tier Architecture Refactoring :**
  - Refactors the Quantity Measurement Application from a monolithic design into a structured **N-Tier architecture**.
  - Introduces layered separation including **Controller, Service, Repository, Model, Entity, DTO, Interfaces, and Units** packages.
  - Moves business logic into the **Service layer**, while the **Controller layer** manages application interaction and orchestration.
  - Adds a **Repository layer with a cache-based storage implementation** to record measurement operations.
  - Standardizes data flow using **QuantityDTO for external transfer**, **QuantityModel for internal processing**, and **QuantityMeasurementEntity for persistence**.
  - Improves **modularity, testability, maintainability, and extensibility**, preparing the system for future integration with **REST APIs or database storage**.

- 🧩 **UC16 – Database Integration with JDBC for Quantity Measurement Persistence :**
  - Extends the N-Tier architecture established in UC15 with **persistent relational database storage** using **JDBC (Java Database Connectivity)**.
  - Introduces `QuantityMeasurementDatabaseRepository` as a full JDBC-based replacement for the in-memory `QuantityMeasurementCacheRepository`, enabling long-term data persistence across application restarts.
  - Adds `ApplicationConfig` utility class that loads all database configuration from `application.properties`, supporting environment-specific settings for **development, testing, and production**.
  - Introduces `ConnectionPool` utility class that manages a pool of reusable JDBC connections for efficient resource usage.
  - Extends `IQuantityMeasurementRepository` interface with four new methods: `getMeasurementsByOperation()`, `getMeasurementsByType()`, `getTotalCount()`, and `deleteAll()`.
  - Adds `DatabaseException` to the custom exception hierarchy, with static factory methods for structured database error handling.
  - Adopts **parameterized SQL queries** (`PreparedStatement`) throughout the database repository to prevent SQL injection attacks.
  - Migrates all `System.out.println` logging to **Java's built-in `java.util.logging` (JUL)** framework via SLF4J and Logback.
  - Uses **H2 embedded database** by default with the ability to switch to MySQL or PostgreSQL via `application.properties`.
  - Adds integration tests (`QuantityMeasurementIntegrationTest`) and unit tests for each layer using H2 in-memory database.
  - Demonstrates enterprise-level practices including **connection pooling, transaction awareness, resource cleanup with try-finally, and environment-specific database profiles**.

- 🧩 **UC17 – Spring Boot Integration with REST Services and JPA Persistence :**
  - Migrates the entire application from a standalone JDBC-based design to a **Spring Boot REST service** while preserving all domain models and business logic from UC1–UC16.
  - Introduces `QuantityMeasurementApplication` as the **Spring Boot entry point** with `@SpringBootApplication` and `@OpenAPIDefinition` for application metadata.
  - **Replaces manual JDBC repositories** (`QuantityMeasurementDatabaseRepository`, `QuantityMeasurementCacheRepository`, `ApplicationConfig`, `ConnectionPool`) with **Spring Data JPA** — `QuantityMeasurementRepository` extending `JpaRepository<QuantityMeasurementEntity, Long>`.
  - `QuantityMeasurementRepository` defines derived-query methods: `findByOperation`, `findByThisMeasurementType`, `findByCreatedAtAfter`, `countByOperationAndErrorFalse`, `findByErrorTrue`, and a custom `@Query` method `findSuccessfulByOperation`.
  - **Refactors the package layout** — introduces three distinct packages: `entity` for JPA-mapped database classes (`QuantityMeasurementEntity`), `dto` for API request/response objects (`QuantityDTO`, `QuantityInputDTO`, `QuantityMeasurementDTO`), and `model` for pure domain/business objects (`Quantity`, `QuantityModel`, `OperationType`).
  - **Refactors `QuantityDTO`** to include Bean Validation annotations (`@Data`, `@NotNull`, `@NotEmpty`, `@Pattern`, `@AssertTrue`) enforcing input integrity at the API boundary.
  - Introduces **`QuantityMeasurementDTO`** as a structured API response object with static factory methods: `fromEntity()`, `toEntity()`, `fromEntityList()`, and `toEntityList()` using the Java Stream API for efficient collection mapping.
  - Adds **`QuantityInputDTO`** to encapsulate the two-operand input structure accepted by all POST endpoints.
  - Introduces **`OperationType` enum** with constants `ADD`, `SUBTRACT`, `MULTIPLY`, `DIVIDE`, `COMPARE`, and `CONVERT` for type-safe operation representation throughout the application.
  - **Exposes RESTful API endpoints** through `QuantityMeasurementController` using `@RestController` and `@RequestMapping("/api/v1/quantities")`:
    - `POST /compare`, `/convert`, `/add`, `/subtract`, `/divide` — accept `QuantityInputDTO`, return `QuantityMeasurementDTO`.
    - `GET /history/operation/{operation}`, `/history/type/{measurementType}`, `/history/errored` — return `List<QuantityMeasurementDTO>`.
    - `GET /count/{operation}` — returns operation count.
  - Adds **Swagger/OpenAPI annotations** (`@Operation`, `@Tag`, `@Parameter`) on all controller methods to generate interactive API documentation.
  - Implements **centralized exception handling** via `GlobalExceptionHandler` (`@ControllerAdvice`) with handlers for `MethodArgumentNotValidException`, `QuantityMeasurementException`, and general `Exception` — returning structured JSON error responses with timestamp, status, error type, message, and path.
  - **Removes `DatabaseException`** — exception handling is now managed declaratively through `GlobalExceptionHandler` and Spring's exception translation layer.
  - Adds **`SecurityConfig`** in a dedicated `config` package preparing the system for Spring Security integration; currently permits all requests for development and testing.
  - Supports **environment-based configuration** through `application.properties` (H2, development) and `application-prod.properties` (MySQL, production), replacing the custom `ApplicationConfig` and manual property loading from UC16.
  - **HikariCP** is used as the default connection pool (auto-configured by Spring Boot), replacing the manual `ConnectionPool` implementation from UC16.
  - **Schema is managed by JPA auto-DDL** (`spring.jpa.hibernate.ddl-auto=create-drop` in dev), replacing the explicit `schema.sql` from UC16.
  - Adds **Spring Boot Actuator** for monitoring via `/actuator/health`, `/actuator/info`, and `/actuator/metrics`.
  - Adds comprehensive **Spring Boot testing**:
    - `QuantityMeasurementControllerTest` — controller unit tests using `@WebMvcTest` and `MockMvc`.
    - `QuantityMeasurementApplicationTests` — full-stack integration tests using `@SpringBootTest` and `TestRestTemplate`.
    - `QuantityMeasurementServiceIntegrationTest` — service-layer integration tests using `@SpringBootTest`.
    - `QuantityMeasurementRepositoryTest` — Spring Data JPA repository tests.
  - Demonstrates migration from **JDBC-based persistence (UC16)** to a modern **Spring Boot + JPA enterprise architecture** while maintaining the original measurement logic and full test coverage.

- 🧩 **UC18 – Spring Security with JWT Authentication, Google/GitHub OAuth2 & Industry-Standard Refactoring:**
  - Activates full **Spring Security** with JWT, Google OAuth2, and GitHub OAuth2 authentication, secured REST endpoints, role-based authorization, and complete security-focused test coverage.
  - Introduces a `security` package containing `JwtTokenProvider`, `JwtAuthenticationFilter`, `JwtAuthenticationEntryPoint`, `JwtAccessDeniedHandler`, `CustomUserDetailsService`, `UserPrincipal`, `CustomOAuth2UserService`, `OAuth2AuthenticationSuccessHandler`, and `OAuth2AuthenticationFailureHandler`.
  - **JWT lifecycle** — `JwtTokenProvider` generates signed HS256 tokens from authenticated principals, extracts email and role claims, and validates tokens on every request; configured via `app.jwt.secret` (Base64-encoded) and `app.jwt.expiration-ms` in `application.properties`.
  - **Local authentication** — `AuthController` (`/api/v1/auth`) exposes `POST /register` (BCrypt-hash password, persist `User`, return JWT), `POST /login` (verify credentials, return JWT), and `GET /me` (return profile of authenticated user).
  - **Google OAuth2** — Spring Security's built-in OAuth2 login filter handles the Authorization Code flow (`/oauth2/authorization/google`); `CustomOAuth2UserService` resolves the Google profile to a local `User` (create-or-update), and `OAuth2AuthenticationSuccessHandler` issues a JWT redirect to the configured frontend URI.
  - **GitHub OAuth2** — identical flow at `/oauth2/authorization/github`; `CustomOAuth2UserService` dispatches on the `registrationId` and applies GitHub-specific attribute extraction (`id` → `providerId`, `login` as name fallback, `avatar_url` as image). GitHub's `email` field may be `null` when the user's primary email is private; the service rejects such logins with a descriptive error. Requires `read:user,user:email` scope and a GitHub OAuth App registered at https://github.com/settings/developers.
  - Introduces `User` JPA entity (table `app_user`) with fields: `email`, `name`, `password` (nullable for OAuth2), `provider` (`AuthProvider` enum: `LOCAL`/`GOOGLE`/`GITHUB`), `providerId`, `role` (`Role` enum: `USER`/`ADMIN`), `imageUrl`, and `createdAt` (set via `@PrePersist`).
  - Adds `UserRepository` (Spring Data JPA) with `existsByEmail()` and `findByEmail()` derived queries.
  - Adds `AuthRequest`, `AuthResponse` (Builder pattern), and `RegisterRequest` DTOs with Bean Validation constraints (`@NotBlank`, `@Email`, `@Size`).
  - **Role-based access control** via `@EnableMethodSecurity` and URL-level rules: public auth/OAuth2/Swagger/Actuator endpoints; `USER`+`ADMIN` for all quantity operations; `ADMIN` only for `GET /api/v1/quantities/history/errored`.
  - **STATELESS session policy** — no HTTP session is ever created; CSRF disabled; HTTP Basic and form login disabled.
  - `SecurityConfig` registers `DaoAuthenticationProvider` (BCrypt + `CustomUserDetailsService`), exposes `AuthenticationManager` as a bean, and inserts `JwtAuthenticationFilter` before `UsernamePasswordAuthenticationFilter`.
  - Adds `app.jwt.secret`, `app.jwt.expiration-ms`, `spring.security.oauth2.client.registration.google.*`, and `app.oauth2.redirect-uri` to `application.properties` (all resolved from environment variables in production).
  - Adds comprehensive **unit and integration test coverage for authentication and security components** ensuring correctness of JWT generation, user principal resolution, DTO validation, repository interaction, and controller endpoints.
  - Introduces new test classes validating authentication workflows:

    - `AuthControllerTest`
      - Tests `/api/v1/auth/register`, `/login`, and `/me` endpoints using `@WebMvcTest`.
      - Verifies request validation, JWT response structure, and authentication behaviour.

    - `JwtTokenProviderTest`
      - Validates JWT creation, parsing, claim extraction, expiration handling, and signature verification.
      - Ensures tokens are securely generated using HS256 algorithm and Base64 secret.

    - `UserPrincipalTest`
      - Verifies Spring Security `UserDetails` mapping from `User` entity.
      - Ensures roles and authorities are correctly exposed to the security context.

    - `UserRepositoryTest`
      - Validates Spring Data JPA derived query methods:
        - `existsByEmail`
        - `findByEmail`
      - Confirms persistence behaviour for LOCAL and GOOGLE authentication providers.

    - `AuthDTOTest`
      - Validates Bean Validation constraints on:
        - `AuthRequest`
        - `RegisterRequest`
        - `AuthResponse`
      - Ensures email format, password constraints, and required fields are enforced.

    - `CustomOAuth2UserServiceTest`
      - Tests OAuth2 login processing for **GOOGLE** and **GITHUB** providers.
      - Verifies user registration, profile update, provider conflict handling, null email validation (GitHub private email case), and correct `UserPrincipal` mapping.

    - Updated `QuantityMeasurementApplicationTests`
      - Ensures full Spring Boot context loads correctly with Security configuration enabled.
      - Verifies compatibility between SecurityFilterChain, JPA, Controllers, and OAuth2 configuration.

    - Updated `QuantityMeasurementControllerTest`
      - Ensures secured endpoints are accessible only with valid JWT authentication.
      - Uses Spring Security test support (`@WithMockUser`, MockMvc JWT setup).

  - Demonstrates **security-focused TDD approach** ensuring:
    - Authentication logic correctness
    - JWT integrity
    - OAuth2 user mapping reliability
    - Role-based authorization behaviour
    - Backward compatibility with existing quantity measurement features

  - Performs a comprehensive architectural and code-quality refactoring of the UC18 codebase to align with professional Java / Spring Boot industry conventions.
  - **Switches logging from `java.util.logging` (JUL) to SLF4J via Lombok's `@Slf4j`** across all main source files — eliminates every `Logger.getLogger(...)` field declaration, adds `@Slf4j` class annotation, and replaces all `logger.*()` call sites with the equivalent `log.*()` SLF4J calls.
  - **Restructures the `dto` package** into explicit `request` and `response` sub-packages:
    - `dto/request/` — `AuthRequest`, `RegisterRequest`, `QuantityInputDTO`, `QuantityMeasurementDTO`
    - `dto/response/` — `AuthResponse`, `QuantityDTO`
  - **Restructures the `security` package** into explicit `jwt` and `oauth2` sub-packages:
    - `security/jwt/` — `JwtTokenProvider`, `JwtAuthenticationFilter`, `JwtAuthenticationEntryPoint`, `JwtAccessDeniedHandler`
    - `security/oauth2/` — `CustomOAuth2UserService`, `OAuth2AuthenticationSuccessHandler`, `OAuth2AuthenticationFailureHandler`
    - `security/` (root) — `CustomUserDetailsService`, `UserPrincipal`
  - **Introduces an `enums` package** by extracting `AuthProvider`, `Role`, and `OperationType` from the `model` package into a dedicated `enums` package, separating pure enum types from domain model classes.
  - **Adds profile-specific `application-dev.properties` and `application-test.properties`** alongside the existing base `application.properties` and `application-prod.properties`, completing the full four-profile configuration set (base / dev / prod / test).
  - **Updates all cross-file imports** throughout main and test source trees to reference the correct new package paths for every moved class.
  - **Renames test `integrationTests/` package to `integration/`** and relocates `QuantityMeasurementServiceIntegrationTest` from `service/` into `integration/` to match the target structure.
  - **Cleans up `.gitignore`** — removes duplicate entries and adds `application-prod.properties` and `.env` to the secrets ignore list.
  - **Updates `pom.xml`** — extracts `${lombok.version}` property so the version is declared once and referenced in both the `<dependency>` block and the `maven-compiler-plugin` annotationProcessorPaths.
  - All existing UC1–UC17 functionality, tests, and API contracts are fully preserved; this UC contains no functional changes — only structural and quality improvements.

### 🧰 Tech Stack

- **Java 17+** — core language
- **Maven** — build and dependency management

#### 🚀 Backend Framework
- **Spring Boot 3.2.2** — application framework with auto-configuration
- **Spring Web (spring-boot-starter-web)** — REST APIs (Spring MVC + embedded Tomcat)
- **Spring Data JPA (spring-boot-starter-data-jpa)** — ORM abstraction (Hibernate)
- **Spring Security (spring-boot-starter-security)** — stateless JWT + Google OAuth2 authentication and role-based authorization 
- **Spring Security OAuth2 Client (spring-boot-starter-oauth2-client)** — Google OAuth2 Authorization Code flow
- **JJWT (io.jsonwebtoken)** — JWT generation, claims extraction, and HS256 signature validation 
- **Spring Boot Validation (spring-boot-starter-validation)** — Bean Validation for request validation
- **Spring Boot Actuator** — monitoring endpoints (`/actuator/health`, `/metrics`, etc.)

#### 📄 API Documentation
- **Swagger / OpenAPI (springdoc-openapi-starter-webmvc-ui)** — interactive API docs

#### 🗄️ Database
- **H2 Database** — in-memory DB for development/testing
- **MySQL Connector/J** — optional production database support

#### ⚙️ Utilities
- **Lombok** — reduces boilerplate (getters/setters, constructors, `@Slf4j` logging, etc.)
- **SLF4J + Logback** — logging facade via Lombok `@Slf4j`; replaces `java.util.logging` throughout 
- **HikariCP** — auto-configured connection pool (Spring Boot default)

#### 🧪 Testing
- **Spring Boot Test (JUnit 5, Mockito, MockMvc)** — unit, integration, and controller testing
- **Spring Security Test** — authentication and authorization testing support

### ▶️ Build / Run

- Clean and compile the project:

```
mvn clean compile
```

- Run the Spring Boot application:

```
mvn spring-boot:run
```

- Run all tests:

```
mvn clean test
```

- Run specific test class:

```
mvn test -Dtest=QuantityMeasurementServiceIntegrationTest
```

- Build executable JAR:

```
mvn clean package
```

- Run the packaged application:

```
java -jar target/quantity-measurement-app-0.0.1-SNAPSHOT.jar
```

Once the application starts:

- **API Base URL**

```
http://localhost:8080/api/v1/quantities
```

- **Swagger API Documentation**

```
http://localhost:8080/swagger-ui.html
```

- **H2 Database Console**

```
http://localhost:8080/h2-console
```

- **Spring Boot Actuator Endpoints**

```
http://localhost:8080/actuator/health

http://localhost:8080/actuator/info

http://localhost:8080/actuator/metrics
```

### ⚙️ Configuration

The application uses four profile-specific property files in `src/main/resources/`:

| File | Profile | Purpose |
|---|---|---|
| `application.properties` | base | Shared defaults; sets `spring.profiles.active=prod` |
| `application-dev.properties` | `dev` | H2 in-memory DB, verbose logging, H2 console enabled |
| `application-prod.properties` | `prod` | MySQL datasource, reduced logging, Swagger disabled |
| `application-test.properties` | `test` | Isolated H2 test DB, stub OAuth2 credentials, fixed JWT secret |

Key properties in `application.properties`:

```properties
# Application name and active profile
spring.application.name=quantity-measurement-app
spring.profiles.active=dev

# H2 In-Memory Database (Development)
spring.datasource.url=jdbc:h2:mem:quantitymeasurementdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# Spring Data JPA / Hibernate
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true

# H2 Console (Development only)
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# Swagger / OpenAPI
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.enabled=true

# Actuator
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=always

# JWT Configuration (UC18)
app.jwt.secret=${JWT_SECRET}
app.jwt.expiration-ms=${JWT_EXPIRATION_MS}

# Google OAuth2 Configuration (UC18)
spring.security.oauth2.client.registration.google.client-id=<your-google-client-id>
spring.security.oauth2.client.registration.google.client-secret=<your-google-client-secret>
spring.security.oauth2.client.registration.google.scope=openid,profile,email

# GitHub OAuth2 Configuration (UC18)
spring.security.oauth2.client.registration.github.client-id=<your-github-client-id>
spring.security.oauth2.client.registration.github.client-secret=<your-github-client-secret>
spring.security.oauth2.client.registration.github.scope=read:user,user:email

app.oauth2.redirect-uri=http://localhost:8080/swagger-ui.html
```

To switch to **MySQL in production**, activate the prod profile:

```
java -jar target/quantity-measurement-app-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

And supply all required environment variables (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `JWT_EXPIRATION_MS`, `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, `OAUTH2_REDIRECT_URI`) via your deployment environment or a `.env` file (never committed to VCS).

### 📂 Project Structure

```
📦 QuantityMeasurementApp
│
├── 📁 src
│   ├── 📁 main
│   │   ├── 📁 java
│   │   │   └── 📁 com
│   │   │       └── 📁 app
│   │   │           └── 📁 quantitymeasurement
│   │   │               ├── 📄 QuantityMeasurementApplication.java
│   │   │               │
│   │   │               ├── 📁 config
│   │   │               │   └── 📄 SecurityConfig.java              ← UPDATED (UC18)
│   │   │               │
│   │   │               ├── 📁 controller
│   │   │               │   ├── 📄 AuthController.java              ← NEW (UC18)
│   │   │               │   └── 📄 QuantityMeasurementController.java
│   │   │               │
│   │   │               ├── 📁 dto
│   │   │               │   ├── 📁 request                         ← NEW (UC18)
│   │   │               │   │   ├── 📄 AuthRequest.java            ← NEW (UC18)
│   │   │               │   │   ├── 📄 RegisterRequest.java        ← NEW (UC18)
│   │   │               │   │   ├── 📄 QuantityInputDTO.java
│   │   │               │   │   └── 📄 QuantityMeasurementDTO.java
│   │   │               │   │
│   │   │               │   └── 📁 response                        ← NEW (UC18)
│   │   │               │       ├── 📄 AuthResponse.java           ← NEW (UC18)
│   │   │               │       └── 📄 QuantityDTO.java
│   │   │               │
│   │   │               ├── 📁 entity
│   │   │               │   ├── 📄 QuantityMeasurementEntity.java
│   │   │               │   └── 📄 User.java                       ← NEW (UC18)
│   │   │               │
│   │   │               ├── 📁 enums                               ← NEW (UC18)
│   │   │               │   ├── 📄 AuthProvider.java               ← MOVED from model/ (UC18)
│   │   │               │   ├── 📄 OperationType.java              ← MOVED from model/ (UC18)
│   │   │               │   └── 📄 Role.java                       ← MOVED from model/ (UC18)
│   │   │               │
│   │   │               ├── 📁 exception
│   │   │               │   ├── 📄 GlobalExceptionHandler.java
│   │   │               │   └── 📄 QuantityMeasurementException.java
│   │   │               │
│   │   │               ├── 📁 model
│   │   │               │   ├── 📄 Quantity.java
│   │   │               │   └── 📄 QuantityModel.java
│   │   │               │
│   │   │               ├── 📁 repository
│   │   │               │   ├── 📄 QuantityMeasurementRepository.java
│   │   │               │   └── 📄 UserRepository.java             ← NEW (UC18)
│   │   │               │
│   │   │               ├── 📁 security
│   │   │               │   ├── 📁 jwt                             ← NEW (UC18)
│   │   │               │   │   ├── 📄 JwtTokenProvider.java       ← MOVED (UC18)
│   │   │               │   │   ├── 📄 JwtAuthenticationFilter.java ← MOVED (UC18)
│   │   │               │   │   ├── 📄 JwtAuthenticationEntryPoint.java ← MOVED (UC18)
│   │   │               │   │   └── 📄 JwtAccessDeniedHandler.java ← MOVED (UC18)
│   │   │               │   │
│   │   │               │   ├── 📁 oauth2                          ← NEW (UC18)
│   │   │               │   │   ├── 📄 CustomOAuth2UserService.java ← MOVED (UC18)
│   │   │               │   │   ├── 📄 OAuth2AuthenticationSuccessHandler.java ← MOVED (UC18)
│   │   │               │   │   └── 📄 OAuth2AuthenticationFailureHandler.java ← MOVED (UC18)
│   │   │               │   │
│   │   │               │   ├── 📄 CustomUserDetailsService.java   ← NEW (UC18)
│   │   │               │   └── 📄 UserPrincipal.java              ← NEW (UC18)
│   │   │               │
│   │   │               ├── 📁 service
│   │   │               │   ├── 📄 IQuantityMeasurementService.java
│   │   │               │   └── 📄 QuantityMeasurementServiceImpl.java
│   │   │               │
│   │   │               └── 📁 unit
│   │   │                   ├── 📄 IMeasurable.java
│   │   │                   ├── 📄 SupportsArithmetic.java
│   │   │                   ├── 📄 LengthUnit.java
│   │   │                   ├── 📄 WeightUnit.java
│   │   │                   ├── 📄 VolumeUnit.java
│   │   │                   └── 📄 TemperatureUnit.java
│   │   │
│   │   └── 📁 resources
│   │       ├── 📄 application.properties           (base / shared defaults)
│   │       ├── 📄 application-dev.properties         ← NEW (UC18)
│   │       ├── 📄 application-prod.properties        ← UPDATED (UC18)
│   │       └── 📄 application-test.properties        ← NEW (UC18)
│   │
│   └── 📁 test
│       ├── 📁 java
│       │   └── 📁 com
│       │       └── 📁 app
│       │           └── 📁 quantitymeasurement
│       │               ├── 📁 controller
│       │               │   ├── 📄 AuthControllerTest.java                ← NEW (UC18)
│       │               │   └── 📄 QuantityMeasurementControllerTest.java ← UPDATED (UC18)
│       │               │
│       │               ├── 📁 dto
│       │               │   ├── 📄 AuthDTOTest.java                       ← NEW (UC18)
│       │               │   └── 📄 QuantityDTOTest.java
│       │               │
│       │               ├── 📁 entity
│       │               │   ├── 📄 UserTest.java                          ← NEW (UC18)
│       │               │   └── 📄 QuantityMeasurementEntityTest.java
│       │               │
│       │               ├── 📁 exception
│       │               │   └── 📄 QuantityMeasurementExceptionTest.java
│       │               │
│       │               ├── 📁 integration                                ← RENAMED from integrationTests/ (UC18)
│       │               │   ├── 📄 QuantityMeasurementApplicationTests.java ← UPDATED (UC18)
│       │               │   └── 📄 QuantityMeasurementServiceIntegrationTest.java ← MOVED from service/ (UC18)
│       │               │
│       │               ├── 📁 model
│       │               │   ├── 📄 QuantityArithmeticTest.java
│       │               │   ├── 📄 QuantityConversionTest.java
│       │               │   ├── 📄 QuantityEqualityTest.java
│       │               │   └── 📄 QuantityModelTest.java
│       │               │
│       │               ├── 📁 repository
│       │               │   ├── 📄 UserRepositoryTest.java                ← NEW (UC18)
│       │               │   └── 📄 QuantityMeasurementRepositoryTest.java
│       │               │
│       │               ├── 📁 security
│       │               │   ├── 📄 JwtTokenProviderTest.java              ← NEW (UC18)
│       │               │   ├── 📄 UserPrincipalTest.java                 ← NEW (UC18)
│       │               │   └── 📄 CustomOAuth2UserServiceTest.java       ← NEW (UC18)
│       │               │
│       │               ├── 📁 service
│       │               │   └── 📄 QuantityMeasurementServiceTest.java
│       │               │
│       │               └── 📁 unit
│       │                   ├── 📄 IMeasurableTest.java
│       │                   ├── 📄 LengthUnitTest.java
│       │                   ├── 📄 WeightUnitTest.java
│       │                   ├── 📄 VolumeUnitTest.java
│       │                   └── 📄 TemperatureUnitTest.java
│       │
│       └── 📁 resources
│           └── 📄 application.properties
│
├── ⚙️ pom.xml
├── 🚫 .gitignore
├── 📜 LICENSE
└── 📘 README.md
```

> **Note on UC17 → UC18 changes:**
> The permissive `SecurityConfig` stub introduced in UC17 was fully replaced in UC18 with a **production-ready stateless Spring Security configuration** supporting **JWT authentication and Google OAuth2 login**.  
> This builds on top of UC18 with a **structural and code-quality refactoring** — no API changes, only improved package organisation, SLF4J logging, and profile-based configuration.
 The permissive `SecurityConfig` stub introduced in UC17 has been fully replaced with a **production-ready stateless Spring Security configuration** supporting **JWT authentication and Google OAuth2 login**.  
> All existing UC17 functionality remains intact; UC18 extends the architecture with authentication, authorization, and security-focused validation layers.

> **Updated Components**
> - `SecurityConfig.java` → UPDATED: implements stateless `SecurityFilterChain`, disables session creation, configures endpoint authorization rules, and integrates JWT + OAuth2 filters
> - `application.properties` → UPDATED: adds JWT, Google OAuth2, and **GitHub OAuth2** configuration properties
> - `application-prod.properties` → UPDATED: adds `GITHUB_CLIENT_ID` and `GITHUB_CLIENT_SECRET` environment variable entries
> - `QuantityMeasurementControllerTest` → UPDATED: validates secured endpoints with authentication context
> - `QuantityMeasurementApplicationTests` → UPDATED: verifies application context loads successfully with Spring Security enabled

> **Security Package (UC18 structure)**
> - `security/jwt/` sub-package (UC18):
>   - `JwtTokenProvider` → JWT generation, parsing, validation, claim extraction
>   - `JwtAuthenticationFilter` → intercepts requests and sets authentication context
>   - `JwtAuthenticationEntryPoint` → handles unauthorized access attempts (401)
>   - `JwtAccessDeniedHandler` → handles insufficient permission scenarios (403)
> - `security/oauth2/` sub-package (UC18):
>   - `CustomOAuth2UserService` → maps Google OAuth2 user profile to application user
>   - `OAuth2AuthenticationSuccessHandler` → generates JWT after successful OAuth2 login
>   - `OAuth2AuthenticationFailureHandler` → handles OAuth2 authentication failures
> - `security/` root (UC18):
>   - `CustomUserDetailsService` → loads user-specific data for authentication
>   - `UserPrincipal` → Spring Security compliant authenticated user representation

> **New Authentication Domain Components**
> - `User` entity → stores user identity, provider details, role, and profile metadata
> - `UserRepository` → Spring Data JPA repository supporting lookup by email
> - DTO layer additions:
>   - `AuthRequest` → login request payload
>   - `RegisterRequest` → user registration payload
>   - `AuthResponse` → JWT authentication response
> - Authorization model additions (moved to `enums/` in UC18):
>   - `Role` enum → defines USER and ADMIN roles
>   - `AuthProvider` enum → distinguishes LOCAL, GOOGLE, and GITHUB authentication sources
>   - `OperationType` enum → type-safe operation constants (also moved to `enums/` in UC18)

> **New Test Coverage (UC18)**
> - Adds dedicated test classes validating authentication flow and security behaviour:
>   - `AuthControllerTest` → verifies register, login, and authenticated profile endpoints
>   - `JwtTokenProviderTest` → validates token creation, signature verification, and expiration handling
>   - `CustomOAuth2UserServiceTest` → tests OAuth2 user processing logic for Google and GitHub providers
>   - `UserPrincipalTest` → verifies correct mapping of User → UserDetails
>   - `UserRepositoryTest` → validates repository queries for user lookup and existence checks
>   - `AuthDTOTest` → validates Bean Validation constraints on authentication DTOs
>   - `UserRepositoryTest` → extended with GitHub provider tests: `findByProviderAndProviderId(GITHUB)`, provider isolation (GitHub vs Google with same numeric ID), null-name fallback, `createdAt` and `providerId` persistence
>   - `UserTest` → extended with GitHub builder tests: full field population, null-name fallback to login username, `toString` safety
>   - Updated integration and controller tests ensure compatibility between Spring Security, JPA, and REST endpoints

> UC18 establishes a **secure, stateless authentication architecture** aligned with modern Spring Boot practices while preserving full backward compatibility with the measurement domain logic developed in UC1–UC17.

> **UC18 – Refactoring Summary**
> - **Logging:** all classes switched from `java.util.logging.Logger` to Lombok `@Slf4j`; no functional change, consistent log output
> - **`dto` restructured:** request DTOs in `dto/request/`, response DTOs in `dto/response/`
> - **`security` restructured:** JWT classes in `security/jwt/`, OAuth2 classes in `security/oauth2/`
> - **`enums` introduced:** `AuthProvider`, `Role`, `OperationType` extracted from `model/` into a dedicated `enums/` package
> - **`integrationTests/` renamed** to `integration/`; `QuantityMeasurementServiceIntegrationTest` moved from `service/` to `integration/`
> - **Profile-based config completed:** `application-dev.properties` and `application-test.properties` added alongside existing `application.properties` and `application-prod.properties`
> - **`pom.xml`:** `${lombok.version}` property extracted; Lombok `@Slf4j` documented
> - **`.gitignore`:** duplicates removed; `application-prod.properties` and `.env` added to secrets exclusions

### ⚙️ Development Approach

> This project follows an incremental **Test-Driven Development (TDD)** workflow:

- Tests are written first to define expected behaviour.
- Implementation code is developed to satisfy the tests.
- Each Use Case introduces new functionality in small, controlled steps.
- Existing behaviour is preserved through continuous refactoring.
- Design evolves toward clean, maintainable, and well-tested software.
- Later use cases introduce capability-based behavior where different measurement categories support different operations safely.
