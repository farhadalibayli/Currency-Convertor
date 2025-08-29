# Service B - Currency Conversion Service

A Spring Boot application for handling currency conversions and storing conversion history in a MySQL database.

## Prerequisites

- Java 21 or higher
- Maven 3.6 or higher
- MySQL 8.0 or higher

## Configuration

- **Port**: 8082
- **Java Version**: 21
- **Spring Boot Version**: 3.3.0
- **Database**: MySQL

## Dependencies

- Spring Web
- Spring Data JPA
- MySQL Connector
- Lombok
- Spring Boot Test (for testing)

## Database Setup

1. **Create MySQL Database:**
   ```sql
   CREATE DATABASE currency_converter;
   ```

2. **Update Database Configuration:**
   Edit `src/main/resources/application.properties` and update:
   ```properties
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```

## Running the Application

### Using Maven

```bash
# Navigate to the service-b directory
cd service-b

# Clean and compile
mvn clean compile

# Run the application
mvn spring-boot:run
```

### Using IDE

1. Open the project in your IDE (IntelliJ IDEA, Eclipse, VS Code)
2. Run the `ServiceBApplication.java` main class

## API Endpoints

### Health Check

```
GET http://localhost:8082/api/health
```

Expected response:
```json
{
  "status": "UP",
  "service": "service-b",
  "timestamp": "2024-01-01T12:00:00",
  "port": 8082
}
```

### Currency Conversion

```
POST http://localhost:8082/api/conversions/convert
```

Request body:
```json
{
  "date": "2024-01-01",
  "fromCurrency": "USD",
  "toCurrency": "AZN",
  "amount": 100.00
}
```

Response:
```json
{
  "originalAmount": 100.00,
  "fromCurrency": "USD",
  "convertedAmount": 170.00,
  "toCurrency": "AZN",
  "exchangeRate": 1.7000,
  "conversionTime": "2024-01-01T12:00:00",
  "status": "SUCCESS"
}
```

**Note**: Exchange rates are fetched in real-time from the Central Bank of Azerbaijan (CBAR) via Service A.

### Conversion History

```
GET http://localhost:8082/api/conversions/history?date=2024-01-01
```

### Conversion History by Currency

```
GET http://localhost:8082/api/conversions/history/currency?fromCurrency=USD&toCurrency=AZN
```

### Conversion Count

```
GET http://localhost:8082/api/conversions/count?date=2024-01-01
```

## Features

- **Currency Conversion**: Convert between different currencies
- **Conversion History**: Store and retrieve conversion history
- **Database Integration**: MySQL database for persistent storage
- **CORS Support**: Configured for frontend integration
- **Logging**: Comprehensive logging for debugging
- **Error Handling**: Proper error handling and responses

## Database Schema

The application automatically creates the following table:

```sql
CREATE TABLE conversion_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversion_date DATE NOT NULL,
    from_currency VARCHAR(3) NOT NULL,
    to_currency VARCHAR(3) NOT NULL,
    original_amount DECIMAL(19,4) NOT NULL,
    converted_amount DECIMAL(19,4) NOT NULL,
    exchange_rate DECIMAL(19,6) NOT NULL,
    created_at DATETIME NOT NULL
);
```

## Development

The application includes Spring Boot DevTools for hot reloading during development. Any changes to the source code will automatically restart the application.

## Project Structure

```
service-b/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/serviceb/
│   │   │       ├── ServiceBApplication.java
│   │   │       ├── config/
│   │   │       │   └── WebConfig.java
│   │   │       ├── controller/
│   │   │       │   ├── HealthController.java
│   │   │       │   └── ConversionController.java
│   │   │       ├── service/
│   │   │       │   └── ConversionService.java
│   │   │       ├── model/
│   │   │       │   ├── ConversionRequest.java
│   │   │       │   ├── ConversionResponse.java
│   │   │       │   └── ConversionHistory.java
│   │   │       └── repository/
│   │   │           └── ConversionHistoryRepository.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
└── pom.xml
```
