package com.example.kapt.scheduler;

import com.example.kapt.service.CryptocurrencyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CryptocurrencyUpdateScheduler {

    private static final Logger logger = LoggerFactory.getLogger(CryptocurrencyUpdateScheduler.class);

    private final CryptocurrencyService cryptocurrencyService;

    @Value("${app.scheduler.enabled:true}")
    private boolean schedulerEnabled;

    public CryptocurrencyUpdateScheduler(CryptocurrencyService cryptocurrencyService) {
        this.cryptocurrencyService = cryptocurrencyService;
    }

    @Scheduled(fixedRate = 900000)
    public void updateCryptocurrencyData() {
        if (!schedulerEnabled) {
            logger.debug("Cryptocurrency update scheduler is disabled");
            return;
        }

        logger.info("Starting scheduled cryptocurrency data update");

        try {
            long startTime = System.currentTimeMillis();

            cryptocurrencyService.fetchAndSaveCryptocurrencies();

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            logger.info("Completed scheduled cryptocurrency data update in {} ms", duration);

        } catch (Exception e) {
            logger.error("Error during scheduled cryptocurrency data update", e);
        }
    }

    @Scheduled(initialDelay = 30000, fixedRate = Long.MAX_VALUE)
    public void initialDataLoad() {
        if (!schedulerEnabled) {
            return;
        }

        logger.info("Performing initial cryptocurrency data load");

        try {
            long count = cryptocurrencyService.getCryptocurrencyCount();

            if (count == 0) {
                logger.info("No cryptocurrency data found, performing initial load");
                cryptocurrencyService.fetchAndSaveCryptocurrencies();
            } else {
                logger.info("Found {} existing cryptocurrencies, skipping initial load", count);
            }

        } catch (Exception e) {
            logger.error("Error during initial cryptocurrency data load", e);
        }
    }
}
