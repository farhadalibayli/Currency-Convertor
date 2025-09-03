package com.example.servicea.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "cached_currencies", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"currency_date", "currency_code"}))
public class CachedCurrency {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "currency_date", nullable = false)
    private LocalDate currencyDate;
    
    @Column(name = "currency_code", nullable = false, length = 10)
    private String currencyCode;
    
    @Column(name = "currency_name", nullable = false, length = 255)
    private String currencyName;
    
    @Column(name = "exchange_rate", nullable = false, precision = 19, scale = 6)
    private BigDecimal exchangeRate;
    
    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt;
    
    // Constructors
    public CachedCurrency() {}
    
    public CachedCurrency(LocalDate currencyDate, String currencyCode, String currencyName, BigDecimal exchangeRate) {
        this.currencyDate = currencyDate;
        this.currencyCode = currencyCode;
        this.currencyName = currencyName;
        this.exchangeRate = exchangeRate;
        this.createdAt = LocalDate.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public LocalDate getCurrencyDate() {
        return currencyDate;
    }
    
    public void setCurrencyDate(LocalDate currencyDate) {
        this.currencyDate = currencyDate;
    }
    
    public String getCurrencyCode() {
        return currencyCode;
    }
    
    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }
    
    public String getCurrencyName() {
        return currencyName;
    }
    
    public void setCurrencyName(String currencyName) {
        this.currencyName = currencyName;
    }
    
    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }
    
    public void setExchangeRate(BigDecimal exchangeRate) {
        this.exchangeRate = exchangeRate;
    }
    
    public LocalDate getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }
}
