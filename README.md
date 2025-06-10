# KAPT - Криптовалютный Агрегатор с Новостями

Система для сбора, хранения и предоставления информации о криптовалютах и новостях из различных источников. Система предоставляет REST API, Telegram бот интерфейс и автоматическое обновление данных.

## 🚀 Основные возможности

### 📊 Криптовалютные данные
- **Автоматический сбор данных** из CoinGecko API
- **Данные о криптовалютах**
- **Цены, рыночная капитализация, объемы торгов**
- **Анализ изменений за 24 часа**
- **Поиск и фильтрация** по различным параметрам

### 📰 Новости о криптовалютах
- **Автоматический сбор новостей** из AlphaVantage News API
- **Фильтрация дубликатов**

### 🔧 Технические возможности
- **REST API** для доступа к данным
- **Telegram бот** для удобного доступа к информации
- **PostgreSQL** для надежного хранения данных
- **Автоматическое обновление** каждые 15 минут (крипто) и 1 час (новости)
- **Покрытие тестами** (unit и integration тесты)
- **Docker поддержка** для развертывания

## 📋 Функциональные возможности

### 💰 Криптовалютные данные
- Автоматическая загрузка данных о криптовалютах
- Извлечение цены, рыночной капитализации, объема торгов
- Расчет изменений за 24 часа
- Исключение дублирующихся записей
- Сохранение в PostgreSQL с индексами для быстрого поиска


### 🔍 Поиск и фильтрация
#### Криптовалюты:
- Поиск по символу криптовалюты (BTC, ETH)
- Поиск по названию (Bitcoin, Ethereum)
- Фильтрация по изменению цены за 24ч
- Получение топ-криптовалют по капитализации
- Список лидеров роста и падения

### 📈 Статистика
#### Криптовалюты:
- Общая статистика рынка
- Средние показатели изменения цен
- Подсчет общей рыночной капитализации

### 🎯 Интерфейсы
- **REST API** для программного доступа
- **Telegram бот** для пользователей
- **JSON ответы** с полной информацией

## 🛠 Технологический стек

- **Java 17** - основной язык программирования
- **Spring Boot 3.3.5** - фреймворк приложения
- **Spring Data JPA** - работа с базой данных
- **PostgreSQL** - реляционная база данных
- **H2** - встроенная база данных для тестирования
- **Flyway** - миграции базы данных
- **Spring WebFlux** - HTTP клиент для внешних API
- **Telegram Bots API** - интеграция с Telegram
- **AlphaVantage API** - источник новостей о криптовалютах
- **CoinGecko API** - источник данных о криптовалютах
- **OpenAPI 3** - документация API
- **JUnit 5** - unit тестирование
- **MockMvc** - integration тестирование
- **Gradle** - система сборки
- **Docker** - контейнеризация приложения

## 📁 Архитектура проекта

Проект следует принципам **SOLID** и **GRASP**:

```
src/main/java/com/example/kapt/
├── config/                 # Конфигурация
│   ├── TelegramBotConfig.java
│   └── WebClientConfig.java
├── controller/             # REST контроллеры
│   ├── CryptocurrencyController.java
│   ├── NewsController.java
│   └── NewsTestController.java
├── dto/                    # Data Transfer Objects
│   └── CoinGeckoResponseDto.java
├── model/                  # JPA сущности
│   ├── Cryptocurrency.java
│   └── News.java
├── repository/             # Репозитории данных
│   ├── CryptocurrencyRepository.java
│   └── NewsRepository.java
├── scheduler/              # Планировщики задач
│   └── NewsScheduler.java
├── service/                # Бизнес-логика
│   ├── AlphaVantageNewsService.java
│   ├── CoinGeckoService.java
│   ├── CryptocurrencyService.java
│   └── NewsService.java
├── telegram/               # Telegram бот
│   └── CryptocurrencyTelegramBot.java
└── KaptApplication.java    # Главный класс
```

