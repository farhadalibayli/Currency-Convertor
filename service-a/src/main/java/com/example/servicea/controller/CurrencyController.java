package com.example.servicea.controller;

import com.example.servicea.model.Currency;
import com.example.servicea.service.CbarService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/currencies")
public class CurrencyController {
    
    private static final Logger log = LoggerFactory.getLogger(CurrencyController.class);
    
    private final CbarService cbarService;
    
    public CurrencyController(CbarService cbarService) {
        this.cbarService = cbarService;
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
            
            List<Currency> currencies = cbarService.getCurrencies(date.toString());
            
            // Find the specific currency
            Currency targetCurrency = currencies.stream()
                    .filter(c -> c.getCode().equalsIgnoreCase(currency.trim()))
                    .findFirst()
                    .orElse(null);
            
            if (targetCurrency == null) {
                log.warn("Currency not found: {} for date: {}", currency, date);
                return ResponseEntity.notFound().build();
            }
            
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
}
