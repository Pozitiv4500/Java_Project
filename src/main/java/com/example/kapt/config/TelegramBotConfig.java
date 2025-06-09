package com.example.kapt.config;

import com.example.kapt.telegram.CryptocurrencyTelegramBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class TelegramBotConfig {

    private static final Logger logger = LoggerFactory.getLogger(TelegramBotConfig.class);

    @Value("${app.telegram.bot.enabled:false}")
    private boolean telegramBotEnabled;

    @Bean
    public TelegramBotsApi telegramBotsApi(CryptocurrencyTelegramBot cryptocurrencyTelegramBot) throws TelegramApiException {
        if (!telegramBotEnabled) {
            logger.info("Telegram bot is disabled");
            return null;
        }

        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);

        try {
            botsApi.registerBot(cryptocurrencyTelegramBot);
            logger.info("Telegram bot registered successfully");
        } catch (TelegramApiException e) {
            logger.error("Failed to register Telegram bot", e);
            throw e;
        }

        return botsApi;
    }
}
