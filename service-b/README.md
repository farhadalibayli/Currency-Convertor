# Service B - Currency Conversion Service

A Spring Boot microservice that handles currency conversions, stores conversion history in a MySQL database, and provides a comprehensive API for currency conversion operations. This service acts as the core business logic layer for the currency conversion application.

## 🎯 Purpose

Service B is responsible for:
- Performing real-time currency conversions using rates from Service A
- Storing and retrieving conversion history in MySQL database
- Providing RESTful API endpoints for conversion operations
- Managing conversion statistics and analytics
- Serving as the main backend for the frontend application

## 🏗️ Architecture

```
┌─────────────┐    HTTP/REST    ┌─────────────┐    HTTP/REST    ┌─────────────┐
│   Frontend  │ ──────────────► │ Service B   │ ──────────────► │ Service A   │
│   (Port 3000)│                 │ (Port 8082) │                 │ (Port 8081) │
└─────────────┘                 └─────────────┘                 └─────────────┘
                                        │
                                        ▼
                                 ┌─────────────┐
                                 │   MySQL     │
                                 │  Database   │
                                 │             │
                                 └─────────────┘
```

## 📋 Prerequisites

- **Java 21** or higher
- **Maven 3.6** or higher
- **MySQL 8.0** or higher
- **Service A** running on port 8081

## ⚙️ Configuration

### Application Properties

```properties
# Server Configuration
server.port=8082
spring.application.name=service-b

# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/currency_converter
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
spring.jpa.properties.hibernate.format_sql=true

# Character Encoding (UTF-8)
server.servlet.encoding.enabled=true
server.servlet.encoding.force=true
spring.http.encoding.charset=UTF-8
spring.http.encoding.enabled=true
spring.http.encoding.force=true

# Service A Configuration
service-a.base-url=http://localhost:8081

# Logging
logging.level.com.example.serviceb=INFO
logging.level.org.springframework.web=INFO
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

## 🚀 Running the Application

### 1. Database Setup

```sql
-- Create the database
CREATE DATABASE currency_converter;

-- The application will automatically create tables
-- using JPA/Hibernate with ddl-auto=update
```

### 2. Update Database Credentials

Edit `src/main/resources/application.properties`:
```properties
spring.datasource.username=your_mysql_username
spring.datasource.password=your_mysql_password
```

### 3. Start the Application

#### Using Maven

```bash
# Navigate to the service-b directory
cd service-b

# Clean and compile
mvn clean compile

# Run the application
mvn spring-boot:run
```

#### Using IDE

1. Open the project in your IDE (IntelliJ IDEA, Eclipse, VS Code)
2. Run the `ServiceBApplication.java` main class

#### Using JAR

```bash
# Build the JAR
mvn clean package

# Run the JAR
java -jar target/service-b-0.0.1-SNAPSHOT.jar
```

## 📚 API Endpoints

### Health Check

```
GET http://localhost:8082/api/health
```

**Response:**
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
Content-Type: application/json
```

**Request Body:**
```json
{
  "date": "2024-01-01",
  "fromCurrency": "USD",
  "toCurrency": "AZN",
  "amount": 100.00
}
```

**Success Response:**
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

**Error Response:**
```json
{
  "timestamp": "2024-01-01T12:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid currency code: INVALID",
  "path": "/api/conversions/convert"
}
```

### Get Conversion History

```
GET http://localhost:8082/api/conversions/history?date=2024-01-01
```

**Response:**
```json
[
  {
    "id": 1,
    "conversionDate": "2024-01-01",
    "fromCurrency": "USD",
    "toCurrency": "AZN",
    "originalAmount": 100.00,
    "convertedAmount": 170.00,
    "exchangeRate": 1.7000,
    "createdAt": "2024-01-01T12:00:00"
  }
]
```

### Get Conversion History by Currency

```
GET http://localhost:8082/api/conversions/history/currency?fromCurrency=USD&toCurrency=AZN
```

### Get Conversion Count

```
GET http://localhost:8082/api/conversions/count?date=2024-01-01
```

**Response:**
```json
{
  "date": "2024-01-01",
  "conversionCount": 25
}
```

### Get Available Currencies

```
GET http://localhost:8082/api/currencies?date=2024-01-01
```

**Response:**
```json
[
  {
    "code": "USD",
    "name": "US Dollar",
    "rate": 1.7000
  },
  {
    "code": "EUR",
    "name": "Euro",
    "rate": 1.8500
  },
  {
    "code": "XAU",
    "name": "Qızıl",
    "rate": 3200.50
  }
]
```

## 🔧 Key Features

### 1. Currency Conversion
- **Real-time conversion** using live exchange rates from Service A
- **Precise calculations** using BigDecimal for accuracy
- **Support for all currencies** including precious metals (XAU, XAG, XPD)
- **Bidirectional conversion** (any currency to any currency)

### 2. Database Integration
- **MySQL database** for persistent storage
- **JPA/Hibernate** for object-relational mapping
- **Automatic table creation** with ddl-auto=update
- **Transaction management** for data consistency

