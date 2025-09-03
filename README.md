# Currency Converter Application

A comprehensive currency conversion application built with a microservices architecture, featuring real-time exchange rates from the Central Bank of Azerbaijan (CBAR) and a modern React frontend.

## 🏗️ Architecture Overview

This application follows a microservices architecture with three main components:

```
┌─────────────┐    HTTP/REST    ┌─────────────┐    HTTP/REST    ┌─────────────┐
│   Frontend  │ ──────────────► │ Service B   │ ──────────────► │ Service A   │
│   (React)   │                 │ (Conversion │                 │ (CBAR API)  │
│   Port 3000 │                 │  Service)   │                 │ Port 8081   │
│             │                 │ Port 8082   │                 │             │
└─────────────┘                 └─────────────┘                 └─────────────┘
                                        │
                                        ▼
                                 ┌─────────────┐
                                 │   MySQL     │
                                 │  Database   │
                                 │             │
                                 └─────────────┘
```

## 🚀 Quick Start

### Prerequisites

- **Java 21** or higher
- **Node.js 18** or higher
- **MySQL 8.0** or higher
- **Maven 3.6** or higher

### 1. Database Setup

```sql
CREATE DATABASE currency_converter;
```

### 2. Start Service A (CBAR Data Service)

```bash
cd service-a
mvn clean compile
mvn spring-boot:run
```

Service A will start on `http://localhost:8081`

### 3. Start Service B (Conversion Service)

```bash
cd service-b
# Update database credentials in application.properties
mvn clean compile
mvn spring-boot:run
```

Service B will start on `http://localhost:8082`

### 4. Start Frontend

```bash
cd frontend
npm install
npm start
```

The application will open at `http://localhost:3000`

## 📋 Services Overview

### Service A - CBAR Data Service
- **Port**: 8081
- **Purpose**: Fetches real-time currency exchange rates from Central Bank of Azerbaijan
- **Features**:
  - XML parsing of CBAR API responses
  - UTF-8 encoding support for Azerbaijani characters
  - Comprehensive error handling
  - Support for precious metals (XAU, XAG, XPD)

### Service B - Conversion Service
- **Port**: 8082
- **Purpose**: Handles currency conversions and stores conversion history
- **Features**:
  - Real-time currency conversion
  - MySQL database integration
  - Conversion history tracking
  - RESTful API endpoints

### Frontend - React Application
- **Port**: 3000
- **Purpose**: Modern web interface for currency conversion
- **Features**:
  - Real-time currency conversion
  - Historical conversion tracking
  - Responsive design
  - Modern UI/UX

## 🔧 Configuration

### Service A Configuration
Edit `service-a/src/main/resources/application.properties`:
```properties
server.port=8081
spring.application.name=service-a
# CBAR API configuration
cbar.base-url=https://cbar.az/currencies
```

### Service B Configuration
Edit `service-b/src/main/resources/application.properties`:
```properties
server.port=8082
spring.application.name=service-b
# Database configuration
spring.datasource.url=jdbc:mysql://localhost:3306/currency_converter
spring.datasource.username=your_username
spring.datasource.password=your_password
```

## 📚 API Documentation

### Service A Endpoints

#### Health Check
```
GET http://localhost:8081/api/health
```

#### Get Currencies
```
GET http://localhost:8081/currencies?date=YYYY-MM-DD
```

### Service B Endpoints

#### Health Check
```
GET http://localhost:8082/api/health
```

#### Currency Conversion
```
POST http://localhost:8082/api/conversions/convert
Content-Type: application/json

{
  "date": "2024-01-01",
  "fromCurrency": "USD",
  "toCurrency": "AZN",
  "amount": 100.00
}
```

#### Conversion History
```
GET http://localhost:8082/api/conversions/history?date=2024-01-01
```

## 🛠️ Development

### Building the Project

```bash
# Build Service A
cd service-a
mvn clean package

# Build Service B
cd service-b
mvn clean package

# Build Frontend
cd frontend
npm run build
```

### Running Tests

```bash
# Service A tests
cd service-a
mvn test

# Service B tests
cd service-b
mvn test

# Frontend tests
cd frontend
npm test
```

## 🔍 Troubleshooting

### Common Issues

1. **Service A won't start**: Check if port 8081 is available
2. **Service B won't start**: Verify MySQL connection and database exists
3. **Frontend can't connect**: Ensure both services are running
4. **Encoding issues**: Verify UTF-8 configuration in Service A

### Logs

- Service A logs: Check console output or `service-a/logs/`
- Service B logs: Check console output or `service-b/logs/`
- Frontend logs: Check browser console

## 📁 Project Structure

```
Currency-Convertor/
├── service-a/                 # CBAR Data Service
│   ├── src/main/java/
│   │   └── com/example/servicea/
│   │       ├── controller/    # REST controllers
│   │       ├── service/       # Business logic
│   │       ├── model/         # Data models
│   │       └── exception/     # Error handling
│   └── pom.xml
├── service-b/                 # Conversion Service
│   ├── src/main/java/
│   │   └── com/example/serviceb/
│   │       ├── controller/    # REST controllers
│   │       ├── service/       # Business logic
│   │       ├── model/         # Data models
│   │       └── repository/    # Database access
│   └── pom.xml
├── frontend/                  # React Application
│   ├── src/
│   │   ├── components/        # React components
│   │   ├── services/          # API calls
│   │   └── utils/             # Utility functions
│   └── package.json
└── README.md
```