# Order Management System

A Spring Boot application following Hexagonal Architecture (Ports and Adapters) pattern.

## Project Structure

```
src/main/java/com/example/order_management/
├── application/                # Application Layer
│   ├── ports/                 # Ports (Interfaces)
│   │   ├── in/                # Input Ports (Use Cases interfaces)
│   │   └── out/               # Output Ports (Repository/External interfaces)
│   └── services/              # Use Case Implementations
├── domain/                     # Domain Layer (Pure Logic)
│   ├── model/                 # Domain Entities/VOs
│   └── exception/             # Domain Exceptions
├── infrastructure/             # Infrastructure Layer (Adapters)
│   ├── adapters/
│   │   ├── in/                # Input Adapters (Web/REST/Controllers)
│   │   └── out/               # Output Adapters (Persistence/External APIs)
│   └── config/                # Framework-specific configuration
└── OrderManagementApplication.java  # Main Entry Point
```

## Getting Started

### Prerequisites
- Java 17 or higher
- Gradle 7.x or higher

### Running the Application

```bash
./gradlew bootRun
```

### Running Tests

```bash
./gradlew test
```

## Architecture

This project follows the Hexagonal Architecture pattern, which separates the application into three main layers:

- **Domain Layer**: Contains pure business logic, entities, and domain exceptions
- **Application Layer**: Contains use cases (services) and ports (interfaces)
- **Infrastructure Layer**: Contains adapters for external concerns (web controllers, database repositories, external APIs)
