package com.example.serviceb.service;

import com.example.serviceb.model.ConversionHistory;
import com.example.serviceb.repository.ConversionHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ConversionService {
    
    private static final Logger log = LoggerFactory.getLogger(ConversionService.class);
    
    private final ConversionHistoryRepository conversionHistoryRepository;
    private final RestTemplate restTemplate;
    
    private static final String SERVICE_A_BASE_URL = "http://localhost:8081";
    
    @Autowired
    public ConversionService(ConversionHistoryRepository conversionHistoryRepository) {
        this.conversionHistoryRepository = conversionHistoryRepository;
        this.restTemplate = new RestTemplate();
    }
    
    /**
     * Convert foreign currency to Manat (AZN)
     * @param amount The amount to convert
     * @param rate The exchange rate
     * @return The converted amount in Manat
     */
    public BigDecimal toManat(BigDecimal amount, BigDecimal rate) {
        try {
            log.info("Converting {} to Manat with rate: {}", amount, rate);
            
            BigDecimal result = amount.multiply(rate).setScale(4, RoundingMode.HALF_UP);
            
            // Save conversion to database
            saveConversionHistory(ConversionHistory.ConversionType.toManat, amount, rate, result);
            
            log.info("Conversion to Manat completed: {} * {} = {}", amount, rate, result);
            return result;
            
        } catch (Exception e) {
            log.error("Error converting to Manat: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to convert to Manat: " + e.getMessage(), e);
        }
    }
    
    /**
     * Convert Manat (AZN) to foreign currency
     * @param amount The amount in Manat to convert
     * @param rate The exchange rate
     * @return The converted amount in foreign currency
     */
    public BigDecimal fromManat(BigDecimal amount, BigDecimal rate) {
        try {
            log.info("Converting {} from Manat with rate: {}", amount, rate);
            
            // Validate rate is not zero to prevent division by zero
            if (rate.compareTo(BigDecimal.ZERO) == 0) {
                log.error("Cannot convert from Manat with zero rate");
                throw new RuntimeException("Exchange rate cannot be zero for conversion from Manat");
            }
            
            BigDecimal result = amount.divide(rate, 4, RoundingMode.HALF_UP);
            
            // Save conversion to database
            saveConversionHistory(ConversionHistory.ConversionType.fromManat, amount, rate, result);
            
            log.info("Conversion from Manat completed: {} / {} = {}", amount, rate, result);
            return result;
            
        } catch (Exception e) {
            log.error("Error converting from Manat: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to convert from Manat: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get exchange rate from Service A
     * @param date The date for the exchange rate
     * @param currency The currency code
     * @return The exchange rate
     */
    public BigDecimal getRateFromServiceA(LocalDate date, String currency) {
        try {
            log.info("=== START: Fetching rate from Service A for currency: {} on date: {} ===", currency, date);
            
            String url = String.format("%s/currencies/rate?date=%s&currency=%s", 
                    SERVICE_A_BASE_URL, date.toString(), currency);
            
            log.info("Calling Service A URL: {}", url);
            
            ResponseEntity<Object> response = restTemplate.getForEntity(url, Object.class);
            
            log.info("Service A response status: {}", response.getStatusCode());
            log.info("Service A response body type: {}", response.getBody() != null ? response.getBody().getClass().getSimpleName() : "NULL");
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // Parse the response to extract the rate
                String responseBody = response.getBody().toString();
                log.info("Service A response body: {}", responseBody);
                
                // Extract the rate from the JSON response
                // Expected format: {"code":"EUR","name":"1 Avro","rate":1.9829}
                String rateStr = extractRateFromJsonResponse(responseBody);
                if (rateStr != null) {
                    try {
                        BigDecimal rate = new BigDecimal(rateStr);
                        log.info("=== SUCCESS: Successfully extracted rate: {} for currency: {} ===", rate, currency);
                        return rate;
                    } catch (NumberFormatException e) {
                        log.error("Failed to parse rate value '{}' as BigDecimal", rateStr, e);
                        throw new RuntimeException("Failed to parse rate value '" + rateStr + "' as BigDecimal for currency " + currency, e);
                    }
                } else {
                    log.error("=== ERROR: Could not extract 'rate' field from Service A response ===");
                    log.error("Raw response body: {}", responseBody);
                    log.error("Expected JSON format: {\"code\":\"CURRENCY\",\"name\":\"Currency Name\",\"rate\":NUMBER}");
                    throw new RuntimeException("Missing 'rate' field in Service A response for currency " + currency + ". Raw response: " + responseBody);
                }
            } else {
                log.error("Failed to get rate from Service A. Status: {}", response.getStatusCode());
                throw new RuntimeException("Failed to get rate from Service A - Status: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            log.error("=== ERROR: Error fetching rate from Service A: {} ===", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch rate from Service A for currency " + currency + ": " + e.getMessage(), e);
        }
    }
    
    /**
     * Extract rate value from Service A response
     * @param responseBody The response body as string
     * @return The rate value as string, or null if not found
     */
    private String extractRateFromJsonResponse(String responseBody) {
        try {
            log.info("Attempting to parse response: {}", responseBody);
            
            // Parse response to extract rate
            // Actual format from Service A: {code=EUR, name=1 Avro, rate=1.9829}
            // Expected JSON format: {"code":"EUR","name":"1 Avro","rate":1.9829}
            
            // Try to extract rate from actual Service A format (no quotes around field names)
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("rate=([0-9]+\\.[0-9]+)");
            java.util.regex.Matcher matcher = pattern.matcher(responseBody);
            
            if (matcher.find()) {
                String rate = matcher.group(1);
                log.info("Found rate (actual Service A format): {}", rate);
                return rate;
            }
            
            // Try to extract rate as integer from actual Service A format
            pattern = java.util.regex.Pattern.compile("rate=([0-9]+)");
            matcher = pattern.matcher(responseBody);
            
            if (matcher.find()) {
                String rate = matcher.group(1) + ".0";
                log.info("Found rate (integer, actual Service A format): {}", rate);
                return rate;
            }
            
            // Try to extract rate as decimal number with quotes around field name (JSON format)
            pattern = java.util.regex.Pattern.compile("\"rate\"\\s*:\\s*([0-9]+\\.[0-9]+)");
            matcher = pattern.matcher(responseBody);
            
            if (matcher.find()) {
                String rate = matcher.group(1);
                log.info("Found rate (JSON format with quotes): {}", rate);
                return rate;
            }
            
            // Try to extract rate as integer with quotes around field name (JSON format)
            pattern = java.util.regex.Pattern.compile("\"rate\"\\s*:\\s*([0-9]+)");
            matcher = pattern.matcher(responseBody);
            
            if (matcher.find()) {
                String rate = matcher.group(1) + ".0";
                log.info("Found rate (integer, JSON format with quotes): {}", rate);
                return rate;
            }
            
            // Try to extract rate as decimal number without quotes around field name
            pattern = java.util.regex.Pattern.compile("rate\\s*:\\s*([0-9]+\\.[0-9]+)");
            matcher = pattern.matcher(responseBody);
            
            if (matcher.find()) {
                String rate = matcher.group(1);
                log.info("Found rate (decimal without quotes): {}", rate);
                return rate;
            }
            
            // Try to extract rate as integer without quotes around field name
            pattern = java.util.regex.Pattern.compile("rate\\s*:\\s*([0-9]+)");
            matcher = pattern.matcher(responseBody);
            
            if (matcher.find()) {
                String rate = matcher.group(1) + ".0";
                log.info("Found rate (integer without quotes): {}", rate);
                return rate;
            }
            
            log.error("No 'rate' field found in response body: {}", responseBody);
            log.error("Tried patterns: 'rate=([0-9]+\\.[0-9]+)', 'rate=([0-9]+)', '\"rate\"\\s*:\\s*([0-9]+\\.[0-9]+)', '\"rate\"\\s*:\\s*([0-9]+)', 'rate\\s*:\\s*([0-9]+\\.[0-9]+)', 'rate\\s*:\\s*([0-9]+)'");
            return null;
        } catch (Exception e) {
            log.error("Error extracting rate from response: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Convert to Manat using rate from Service A
     * @param amount The amount to convert
     * @param date The date for the exchange rate
     * @param currency The currency code
     * @return The converted amount in Manat
     */
    public BigDecimal toManatWithServiceA(BigDecimal amount, LocalDate date, String currency) {
        BigDecimal rate = getRateFromServiceA(date, currency);
        return toManat(amount, rate);
    }
    
    /**
     * Convert from Manat using rate from Service A
     * @param amount The amount in Manat to convert
     * @param date The date for the exchange rate
     * @param currency The currency code
     * @return The converted amount in foreign currency
     */
    public BigDecimal fromManatWithServiceA(BigDecimal amount, LocalDate date, String currency) {
        BigDecimal rate = getRateFromServiceA(date, currency);
        return fromManat(amount, rate);
    }
    
    private void saveConversionHistory(ConversionHistory.ConversionType type, BigDecimal amount, 
                                     BigDecimal rate, BigDecimal result) {
        try {
            ConversionHistory history = new ConversionHistory();
            history.setType(type);
            history.setDate(LocalDate.now());
            history.setCurrency("AZN"); // For now, hardcoded - you might want to make this dynamic
            history.setAmount(amount);
            history.setRate(rate);
            history.setResult(result);
            
            conversionHistoryRepository.save(history);
            log.info("Conversion history saved with ID: {}", history.getId());
            
        } catch (Exception e) {
            log.error("Error saving conversion history: {}", e.getMessage(), e);
            // Don't throw exception here to avoid breaking the conversion flow
        }
    }
    
    public List<ConversionHistory> getConversionHistory(LocalDate date) {
        return conversionHistoryRepository.findByDateOrderByCreatedAtDesc(date);
    }
    
    public List<ConversionHistory> getConversionHistoryByCurrency(String currency) {
        return conversionHistoryRepository.findByCurrencyOrderByCreatedAtDesc(currency);
    }
    
    public List<ConversionHistory> getConversionHistoryByType(ConversionHistory.ConversionType type) {
        return conversionHistoryRepository.findByTypeOrderByCreatedAtDesc(type);
    }
    
    public long getConversionCount(LocalDate date) {
        return conversionHistoryRepository.countByDate(date);
    }
}
