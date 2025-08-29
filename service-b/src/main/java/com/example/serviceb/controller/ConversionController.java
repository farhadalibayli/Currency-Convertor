package com.example.serviceb.controller;

import com.example.serviceb.model.ConversionHistory;
import com.example.serviceb.model.ConversionRequest;
import com.example.serviceb.model.ConversionResponse;
import com.example.serviceb.model.SimpleConversionResponse;
import com.example.serviceb.service.ConversionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/conversions")
public class ConversionController {
    
    private static final Logger log = LoggerFactory.getLogger(ConversionController.class);
    
    private final ConversionService conversionService;
    
    @Autowired
    public ConversionController(ConversionService conversionService) {
        this.conversionService = conversionService;
    }
    
    /**
     * Convert foreign currency to Manat (AZN)
     * Receives JSON with date, currency, and amount
     */
    @PostMapping("/toManat")
    public ResponseEntity<SimpleConversionResponse> convertToManat(@RequestBody ConversionRequest request) {
        try {
            log.info("Converting {} {} to Manat for date: {}", request.getAmount(), request.getCurrency(), request.getDate());
            
            // Validate request
            if (request.getAmount() == null || request.getCurrency() == null || request.getDate() == null) {
                log.error("Invalid request parameters: amount={}, currency={}, date={}", 
                         request.getAmount(), request.getCurrency(), request.getDate());
                return ResponseEntity.badRequest()
                    .body(new SimpleConversionResponse(null, "Missing required parameters", 
                           java.time.LocalDateTime.now(), "ERROR"));
            }
            
            if (request.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                log.error("Invalid amount: {}", request.getAmount());
                return ResponseEntity.badRequest()
                    .body(new SimpleConversionResponse(null, "Amount must be greater than zero", 
                           java.time.LocalDateTime.now(), "ERROR"));
            }
            
            // Get rate from Service A
            java.math.BigDecimal rate = conversionService.getRateFromServiceA(request.getDate(), request.getCurrency());
            
            // Perform conversion
            java.math.BigDecimal result = conversionService.toManat(request.getAmount(), rate);
            
            SimpleConversionResponse response = new SimpleConversionResponse(
                result,
                String.format("Successfully converted %s %s to %s AZN", 
                             request.getAmount(), request.getCurrency(), result),
                java.time.LocalDateTime.now(),
                "SUCCESS"
            );
            
            log.info("Conversion to Manat completed: {} {} = {} AZN", 
                    request.getAmount(), request.getCurrency(), result);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error converting to Manat: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(new SimpleConversionResponse(null, "Internal server error: " + e.getMessage(), 
                       java.time.LocalDateTime.now(), "ERROR"));
        }
    }
    
