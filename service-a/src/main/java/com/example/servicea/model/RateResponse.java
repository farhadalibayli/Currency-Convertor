package com.example.servicea.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RateResponse {
    private String currency;
    private BigDecimal rate;
    private LocalDate date;
    private String name;
}