### Модель данных

#### Cryptocurrency (Криптовалюта)
- Основные данные: символ, название, цена
- Рыночные показатели: капитализация, объем, ранг
- Изменения: за 24 часа в абсолютных и процентных значениях
- Предложение: текущее, общее, максимальное

#### News (Новости)
- Основные данные: заголовок, описание, ссылка
- Метаданные: источник, язык, дата публикации
- Дополнительно: настроение (от AlphaVantage API), упомянутые криптовалюты
- Обработка: статус дубликата

### Принципы проектирования

**SOLID принципы:**
- **Single Responsibility** - каждый класс имеет одну ответственность
- **Open/Closed** - открыт для расширения, закрыт для модификации
- **Liskov Substitution** - использование интерфейсов и абстракций
- **Interface Segregation** - специализированные интерфейсы
- **Dependency Inversion** - зависимость от абстракций

**GRASP принципы:**
- **Information Expert** - логика находится в соответствующих классах
- **Creator** - объекты создаются в логических местах
- **Controller** - отдельные контроллеры для обработки запросов
- **Low Coupling** - слабая связанность между компонентами
- **High Cohesion** - высокая связность внутри компонентов

## 🔧 Требования для запуска

- **Java 17** или выше
- **PostgreSQL 12** или выше
- **Gradle 7+** (или используйте встроенный gradlew)
- **AlphaVantage API ключ** для новостей
- **Telegram бот токен** (опционально, для Telegram интерфейса)

## 📦 Установка и запуск

### 1. Клонирование репозитория

```bash
git clone <repository-url>
cd KAPT
```

### 2. Настройка базы данных

Создайте базу данных PostgreSQL:

```sql
CREATE DATABASE crypto_db;
CREATE USER crypto_user WITH PASSWORD 'crypto_password';
GRANT ALL PRIVILEGES ON DATABASE crypto_db TO crypto_user;
```

### 3. Конфигурация

Отредактируйте `src/main/resources/application.properties` или установите переменные окружения:

```properties
# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/crypto_db
spring.datasource.username=crypto_user
spring.datasource.password=crypto_password

# AlphaVantage API для новостей
app.alphavantage.api.key=YOUR_ALPHAVANTAGE_API_KEY

# Telegram Bot (опционально)
app.telegram.bot.enabled=true
app.telegram.bot.username=YOUR_BOT_USERNAME
app.telegram.bot.token=YOUR_BOT_TOKEN

# Планировщики
app.scheduler.enabled=true
app.news.scheduler.enabled=true
```

### 4. Получение API ключей

#### AlphaVantage API (для новостей):
1. Зарегистрируйтесь на https://www.alphavantage.co/support/#api-key
2. Получите бесплатный API ключ
3. Установите в конфигурации или переменной окружения `ALPHAVANTAGE_API_KEY`

#### Telegram Bot (опционально):
1. Создайте бота через @BotFather в Telegram
2. Получите токен бота
3. Установите токен в переменной окружения `TELEGRAM_BOT_TOKEN`

### 5. Запуск приложения

#### Используя Gradle:

```powershell
# Windows PowerShell
.\gradlew.bat bootRun
```

#### Используя JAR:

```powershell
# Сборка
.\gradlew.bat build

# Запуск
java -jar build\libs\KAPT-0.0.1-SNAPSHOT.jar
```

#### Используя Docker:

```bash
# Сборка образа
docker build -t kapt-app .

# Запуск с Docker Compose
docker-compose up -d
```

### 6. Проверка работы

Приложение будет доступно по адресу:
- **API**: http://localhost:8080/api/v1/
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/api-docs

#### Быстрая проверка:
```powershell
# Получить все криптовалюты
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/cryptocurrencies" -Method Get

# Получить новости
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/news" -Method Get

# Получить Bitcoin
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/cryptocurrencies/BTC" -Method Get
```

## 📚 Использование API

