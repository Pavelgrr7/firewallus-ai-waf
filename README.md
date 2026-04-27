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

1. **Nginx:** L4/L6 балансировщик.
2. **Ktor Gateway:** Ядро WAF. Перехватывает HTTP-запросы, выполняет быструю проверку по кэшу (Fast-Path) и асинхронно проксирует трафик на защищаемый бэкенд.
3. **Apache Kafka:** Брокер сообщений для асинхронной передачи слепков трафика (метаданные + фрагменты body) из Gateway в ML-сервис (Fire-and-Forget).
4. **ML Service (Python):** Читает логи из Kafka, анализирует признаки и принимает решение о блокировке.
5. **Redis:** Хранилище для черного списка адресов.
6. **PostgreSQL & Spring Boot:** Хранилище для долговременного аудита безопасности, хранения статических правил и админ-панели (с использованием Flyway миграций).

## Технологический стек
*   **Backend (Gateway):** Kotlin, Ktor, Koin (DI), Lettuce (асинхронный Redis клиент), Kafka Producer.
*   **Backend (Admin Panel):** Java/Kotlin, Spring Boot, Spring Data JPA, Flyway.
*   **Infrastructure:** Docker, Docker Compose, Nginx.
*   **Data Stores:** PostgreSQL (Cold data), Redis (Hot data).
*   **ML & Analytics:** Python (в разработке).
*   **Frontend (Admin Panel):** TS, React

## Текущий статус (Что реализовано)
В данный момент реализован базовый инфраструктурный контур и бэкенд-маршрутизация:

- [x] **Docker-инфраструктура:** Настроен единый `docker-compose.yml` для поднятия Kafka (KRaft), PostgreSQL, Redis и Nginx.
- [x] **Traffic Pipeline:** Реализован перехват запросов в Ktor, извлечение L7-метаданных и проксирование трафика.
- [x] **Оптимизированный Redis-клиент:** Написан потокобезопасный (`by lazy`) неблокирующий клиент на базе Lettuce (CompletableFuture API + Coroutines). Реализована агрегация запросов (`EXISTS` с вараргом) для минимизации Network Latency.
- [x] **Fail-Open Design:** Внедрена стратегия отказоустойчивости. При падении Redis или Kafka, WAF пропускает трафик, чтобы не прерывать бизнес-процессы (graceful degradation).
- [x] **DI & Resource Management:** Настроен Koin с кастомными inline/reified функциями для безопасного закрытия ресурсов (`AutoCloseable`) при остановке контейнера, исключающий утечки памяти и фантомную инициализацию бинов.
- [x] **Схема БД (Flyway):** Спроектирована схема для PostgreSQL с использованием `UUID`, `JSONB`, `ENUM` и грамотным индексированием внешних ключей.

## План дальнейшего развития (Roadmap)
- [ ] **ML Service:** Реализация Python-консьюмера для чтения топика `traffic-logs` и алгоритма машинного обучения.
- [ ] **Advanced Banning:** Внедрение 3-уровневой системы банов (по JWT-токену, Fingerprint и IP-адресу).
- [ ] **Admin Panel:** Разработка REST API на Spring Boot для управления конфигурацией WAF и просмотра `security_audit_logs`.
- [ ] **Rate Limiting:** Добавление модуля защиты от логического брутфорса на стороне Ktor.
- [ ] **Load Testing:** Нагрузочное тестирование Gateway (Gatling/JMeter) для оценки Overhead-задержки.

## Как запустить локально (Dev-окружение)

Для запуска необходимо иметь установленный Docker и Docker Compose.

```bash
git clone https://github.com/Pavelgrr7/firewallus-ai-waf.git
cd firewallus-ai-waf

docker compose up -
# Можно читать логи, кидать запросы через postman/insomnia (frontend WIP), ну и радоваться!
```

*Доступ к DbGate для управления БД: `http://localhost:8080`*
