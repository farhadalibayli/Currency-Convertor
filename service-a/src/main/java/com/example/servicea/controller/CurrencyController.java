package com.example.servicea.controller;

import com.example.servicea.model.Currency;
import com.example.servicea.service.CbarService;
import com.example.servicea.service.CurrencyCacheService;
import com.example.servicea.service.CacheCleanupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/currencies")
public class CurrencyController {
    
    private static final Logger log = LoggerFactory.getLogger(CurrencyController.class);
    
    private final CbarService cbarService;
    private final CurrencyCacheService cacheService;
    private final CacheCleanupService cleanupService;
    
    public CurrencyController(CbarService cbarService, CurrencyCacheService cacheService, CacheCleanupService cleanupService) {
        this.cbarService = cbarService;
        this.cacheService = cacheService;
        this.cleanupService = cleanupService;
    }
    
    @GetMapping
    public ResponseEntity<List<Currency>> getCurrencies(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        
        try {
            log.info("Received request for currencies on date: {}", date);
            
            // Validate date is not in the future
            if (date.isAfter(LocalDate.now())) {
                return ResponseEntity.badRequest().build();
            }
            
            List<Currency> currencies = cbarService.getCurrencies(date.toString());
            
            log.info("Returning {} currencies for date: {}", currencies.size(), date);
            return ResponseEntity.ok(currencies);
            
        } catch (Exception e) {
            log.error("Error processing currency request for date {}: {}", date, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/rate")
    public ResponseEntity<Currency> getRate(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam String currency) {
        
        try {
            log.info("Received request for rate on date: {} for currency: {}", date, currency);
            
            // Validate date is not in the future
            if (date.isAfter(LocalDate.now())) {
                return ResponseEntity.badRequest().build();
            }
            
            // Validate currency parameter
            if (currency == null || currency.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            // Use the new caching-enabled method
            Currency targetCurrency = cbarService.getCurrencyRate(date.toString(), currency.trim());
            
            log.info("Returning rate for currency: {} on date: {} - rate: {}", 
                    currency, date, targetCurrency.getRate());
            log.info("Currency object details: code={}, name={}, rate={}", 
                    targetCurrency.getCode(), targetCurrency.getName(), targetCurrency.getRate());
            return ResponseEntity.ok(targetCurrency);
            
        } catch (Exception e) {
            log.error("Error processing rate request for date {} and currency {}: {}", 
                    date, currency, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/cache/status")
    public ResponseEntity<Map<String, Object>> getCacheStatus(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        
        try {
            Map<String, Object> status = new HashMap<>();
            boolean isCached = cacheService.isCached(date);
            
            status.put("date", date.toString());
            status.put("isCached", isCached);
            
            if (isCached) {
                List<Currency> cachedCurrencies = cacheService.getFromCache(date);
                status.put("cachedCurrenciesCount", cachedCurrencies.size());
                status.put("cacheSource", "database");
            } else {
                status.put("cacheSource", "CBAR API");
            }
            
            return ResponseEntity.ok(status);
            
        } catch (Exception e) {
            log.error("Error getting cache status for date {}: {}", date, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/cache/cleanup")
    public ResponseEntity<Map<String, String>> cleanupCache(
            @RequestParam(defaultValue = "30") int daysToKeep) {
        
        try {
            log.info("Manual cache cleanup requested for data older than {} days", daysToKeep);
            cleanupService.manualCleanup(daysToKeep);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Cache cleanup completed successfully");
            response.put("daysKept", String.valueOf(daysToKeep));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error during manual cache cleanup: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
