package com.example.kapt.telegram;

import com.example.kapt.model.Cryptocurrency;
import com.example.kapt.model.News;
import com.example.kapt.service.CryptocurrencyService;
import com.example.kapt.service.NewsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.*;

@Component
public class CryptocurrencyTelegramBot extends TelegramLongPollingBot {

    private static final Logger logger = LoggerFactory.getLogger(CryptocurrencyTelegramBot.class);

    private final CryptocurrencyService cryptocurrencyService;
    private final NewsService newsService;
    private final NumberFormat currencyFormatter;

    @Value("${app.telegram.bot.username}")
    private String botUsername;

    @Value("${app.telegram.bot.token}")
    private String botToken;

    public CryptocurrencyTelegramBot(CryptocurrencyService cryptocurrencyService, NewsService newsService) {
        this.cryptocurrencyService = cryptocurrencyService;
        this.newsService = newsService;
        this.currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            String firstName = update.getMessage().getFrom().getFirstName();

            logger.info("Received message from {}: {}", firstName, messageText);

            try {
                handleMessage(chatId, messageText, firstName);
            } catch (Exception e) {
                logger.error("Error handling message from {}: {}", firstName, messageText, e);
                sendMessage(chatId, "Произошла ошибка при обработке вашего запроса. Попробуйте позже.");
            }
        } else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            int messageId = update.getCallbackQuery().getMessage().getMessageId();

            logger.info("Received callback query: {}", callbackData);