### 💰 Криптовалютные эндпоинты

#### Получение всех криптовалют
```http
GET /api/v1/cryptocurrencies?page=0&size=20&sortBy=marketCapRank&sortDir=asc
```

#### Поиск по символу
```http
GET /api/v1/cryptocurrencies/BTC
```

#### Поиск по названию
```http
GET /api/v1/cryptocurrencies/search?q=bitcoin
```

#### Топ криптовалют
```http
GET /api/v1/cryptocurrencies/top?limit=10
```

#### Лидеры роста
```http
GET /api/v1/cryptocurrencies/gainers?limit=10
```

#### Лидеры падения
```http
GET /api/v1/cryptocurrencies/losers?limit=10
```

#### Статистика рынка
```http
GET /api/v1/cryptocurrencies/statistics
```

#### Ручное обновление данных
```http
POST /api/v1/cryptocurrencies/update
```

### 📰 Новостные эндпоинты

#### Получение всех новостей
```http
GET /api/v1/news?page=0&size=20&sortBy=pubDate&sortDir=desc
```

#### Поиск новостей по ключевым словам
```http
GET /api/v1/news/search?q=bitcoin&limit=10
```

#### Новости по криптовалюте
```http
GET /api/v1/news/coin/BTC
```

#### Новости по источнику
```http
GET /api/v1/news/source/CoinDesk
```

#### Последние новости
```http
GET /api/v1/news/recent?hours=24
```

#### Новости по настроению
```http
GET /api/v1/news/sentiment/positive
```

#### Статистика новостей
```http
GET /api/v1/news/statistics
```

#### Ручное обновление новостей
```http
POST /api/v1/news/update?batchSize=50
```

#### Расширенный поиск новостей
```http
GET /api/v1/news/search/advanced?keyword=ethereum&sourceName=CoinTelegraph&sentiment=positive&coin=ETH&fromDate=2023-01-01&toDate=2023-12-31
```

### 📊 Примеры ответов

#### Получение криптовалюты по символу
```json
{
  "id": 1,
  "symbol": "BTC",
  "name": "Bitcoin",
  "currentPrice": 45000.00,
  "marketCap": 850000000000.00,
  "marketCapRank": 1,
  "totalVolume": 25000000000.00,
  "priceChange24h": 1250.50,
  "priceChangePercentage24h": 2.85,
  "circulatingSupply": 19000000.00,
  "totalSupply": 21000000.00,
  "maxSupply": 21000000.00,
  "lastUpdated": "2023-12-07T10:30:00"
}
```

#### Получение новости
```json
{
  "id": 1,
  "articleId": "unique-article-id",
  "title": "Bitcoin Reaches New All-Time High",
  "description": "Bitcoin has reached a new all-time high of $70,000...",
  "link": "https://example.com/bitcoin-ath",
  "sourceName": "CoinDesk",
  "language": "en",
  "pubDate": "2023-12-07T10:30:00",
  "coinMentioned": ["BTC"],
  "category": ["cryptocurrency"],
  "sentiment": "positive",
  "keywords": ["bitcoin", "price", "ath"],
  "duplicate": false,
  "createdAt": "2023-12-07T10:35:00",
  "updatedAt": "2023-12-07T10:35:00"
}
```

#### Статистика новостей
```json
{
  "totalCount": 1247,
  "sourceStats": [
    {"source": "CoinDesk", "count": 342},
    {"source": "CoinTelegraph", "count": 298}
  ],
  "sentimentStats": [
    {"sentiment": "positive", "count": 524},
    {"sentiment": "neutral", "count": 456},
    {"sentiment": "negative", "count": 267}
  ],
  "coinStats": [
    {"coin": "BTC", "count": 789},
    {"coin": "ETH", "count": 456}
  ],
  "trendingKeywords": [
    {"keyword": "bitcoin", "count": 234},
    {"keyword": "ethereum", "count": 189}
  ]
}
```

