# Firewallus: AI-Powered Out-of-Band WAF

## Концепция продукта

Firewallus — это гибридный Web Application Firewall прикладного уровня (L7).
Основной принцип: Out-of-Band анализ. Система не блокирует входящие HTTP-запросы во время работы ML-модели, чтобы не создавать задержек для легитимных пользователей. Анализ трафика происходит асинхронно. Обнаруженные аномалии приводят к мгновенному обновлению горячего кэша (Redis), блокируя последующие атаки злоумышленника на уровне шлюза.

### Ограничения MVP (Out of Scope)

* Не анализируем: L3/L4 атаки (SYN Flood, UDP амплификации).
* Тела запросов (Body): Для MVP анализируем только заголовки (Headers), метаданные (URI, Method, Content-Length) и небольшие тела запросов (до 8-64 КБ). Крупные файлы (Multipart) игнорируются, анализируются только их метаданные.
* Инфраструктура: Без Kubernetes (используем Docker Compose).


## Архитектура и Зоны ответственности

### Система состоит из 4-х основных узлов (микросервисов).

### 2.1. Nginx (Reverse Proxy & Load Balancer)

Стек: Nginx (Alpine Docker Image).

Роль: Входная точка в систему (DMZ).

Обязанности:

Терминация публичного трафика (80/443 порты).

Проброс реальных IP-адресов клиента (X-Real-IP, X-Forwarded-For).

Балансировка нагрузки (Round Robin) между инстансами Ktor Gateway.

Базовый Rate Limiting (опционально для MVP).

### 2.2. Ktor Gateway (Traffic Guard)

Стек: Kotlin, Ktor, Netty, Kotlin Coroutines.

Роль: Data Plane. Умный шлюз, принимающий решения о пропуске трафика.

Обязанности:

Hot Check: При входе запроса проверяет IP в Redis (waf:ban:ip:{ip}). Если есть бан — отдает 403 Forbidden.

Rules Enforcement: Проверяет запрос по статическим правилам из Redis (waf:active_rules).

Shadowing: Асинхронно (fire-and-forget) отправляет JSON с метаданными запроса в Kafka (топик traffic-logs).

Проксирует легитимный трафик на защищаемый целевой бэкенд.

### 2.3. Manager Core (Control Plane)

Стек: Java 21, Spring Boot 3, Spring Data JPA, Spring Security.

Роль: Админка, управление правилами и хранение истории.

Обязанности:

CRUD для управления администраторами и правилами WAF (REST API).

Write-Through Cache: При изменении правила в Postgres, синхронно пушит обновленный список правил в Redis.

Чтение топика incidents из Kafka и сохранение инцидентов в Postgres.

Отдача логов и инцидентов для дашборда (в перспективе - через SSE).

### 2.4. AI Brain (ML Analyzer)

Стек: Python, FastAPI, Scikit-learn / PyTorch.

Роль: Анализатор аномалий (Unsupervised Learning).

Обязанности:

Читает топик traffic-logs из Kafka (желательно батчами для скорости).

Извлекает фичи (Feature extraction) из JSON-логов.

Выполняет предикты (поиск аномалий).

При обнаружении атаки: 1. Пишет бан в Redis (waf:ban:ip:{ip} с TTL, например, 10 минут).
2. Отправляет детали инцидента в Kafka (топик incidents), чтобы Spring мог сохранить их в БД.

## 3. Инфраструктура данных

### 3.1. Брокер сообщений: Kafka

Используется как буфер (Shock Absorber) для сглаживания пиковых нагрузок.

Topic traffic-logs: Producer = Ktor, Consumer = Python. (Сырой трафик).

Topic incidents: Producer = Python, Consumer = Spring. (Вердикты об атаках).

### 3.2. Горячее хранилище: Redis

waf:ban:ip:{ip} (String) - Временные баны от ML (с TTL).

waf:manual_ban:ip:{ip} (String) - Постоянные баны от админа (без TTL).

waf:active_rules (JSON) - Кэш статических правил.

### 3.3. Холодное хранилище: PostgreSQL

Реляционная БД, принадлежащая исключительно Spring Manager.

admins: admin_id (UUID), username, password_hash (VARCHAR 255), role.

rules: rule_id (UUID), name, rule_type, condition_value, is_active, created_by.

incident_logs: incident_id (UUID), timestamp, attacker_ip, target_uri, confidence_score, payload_dump (JSONB). (Без Foreign Keys).

audit_logs: action_id (UUID), admin_id, action_type, timestamp, details (JSONB).

## 4. Контракт REST API (Manager Core)

Базовый путь: /api/v1

GET /admin/profile — Данные текущего админа.

GET /rules — Список правил (с пагинацией).

GET /rules/{id} — Конкретное правило.

POST /rules — Создать правило.

PATCH /rules/{id} — Включить/выключить (isActive).

DELETE /rules/{id} — Удалить правило.

GET /logs — Просмотр инцидентов (обязательна пагинация).

GET/POST/DELETE /blacklist — Ручное управление черным списком IP.

Документ является живым (Living Documentation) и может дополняться по мере разработки.