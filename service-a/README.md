# Service A - CBAR Data Service

A Spring Boot microservice that fetches real-time currency exchange rates from the Central Bank of Azerbaijan (CBAR) API. This service acts as a data provider for the currency conversion application.

## 🎯 Purpose

Service A is responsible for:
- Fetching currency data from CBAR's XML API
- Parsing XML responses and converting them to JSON
- Handling encoding issues for Azerbaijani characters
- Providing exchange rates for all supported currencies including precious metals (XAU, XAG, XPD)
- Serving as the data source for Service B (Conversion Service)

## 🏗️ Architecture

```
┌─────────────┐    HTTP/REST    ┌─────────────┐    HTTP/REST    ┌─────────────┐
│ Service B   │ ──────────────► │ Service A   │ ──────────────► │ CBAR API    │
│ (Port 8082) │                 │ (Port 8081) │                 │ (cbar.az)   │
└─────────────┘                 └─────────────┘                 └─────────────┘
```

## 📋 Prerequisites

- **Java 17** or higher
- **Maven 3.6** or higher
- **Internet connection** (for CBAR API access)

## ⚙️ Configuration

### Application Properties

```properties
# Server Configuration
server.port=8081
spring.application.name=service-a

# CBAR API Configuration
cbar.base-url=https://cbar.az/currencies

# Character Encoding (UTF-8)
server.servlet.encoding.charset=UTF-8
server.servlet.encoding.enabled=true
server.servlet.encoding.force=true
spring.http.encoding.charset=UTF-8
spring.http.encoding.enabled=true
spring.http.encoding.force=true

# Jackson Configuration
spring.jackson.default-property-inclusion=non_null
spring.jackson.serialization.write-dates-as-timestamps=false
spring.jackson.encoding=UTF-8

# Logging
logging.level.com.example.servicea=INFO
logging.level.org.springframework.web=INFO
```

## 🚀 Running the Application

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

### Using JAR

```bash
# Build the JAR
mvn clean package

# Run the JAR
java -jar target/service-a-0.0.1-SNAPSHOT.jar
```

## 📚 API Endpoints

### Health Check

```
GET http://localhost:8081/api/health
```

**Response:**
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

**Example Request:**
```
GET http://localhost:8081/currencies?date=2024-01-01
```

**Success Response:**
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

## 🔧 Key Features

### 1. CBAR Integration
- Fetches real-time currency data from Central Bank of Azerbaijan
- Supports historical data retrieval by date
- Handles CBAR's XML response format

### 2. XML Parsing
- Uses Jackson XML for parsing CBAR responses
- Handles complex XML structures with nested elements
- Supports various currency types (foreign currencies, precious metals)

### 3. Character Encoding Support
- **UTF-8 encoding** for proper handling of Azerbaijani characters
- **Encoding fixes** for corrupted characters in currency names
- **Specific mappings** for known problematic currency names

### 4. Precious Metals Support
- **XAU (Gold)**: Proper rate calculation for troy ounce notation
- **XAG (Silver)**: Silver price conversion
- **XPD (Palladium)**: Palladium price conversion
- **XPT (Platinum)**: Platinum price conversion

### 5. Error Handling
- Comprehensive error handling with detailed messages
- Graceful degradation when CBAR API is unavailable
- Input validation for date parameters

### 6. Logging
- Detailed logging for debugging and monitoring
- Performance metrics for API calls
- Error tracking and troubleshooting information

## 🛠️ Technical Implementation

### Core Components

#### CbarService
The main service class that handles:
- HTTP requests to CBAR API
- XML response parsing
- Exchange rate calculations
- Character encoding fixes

#### Currency Model
```java
public class Currency {
    private String code;        // Currency code (e.g., "USD", "XAU")
    private String name;        // Currency name (e.g., "US Dollar", "Qızıl")
    private BigDecimal rate;    // Exchange rate to AZN
}
```

#### Exchange Rate Calculation
- Handles different nominal values (e.g., "1 t.u." for troy ounces)
- Uses BigDecimal for precise calculations
- Supports various currency formats

### Character Encoding Fixes

The service includes comprehensive encoding fixes for Azerbaijani characters:

```java
// Example of encoding fixes
"QÄ±zÄ±l" → "Qızıl" (Gold)
"GÃ¼mÃ¼Å" → "Gümüş" (Silver)
"TÃ¼rk lirÉOsi" → "Türk lirəsi" (Turkish Lira)
```

## 🔍 Troubleshooting

### Common Issues

1. **Service won't start**
   - Check if port 8081 is available
   - Verify Java version (17+)
   - Check Maven installation

2. **CBAR API connection issues**
   - Verify internet connection
   - Check CBAR API availability
   - Review firewall settings

3. **Encoding issues**
   - Ensure UTF-8 configuration is correct
   - Check application.properties encoding settings
   - Verify XML mapper configuration

4. **XAU rate showing 0.0**
   - Check troy ounce notation handling
   - Verify nominal value parsing
   - Review exchange rate calculation logic

### Debugging

Enable debug logging:
```properties
logging.level.com.example.servicea=DEBUG
logging.level.org.springframework.web=DEBUG
```

### Health Check

Monitor service health:
```bash
curl http://localhost:8081/api/health
```

## 📁 Project Structure

```
service-a/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/servicea/
│   │   │       ├── ServiceAApplication.java      # Main application class
│   │   │       ├── config/
│   │   │       │   └── WebConfig.java           # CORS configuration
│   │   │       ├── controller/
│   │   │       │   ├── HealthController.java    # Health check endpoint
│   │   │       │   └── CurrencyController.java  # Currency data endpoint
│   │   │       ├── service/
│   │   │       │   └── CbarService.java         # CBAR API integration
│   │   │       ├── model/
│   │   │       │   ├── Currency.java            # Currency data model
│   │   │       │   └── CbarResponse.java        # CBAR XML response model
│   │   │       └── exception/
│   │   │           └── GlobalExceptionHandler.java # Error handling
│   │   └── resources/
│   │       └── application.properties           # Configuration
│   └── test/                                    # Unit tests
└── pom.xml                                     # Maven dependencies
```

## 🔄 Recent Updates

### Latest Fixes
- **Fixed XAU rate calculation**: Resolved issue where gold rates showed 0.0
- **Enhanced encoding support**: Improved handling of Azerbaijani characters
- **Better error handling**: More detailed error messages and logging
- **Performance improvements**: Optimized XML parsing and HTTP requests

### Version History
- **v1.2.0**: Added precious metals support and encoding fixes
- **v1.1.0**: Enhanced error handling and logging
- **v1.0.0**: Initial release with basic CBAR integration

## 🤝 Integration

### Service B Integration
Service A is designed to work seamlessly with Service B:
- RESTful API endpoints
- JSON response format
- CORS configuration for cross-origin requests
- Health check endpoint for monitoring

### Frontend Integration
The service can be directly consumed by frontend applications:
- Simple HTTP GET requests
- JSON response format
- No authentication required
- CORS enabled for browser access
