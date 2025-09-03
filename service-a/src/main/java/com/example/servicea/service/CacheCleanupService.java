package com.example.servicea.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class CacheCleanupService {
    
    private static final Logger log = LoggerFactory.getLogger(CacheCleanupService.class);
    
    private final CurrencyCacheService currencyCacheService;
    
    // Keep cache data for 30 days
    private static final int DAYS_TO_KEEP = 30;
    
    public CacheCleanupService(CurrencyCacheService currencyCacheService) {
        this.currencyCacheService = currencyCacheService;
    }
    
    /**
     * Clean up old cache data every day at 2:00 AM
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupOldCache() {
        log.info("Starting scheduled cache cleanup task");
        try {
            currencyCacheService.cleanupOldCache(DAYS_TO_KEEP);
            log.info("Scheduled cache cleanup completed successfully");
        } catch (Exception e) {
            log.error("Error during scheduled cache cleanup: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Manual cleanup method that can be called via API if needed
     */
    public void manualCleanup(int daysToKeep) {
        log.info("Starting manual cache cleanup for data older than {} days", daysToKeep);
        try {
            currencyCacheService.cleanupOldCache(daysToKeep);
            log.info("Manual cache cleanup completed successfully");
        } catch (Exception e) {
            log.error("Error during manual cache cleanup: {}", e.getMessage(), e);
        }
    }
}
