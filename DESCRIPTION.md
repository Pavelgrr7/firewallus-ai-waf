# Firewallus: AI-Powered Out-of-Band WAF

## 1. Концепция продукта
**Firewallus** — это гибридный Web Application Firewall прикладного уровня (L7). Основной принцип: Out-of-Band анализ. Система не блокирует входящие HTTP-запросы во время работы ML-модели, чтобы не создавать задержек для легитимных пользователей. Анализ трафика происходит асинхронно. Обнаруженные аномалии приводят к мгновенному обновлению горячего кэша (Redis), блокируя последующие атаки злоумышленника на уровне шлюза.

### Ограничения MVP (Out of Scope)
*   **Не анализируем:** L3/L4 атаки (SYN Flood, UDP амплификации).
*   **Тела запросов (Body):** Для MVP анализируем только заголовки (Headers), метаданные (URI, Method, Content-Length) и небольшие тела запросов (до 8-16 КБ). Крупные файлы (Multipart) игнорируются, анализируются только их метаданные.
*   **Инфраструктура:** Без Kubernetes (используем Docker Compose).

## 2. Архитектура и Зоны ответственности
Система состоит из 4-х основных узлов (микросервисов).

### 2.1. Nginx (Reverse Proxy & Load Balancer)
*   **Стек:** Nginx (Alpine Docker Image).
*   **Роль:** Входная точка в систему (DMZ).
*   **Обязанности:**
    *   Терминация публичного трафика (80/443 порты).
    *   Проброс реальных IP-адресов клиента (`X-Real-IP`, `X-Forwarded-For`).
    *   Балансировка нагрузки (Round Robin) между инстансами Ktor Gateway.

### 2.2. Ktor Gateway (Traffic Guard)
*   **Стек:** Kotlin, Ktor, Netty, Kotlin Coroutines.
*   **Роль:** Data Plane. Умный шлюз, принимающий решения о пропуске трафика.
*   **Обязанности:**
    *   **Hot Check:** Проверяет IP в Redis (`waf:ban:ip:{ip}`). Если есть бан — отдает 403 Forbidden.
    *   **Rule Engine:** Проверяет запрос по статическим правилам из Redis (in-memory кэш из `waf:active_rules`).
    *   **Shadowing:** Асинхронно (fire-and-forget) отправляет JSON с метаданными запроса в Kafka (топик `traffic-logs`).
    *   Проксирует легитимный трафик на защищаемый бэкенд.

### 2.3. Manager Core (Control Plane)
*   **Стек:** Java 21, Kotlin, Spring Boot 3, Spring Data JPA, Spring Security.
*   **Роль:** Админка, управление правилами и хранение истории.
*   **Обязанности:**
    *   CRUD для управления администраторами и правилами WAF (REST API).
    *   **Write-Through Cache:** При изменении правила в Postgres, синхронно (через Spring Events) обновляет словарь правил в Redis.
    *   Чтение топика `incidents` из Kafka и сохранение в Postgres.

### 2.4. AI Brain (ML Analyzer)
*   **Стек:** Python, FastAPI, Scikit-learn / PyTorch.
*   **Роль:** Анализатор аномалий.
*   **Обязанности:**
    *   Читает топик `traffic-logs` из Kafka.
    *   Извлекает фичи (Feature extraction).
    *   Выполняет предикты (поиск аномалий).
    *   При обнаружении атаки: 
        1. Пишет бан в Redis (`waf:ban:ip:{ip}`) с TTL. 
        2. Отправляет инцидент в Kafka (`incidents`) для аудита в Spring.

## 3. Инфраструктура данных

### 3.1. Брокер сообщений: Kafka
*   **Topic `traffic-logs`:** Producer = Ktor, Consumer = Python. (Сырой трафик).
*   **Topic `incidents`:** Producer = Python, Consumer = Spring. (Вердикты об атаках).

### 3.2. Горячее хранилище: Redis
*   `waf:ban:ip:{ip}` (String) - Временные баны от ML (с TTL).
*   `waf:manual_ban:ip:{ip}` (String) - Постоянные баны от админа (без TTL).
*   `waf:active_rules` (Redis Hash) - Кэш статических правил (Key: RuleID, Value: Rule JSON).

### 3.3. Холодное хранилище: PostgreSQL
*   `admins`: admin_id (UUID), username, password_hash, role.
*   `rules`: rule_id (SERIAL), name, action, is_active, created_by, created_at, **conditions (JSONB)**.
*   `incident_logs`: incident_id (UUID), timestamp, attacker_ip, target_uri, confidence_score, payload_dump (JSONB).

## 4. Контракт REST API (Manager Core)
Базовый путь: `/api/v1`
*   `POST /auth/login` — Получение JWT токена.
*   `GET /rules` — Список правил (с пагинацией `?page=0&size=20`).
*   `POST /rules` — Создать правило.
*   `POST /rules/{id}/enable` — Включить правило (Action endpoint).
*   `POST /rules/{id}/disable` — Выключить правило.
*   `DELETE /rules/{id}` — Удалить правило.
*   `GET /logs` — Просмотр инцидентов (пагинация).

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
