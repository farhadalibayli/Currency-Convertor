package com.example.serviceb.repository;

import com.example.serviceb.model.ConversionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ConversionHistoryRepository extends JpaRepository<ConversionHistory, Long> {
    
    List<ConversionHistory> findByDateOrderByCreatedAtDesc(LocalDate date);
    
    List<ConversionHistory> findByCurrencyOrderByCreatedAtDesc(String currency);
    
    List<ConversionHistory> findByTypeOrderByCreatedAtDesc(ConversionHistory.ConversionType type);
    
    @Query("SELECT ch FROM ConversionHistory ch WHERE ch.date BETWEEN :startDate AND :endDate ORDER BY ch.createdAt DESC")
    List<ConversionHistory> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT COUNT(ch) FROM ConversionHistory ch WHERE ch.date = :date")
    long countByDate(@Param("date") LocalDate date);
}
