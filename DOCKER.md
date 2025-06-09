# 🐳 Docker Deployment Guide

Этот документ описывает, как запустить криптовалютный агрегатор в Docker.

## 📋 Предварительные требования

- [Docker Desktop](https://www.docker.com/products/docker-desktop) (включает docker-compose)
- Минимум 2GB свободной RAM
- Порты 8080, 5432 должны быть свободны

## 🚀 Быстрый старт

```bash
# Запуск всех сервисов
docker-compose up --build -d

# Просмотр логов
docker-compose logs -f app

# Остановка сервисов
docker-compose down
```

## 🌐 Доступные эндпоинты

После успешного запуска приложение будет доступно по следующим адресам:

- **API**: http://localhost:8080/api/v1/cryptocurrencies
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Health Check**: http://localhost:8080/actuator/health
- **Database**: localhost:5432 (crypto_db / crypto_user / crypto_password)

## 🔧 Конфигурация

### Переменные окружения

Вы можете настроить приложение через переменные окружения в `docker-compose.yml`:

```yaml
environment:
  # База данных
  SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/crypto_db
  SPRING_DATASOURCE_USERNAME: crypto_user
  SPRING_DATASOURCE_PASSWORD: crypto_password
  
  # Telegram Bot (раскомментируйте и укажите свои значения)
  APP_TELEGRAM_BOT_ENABLED: "true"
  TELEGRAM_BOT_USERNAME: "your_bot_username"
  TELEGRAM_BOT_TOKEN: "your_bot_token"
  
  # Планировщик обновлений
  APP_SCHEDULER_ENABLED: "true"
  
  # Логирование
  LOGGING_LEVEL_COM_EXAMPLE_KAPT: INFO
```

### Telegram Bot

Для активации Telegram бота:

1. Создайте бота через [@BotFather](https://t.me/botfather)
2. Получите токен и username
3. Раскомментируйте соответствующие строки в `docker-compose.yml`
4. Укажите ваши значения токена и username
5. Перезапустите контейнеры: `docker-compose up -d`

## 📊 Мониторинг

### Логи
```bash
# Логи всех сервисов
docker-compose logs -f

# Логи только приложения
docker-compose logs -f app

# Логи только базы данных
docker-compose logs -f postgres
```

### Статус сервисов
```bash
# Статус контейнеров
docker-compose ps

# Детальная информация
docker-compose top
```

### Health Checks
```bash
# Проверка здоровья приложения
curl http://localhost:8080/actuator/health

# Проверка базы данных
docker-compose exec postgres pg_isready -U crypto_user -d crypto_db
```

## 🛠 Разработка

### Режим разработки
```bash
# Запуск только базы данных
docker-compose up postgres -d

# Запуск приложения локально с профилем docker
./gradlew bootRun --args='--spring.profiles.active=docker'
```

### Пересборка приложения
```bash
# Пересборка и перезапуск только приложения
docker-compose up --build app
```

## 🔄 Обновление данных

Приложение автоматически:
- Обновляет данные о криптовалютах каждые 15 минут
- Выполняет миграции базы данных при запуске
- Сохраняет данные в PostgreSQL

## 🗂 Структура томов

```
postgres_data/    # Данные PostgreSQL
app_logs/        # Логи приложения
```

## ❌ Устранение неполадок

### Приложение не запускается
```bash
# Проверьте логи
docker-compose logs app

# Проверьте, что база данных готова
docker-compose logs postgres
```

### Порт уже занят
```bash
# Найти процесс на порту 8080
netstat -ano | findstr :8080  # Windows
lsof -i :8080                # Linux/MacOS

# Изменить порт в docker-compose.yml
ports:
  - "8081:8080"  # Измените на свободный порт
```

### Очистка данных
```bash
# Остановка и удаление всех данных
docker-compose down -v

# Удаление образов
docker-compose down --rmi all -v
```

## 🔒 Безопасность

В продакшене рекомендуется:

1. Изменить пароли базы данных
2. Настроить SSL/TLS
3. Ограничить доступ к портам
4. Использовать Docker secrets для токенов
5. Настроить firewall

## 📝 Примеры API запросов

```bash
# Получить список криптовалют
curl http://localhost:8080/api/v1/cryptocurrencies

# Получить статистику рынка
curl http://localhost:8080/api/v1/cryptocurrencies/statistics

# Поиск по криптовалюте
curl "http://localhost:8080/api/v1/cryptocurrencies/search?query=bitcoin"

# Топ криптовалют по капитализации
curl "http://localhost:8080/api/v1/cryptocurrencies/top?limit=10"
```
