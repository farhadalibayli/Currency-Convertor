package com.example.serviceb.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Entity
@Table(name = "conversion_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversionHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private ConversionType type;
    
    @Column(name = "date", nullable = false)
    private LocalDate date;
    
    @Column(name = "currency", nullable = false, length = 10)
    private String currency;
    
    @Column(name = "amount", nullable = false, precision = 15, scale = 4)
    private BigDecimal amount;
    
    @Column(name = "rate", nullable = false, precision = 15, scale = 6)
    private BigDecimal rate;
    
    @Column(name = "result", nullable = false, precision = 15, scale = 4)
    private BigDecimal result;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Getters
    public Long getId() { return id; }
    public ConversionType getType() { return type; }
    public LocalDate getDate() { return date; }
    public String getCurrency() { return currency; }
    public BigDecimal getAmount() { return amount; }
    public BigDecimal getRate() { return rate; }
    public BigDecimal getResult() { return result; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    
    // Setters
    public void setId(Long id) { this.id = id; }
    public void setType(ConversionType type) { this.type = type; }
    public void setDate(LocalDate date) { this.date = date; }
    public void setCurrency(String currency) { this.currency = currency; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public void setRate(BigDecimal rate) { this.rate = rate; }
    public void setResult(BigDecimal result) { this.result = result; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public enum ConversionType {
        toManat, fromManat
    }
}
