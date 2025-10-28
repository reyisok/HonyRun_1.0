# HonyRun Backend

<div align="center">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.5.7-green.svg" alt="Spring Boot">
  <img src="https://img.shields.io/badge/JDK-21-blue.svg" alt="JDK">
  <img src="https://img.shields.io/badge/MySQL-9.4.0-orange.svg" alt="MySQL">
  <img src="https://img.shields.io/badge/Redis-8.2.1-red.svg" alt="Redis">
  <img src="https://img.shields.io/badge/License-MIT-yellow.svg" alt="License">
</div>

## Overview
HonyRun System is an internally-managed business support platform based on Spring WebFlux reactive architecture, providing convenient business processing, data querying, and system management functions. The system adopts non-blocking I/O and event-driven programming models, supports multi-user concurrent access, includes reactive business function modules and system management modules, and provides comprehensive reactive logging and exception alert mechanisms.

**For personal reference and learning purposes only, the project is still being improved.**

## Table of Contents

- [Features](#features)
- [Technology Stack](#technology-stack)
- [System Requirements](#system-requirements)
- [Quick Start](#quick-start)
- [Configuration](#configuration)
- [Building and Running](#building-and-running)
- [API Documentation](#api-documentation)
- [Security](#security)
- [Testing](#testing)
- [License](#license)
- [Copyright](#copyright)
- [Contact](#contact)

## Features

- **Reactive Architecture**: Built on Spring WebFlux for non-blocking, event-driven programming
- **Comprehensive Authentication**: JWT-based authentication with secure token management
- **User Authorization**: Access control with SYSTEM_USER, NORMAL_USER, and GUEST user types
- **Unified Response Format**: Standardized API responses using ApiResponse model
- **High Performance**: Optimized for 32GB RAM environments with parallel processing capabilities
- **Monitoring**: Integrated with Prometheus and Grafana for system monitoring
- **Logging**: Structured logging with trace ID propagation for better diagnostics
- **Unified Configuration**: Centralized configuration management system
- **Health Check**: Comprehensive system health verification mechanisms

## Technology Stack

| Category | Technology | Version |
|----------|------------|--------|
| **Framework** | Spring Boot | 3.5.7 |
| | Spring WebFlux | - |
| **JDK** | Oracle JDK / OpenJDK | 21 |
| **Database** | MySQL / MariaDB | 9.4.0 |
| **Cache** | Redis | 8.2.1 |
| | Lettuce (Redis client) | 6.8.0 |
| **Data Access** | R2DBC | - |
| **Security** | Spring Security | - |
| | JWT (JJWT) | 0.12.6 |
| **Build Tool** | Maven | 3.8.0+ |
| **Testing** | JUnit 5 | - |
| | WebTestClient | - |
| | Mockito | - |
| | Testcontainers | - |
| **Monitoring** | Prometheus | - |
| | Grafana | - |

## System Requirements

### Hardware Requirements

- **Memory**: 32GB RAM (optimal for development environment)
- **Disk Space**: 10GB minimum for application, dependencies and logs

### Software Requirements

- **JDK**: 21
- **Operating System**: Windows 11 (development environment)
- **Build Tool**: Maven 3.8.0 or higher
- **Database**: MySQL 9.4.0/MariaDB (port 8906)
- **Cache**: Redis 8.2.1 (port 8902)

## Quick Start

### Prerequisites
1. Install JDK 21 or higher
2. Install MySQL 9.4.0 and Redis 8.2.1
3. Configure MySQL with:
   - Username: `honyrunMysql` or `root`
   - Password: `honyrun@sys` or `rootpwd`
   - Database: `honyrundb`
   - Port: 8906
4. Configure Redis with:
   - Port: 8902

### Installation

1. **Clone the repository**:
   ```bash
   git clone https://github.com/reyiosk/honyrun.git
   cd honyrun/backend
   ```

2. **Configure environment**:
   Update configuration files as needed (see [Configuration](#configuration) section)

3. **Build the application**:
   ```bash
   ./mvnw clean package
   ```

4. **Run the application**:
   ```bash
   java -jar target/honyrun-*.jar
   ```

## Configuration

### Environment Variables

The application can be configured using the following environment variables:

| Environment Variable | Description | Default Value |
|----------------------|-------------|--------------|
| `SERVER_PORT` | Application port | 8901 |
| `SPRING_DATASOURCE_URL` | MySQL connection URL | - |
| `SPRING_DATASOURCE_USERNAME` | MySQL username | - |
| `SPRING_DATASOURCE_PASSWORD` | MySQL password | - |
| `SPRING_REDIS_HOST` | Redis host | localhost |
| `SPRING_REDIS_PORT` | Redis port | 8902 |
| `JWT_SECRET` | Secret key for JWT token generation | - |
| `JWT_EXPIRATION` | JWT token expiration time in milliseconds | - |

### Configuration Files

The application uses the following configuration files:
- `application.properties` - Base configuration
- `application-dev.properties` - Development environment configuration
- `application-prod.properties` - Production environment configuration
- `application-test.properties` - Testing environment configuration (only in test directory)

### JVM Configuration

For optimal performance in 32GB RAM environments, use the following JVM settings:

```
-Xms8g -Xmx16g -XX:+UseG1GC -XX:+UseStringDeduplication
```

## Building and Running

### Building with Maven

```bash
# Build with Maven wrapper
./mvnw clean package

# Build with parallel processing (recommended for 32GB environments)
./mvnw clean package -T 8C
```

### Running the Application

```bash
# Run with default configuration
java -jar target/honyrun-*.jar

# Run with specific profile
java -jar -Dspring.profiles.active=dev target/honyrun-*.jar

# Run with custom JVM parameters
java -Xms8g -Xmx16g -jar target/honyrun-*.jar
```

## API Documentation

API documentation is available at:

- **Swagger UI**: [http://localhost:8901/swagger-ui.html](http://localhost:8901/swagger-ui.html)
- **API Specification**: [http://localhost:8901/v3/api-docs](http://localhost:8901/v3/api-docs)

## Security

### Authentication

- JWT-based authentication is used for all API endpoints except login endpoints
- Test users are available:
  - **System users**: `honyrun-sys`, `honyrunsys2` (password: `honyrun@sys`)
  - **Normal users**: `user1`, `user2` (password: `honyrun@sys`)

### Authorization

- User types are strictly enforced: SYSTEM_USER, NORMAL_USER, GUEST
- All non-authentication endpoints require proper authorization
- Security test environment is standardized using TestJwtSecurityConfig

### Password Storage

- Passwords are hashed using BCrypt with configurable cost factor
- Password history is maintained for security auditing

## Testing

### Running Tests

```bash
# Run all tests
./mvnw test

# Run tests with coverage
./mvnw test jacoco:report
```

### Test Guidelines

- Minimal mocking principle is followed
- Real external Redis is used for testing
- Only uncontrollable external dependencies use mocking
- Test logs are available in the `backend/logs` directory

## License

This project is licensed under the MIT License - see the [LICENSE.txt](LICENSE.txt) file for details.

## Copyright

© 2025 Mr.Rey - All rights reserved.

## Contact

- **GitHub**: [https://github.com/reyiosk](https://github.com/reyiosk)
- **Email**: reyisok@live.com
