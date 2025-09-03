package com.example.servicea.service;

import com.example.servicea.model.CachedCurrency;
import com.example.servicea.model.Currency;
import com.example.servicea.repository.CachedCurrencyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CurrencyCacheService {
    
    private static final Logger log = LoggerFactory.getLogger(CurrencyCacheService.class);
    
    private final CachedCurrencyRepository cachedCurrencyRepository;
    
    public CurrencyCacheService(CachedCurrencyRepository cachedCurrencyRepository) {
        this.cachedCurrencyRepository = cachedCurrencyRepository;
    }
    
    /**
     * Check if currencies are cached for a specific date
     */
    public boolean isCached(LocalDate date) {
        boolean exists = cachedCurrencyRepository.existsByCurrencyDate(date);
        log.debug("Cache check for date {}: {}", date, exists);
        return exists;
    }
    
    /**
     * Get currencies from cache for a specific date
     */
    public List<Currency> getFromCache(LocalDate date) {
        log.debug("Retrieving currencies from cache for date: {}", date);
        List<CachedCurrency> cachedCurrencies = cachedCurrencyRepository.findByCurrencyDate(date);
        
        List<Currency> currencies = cachedCurrencies.stream()
                .map(this::convertToCurrency)
                .collect(Collectors.toList());
        
        log.info("Retrieved {} currencies from cache for date: {}", currencies.size(), date);
        return currencies;
    }
    
    /**
     * Get a specific currency from cache
     */
    public Optional<Currency> getFromCache(LocalDate date, String currencyCode) {
        log.debug("Retrieving currency {} from cache for date: {}", currencyCode, date);
        Optional<CachedCurrency> cached = cachedCurrencyRepository.findByCurrencyDateAndCurrencyCode(date, currencyCode);
        
        if (cached.isPresent()) {
            log.info("Currency {} found in cache for date: {}", currencyCode, date);
            return Optional.of(convertToCurrency(cached.get()));
        } else {
            log.debug("Currency {} not found in cache for date: {}", currencyCode, date);
            return Optional.empty();
        }
    }
    
    /**
     * Save currencies to cache
     */
    @Transactional
    public void saveToCache(LocalDate date, List<Currency> currencies) {
        log.info("Saving {} currencies to cache for date: {}", currencies.size(), date);
        
        List<CachedCurrency> cachedCurrencies = currencies.stream()
                .map(currency -> new CachedCurrency(date, currency.getCode(), currency.getName(), currency.getRate()))
                .collect(Collectors.toList());
        
        cachedCurrencyRepository.saveAll(cachedCurrencies);
        log.info("Successfully saved {} currencies to cache for date: {}", currencies.size(), date);
    }
    
    /**
     * Convert CachedCurrency to Currency
     */
    private Currency convertToCurrency(CachedCurrency cached) {
        return new Currency(cached.getCurrencyCode(), cached.getCurrencyName(), cached.getExchangeRate());
    }
    
    /**
     * Clean up old cached data (older than specified days)
     */
    @Transactional
    public void cleanupOldCache(int daysToKeep) {
        LocalDate cutoffDate = LocalDate.now().minusDays(daysToKeep);
        log.info("Cleaning up cache data older than {} days (cutoff date: {})", daysToKeep, cutoffDate);
        
        try {
            cachedCurrencyRepository.deleteByCurrencyDateBefore(cutoffDate);
            log.info("Successfully cleaned up old cache data");
        } catch (Exception e) {
            log.error("Error cleaning up old cache data: {}", e.getMessage(), e);
        }
    }
}