## 🤖 Telegram бот

### Настройка

1. Создайте бота через @BotFather в Telegram
2. Получите токен бота
3. Установите токен в переменной окружения `TELEGRAM_BOT_TOKEN`
4. Установите `app.telegram.bot.enabled=true`
5. Укажите имя бота в `app.telegram.bot.username`

### Команды бота

#### 🚀 Основные команды
- `/start` - Начало работы с ботом
- `/help` - Справка по командам

#### 💰 Криптовалюты
- `/top` или "🔥 Топ-10" - Топ криптовалют по капитализации
- `/gainers` или "📈 Лидеры роста" - Криптовалюты с наибольшим ростом
- `/losers` или "📉 Лидеры падения" - Криптовалюты с наибольшим падением
- `/stats` или "📊 Статистика" - Общая статистика рынка
- `/search <название>` - Поиск криптовалюты по названию
- `/price <символ>` - Текущая цена криптовалюты
- `BTC`, `ETH`, `ADA` и т.д. - Прямой запрос по символу

#### 📰 Новости
- `/news` или "📰 Новости" - Просмотр последних новостей о криптовалютах

### Примеры использования

```
Пользователь: BTC
Бот: 🪙 Bitcoin (BTC)
💰 Цена: $45,250.50 (+2.45%)
📊 Капитализация: $887.2B
📈 Изменение за 24ч: +$1,082.50
🏆 Ранг: #1

Пользователь: /news
Бот: 📰 Последние новости:

1. 🟢 Bitcoin Reaches New Heights
   📅 2 часа назад | 🔗 CoinDesk

2. 🔵 Ethereum Network Upgrade Complete
   📅 4 часа назад | 🔗 CoinTelegraph
```

## 🧪 Тестирование

### Запуск всех тестов

```powershell
# Запуск всех тестов
.\gradlew.bat test

# Запуск только unit тестов
.\gradlew.bat test --tests "*Test*" --exclude-tests "*Integration*"

# Запуск только integration тестов
.\gradlew.bat test --tests "*Integration*"

# Запуск с отчетом о покрытии
.\gradlew.bat test jacocoTestReport
```

### Типы тестов

1. **Unit тесты** - тестируют отдельные компоненты изолированно
   - Сервисы с моками внешних зависимостей
   - Контроллеры с MockMvc
   - DTO и валидация данных

2. **Integration тесты** - тестируют интеграцию компонентов
   - Полный цикл работы с базой данных (H2)
   - HTTP слой с реальными запросами
   - Тестирование планировщиков

3. **Controller тесты** - тестируют HTTP слой
   - REST API эндпоинты
   - Валидация параметров
   - Обработка ошибок

### Структура тестов

```
src/test/java/com/example/kapt/
├── controller/             # Тесты контроллеров
│   ├── CryptocurrencyControllerTest.java
│   └── NewsControllerTest.java
├── integration/            # Интеграционные тесты
│   └── NewsIntegrationTest.java
├── model/                  # Тесты моделей
├── repository/             # Тесты репозиториев
└── service/               # Тесты сервисов
```

### Покрытие

Проект имеет высокое покрытие тестами:
- **Сервисы**: unit тесты с моками для внешних API
- **Репозитории**: integration тесты с H2 базой данных
- **Контроллеры**: web слой тесты с MockMvc
- **Модели**: тесты валидации и сериализации
- **DTO конверсия**: unit тесты трансформации данных

### Конфигурация тестов

Тесты используют отдельный профиль `test` с настройками:

```properties
# application-test.properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.jpa.hibernate.ddl-auto=create-drop
spring.flyway.enabled=false
app.telegram.bot.enabled=false
app.scheduler.enabled=false
app.news.scheduler.enabled=false
```

### Примеры тестирования