    /**
     * Convert Manat (AZN) to foreign currency
     * Receives JSON with date, currency, and amount
     */
    @PostMapping("/fromManat")
    public ResponseEntity<SimpleConversionResponse> convertFromManat(@RequestBody ConversionRequest request) {
        try {
            log.info("Converting {} Manat to {} for date: {}", request.getAmount(), request.getCurrency(), request.getDate());
            
            // Validate request
            if (request.getAmount() == null || request.getCurrency() == null || request.getDate() == null) {
                log.error("Invalid request parameters: amount={}, currency={}, date={}", 
                         request.getAmount(), request.getCurrency(), request.getDate());
                return ResponseEntity.badRequest()
                    .body(new SimpleConversionResponse(null, "Missing required parameters", 
                           java.time.LocalDateTime.now(), "ERROR"));
            }
            
            if (request.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                log.error("Invalid amount: {}", request.getAmount());
                return ResponseEntity.badRequest()
                    .body(new SimpleConversionResponse(null, "Amount must be greater than zero", 
                           java.time.LocalDateTime.now(), "ERROR"));
            }
            
            // Get rate from Service A
            java.math.BigDecimal rate = conversionService.getRateFromServiceA(request.getDate(), request.getCurrency());
            
            // Perform conversion
            java.math.BigDecimal result = conversionService.fromManat(request.getAmount(), rate);
            
            SimpleConversionResponse response = new SimpleConversionResponse(
                result,
                String.format("Successfully converted %s AZN to %s %s", 
                             request.getAmount(), result, request.getCurrency()),
                java.time.LocalDateTime.now(),
                "SUCCESS"
            );
            
            log.info("Conversion from Manat completed: {} AZN = {} {}", 
                    request.getAmount(), result, request.getCurrency());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error converting from Manat: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(new SimpleConversionResponse(null, "Internal server error: " + e.getMessage(), 
                       java.time.LocalDateTime.now(), "ERROR"));
        }
    }
    

    
    @GetMapping("/history")
    public ResponseEntity<List<ConversionHistory>> getConversionHistory(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        try {
            log.info("Fetching conversion history for date: {}", date);
            
            List<ConversionHistory> history = conversionService.getConversionHistory(date);
            
            log.info("Found {} conversion records for date: {}", history.size(), date);
            return ResponseEntity.ok(history);
            
        } catch (Exception e) {
            log.error("Error fetching conversion history: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/history/currency")
    public ResponseEntity<List<ConversionHistory>> getConversionHistoryByCurrency(
            @RequestParam String currency) {
        try {
            log.info("Fetching conversion history for currency: {}", currency);
            
            List<ConversionHistory> history = conversionService.getConversionHistoryByCurrency(currency);
            
            log.info("Found {} conversion records for currency: {}", history.size(), currency);
            return ResponseEntity.ok(history);
            
        } catch (Exception e) {
            log.error("Error fetching conversion history by currency: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/count")
    public ResponseEntity<Long> getConversionCount(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        try {
            log.info("Fetching conversion count for date: {}", date);
            
            long count = conversionService.getConversionCount(date);
            
            log.info("Found {} conversions for date: {}", count, date);
            return ResponseEntity.ok(count);
            
        } catch (Exception e) {
            log.error("Error fetching conversion count: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/to-manat")
    public ResponseEntity<ConversionResponse> convertToManat(
            @RequestParam java.math.BigDecimal amount,
            @RequestParam java.math.BigDecimal rate) {
        try {
            log.info("Converting {} to Manat with rate: {}", amount, rate);
            
            java.math.BigDecimal result = conversionService.toManat(amount, rate);
            
            ConversionResponse response = new ConversionResponse();
            response.setOriginalAmount(amount);
            response.setFromCurrency("FOREIGN");
            response.setConvertedAmount(result);
            response.setToCurrency("AZN");
            response.setExchangeRate(rate);
            response.setConversionDate(java.time.LocalDateTime.now());
            response.setStatus("SUCCESS");
            
            log.info("Conversion to Manat completed: {} = {}", amount, result);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error converting to Manat: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/from-manat")
    public ResponseEntity<ConversionResponse> convertFromManat(
            @RequestParam java.math.BigDecimal amount,
            @RequestParam java.math.BigDecimal rate) {
        try {
            log.info("Converting {} from Manat with rate: {}", amount, rate);
            
            java.math.BigDecimal result = conversionService.fromManat(amount, rate);
            
            ConversionResponse response = new ConversionResponse();
            response.setOriginalAmount(amount);
            response.setFromCurrency("AZN");
            response.setConvertedAmount(result);
            response.setToCurrency("FOREIGN");
            response.setExchangeRate(rate);
            response.setConversionDate(java.time.LocalDateTime.now());
            response.setStatus("SUCCESS");
            
            log.info("Conversion from Manat completed: {} = {}", amount, result);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error converting from Manat: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/to-manat-with-service-a")
    public ResponseEntity<ConversionResponse> convertToManatWithServiceA(
            @RequestParam java.math.BigDecimal amount,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam String currency) {
        try {
            log.info("Converting {} {} to Manat using Service A for date: {}", amount, currency, date);
            
            java.math.BigDecimal result = conversionService.toManatWithServiceA(amount, date, currency);
            
            ConversionResponse response = new ConversionResponse();
            response.setOriginalAmount(amount);
            response.setFromCurrency(currency);
            response.setConvertedAmount(result);
            response.setToCurrency("AZN");
            response.setExchangeRate(java.math.BigDecimal.ZERO); // Will be set from Service A
            response.setConversionDate(java.time.LocalDateTime.now());
            response.setStatus("SUCCESS");
            
            log.info("Conversion to Manat with Service A completed: {} {} = {} AZN", amount, currency, result);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error converting to Manat with Service A: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/from-manat-with-service-a")
    public ResponseEntity<ConversionResponse> convertFromManatWithServiceA(
            @RequestParam java.math.BigDecimal amount,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam String currency) {
        try {
            log.info("Converting {} Manat to {} using Service A for date: {}", amount, currency, date);
            
            java.math.BigDecimal result = conversionService.fromManatWithServiceA(amount, date, currency);
            
            ConversionResponse response = new ConversionResponse();
            response.setOriginalAmount(amount);
            response.setFromCurrency("AZN");
            response.setConvertedAmount(result);
            response.setToCurrency(currency);
            response.setExchangeRate(java.math.BigDecimal.ZERO); // Will be set from Service A
            response.setConversionDate(java.time.LocalDateTime.now());
            response.setStatus("SUCCESS");
            
            log.info("Conversion from Manat with Service A completed: {} AZN = {} {}", amount, result, currency);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error converting from Manat with Service A: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/history/type")
    public ResponseEntity<List<ConversionHistory>> getConversionHistoryByType(
            @RequestParam String type) {
        try {
            log.info("Fetching conversion history for type: {}", type);
            
            ConversionHistory.ConversionType conversionType = ConversionHistory.ConversionType.valueOf(type);
            List<ConversionHistory> history = conversionService.getConversionHistoryByType(conversionType);
            
            log.info("Found {} conversion records for type: {}", history.size(), type);
            return ResponseEntity.ok(history);
            
        } catch (Exception e) {
            log.error("Error fetching conversion history by type: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