### 3. Conversion History
- **Complete audit trail** of all conversions
- **Historical data retrieval** by date and currency pairs
- **Statistics and analytics** for conversion patterns
- **Data retention** and cleanup policies

### 4. API Management
- **RESTful endpoints** following best practices
- **Comprehensive error handling** with detailed messages
- **Input validation** for all parameters
- **CORS configuration** for frontend integration

### 5. Service Integration
- **HTTP client** for communication with Service A
- **Fallback mechanisms** when Service A is unavailable
- **Rate limiting** and connection pooling
- **Health monitoring** of dependent services

### 6. Performance & Monitoring
- **Connection pooling** for database and HTTP clients
- **Caching strategies** for frequently accessed data
- **Performance metrics** and logging
- **Health checks** and monitoring endpoints

## 🛠️ Technical Implementation

### Core Components

#### ConversionService
The main service class that handles:
- Currency conversion logic
- Database operations
- Service A integration
- Business rule validation

#### ConversionHistory Entity
```java
@Entity
@Table(name = "conversion_history")
public class ConversionHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "conversion_date", nullable = false)
    private LocalDate conversionDate;
    
    @Column(name = "from_currency", nullable = false, length = 3)
    private String fromCurrency;
    
    @Column(name = "to_currency", nullable = false, length = 3)
    private String toCurrency;
    
    @Column(name = "original_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal originalAmount;
    
    @Column(name = "converted_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal convertedAmount;
    
    @Column(name = "exchange_rate", nullable = false, precision = 19, scale = 6)
    private BigDecimal exchangeRate;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
```

#### Database Schema
```sql
CREATE TABLE conversion_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversion_date DATE NOT NULL,
    from_currency VARCHAR(3) NOT NULL,
    to_currency VARCHAR(3) NOT NULL,
    original_amount DECIMAL(19,4) NOT NULL,
    converted_amount DECIMAL(19,4) NOT NULL,
    exchange_rate DECIMAL(19,6) NOT NULL,
    created_at DATETIME NOT NULL,
    INDEX idx_conversion_date (conversion_date),
    INDEX idx_currencies (from_currency, to_currency),
    INDEX idx_created_at (created_at)
);
```

### Conversion Logic

#### Rate Calculation
```java
// Example conversion logic
BigDecimal rate = getExchangeRate(fromCurrency, toCurrency, date);
BigDecimal convertedAmount = originalAmount.multiply(rate)
    .setScale(4, RoundingMode.HALF_UP);
```

#### Cross-Currency Conversion
```java
// Convert USD to EUR via AZN
BigDecimal usdToAzn = getRate("USD", "AZN", date);
BigDecimal aznToEur = getRate("AZN", "EUR", date);
BigDecimal usdToEur = usdToAzn.multiply(aznToEur);
```

## 🔍 Troubleshooting

### Common Issues

1. **Service won't start**
   - Check if port 8082 is available
   - Verify Java version (21+)
   - Check MySQL connection and credentials
   - Ensure Service A is running

2. **Database connection issues**
   - Verify MySQL is running
   - Check database credentials
   - Ensure database exists
   - Check network connectivity

3. **Service A integration issues**
   - Verify Service A is running on port 8081
   - Check network connectivity
   - Review service-a.base-url configuration
   - Check CORS settings

4. **Conversion errors**
   - Verify currency codes are valid
   - Check date format (YYYY-MM-DD)
   - Ensure amount is positive
   - Review exchange rate availability

### Debugging

Enable debug logging:
```properties
logging.level.com.example.serviceb=DEBUG
logging.level.org.springframework.web=DEBUG
logging.level.org.hibernate.SQL=DEBUG
```

### Health Check

Monitor service health:
```bash
curl http://localhost:8082/api/health
```

### Database Queries

Check conversion history:
```sql
SELECT * FROM conversion_history 
WHERE conversion_date = '2024-01-01' 
ORDER BY created_at DESC;
```

## 📁 Project Structure

```
service-b/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/serviceb/
│   │   │       ├── ServiceBApplication.java      # Main application class
│   │   │       ├── config/
│   │   │       │   └── WebConfig.java           # CORS configuration
│   │   │       ├── controller/
│   │   │       │   ├── HealthController.java    # Health check endpoint
│   │   │       │   └── ConversionController.java # Conversion endpoints
│   │   │       ├── service/
│   │   │       │   └── ConversionService.java   # Business logic
│   │   │       ├── model/
│   │   │       │   ├── ConversionRequest.java   # Request DTO
│   │   │       │   ├── ConversionResponse.java  # Response DTO
│   │   │       │   └── ConversionHistory.java   # Database entity
│   │   │       └── repository/
│   │   │           └── ConversionHistoryRepository.java # Data access
│   │   └── resources/
│   │       └── application.properties           # Configuration
│   └── test/                                    # Unit tests
└── pom.xml                                     # Maven dependencies
```