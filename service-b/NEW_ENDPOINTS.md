# Service B - New Conversion Endpoints

## Overview
New simplified conversion endpoints that accept JSON requests and automatically fetch rates from Service A.

## Base URL
`http://localhost:8082/api/conversions`

## New Endpoints

### 1. Convert to Manat (AZN)
**POST** `/toManat`

Converts foreign currency to Azerbaijani Manat (AZN) using real-time rates from Service A.

**Request Body (JSON):**
```json
{
  "date": "2024-01-15",
  "currency": "USD",
  "amount": 100.00
}
```

**Parameters:**
- `date` (String): The date for the exchange rate (format: yyyy-MM-dd)
- `currency` (String): The currency code (e.g., USD, EUR, GBP)
- `amount` (BigDecimal): The amount to convert

**Example Request:**
```bash
curl -X POST "http://localhost:8082/api/conversions/toManat" \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2024-01-15",
    "currency": "USD",
    "amount": 100.00
  }'
```

**Success Response (200 OK):**
```json
{
  "result": 170.0000,
  "message": "Successfully converted 100.00 USD to 170.0000 AZN",
  "timestamp": "2024-01-15T10:30:00",
  "status": "SUCCESS"
}
```

**Error Response (400 Bad Request):**
```json
{
  "result": null,
  "message": "Missing required parameters",
  "timestamp": "2024-01-15T10:30:00",
  "status": "ERROR"
}
```

### 2. Convert from Manat (AZN)
**POST** `/fromManat`

Converts Azerbaijani Manat (AZN) to foreign currency using real-time rates from Service A.

**Request Body (JSON):**
```json
{
  "date": "2024-01-15",
  "currency": "USD",
  "amount": 170.00
}
```

**Parameters:**
- `date` (String): The date for the exchange rate (format: yyyy-MM-dd)
- `currency` (String): The currency code (e.g., USD, EUR, GBP)
- `amount` (BigDecimal): The amount in Manat to convert

**Example Request:**
```bash
curl -X POST "http://localhost:8082/api/conversions/fromManat" \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2024-01-15",
    "currency": "USD",
    "amount": 170.00
  }'
```

**Success Response (200 OK):**
```json
{
  "result": 100.0000,
  "message": "Successfully converted 170.00 AZN to 100.0000 USD",
  "timestamp": "2024-01-15T10:30:00",
  "status": "SUCCESS"
}
```

**Error Response (400 Bad Request):**
```json
{
  "result": null,
  "message": "Amount must be greater than zero",
  "timestamp": "2024-01-15T10:30:00",
  "status": "ERROR"
}
```

## Request Models

### ConversionRequest
```java
public class ConversionRequest {
    private LocalDate date;
    private String currency;
    private BigDecimal amount;
}
```

### SimpleConversionResponse
```java
public class SimpleConversionResponse {
    private BigDecimal result;
    private String message;
    private LocalDateTime timestamp;
    private String status;
}
```

## Error Handling

### Validation Errors (400 Bad Request)
- Missing required parameters (date, currency, amount)
- Amount less than or equal to zero
- Invalid date format

### Server Errors (500 Internal Server Error)
- Service A unavailable
- Database connection issues
- Unexpected exceptions

### Error Response Format
All error responses follow the same JSON structure:
```json
{
  "result": null,
  "message": "Error description",
  "timestamp": "2024-01-15T10:30:00",
  "status": "ERROR"
}
```

## Integration Flow

1. **Request Validation**: Validates all required parameters
2. **Rate Fetching**: Calls Service A to get real-time exchange rate
3. **Conversion**: Performs the currency conversion
4. **Database Storage**: Automatically saves conversion history
5. **Response**: Returns the conversion result with status

## Supported Currencies

The endpoints support all currencies available from Service A, including:
- USD (US Dollar)
- EUR (Euro)
- GBP (British Pound)
- RUB (Russian Ruble)
- And other currencies provided by CBAR

## Logging

All operations are logged with appropriate levels:
- **INFO**: Successful conversions and requests
- **ERROR**: Validation errors and exceptions
- **DEBUG**: Detailed operation information

## Testing Examples

### Test with USD
```bash
# Convert 100 USD to Manat
curl -X POST "http://localhost:8082/api/conversions/toManat" \
  -H "Content-Type: application/json" \
  -d '{"date": "2024-01-15", "currency": "USD", "amount": 100.00}'

# Convert 170 Manat to USD
curl -X POST "http://localhost:8082/api/conversions/fromManat" \
  -H "Content-Type: application/json" \
  -d '{"date": "2024-01-15", "currency": "USD", "amount": 170.00}'
```

### Test with EUR
```bash
# Convert 100 EUR to Manat
curl -X POST "http://localhost:8082/api/conversions/toManat" \
  -H "Content-Type: application/json" \
  -d '{"date": "2024-01-15", "currency": "EUR", "amount": 100.00}'

# Convert 185 Manat to EUR
curl -X POST "http://localhost:8082/api/conversions/fromManat" \
  -H "Content-Type: application/json" \
  -d '{"date": "2024-01-15", "currency": "EUR", "amount": 185.00}'
```
