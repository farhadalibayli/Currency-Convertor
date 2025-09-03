package com.example.servicea.repository;

import com.example.servicea.model.CachedCurrency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CachedCurrencyRepository extends JpaRepository<CachedCurrency, Long> {
    
    /**
     * Find a specific currency by date and currency code
     */
    Optional<CachedCurrency> findByCurrencyDateAndCurrencyCode(LocalDate currencyDate, String currencyCode);
    
    /**
     * Find all currencies for a specific date
     */
    List<CachedCurrency> findByCurrencyDate(LocalDate currencyDate);
    
    /**
     * Check if currencies exist for a specific date
     */
    boolean existsByCurrencyDate(LocalDate currencyDate);
    
    /**
     * Find currencies by date and currency codes
     */
    @Query("SELECT c FROM CachedCurrency c WHERE c.currencyDate = :date AND c.currencyCode IN :codes")
    List<CachedCurrency> findByCurrencyDateAndCurrencyCodeIn(@Param("date") LocalDate date, @Param("codes") List<String> codes);
    
    /**
     * Delete old cached data (older than specified days)
     */
    @Query("DELETE FROM CachedCurrency c WHERE c.currencyDate < :cutoffDate")
    void deleteByCurrencyDateBefore(@Param("cutoffDate") LocalDate cutoffDate);
}
