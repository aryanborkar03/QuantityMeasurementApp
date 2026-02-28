# 📏 QuantityMeasurementApp

> A Java application developed using Test-Driven Development (TDD) to progressively design and refine a quantity measurement system. The project emphasizes incremental development, clean object-oriented design, and continuous refactoring to build a flexible and maintainable domain model over time.

### 📖 Overview

- Modular Java project focused on modelling quantity measurements.
- Organized around incremental Use Cases to evolve the domain design.
- Emphasizes clarity, consistency, and maintainable structure as the system grows.

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

### 🧰 Tech Stack

- **Java 17+** — core language and application development  
- **Maven** — build automation and dependency management  
- **JUnit 5** — unit testing framework supporting TDD workflow

### ▶️ Build / Run

 - Build the project:
  
    ```
    mvn clean install
    ```

- Run tests:
    
    ```
    mvn test
    ```
### ⚙️ Development Approach

 > This project follows an incremental **Test-Driven Development (TDD)** workflow:

- Tests are written first to define expected behaviour.
- Implementation code is developed to satisfy the tests.
- Each Use Case introduces new functionality in small, controlled steps.
- Existing behaviour is preserved through continuous refactoring.
- Design evolves toward clean, maintainable, and well-tested software.