#### Unit тест сервиса:
```java
@ExtendWith(MockitoExtension.class)
class NewsServiceTest {
    @Mock private NewsRepository newsRepository;
    @Mock private AlphaVantageNewsService alphaVantageService;
    @InjectMocks private NewsService newsService;
    
    @Test void shouldFindNewsByKeyword() {
        // given
        when(newsRepository.searchByTitleOrDescription("bitcoin"))
            .thenReturn(Arrays.asList(createTestNews()));
        
        // when & then
        List<News> result = newsService.searchNews("bitcoin");
        assertThat(result).hasSize(1);
    }
}
```

#### Integration тест:
```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NewsIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private NewsRepository newsRepository;
    
    @Test void shouldRetrieveNewsWithPagination() throws Exception {
        mockMvc.perform(get("/api/v1/news"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(2)))
            .andExpect(jsonPath("$.totalElements", is(2)));
    }
}
```

## 📊 Мониторинг и логирование

### Логи

Приложение ведет подробное логирование:

#### Криптовалютные данные:
- Процесс загрузки данных из CoinGecko API
- Количество обновленных/новых записей
- Ошибки API и обработка лимитов запросов
- Performance метрики операций с базой данных

#### Новости:
- Процесс загрузки новостей из AlphaVantage API
- Обработка и сохранение контента
- Извлечение упомянутых криптовалют
- Обнаружение дубликатов
- Статистика обработанных новостей

#### Telegram бот:
- Взаимодействия пользователей
- Обработка команд и запросов
- Ошибки отправки сообщений

#### Системные логи:
- Запуск и остановка планировщиков
- Подключения к базе данных
- HTTP запросы и ответы (в DEBUG режиме)
- Обработка исключений

### Уровни логирования

```properties
# Основная информация о работе приложения
logging.level.com.example.kapt=INFO

# Детальная диагностика HTTP клиентов
logging.level.org.springframework.web.reactive.function.client=DEBUG

# Логи SQL запросов (при необходимости)
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true
```

### Форматы логов

```properties
# Консольный вывод
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %msg%n

# Файловый вывод
logging.pattern.file=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n
```

### Примеры логов

```
2023-12-07 10:30:15 - Starting cryptocurrency data update...
2023-12-07 10:30:16 - Fetching data for 300 cryptocurrencies from CoinGecko
2023-12-07 10:30:18 - Successfully processed 300 cryptocurrencies
2023-12-07 10:30:18 - Updated: 298, New: 2, Errors: 0

2023-12-07 11:00:12 - Starting news fetch and save process with batch size: 50
2023-12-07 11:00:15 - Successfully fetched 50 crypto news articles
2023-12-07 11:00:16 - Saved new news article: av_1234567890
2023-12-07 11:00:17 - News fetch completed - saved: 47, duplicates: 3
```

## 🔒 Безопасность

### Меры безопасности

- **Валидация входных данных** - проверка всех пользовательских входов
- **SQL injection защита** - использование JPA/Hibernate с параметризованными запросами
- **Rate limiting** - ограничение частоты запросов к внешним API
- **Конфигурация через переменные окружения** - чувствительные данные не хранятся в коде
- **HTTPS поддержка** - шифрование трафика в production
- **Валидация API ключей** - проверка корректности внешних API ключей

### Рекомендации по безопасности

```properties
# Используйте переменные окружения для чувствительных данных
ALPHAVANTAGE_API_KEY=your_real_api_key
TELEGRAM_BOT_TOKEN=your_real_bot_token
DATABASE_PASSWORD=strong_password

# Ограничьте доступ к базе данных
spring.datasource.username=limited_user
spring.datasource.password=${DATABASE_PASSWORD}

# Отключите отладочные функции в production
spring.jpa.show-sql=false
logging.level.org.springframework.web.reactive.function.client=WARN
```

## 🚀 Производительность

### Оптимизации базы данных

