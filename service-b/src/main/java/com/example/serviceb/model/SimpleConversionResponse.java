package com.example.serviceb.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
public class SimpleConversionResponse {
    private BigDecimal result;
    private String message;
    private LocalDateTime timestamp;
    private String status;
    
    // Constructor for error responses with null result
    public SimpleConversionResponse(BigDecimal result, String message, LocalDateTime timestamp, String status) {
        this.result = result;
        this.message = message;
        this.timestamp = timestamp;
        this.status = status;
    }
    
    // Getters
    public BigDecimal getResult() {
        return result;
    }
    
    public String getMessage() {
        return message;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public String getStatus() {
        return status;
    }
    
    // Setters
    public void setResult(BigDecimal result) {
        this.result = result;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
}
