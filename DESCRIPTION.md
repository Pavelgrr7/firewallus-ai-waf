# Firewallus: AI-Powered Out-of-Band WAF

## 1. Концепция продукта
**Firewallus** — это гибридный Web Application Firewall прикладного уровня (L7). Основной принцип: Out-of-Band анализ. Система не блокирует входящие HTTP-запросы во время работы ML-модели, чтобы не создавать задержек для легитимных пользователей. Анализ трафика происходит асинхронно. Обнаруженные аномалии приводят к мгновенному обновлению горячего кэша (Redis), блокируя последующие атаки злоумышленника на уровне шлюза.

### Ограничения MVP (Out of Scope)
*   **Не анализируем:** L3/L4 атаки (SYN Flood, UDP амплификации).
*   **Тела запросов (Body):** Для MVP анализируем только заголовки (Headers), метаданные (URI, Method, Content-Length) и небольшие тела запросов (до 8-16 КБ). Крупные файлы (Multipart) игнорируются, анализируются только их метаданные.
*   **Инфраструктура:** Без Kubernetes (используем Docker Compose).

## 2. Архитектура и Зоны ответственности
Система состоит из 3-х основных узлов (микросервисов).

### 2.1 Ktor Gateway (Traffic Guard)
**Пайплайн фильтрации (Fail-Fast):**
1. Извлечение `ClientIdentity` (IP, MD5(Fingerprint), MD5(JWT)).
2. Проверка **Whitelist** (Redis) -> Пропуск.
3. Проверка **Ban Lists** (Redis MGET) -> HTTP 403.
4. Проверка **Rate Limiter** (Lua/Redis) -> HTTP 429.
5. Оценка по **Rule Engine** (Local In-Memory Cache) -> HTTP 403 или Log.
6. Отправка `TrafficEventDto` в Kafka.
7. Проксирование на целевой бэкенд.

### 2.2 Manager Core (Spring Boot)
*   Управление ACL (Black/White списки).
*   Управление статическими правилами и глобальными настройками (Singleton Table).
*   Трансляция инцидентов из Kafka на Frontend через **SSE (Server-Sent Events)**.
*   Ведение журнала аудита (`audit_logs`) администраторов.

### 2.3 ML Analyzer & Alerting Service (Python)
*   **AI Brain:** Расчет энтропии Шеннона, векторизация TF-IDF, поиск аномалий, блокировка через Redis.
*   **Alerting Worker:** Читает топик `incidents`, использует алгоритм скользящего окна (Sliding Window). При превышении `alert_threshold` шлет уведомление в Telegram (конфигурация читается из Redis).

## 3. Инфраструктура данных

### 3.1 Kafka Topics
*   `traffic-logs` — Сырой трафик от Ktor (Producer: Ktor, Consumer: Python ML).
*   `incidents` — Зафиксированные атаки и срабатывания WAF (Producers: Ktor, Python ML; Consumers: Spring, Alerting Service).

### 3.2 Redis (Hot Data)
*   `waf:whitelist:ip:{ip}` — Белый список (без TTL).
*   `waf:manual_ban:ip:{ip}` — Постоянный бан от админа.
*   `waf:ban:ip:{ip}`, `waf:ban:fp:{hash}`, `waf:ban:jwt:{hash}` — ML баны с TTL.
*   `waf:active_rules` — (Hash) Активные статические правила.
*   `waf:global_settings` — (JSON String) Лимиты Rate Limiter и настройки Telegram.
*   `waf:ratelimit:ip:{ip}` — Счетчики (управляются атомарно через Lua).

### 3.3 PostgreSQL (Cold Data / Source of Truth)
*   `admins`: Учетные записи администраторов (BCrypt).
*   `ip_lists`: Blacklist и Whitelist адреса (`ip_address`, `list_type`).
*   `waf_settings`: (Singleton, id=1) Настройки Rate Limit и Telegram-интеграции.
*   `rules`: Статические правила, колонка `conditions` хранится как **JSONB**.
*   `incident_logs`: История атак (без внешних ключей, для производительности).
*   `audit_logs`: История действий администраторов (кто, когда, какое правило изменил).

## 4. Контракт REST API (Manager Core)
*Базовый путь: `/api/v1`*

**Auth:**
*   `POST /auth/login` — Получение JWT.

**Rules:**
*   `GET /rules` — Список (Pageable).
*   `POST /rules` — Создать.
*   `POST /rules/seed-defaults` — Заполнить базу базовыми сигнатурами (SQLi, XSS, Path Traversal).
*   `PATCH /rules/{id}` — Частичное обновление.
*   `POST /rules/{id}/enable` | `/disable` — Смена статуса (RPC-стиль).
*   `DELETE /rules/{id}` — Удаление.

**Access Control (ACL):**
*   `GET /access-control` — Получение IP (опциональный фильтр `?listType=BLACKLIST`).
*   `POST /access-control` — Добавить IP в белый/черный список.
*   `DELETE /access-control/{id}` — Удалить IP.

**Settings:**
*   `GET /settings` — Глобальные лимиты и настройки алертов.
*   `PATCH /settings` — Изменение конфигурации на лету.

**Telemetry & Audit:**
*   `GET /incidents` — Постраничная история атак.
*   `GET /incidents/stream` — Подключение к потоку SSE для дашборда.
*   `GET /audit-logs` — История действий администраторов (Read-Only).

## 5. Структура статических правил (Rule Engine)
Правила хранятся в БД в поле `conditions` (JSONB) и передаются на Frontend/Ktor в виде унифицированного контракта:

```json
{
  "id": 1,
  "name": "Block SQLMap Scanners",
  "action": "BLOCK",            // Варианты: BLOCK, ALLOW, LOG
  "is_active": true,
  "conditions": [
    {
      "target": "HEADER",       // Варианты: IP, URI, HEADER, METHOD
      "target_key": "User-Agent", // Используется только если target == HEADER
      "operator": "CONTAINS",   // Варианты: EQUALS, CONTAINS, REGEX
      "value": "sqlmap"         // Искомое значение
    }
  ]
}
```
*В MVP каждое правило содержит массив `conditions`, объединенных логическим `AND` (все условия должны выполниться).*

Документ является живым (Living Documentation) и может дополняться по мере разработки.
