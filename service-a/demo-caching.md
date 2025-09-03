# Testing the Currency Caching Feature

## Prerequisites
1. Start the service-a application
2. Ensure the H2 database is running (in-memory)

## Step-by-Step Testing

### 1. Start the Application
```bash
cd service-a
mvn spring-boot:run
```

### 2. Test First Request (Should Hit CBAR API)
```bash
# First request for USD on 2024-01-15
curl "http://localhost:8081/currencies/rate?date=2024-01-15&currency=USD"
```
**Expected Result**: 
- Logs should show "Currency data not found in cache, fetching from CBAR API"
- Response time might be slower (external API call)
- Data gets stored in cache

### 3. Test Second Request (Should Hit Cache)
```bash
# Second request for USD on 2024-01-15
curl "http://localhost:8081/currencies/rate?date=2024-01-15&currency=USD"
```
**Expected Result**:
- Logs should show "Currency USD found in cache for date: 2024-01-15"
- Response time should be much faster
- No external API call made

### 4. Test Different Currency (Should Hit Cache if Date is Same)
```bash
# Request for EUR on the same date
curl "http://localhost:8081/currencies/rate?date=2024-01-15&currency=EUR"
```
**Expected Result**:
- Should be served from cache since all currencies for that date are cached
- Fast response time

### 5. Test Different Date (Should Hit CBAR API)
```bash
# Request for USD on a different date
curl "http://localhost:8081/currencies/rate?date=2024-01-16&currency=USD"
```
**Expected Result**:
- Should hit CBAR API since it's a new date
- Slower response time initially

### 6. Check Cache Status
```bash
# Check if data is cached for a specific date
curl "http://localhost:8081/currencies/cache/status?date=2024-01-15"
```
**Expected Result**:
```json
{
  "date": "2024-01-15",
  "isCached": true,
  "cachedCurrenciesCount": 45,
  "cacheSource": "database"
}
```

### 7. Test Cache Cleanup
```bash
# Manually trigger cache cleanup (keeps last 7 days)
curl -X POST "http://localhost:8081/currencies/cache/cleanup?daysToKeep=7"
```
**Expected Result**:
```json
{
  "message": "Cache cleanup completed successfully",
  "daysKept": "7"
}
```

## Monitoring

### 1. Check Application Logs
Look for these log messages:
- `"Currency data found in cache for date: {}"`
- `"Currency data not found in cache, fetching from CBAR API"`
- `"Currency {} found in cache for date: {}"`

### 2. Access H2 Console
- Open browser: `http://localhost:8081/h2-console`
- JDBC URL: `jdbc:h2:mem:currencydb`
- Username: `sa`
- Password: `password`
- Check the `cached_currencies` table

### 3. Performance Comparison
- First request: ~500ms-2000ms (external API)
- Cached request: ~10-50ms (database)

## Expected Benefits

1. **First Request**: Normal response time (fetches from CBAR)
2. **Subsequent Requests**: Fast response time (served from cache)
3. **Reduced API Calls**: Only one call per date per currency
4. **Better Performance**: Database queries are faster than HTTP calls
5. **Offline Capability**: Cached data available even if CBAR is down

## Troubleshooting

### Cache Not Working
- Check if H2 database is running
- Verify JPA configuration in logs
- Check for database connection errors

### Performance Issues
- Monitor database size
- Check cache hit/miss rates
- Verify cleanup task is running

### Data Inconsistency
- Clear cache manually if needed
- Check CBAR API response format
- Verify date parsing logic
