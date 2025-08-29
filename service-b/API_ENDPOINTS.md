# Service B - Currency Conversion API Endpoints

## Overview
Service B provides currency conversion functionality with integration to Service A for real-time exchange rates.

## Base URL
`http://localhost:8082/api/conversions`

## Endpoints

### 1. Convert to Manat (AZN)
**POST** `/to-manat`

Converts foreign currency to Azerbaijani Manat (AZN).

**Parameters:**
- `amount` (BigDecimal): The amount to convert
- `rate` (BigDecimal): The exchange rate

**Example:**
```bash
curl -X POST "http://localhost:8082/api/conversions/to-manat?amount=100&rate=1.7000"
```

**Response:**
```json
{
  "originalAmount": 100.00,
  "fromCurrency": "FOREIGN",
  "convertedAmount": 170.00,
  "toCurrency": "AZN",
  "exchangeRate": 1.7000,
  "conversionDate": "2024-01-15T10:30:00",
  "status": "SUCCESS"
}
```

### 2. Convert from Manat (AZN)
**POST** `/from-manat`

Converts Azerbaijani Manat (AZN) to foreign currency.

**Parameters:**
- `amount` (BigDecimal): The amount in Manat to convert
- `rate` (BigDecimal): The exchange rate

**Example:**
```bash
curl -X POST "http://localhost:8082/api/conversions/from-manat?amount=170&rate=1.7000"
```

**Response:**
```json
{
  "originalAmount": 170.00,
  "fromCurrency": "AZN",
  "convertedAmount": 100.00,
  "toCurrency": "FOREIGN",
  "exchangeRate": 1.7000,
  "conversionDate": "2024-01-15T10:30:00",
  "status": "SUCCESS"
}
```

### 3. Convert to Manat with Service A Integration
**POST** `/to-manat-with-service-a`

Converts foreign currency to Manat using real-time rates from Service A.

**Parameters:**
- `amount` (BigDecimal): The amount to convert
- `date` (Date): The date for the exchange rate (format: yyyy-MM-dd)
- `currency` (String): The currency code (e.g., USD, EUR)

**Example:**
```bash
curl -X POST "http://localhost:8082/api/conversions/to-manat-with-service-a?amount=100&date=2024-01-15&currency=USD"
```

### 4. Convert from Manat with Service A Integration
**POST** `/from-manat-with-service-a`

Converts Manat to foreign currency using real-time rates from Service A.

**Parameters:**
- `amount` (BigDecimal): The amount in Manat to convert
- `date` (Date): The date for the exchange rate (format: yyyy-MM-dd)
- `currency` (String): The currency code (e.g., USD, EUR)

**Example:**
```bash
curl -X POST "http://localhost:8082/api/conversions/from-manat-with-service-a?amount=170&date=2024-01-15&currency=USD"
```

### 5. Get Conversion History by Date
**GET** `/history`

Retrieves conversion history for a specific date.

**Parameters:**
- `date` (Date): The date to query (format: yyyy-MM-dd)

**Example:**
```bash
curl "http://localhost:8082/api/conversions/history?date=2024-01-15"
```

### 6. Get Conversion History by Currency
**GET** `/history/currency`

Retrieves conversion history for a specific currency.

**Parameters:**
- `currency` (String): The currency code

**Example:**
```bash
curl "http://localhost:8082/api/conversions/history/currency?currency=USD"
```

### 7. Get Conversion History by Type
**GET** `/history/type`

Retrieves conversion history by conversion type.

**Parameters:**
- `type` (String): The conversion type ("toManat" or "fromManat")

**Example:**
```bash
curl "http://localhost:8082/api/conversions/history/type?type=toManat"
```

### 8. Get Conversion Count
**GET** `/count`

Retrieves the number of conversions for a specific date.

**Parameters:**
- `date` (Date): The date to query (format: yyyy-MM-dd)

**Example:**
```bash
curl "http://localhost:8082/api/conversions/count?date=2024-01-15"
```

## Database Schema

The conversion history is stored in the `conversion_history` table with the following structure:

```sql
CREATE TABLE conversion_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(20) NOT NULL CHECK (type IN ('toManat', 'fromManat')),
    date DATE NOT NULL,
    currency VARCHAR(10) NOT NULL,
    amount DECIMAL(15,4) NOT NULL,
    rate DECIMAL(15,6) NOT NULL,
    result DECIMAL(15,4) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

## Service A Integration

Service B integrates with Service A to fetch real-time exchange rates:

- **Service A URL**: `http://localhost:8081/currencies/rate`
- **Parameters**: `date` and `currency`
- **Real-time data**: Uses official CBAR (Central Bank of Azerbaijan) exchange rates

## Error Handling

All endpoints return appropriate HTTP status codes:
- `200 OK`: Successful operation
- `400 Bad Request`: Invalid parameters
- `500 Internal Server Error`: Server error

Error responses include detailed logging for debugging purposes.
