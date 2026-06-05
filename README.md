# Firewallus: AI-Powered Next-Generation WAF

![Kotlin](https://img.shields.io/badge/Kotlin-2.3-7F52FF?style=flat&logo=kotlin&logoColor=white)
![Ktor](https://img.shields.io/badge/Ktor-3.4-087CFA?style=flat&logo=ktor&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0-6DB33F?style=flat&logo=spring&logoColor=white)
![Apache Kafka](https://img.shields.io/badge/Kafka-231F20?style=flat&logo=apachekafka&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=flat&logo=redis&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=flat&logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat&logo=docker&logoColor=white)

## О проекте
**Firewallus** — это прототип Web Application Firewall (WAF) нового поколения. В отличие от классических файрволов, опирающихся исключительно на жесткие статические правила (RegEx), Firewallus использует машинное обучение (ML) для выявления аномалий и поведенческого анализа трафика на прикладном уровне (L7 OSI).

Проект разрабатывается в команде из двух человек (Backend + ML). Главная цель — создать высоконагруженный, отказоустойчивый шлюз, который не замедляет легитимный трафик, но эффективно отсекает автоматизированные атаки, SQLi, XSS и ботнеты.

## Архитектура системы

Система построена на микросервисной архитектуре с четким разделением на "горячий" и "холодный" контуры:

1. **Nginx:** балансировщик, терминирует SSL и передает заголовки.
2. **Ktor Gateway:** Ядро WAF. Выполняет быструю проверку по кэшу, применяет L7-правила (в т.ч. к телу запроса), асинхронно шлет копии трафика в Kafka и проксирует "чистый" трафик на защищаемый сервер.
3. **Apache Kafka:** Брокер сообщений для асинхронной передачи слепков трафика (метаданные + фрагменты body) из Gateway в ML-сервис (Fire-and-Forget).
4. **ML Service (FastAPI):** Python-модуль на базе `IsolationForest`. Извлекает фичи (Shannon entropy, спецсимволы), выявляет аномалии и отправляет команды на блокировку в Redis.
5. **Alerting Service:** Python-воркер. Агрегирует инциденты из Kafka (алгоритм Sliding Window) и отправляет критические уведомления в Telegram.
6. **Redis:** Используется для 3-уровневой системы банов (IP, JWT, Fingerprint), In-memory кэширования статических правил и атомарного Rate Limiting (через Lua-скрипты).
7. **PostgreSQL & Spring Boot:** Хранилище для долговременного аудита безопасности, хранения статических правил и админ-панели (с использованием Flyway миграций).

## Технологический стек
*   **Backend (Gateway):** Kotlin, Ktor, Koin (DI), Lettuce (асинхронный Redis клиент), Kafka Producer.
*   **Backend (Admin Panel):** Java/Kotlin, Spring Boot, Spring Data JPA, Flyway.
*   **Infrastructure:** Docker, Docker Compose, Nginx.
*   **Data Stores:** PostgreSQL (Cold data), Redis (Hot data).
*   **ML & Analytics:** Python, FastAPI, Scikit-learn (`IsolationForest`, `TF-IDF`), joblib, locust.
*   **Frontend (Admin Panel):** TS, React, Tailwind CSS, react-i18next (i18n).

## Текущий статус (Что реализовано)

- [x] **Real-Time Dashboard:** Разработан React-фронтенд с потоковой передачей данных об инцидентах через Server-Sent Events (SSE), позволяющий мониторить атаки в реальном времени без поллинга. Есть возможность настраивать правила, blacklist/whitelist, настройки WAF. Языки: Rus/Eng.
- [x] **Deep Payload Inspection (DPI):** Инспекция тела запроса на лету (`Target.BODY`). Реализовано умное кэширование Payload (до 16 КБ) с передачей по ссылке для избежания `OutOfMemoryError` и "Double Read" проблемы при проксировании.
- [x] **Smart Reverse Proxy:** Настроен HTTP-клиент Ktor с фильтрацией Hop-by-hop заголовков, отключением генерации исключений на ошибки бэкенда и потоковой передачей ответов (Zero-Copy).
- [x] **AI Anomaly Detection:** Реализован и обучен (датасет CSIC 2010) пайплайн ML-модели на базе `IsolationForest`. Извлечение фичей включает URL-декодирование, расчет энтропии Шеннона и весовые коэффициенты спецсимволов. Настроены эндпоинты для управления (`/model/reload`).
- [x] **Dynamic Routing & Cache Warmup:** Поддержка проброса целевого бэкенда (`targetUrl`) "на лету" из Spring в Ktor. Реализован синхронный прогрев кэшей правил и настроек (`runBlocking`) при старте Gateway.
- [x] **Оптимизированный Redis-клиент:** Написан потокобезопасный неблокирующий клиент на базе Lettuce (CompletableFuture API + Coroutines). Реализована агрегация запросов для минимизации Network Latency.
- [x] **Fail-Open Design:** Внедрена стратегия отказоустойчивости. При падении Redis или Kafka, WAF пропускает трафик, чтобы не прерывать бизнес-процессы (graceful degradation).
- [x] **Rate Limiting:** Добавление модуля защиты от логического брутфорса на стороне Ktor.
- [x] **Security Audit:** Автоматическое логирование всех действий администраторов в фоновых потоках.
- [x] **Advanced Banning:** Внедрение 3-уровневой системы банов (по JWT-токену, Fingerprint и IP-адресу).
- [x] **Load Testing:** Настроен пайплайн в GitHub Actions (Configuration-as-Code через Kotlin DSL). Реализовано автоматическое нагрузочное тестирование с помощью Gatling для контроля Overhead-задержки WAF.
- [x] **Docker-инфраструктура:** Настроен единый `docker-compose.yml` для поднятия Kafka (KRaft), PostgreSQL, Redis и Nginx.

## Как запустить локально

Для запуска необходимо иметь установленный Docker и Docker Compose.

### Предварительные шаги:

1. Создать файл .env в корне проекта

2. Заполнить переменные в .env, ориентируясь на `.env.example`

### Запуск:
```bash
git clone https://github.com/Pavelgrr7/firewallus-ai-waf.git
cd firewallus-ai-waf

docker compose up -d
# Можно читать логи, кидать запросы через postman/insomnia, в админ-панели можно смотреть статистику и работать с правилами
```

*Для запуска с DbGate (`http://localhost:8080`) необходимо использовать команду:

`docker compose -f docker-compose.yml -f docker-compose.dev.yml up -d`*