- **Индексы** - оптимизированные индексы для частых запросов
- **Пагинация** - разбивка больших результатов на страницы
- **Batch операции** - групповая вставка данных для ускорения
- **Connection pooling** - пул соединений для эффективного использования БД
- **Кэширование** - кэширование частых запросов (планируется)

### Оптимизации API

- **Rate limiting** - контроль частоты запросов к внешним API
- **Асинхронные запросы** - использование WebFlux для неблокирующих операций
- **Retry механизм** - автоматические повторы при временных сбоях
- **Timeout настройки** - предотвращение зависания запросов

### Настройки производительности

```properties
# Hibernate batch настройки
spring.jpa.properties.hibernate.jdbc.batch_size=20
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true

# Connection pool настройки
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.idle-timeout=300000

# API rate limiting
app.coingecko.request-delay=1000
app.alphavantage.api.rate-limit-delay=12000
```

### Мониторинг производительности

- **Логирование времени выполнения** операций с базой данных
- **Отслеживание количества API запросов** и лимитов
- **Мониторинг размера базы данных** и роста данных
- **JVM метрики** - использование памяти и GC
- **HTTP метрики** - время ответа эндпоинтов

## 🐛 Устранение неполадок

### Частые проблемы и решения

#### CoinGecko API

**Проблема**: Rate limit exceeded (429 ошибка)
```
ERROR: Too Many Requests - you have exceeded the rate limit
```
**Решение**: 
1. Увеличьте задержку между запросами:
```properties
app.coingecko.request-delay=2000
```
2. Рассмотрите подписку на Pro план CoinGecko

#### AlphaVantage API

**Проблема**: API limit reached
```
ERROR: Thank you for using Alpha Vantage! Our standard API call frequency is 25 requests per day
```
**Решение**:
1. Получите бесплатный API ключ с лимитом 25 запросов/день
2. Для production используйте Premium план
3. Увеличьте интервал обновления новостей:
```properties
app.news.scheduler.fixed-delay=7200000  # 2 часа
```

#### База данных PostgreSQL

**Проблема**: Ошибки подключения
```
ERROR: Connection to localhost:5432 refused
```
**Решение**:
1. Убедитесь, что PostgreSQL запущен
2. Проверьте настройки подключения
3. Создайте базу данных и пользователя:
```sql
CREATE DATABASE crypto_db;
CREATE USER crypto_user WITH PASSWORD 'crypto_password';
GRANT ALL PRIVILEGES ON DATABASE crypto_db TO crypto_user;
```

#### Telegram бот

**Проблема**: Бот не отвечает на команды
**Решение**:
1. Проверьте корректность токена бота
2. Убедитесь, что бот включен:
```properties
app.telegram.bot.enabled=true
```
3. Проверьте логи на ошибки авторизации

#### Проблемы с тестами

**Проблема**: Тесты падают из-за проблем с H2 базой
**Решение**:
1. Проверьте конфигурацию тестового профиля
2. Убедитесь, что H2 база используется для тестов:
```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.jpa.hibernate.ddl-auto=create-drop
```

### Логи для диагностики

Включите детальное логирование для диагностики:

```properties
# Детальные логи приложения
logging.level.com.example.kapt=DEBUG

# HTTP клиент логи
logging.level.org.springframework.web.reactive.function.client=DEBUG

# SQL запросы
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Hibernate логи
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```


## 📈 Планы развития

### Краткосрочные цели (3-6 месяцев)
- [ ] **Новые источники данных**
  - [ ] Интеграция с Binance API для real-time цен
  - [ ] Добавление CryptoCompare для исторических данных
  - [ ] RSS feeds для дополнительных новостных источников

- [ ] **Улучшение новостей**
  - [ ] Дополнительные источники новостей
  - [ ] Улучшенная фильтрация дубликатов
  - [ ] Расширенные возможности поиска

- [ ] **Расширение API**
  - [ ] WebSocket поддержка для real-time обновлений
  - [ ] GraphQL эндпоинты для гибких запросов
  - [ ] API версионирование (v2)

