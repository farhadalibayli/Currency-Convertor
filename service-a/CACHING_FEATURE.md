# Currency Caching Feature

## Overview

The Currency Converter Service A now includes an intelligent caching mechanism that stores currency exchange rates in a local database to reduce API calls to the CBAR (Central Bank of Azerbaijan) service.

## How It Works

### 1. Cache-First Approach
- When a currency request is made, the system first checks if the data exists in the local cache
- If cached data is found, it's returned immediately without making an API call
- If no cached data exists, the system fetches from CBAR API and stores the result in cache

### 2. Database Storage
- Uses H2 in-memory database for fast access
- Stores currency data with the following structure:
  - `currency_date`: The date for which the exchange rate applies
  - `currency_code`: The currency code (e.g., USD, EUR)
  - `currency_name`: The full name of the currency
  - `exchange_rate`: The exchange rate value
  - `created_at`: When the data was cached

### 3. Automatic Cleanup
- Scheduled task runs daily at 2:00 AM to remove old cache data
- Default retention period: 30 days
- Prevents database from growing indefinitely

## Benefits

1. **Reduced API Calls**: Subsequent requests for the same currency/date combination are served from cache
2. **Improved Performance**: Database queries are faster than external API calls
3. **Cost Savings**: Reduces external API usage and associated costs
4. **Better User Experience**: Faster response times for cached data
5. **Offline Capability**: Cached data remains available even if CBAR API is temporarily unavailable

## API Endpoints

### Existing Endpoints (Enhanced with Caching)
- `GET /currencies?date={date}` - Get all currencies for a date (cached if available)
- `GET /currencies/rate?date={date}&currency={code}` - Get specific currency rate (cached if available)

### New Cache Management Endpoints
- `GET /currencies/cache/status?date={date}` - Check if data is cached for a specific date
- `POST /currencies/cache/cleanup?daysToKeep={days}` - Manually trigger cache cleanup

## Cache Status Response Example

```json
{
  "date": "2024-01-15",
  "isCached": true,
  "cachedCurrenciesCount": 45,
  "cacheSource": "database"
}
```

## Configuration

### Database Settings
```properties
# H2 Database Configuration
spring.datasource.url=jdbc:h2:mem:currencydb
spring.datasource.username=sa
spring.datasource.password=password

# JPA Configuration
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
```

### H2 Console
- Access H2 database console at: `http://localhost:8081/h2-console`
- JDBC URL: `jdbc:h2:mem:currencydb`
- Username: `sa`
- Password: `password`

## Monitoring and Maintenance

### Cache Statistics
- Monitor cache hit/miss rates through application logs
- Use cache status endpoint to check cache state
- H2 console provides direct database access for debugging

### Manual Cleanup
- Use the cleanup endpoint to manually remove old cache data
- Adjust retention period based on business requirements
- Monitor database size and performance

## Testing

The caching functionality includes comprehensive unit tests:
- `CurrencyCacheServiceTest` - Tests cache operations
- Test configuration uses separate in-memory database
- Mocked dependencies for isolated testing

## Future Enhancements

1. **Redis Integration**: Replace H2 with Redis for distributed caching
2. **Cache Warming**: Pre-populate cache with frequently requested currencies
3. **Metrics Dashboard**: Real-time cache performance monitoring
4. **Cache Invalidation**: Smart cache invalidation based on data freshness
5. **Multi-Node Support**: Shared cache across multiple service instances

## Troubleshooting

### Common Issues
1. **Cache Not Working**: Check database connection and JPA configuration
2. **Memory Issues**: Monitor H2 database size and adjust cleanup frequency
3. **Performance Degradation**: Review cache hit rates and optimize queries

### Logs to Monitor
- Cache hit/miss messages in application logs
- Database operation logs
- Scheduled cleanup task execution logs
