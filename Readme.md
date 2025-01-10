# WIO (Workplace Office Integration) API

WIO is a Spring Boot–based RESTful API that enables organizations to manage companies, floors, seats, user registrations, and bookings. It also provides:

- JWT-based authentication and role-based access control (ADMIN, EMPLOYEE)
- One-time codes for employee registration
- A booking system with seat availability checks and cancellations
- Automated cleanup tasks (expired tokens, expired shares), and more

## Table of Contents
1. [Project Overview](#1-project-overview)
2. [Features](#2-features)
3. [Technology Stack](#3-technology-stack)
4. [Prerequisites](#4-prerequisites)
5. [Installation & Setup](#5-installation--setup)
6. [Configuration](#6-configuration)
7. [Running the Application](#7-running-the-application)
8. [API Documentation](#8-api-documentation)
9. [Authentication & Security](#9-authentication--security)
10. [Database Schema](#10-database-schema)
11. [Testing](#11-testing)
12. [Project Structure](#12-project-structure)
13. [Contributing](#13-contributing)

## 1. Project Overview

WIO provides:

- Company Management: Create and manage companies (with automated admin user creation)
- Floor Management: Create, delete, lock, or unlock floors
- Seat Management: Create, update, or remove seats (with checks for future bookings)
- User Management: Register as ADMIN or EMPLOYEE (employee requires a one-time code), log in, update profile, reset passwords, etc.
- Booking Management: Reserve seats for specific dates, cancel bookings, track seat availability, and handle seat conflicts
- Sharing: Users can share booking details with other users (inbox system)
- Security: JWT-based token auth, method-level authorization, and role-based access
- Scheduled Tasks: Cleans up expired password-reset tokens and old share data on a schedule

## 2. Features

- JWT Authentication for secure endpoint access
- Role-Based Access Control (RBAC):
    - ADMIN: Create companies, floors, seats, manage employees, etc.
    - EMPLOYEE: Book seats, share bookings, view seat availability, etc.
- OpenAPI/Swagger documentation for easy endpoint exploration
- Validation using Jakarta Bean Validation (e.g., @NotBlank, @Min, @FutureOrPresent)
- Scheduling: Automatic cleanup of expired data
- Extensive Testing: Unit tests (MockMvc, Mockito) and integration tests (end-to-end with a real or in-memory DB)

## 3. Technology Stack

- Java 17+
- Spring Boot 3 (Web, Security, Data JPA, Scheduling, Validation, Mail)
- Spring Data JPA + Hibernate for ORM
- PostgreSQL as the main database
- JWT (jjwt) for authentication and authorization
- MapStruct for object mapping
- Lombok to reduce boilerplate
- Gradle as the build tool
- JUnit 5, Mockito, Testcontainers for testing

## 4. Prerequisites

- Java 17 or higher
- PostgreSQL running locally or accessible over the network
- (Optional) Gradle installed; if not, use the included Gradle Wrapper (./gradlew)

## 5. Installation & Setup

1. Clone the repository:
```bash
git clone https://github.com/Turan10/wio-api.git
cd wio-api
```

2. Configure PostgreSQL:
    - Create a database named WIO (or your preferred name)
    - Update credentials in src/main/resources/application.properties

3. Install dependencies:
```bash
./gradlew clean build
```

4. (Optional) Setup environment variables if you want to override any property files (e.g., spring.datasource.url)

## 6. Configuration

Main configs are in src/main/resources/application.properties. Key entries:

```properties
# Server Port
server.port=8080

# PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5433/WIO
spring.datasource.username=postgres
spring.datasource.password=postgres

# JWT
app.jwt.secret=EHGxxE...
app.jwt.expiration-milliseconds=86400000
```

For testing, see src/test/resources/application.properties.

## 7. Running the Application

```bash
./gradlew bootRun
```

The server starts on http://localhost:8080 by default. Adjust the port in application.properties if needed.

## 8. API Documentation

WIO integrates Springdoc OpenAPI. Once running, visit:

http://localhost:8080/swagger-ui/index.html

to explore all endpoints, view request/response models, and make test requests interactively.

## 9. Authentication & Security

- JWT-based Authentication:
    - Obtain a token via POST /api/users/login with valid credentials (email, password)
    - Use Authorization: Bearer <token> in subsequent requests
- User Roles:
    - ADMIN: Full privileges (managing companies, floors, seats, etc.)
    - EMPLOYEE: Can book seats, share booking info, etc.
- Method-Level Security via @PreAuthorize in controllers ensures correct role/permission checks

## 10. Database Schema

- Company → has many Floor
- Floor → has many Seat
- Seat → has many Booking (over various dates)
- User → has many Booking
- Share → references multiple Booking via ShareBooking

Additional tables handle OneTimeCode, PasswordResetToken, FloorLock, etc.

## 11. Testing

To run all tests:

```bash
./gradlew test
```

- Unit Tests: Found in src/test/java/app/wio/controller, src/test/java/app/wio/service, etc. Use MockMvc + Mockito
- Integration Tests: Found in src/test/java/app/wio/integrationsTest, using Spring's test context and a test DB (H2 or PostgreSQL)

Examples:
- BookingControllerTest, CompanyControllerTest, etc. for unit testing controllers
- BookingControllerIT, CompanyControllerIT, etc. for integration testing the application end-to-end

## 12. Project Structure

```
wio-api/
 ┣ src/main/java/app/wio
 ┃ ┣ controller/        
 ┃ ┣ dto/               
 ┃ ┣ entity/            
 ┃ ┣ exception/         
 ┃ ┣ mapper/            
 ┃ ┣ repository/        
 ┃ ┣ security/          
 ┃ ┣ service/           
 ┃ ┗ WioApplication.java
 ┣ src/test/java/app/wio
 ┃ ┣ controller/        
 ┃ ┣ integrationsTest/  
 ┃ ┗ security/          
 ┣ src/main/resources/  
 ┣ src/test/resources/
 ┣ build.gradle
 ┗ README.md
```

## 13. Contributing

1. Fork the repository and create a feature branch
2. Commit your changes and ensure all tests pass
3. Open a Pull Request describing what you changed and why

We appreciate any contributions—bug reports, feature requests, or direct pull requests!