### Среднесрочные цели (6-12 месяцев)
- [ ] **Уведомления и алерты**
  - [ ] Email уведомления о значительных изменениях цен
  - [ ] Push уведомления в Telegram
  - [ ] Webhook интеграции для внешних систем
  - [ ] Настраиваемые пороги для алертов

- [ ] **Портфолио трекинг**
  - [ ] Пользовательские портфели
  - [ ] Отслеживание прибыли/убытков
  - [ ] Анализ диверсификации
  - [ ] Исторические графики портфелей

- [ ] **Дашборды и визуализация**
  - [ ] Web интерфейс с графиками
  - [ ] Базовые аналитические отчеты
  - [ ] Простые чарты цен

### Долгосрочные цели (12+ месяцев)
- [ ] **Расширенная функциональность**
  - [ ] Улучшенная категоризация новостей
  - [ ] Более сложная аналитика данных
  - [ ] Интеграция с дополнительными API

- [ ] **Пользовательские функции**
  - [ ] Простое мобильное приложение
  - [ ] Базовые уведомления
  - [ ] Пользовательские настройки

- [ ] **Техническое развитие**
  - [ ] Кэширование данных
  - [ ] Улучшенная производительность
  - [ ] Расширенное тестирование

## 🤝 Вклад в проект

### Как внести свой вклад

1. **Fork репозитория** на GitHub
2. **Создайте feature branch** из develop:
   ```bash
   git checkout -b feature/amazing-feature
   ```
3. **Следуйте стандартам кодирования**:
   - Java Code Conventions
   - Spring Best Practices
   - Clean Code принципы
4. **Добавьте тесты** для нового функционала
5. **Убедитесь, что все тесты проходят**:
   ```bash
   .\gradlew test
   ```
6. **Commit изменения** с описательным сообщением:
   ```bash
   git commit -m "feat: add amazing feature for cryptocurrency tracking"
   ```
7. **Push в feature branch**:
   ```bash
   git push origin feature/amazing-feature
   ```
8. **Создайте Pull Request** в develop branch

### Areas для вклада

- 🐛 **Bug fixes** - исправление найденных ошибок
- ✨ **Features** - новая функциональность
- 📝 **Documentation** - улучшение документации
- 🧪 **Testing** - расширение тестового покрытия
- 🔧 **DevOps** - CI/CD, Docker, мониторинг
- 🎨 **UI/UX** - улучшение пользовательского интерфейса

## 📄 Лицензия

Проект распространяется под лицензией **MIT**.

### MIT License Summary

- ✅ Коммерческое использование
- ✅ Модификация
- ✅ Распространение
- ✅ Частное использование
- ❌ Ответственность
- ❌ Гарантия

## 👥 Авторы и участники

### Основная команда
- **Ведущий разработчик**: Алексей)
  - Архитектура приложения
  - Backend разработка
  - API дизайн

### Благодарности
- **CoinGecko** - за предоставление качественного API для криптовалютных данных
- **AlphaVantage** - за новостной API
- **Spring Community** - за отличный фреймворк
- **Telegram** - за Bot API

## 📞 Поддержка и связь

### Получить помощь

- 📖 **Документация**: ознакомьтесь с этим README и API документацией


---

## 🎯 Дисклеймер

> **⚠️ Важно**: Этот проект создан в **образовательных целях** и демонстрирует современные практики разработки Java приложений с использованием Spring Boot.
> 
> Данные о криптовалютах и новости предоставляются внешними API и могут быть неточными или устаревшими. **Не используйте эту информацию для принятия финансовых решений** без дополнительной проверки.
> 
> Разработчики не несут ответственности за любые финансовые потери, возникшие в результате использования данного приложения.

*Проект KAPT - система для сбора и просмотра данных о криптовалютах и новостях, демонстрирующая современные практики разработки Java приложений с использованием Spring Boot.*
