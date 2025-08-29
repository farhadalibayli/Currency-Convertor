package com.example.servicea.model;

import java.math.BigDecimal;

public class Currency {
    private String code;
    private String name;
    private BigDecimal rate;
    
    // Constructors
    public Currency() {}
    
    public Currency(String code, String name) {
        this.code = code;
        this.name = name;
    }
    
    public Currency(String code, String name, BigDecimal rate) {
        this.code = code;
        this.name = name;
        this.rate = rate;
    }
    
    public Currency(String code, String name, double rate) {
        this.code = code;
        this.name = name;
        this.rate = BigDecimal.valueOf(rate);
    }
    
    // Getters and Setters
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public BigDecimal getRate() { return rate; }
    public void setRate(BigDecimal rate) { this.rate = rate; }
    
    public void setRate(double rate) { this.rate = BigDecimal.valueOf(rate); }
}
