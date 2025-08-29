package com.example.serviceb.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
public class ConversionRequest {
    private LocalDate date;
    private String currency;
    private BigDecimal amount;
    
    // Getters
    public LocalDate getDate() {
        return date;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    // Setters
    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