            try {
                handleCallbackQuery(chatId, messageId, callbackData);
            } catch (Exception e) {
                logger.error("Error handling callback query: {}", callbackData, e);
            }
        }
    }

    private void handleMessage(long chatId, String messageText, String firstName) {
        String command = messageText.toLowerCase().trim();

        switch (command) {
            case "/start":
                handleStartCommand(chatId, firstName);
                break;
            case "/help":
            case "📖 помощь":
                handleHelpCommand(chatId);
                break;
            case "/top":
            case "🔥 топ-10":
                handleTopCommand(chatId);
                break;
            case "/gainers":
            case "📈 лидеры роста":
                handleGainersCommand(chatId);
                break;
            case "/losers":
            case "📉 лидеры падения":
                handleLosersCommand(chatId);
                break;
            case "/stats":
            case "📊 статистика":
                handleStatsCommand(chatId);
                break;
            case "/news":
            case "📰 новости":
                handleNewsCommand(chatId);
                break;
            default:
                if (command.startsWith("/search ") || command.startsWith("🔍 поиск:")) {
                    handleSearchCommand(chatId, messageText);
                } else if (command.startsWith("/price ")) {
                    handlePriceCommand(chatId, messageText);
                } else if (isSymbolQuery(messageText)) {
                    handleSymbolQuery(chatId, messageText);
                } else {
                    handleUnknownCommand(chatId);
                }
                break;
        }
    }

    private void handleStartCommand(long chatId, String firstName) {
        String welcomeMessage = String.format(
                "👋 Добро пожаловать, %s!\n\n" +
                        "Я бот для получения информации о криптовалютах. " +
                        "Я помогу вам отслеживать цены, рыночную капитализацию и другие показатели.\n\n" +
                        "Используйте кнопки меню или команды для навигации.",
                firstName
        );

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(welcomeMessage);
        message.setReplyMarkup(createMainKeyboard());

        executeMessage(message);
    }

    private void handleHelpCommand(long chatId) {
        String helpMessage =
                "📖 *Доступные команды:*\n\n" +
                        "🔥 *Топ-10* - топ криптовалют по капитализации\n" +
                        "📈 *Лидеры роста* - криптовалюты с наибольшим ростом\n" +
                        "📉 *Лидеры падения* - криптовалюты с наибольшим падением\n" +
                        "📊 *Статистика* - общая статистика рынка\n" +
                        "📰 *Новости* - последние новости о криптовалютах\n\n" +
                        "*Поиск:*\n" +
                        "• Введите символ криптовалюты (например: BTC, ETH)\n" +
                        "• /search <название> - поиск по названию\n" +
                        "• /price <символ> - цена конкретной криптовалюты\n\n" +
                        "Все данные обновляются каждые 15 минут.";

        sendMarkdownMessage(chatId, helpMessage);
    }

    private void handleTopCommand(long chatId) {
        List<Cryptocurrency> topCryptocurrencies = cryptocurrencyService.getTopByMarketCap(10);

        if (topCryptocurrencies.isEmpty()) {
            sendMessage(chatId, "Данные пока не загружены. Попробуйте позже.");
            return;
        }

        StringBuilder message = new StringBuilder("🔥 *Топ-10 криптовалют по рыночной капитализации:*\n\n");

        for (int i = 0; i < topCryptocurrencies.size(); i++) {
            Cryptocurrency crypto = topCryptocurrencies.get(i);
            message.append(formatCryptocurrencyInfo(i + 1, crypto));
        }

        sendMarkdownMessage(chatId, message.toString());
    }

    private void handleGainersCommand(long chatId) {
        List<Cryptocurrency> gainers = cryptocurrencyService.getTopGainers(10);

        if (gainers.isEmpty()) {
            sendMessage(chatId, "Данные о лидерах роста пока недоступны.");
            return;
        }

        StringBuilder message = new StringBuilder("📈 *Топ-10 лидеров роста (24ч):*\n\n");

        for (int i = 0; i < gainers.size(); i++) {
            Cryptocurrency crypto = gainers.get(i);
            message.append(formatCryptocurrencyInfo(i + 1, crypto));
        }

        sendMarkdownMessage(chatId, message.toString());
    }

    private void handleLosersCommand(long chatId) {
        List<Cryptocurrency> losers = cryptocurrencyService.getTopLosers(10);

        if (losers.isEmpty()) {
            sendMessage(chatId, "Данные о лидерах падения пока недоступны.");
            return;
        }

        StringBuilder message = new StringBuilder("📉 *Топ-10 лидеров падения (24ч):*\n\n");

        for (int i = 0; i < losers.size(); i++) {
            Cryptocurrency crypto = losers.get(i);
            message.append(formatCryptocurrencyInfo(i + 1, crypto));
        }

        sendMarkdownMessage(chatId, message.toString());
    }

    private void handleStatsCommand(long chatId) {
        CryptocurrencyService.MarketStatistics stats = cryptocurrencyService.getMarketStatistics();

        String statsMessage = String.format(
                "📊 *Статистика рынка криптовалют:*\n\n" +
                        "💰 Общая капитализация: $%s\n" +
                        "📈 Количество криптовалют: %d\n" +
                        "📊 Средн. изменение (24ч): %.2f%%\n" +
                        "🚀 Макс. рост (24ч): %.2f%%\n" +
                        "📉 Макс. падение (24ч): %.2f%%",
                formatLargeNumber(stats.getTotalMarketCap()),
                stats.getTotalCount(),
                stats.getAvgPriceChange(),
                stats.getMaxPriceChange(),
                stats.getMinPriceChange()
        );

        sendMarkdownMessage(chatId, statsMessage);
    }

    private void handleNewsCommand(long chatId) {
        try {
            List<News> newsList = newsService.getRecentNews(24);

            if (newsList.isEmpty()) {
                sendMessage(chatId, "📰 Нет новых статей за последние 24 часа.\n\n" +
                        "Попробуйте позже или используйте другие команды для получения информации о криптовалютах.");
                return;
            }

            StringBuilder message = new StringBuilder("📰 *Последние новости о криптовалютах (24ч):*\n\n");


            int limit = Math.min(newsList.size(), 5);
            for (int i = 0; i < limit; i++) {
                News news = newsList.get(i);


                String formattedDate = "Неизвестно";
                if (news.getPubDate() != null) {
                    formattedDate = news.getPubDate().toString().substring(0, 16).replace("T", " ");
                }


                String title = news.getTitle().replace("*", "\\*").replace("_", "\\_");

                message.append(String.format(
                        "• *%s*\n  📅 %s\n  🔗 [Читать полностью](%s)\n\n",
                        title,
                        formattedDate,
                        news.getLink()
                ));
            }
            if (newsList.size() > 5) {
                message.append(String.format("_Показано новостей 1-5 из %d_\n\n", newsList.size()));
            }

            message.append("💡 _Данные обновляются автоматически каждый час_");


            if (newsList.size() > 5) {
                InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rows = new ArrayList<>();
                List<InlineKeyboardButton> row = new ArrayList<>();

                InlineKeyboardButton moreButton = new InlineKeyboardButton();
                moreButton.setText("📰 Показать еще");
                moreButton.setCallbackData("news_more_1");
                row.add(moreButton);

                rows.add(row);
                keyboard.setKeyboard(rows);

                sendMessageWithKeyboard(chatId, message.toString(), keyboard);
            } else {
                sendMarkdownMessage(chatId, message.toString());
            }

        } catch (Exception e) {
            logger.error("Error handling news command for chat {}: {}", chatId, e.getMessage(), e);
            sendMessage(chatId, "❌ Произошла ошибка при загрузке новостей. Попробуйте позже.");
        }
    }

    private void handleSearchCommand(long chatId, String messageText) {
        String searchTerm = extractSearchTerm(messageText, "/search ", "🔍 поиск:");
        if (searchTerm.isEmpty()) {
            sendMessage(chatId, "Укажите название или символ для поиска. Например: /search bitcoin");
            return;
        }

        List<Cryptocurrency> results = cryptocurrencyService.searchCryptocurrencies(searchTerm);

        if (results.isEmpty()) {
            sendMessage(chatId, "По запросу \"" + searchTerm + "\" ничего не найдено.");
            return;
        }

        StringBuilder message = new StringBuilder("🔍 *Результаты поиска:*\n\n");

        int limit = Math.min(results.size(), 10);
        for (int i = 0; i < limit; i++) {
            Cryptocurrency crypto = results.get(i);
            message.append(formatCryptocurrencyInfo(i + 1, crypto));
        }

        if (results.size() > 10) {
            message.append("\n_Показаны первые 10 результатов_");
        }

        sendMarkdownMessage(chatId, message.toString());
    }

    private void handlePriceCommand(long chatId, String messageText) {
        String symbol = extractSearchTerm(messageText, "/price ");
        if (symbol.isEmpty()) {
            sendMessage(chatId, "Укажите символ криптовалюты. Например: /price BTC");
            return;
        }
        handleSymbolQuery(chatId, symbol);
    }

    private void handleSymbolQuery(long chatId, String symbol) {
        Optional<Cryptocurrency> cryptoOpt = cryptocurrencyService.findBySymbol(symbol.toUpperCase());

        if (cryptoOpt.isEmpty()) {
            sendMessage(chatId, "Криптовалюта с символом \"" + symbol.toUpperCase() + "\" не найдена.");
            return;
        }

        Cryptocurrency c = cryptoOpt.get();
        String message = String.format(
                "*%s (%s)*\n\n" +
                        "💰 Цена: %s\n" +
                        "📈 Изменение (24ч): %.2f%%\n" +
                        "💎 Рыночная капитализация: %s\n" +
                        "📊 Объем торгов (24ч): %s\n" +
                        "🏆 Ранг: #%d\n" +
                        "🔄 Обновлено: %s",
                c.getName(),
                c.getSymbol(),
                formatPrice(c.getCurrentPrice()),
                c.getPriceChangePercentage24h(),
                formatLargeNumber(c.getMarketCap()),
                formatLargeNumber(c.getTotalVolume()),
                c.getMarketCapRank(),
                c.getLastUpdated() != null ? c.getLastUpdated().toString().substring(0, 16).replace("T", " ") : "N/A"
        );

        sendMarkdownMessage(chatId, message);
    }

    private void handleUnknownCommand(long chatId) {
        sendMessage(chatId, "Команда не распознана. Используйте /help для получения списка доступных команд.");
    }

    private boolean isSymbolQuery(String text) {
        return text.matches("^[A-Za-z]{2,10}$");
    }

    private String extractSearchTerm(String messageText, String... prefixes) {
        String lowerCaseMessage = messageText.toLowerCase();
        for (String prefix : prefixes) {
            if (lowerCaseMessage.startsWith(prefix)) {
                return messageText.substring(prefix.length()).trim();
            }
        }
        return "";
    }

    private String formatCryptocurrencyInfo(int rank, Cryptocurrency crypto) {
        return String.format(
                "%d. *%s* (%s)\n" +
                        "   💰 %s  📈 %.2f%%\n\n",
                rank,
                crypto.getName(),
                crypto.getSymbol(),
                formatPrice(crypto.getCurrentPrice()),
                crypto.getPriceChangePercentage24h() != null ? crypto.getPriceChangePercentage24h() : BigDecimal.ZERO
        );
    }

    private String formatPrice(BigDecimal price) {
        if (price == null) return "N/A";
        if (price.compareTo(BigDecimal.ONE) < 0 && price.compareTo(BigDecimal.ZERO) != 0) {
            return String.format(Locale.US, "$%.6f", price);
        } else {
            return currencyFormatter.format(price);
        }
    }

    private String formatLargeNumber(BigDecimal number) {
        if (number == null) return "N/A";

        BigDecimal trillion = new BigDecimal("1000000000000");
        BigDecimal billion = new BigDecimal("1000000000");
        BigDecimal million = new BigDecimal("1000000");

        if (number.compareTo(trillion) >= 0) {
            return String.format(Locale.US, "%.2fT", number.divide(trillion, 2, RoundingMode.HALF_UP));
        }
        if (number.compareTo(billion) >= 0) {
            return String.format(Locale.US, "%.2fB", number.divide(billion, 2, RoundingMode.HALF_UP));
        }
        if (number.compareTo(million) >= 0) {
            return String.format(Locale.US, "%.2fM", number.divide(million, 2, RoundingMode.HALF_UP));
        }
        return currencyFormatter.format(number.setScale(0, RoundingMode.HALF_UP));
    }

    private ReplyKeyboardMarkup createMainKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.addAll(Arrays.asList("🔥 Топ-10", "📈 Лидеры роста"));

        KeyboardRow row2 = new KeyboardRow();
        row2.addAll(Arrays.asList("📉 Лидеры падения", "📊 Статистика"));

        KeyboardRow row3 = new KeyboardRow();
        row3.addAll(Arrays.asList("📰 Новости", "📖 Помощь"));

        keyboard.addAll(Arrays.asList(row1, row2, row3));
        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);

        return keyboardMarkup;
    }

    private void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        executeMessage(message);
    }

    private void sendMarkdownMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        message.setParseMode("Markdown");
        executeMessage(message);
    }

    private void executeMessage(SendMessage message) {
        try {
            execute(message);
            logger.debug("Sent message to chat {}: {}", message.getChatId(), message.getText().substring(0, Math.min(50, message.getText().length())));
        } catch (TelegramApiException e) {
            logger.error("Error sending message to chat {}: {}", message.getChatId(), e.getMessage());
        }
    }

    private void handleCallbackQuery(long chatId, int messageId, String callbackData) {
        if (callbackData.startsWith("news_more_")) {
            int page = Integer.parseInt(callbackData.substring("news_more_".length()));
            handleNewsPage(chatId, messageId, page);
        }
    }

    private void handleNewsPage(long chatId, int messageId, int page) {
        try {
            List<News> newsList = newsService.getLatestNews(50);

            if (newsList.isEmpty()) {
                editMessage(chatId, messageId, "📰 *Новости не найдены*\n\nПопробуйте позже.");
                return;
            }

            int newsPerPage = 5;
            int startIndex = page * newsPerPage;
            int endIndex = Math.min(startIndex + newsPerPage, newsList.size());

            if (startIndex >= newsList.size()) {
                editMessage(chatId, messageId, "📰 *Больше новостей нет*\n\nВы просмотрели все доступные новости.");
                return;
            }

            StringBuilder message = new StringBuilder("📰 *Новости о криптовалютах (стр. " + (page + 1) + "):*\n\n");

            for (int i = startIndex; i < endIndex; i++) {
                News news = newsList.get(i);

                String formattedDate = "Неизвестно";
                if (news.getPubDate() != null) {
                    formattedDate = news.getPubDate().toString().substring(0, 16).replace("T", " ");
                }

                String title = news.getTitle().replace("*", "\\*").replace("_", "\\_");

                message.append(String.format(
                        "• *%s*\n  📅 %s\n  🔗 [Читать полностью](%s)\n\n",
                        title,
                        formattedDate,
                        news.getLink()
                ));
            }


            InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();
            List<InlineKeyboardButton> row = new ArrayList<>();


            if (page > 0) {
                InlineKeyboardButton prevButton = new InlineKeyboardButton();
                prevButton.setText("◀️ Предыдущая");
                prevButton.setCallbackData("news_more_" + (page - 1));
                row.add(prevButton);
            }


            if (endIndex < newsList.size()) {
                InlineKeyboardButton nextButton = new InlineKeyboardButton();
                nextButton.setText("Следующая ▶️");
                nextButton.setCallbackData("news_more_" + (page + 1));
                row.add(nextButton);
            }

            if (!row.isEmpty()) {
                rows.add(row);
            }

            keyboard.setKeyboard(rows);

            message.append(String.format("_Показано новостей %d-%d из %d_\n\n",
                    startIndex + 1, endIndex, newsList.size()));
            message.append("💡 _Данные обновляются автоматически каждый час_");

            editMessageWithKeyboard(chatId, messageId, message.toString(), keyboard);

        } catch (Exception e) {
            logger.error("Error handling news page for chat {}: {}", chatId, e.getMessage(), e);
            editMessage(chatId, messageId, "❌ Произошла ошибка при загрузке новостей. Попробуйте позже.");
        }
    }

    private void editMessage(long chatId, int messageId, String text) {
        EditMessageText editMessage = new EditMessageText();
        editMessage.setChatId(chatId);
        editMessage.setMessageId(messageId);
        editMessage.setText(text);
        editMessage.setParseMode("Markdown");

        try {
            execute(editMessage);
        } catch (TelegramApiException e) {
            logger.error("Error editing message in chat {}: {}", chatId, e.getMessage());
        }
    }

    private void editMessageWithKeyboard(long chatId, int messageId, String text, InlineKeyboardMarkup keyboard) {
        EditMessageText editMessage = new EditMessageText();
        editMessage.setChatId(chatId);
        editMessage.setMessageId(messageId);
        editMessage.setText(text);
        editMessage.setParseMode("Markdown");
        editMessage.setReplyMarkup(keyboard);

        try {
            execute(editMessage);
        } catch (TelegramApiException e) {
            logger.error("Error editing message with keyboard in chat {}: {}", chatId, e.getMessage());
        }
    }

    private void sendMessageWithKeyboard(long chatId, String text, InlineKeyboardMarkup keyboard) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        message.setParseMode("Markdown");
        message.setReplyMarkup(keyboard);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            logger.error("Error sending message with keyboard to chat {}: {}", chatId, e.getMessage());
        }
    }
}
