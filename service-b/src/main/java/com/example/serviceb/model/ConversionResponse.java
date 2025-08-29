package com.example.serviceb.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
public class ConversionResponse {
    private BigDecimal originalAmount;
    private String fromCurrency;
    private BigDecimal convertedAmount;
    private String toCurrency;
    private BigDecimal exchangeRate;
    private LocalDateTime conversionDate;
    private String status;
    
    // Getters
    public BigDecimal getOriginalAmount() {
        return originalAmount;
    }
    
    public String getFromCurrency() {
        return fromCurrency;
    }
    
    public BigDecimal getConvertedAmount() {
        return convertedAmount;
    }
    
    public String getToCurrency() {
        return toCurrency;
    }
    
    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }
    
    public LocalDateTime getConversionDate() {
        return conversionDate;
    }
    
    public String getStatus() {
        return status;
    }
    
    // Setters
    public void setOriginalAmount(BigDecimal originalAmount) {
        this.originalAmount = originalAmount;
    }
    
    public void setFromCurrency(String fromCurrency) {
        this.fromCurrency = fromCurrency;
    }
    
    public void setConvertedAmount(BigDecimal convertedAmount) {
        this.convertedAmount = convertedAmount;
    }
    
    public void setToCurrency(String toCurrency) {
        this.toCurrency = toCurrency;
    }
    
    public void setExchangeRate(BigDecimal exchangeRate) {
        this.exchangeRate = exchangeRate;
    }
    
    public void setConversionDate(LocalDateTime conversionDate) {
        this.conversionDate = conversionDate;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
}
