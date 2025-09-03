package com.example.servicea.service;

import com.example.servicea.model.CachedCurrency;
import com.example.servicea.model.Currency;
import com.example.servicea.repository.CachedCurrencyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CurrencyCacheServiceTest {

    @Mock
    private CachedCurrencyRepository cachedCurrencyRepository;

    @InjectMocks
    private CurrencyCacheService currencyCacheService;

    private LocalDate testDate;
    private List<Currency> testCurrencies;

    @BeforeEach
    void setUp() {
        testDate = LocalDate.of(2024, 1, 15);
        testCurrencies = Arrays.asList(
            new Currency("USD", "US Dollar", new BigDecimal("1.7000")),
            new Currency("EUR", "Euro", new BigDecimal("1.8500")),
            new Currency("GBP", "British Pound", new BigDecimal("2.1500"))
        );
    }

    @Test
    void testIsCached_WhenDataExists_ReturnsTrue() {
        // Given
        when(cachedCurrencyRepository.existsByCurrencyDate(testDate)).thenReturn(true);

        // When
        boolean result = currencyCacheService.isCached(testDate);

        // Then
        assertTrue(result);
        verify(cachedCurrencyRepository).existsByCurrencyDate(testDate);
    }

    @Test
    void testIsCached_WhenDataDoesNotExist_ReturnsFalse() {
        // Given
        when(cachedCurrencyRepository.existsByCurrencyDate(testDate)).thenReturn(false);

        // When
        boolean result = currencyCacheService.isCached(testDate);

        // Then
        assertFalse(result);
        verify(cachedCurrencyRepository).existsByCurrencyDate(testDate);
    }

    @Test
    void testGetFromCache_WhenDataExists_ReturnsCurrencies() {
        // Given
        List<CachedCurrency> cachedCurrencies = Arrays.asList(
            new CachedCurrency(testDate, "USD", "US Dollar", new BigDecimal("1.7000")),
            new CachedCurrency(testDate, "EUR", "Euro", new BigDecimal("1.8500"))
        );
        when(cachedCurrencyRepository.findByCurrencyDate(testDate)).thenReturn(cachedCurrencies);

        // When
        List<Currency> result = currencyCacheService.getFromCache(testDate);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("USD", result.get(0).getCode());
        assertEquals("EUR", result.get(1).getCode());
        verify(cachedCurrencyRepository).findByCurrencyDate(testDate);
    }

    @Test
    void testGetFromCache_WhenSpecificCurrencyExists_ReturnsCurrency() {
        // Given
        CachedCurrency cachedCurrency = new CachedCurrency(testDate, "USD", "US Dollar", new BigDecimal("1.7000"));
        when(cachedCurrencyRepository.findByCurrencyDateAndCurrencyCode(testDate, "USD"))
            .thenReturn(Optional.of(cachedCurrency));

        // When
        Optional<Currency> result = currencyCacheService.getFromCache(testDate, "USD");

        // Then
        assertTrue(result.isPresent());
        assertEquals("USD", result.get().getCode());
        assertEquals("US Dollar", result.get().getName());
        assertEquals(new BigDecimal("1.7000"), result.get().getRate());
        verify(cachedCurrencyRepository).findByCurrencyDateAndCurrencyCode(testDate, "USD");
    }

    @Test
    void testGetFromCache_WhenSpecificCurrencyDoesNotExist_ReturnsEmpty() {
        // Given
        when(cachedCurrencyRepository.findByCurrencyDateAndCurrencyCode(testDate, "INVALID"))
            .thenReturn(Optional.empty());

        // When
        Optional<Currency> result = currencyCacheService.getFromCache(testDate, "INVALID");

        // Then
        assertFalse(result.isPresent());
        verify(cachedCurrencyRepository).findByCurrencyDateAndCurrencyCode(testDate, "INVALID");
    }

    @Test
    void testSaveToCache_SavesAllCurrencies() {
        // Given
        when(cachedCurrencyRepository.saveAll(any())).thenReturn(Arrays.asList());

        // When
        currencyCacheService.saveToCache(testDate, testCurrencies);

        // Then
        verify(cachedCurrencyRepository).saveAll(any());
    }
}
