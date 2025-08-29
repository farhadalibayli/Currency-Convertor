# Service A - Spring Boot Application

A Spring Boot application for the currency converter service.

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

## Configuration

- **Port**: 8081
- **Java Version**: 17
- **Spring Boot Version**: 3.2.0

## Dependencies

- Spring Web
- Spring Boot DevTools
- Lombok
- Jackson XML (for parsing CBAR XML responses)
- Spring Boot Test (for testing)

## Running the Application

### Using Maven

```bash
# Navigate to the service-a directory
cd service-a

# Clean and compile
mvn clean compile

# Run the application
mvn spring-boot:run
```

### Using IDE

1. Open the project in your IDE (IntelliJ IDEA, Eclipse, VS Code)
2. Run the `ServiceAApplication.java` main class

## API Endpoints

### Health Check

```
GET http://localhost:8081/api/health
```

Expected response:
```json
{
  "status": "UP",
  "service": "service-a",
  "timestamp": "2024-01-01T12:00:00",
  "port": 8081
}
```

### Get Currencies

```
GET http://localhost:8081/currencies?date=YYYY-MM-DD
```

**Parameters:**
- `date` (required): Date in YYYY-MM-DD format

**Example:**
```
GET http://localhost:8081/currencies?date=2024-01-01
```

**Success Response:**
```json
[
  {
    "code": "USD",
    "name": "US Dollar"
  },
  {
    "code": "EUR", 
    "name": "Euro"
  },
  {
    "code": "TRY",
    "name": "Turkish Lira"
  }
]
```

**Error Responses:**

Invalid date format:
```json
{
  "timestamp": "2024-01-01T12:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid date format. Please use YYYY-MM-DD format.",
  "path": "/currencies"
}
```

Server error:
```json
{
  "timestamp": "2024-01-01T12:00:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Failed to fetch currency data. Please try again later.",
  "details": "Connection timeout"
}
```

## Features

- **CBAR Integration**: Fetches currency data from Central Bank of Azerbaijan
- **XML Parsing**: Parses CBAR XML responses using Jackson XML
- **Error Handling**: Comprehensive error handling with detailed error messages
- **Date Validation**: Validates that requested dates are not in the future
- **Logging**: Detailed logging for debugging and monitoring

## Development

The application includes Spring Boot DevTools for hot reloading during development. Any changes to the source code will automatically restart the application.

## Project Structure

```
service-a/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/servicea/
│   │   │       ├── ServiceAApplication.java
│   │   │       ├── controller/
│   │   │       │   ├── HealthController.java
│   │   │       │   └── CurrencyController.java
│   │   │       ├── service/
│   │   │       │   └── CbarService.java
│   │   │       ├── model/
│   │   │       │   ├── Currency.java
│   │   │       │   └── CbarResponse.java
│   │   │       └── exception/
│   │   │           └── GlobalExceptionHandler.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
└── pom.xml
```
