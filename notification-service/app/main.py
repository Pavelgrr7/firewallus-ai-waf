import json
import time
import os
import sys
from collections import deque, Counter
import requests
from kafka import KafkaConsumer

# Configuration with environment variable overrides
TELEGRAM_BOT_TOKEN = os.getenv("TELEGRAM_BOT_TOKEN", "8299689793:AAFW4wgoPItcI3fmYnnABZ7CTBKx8mcXd_I")
TELEGRAM_CHAT_ID = os.getenv("TELEGRAM_CHAT_ID", "1446939622")
ALERT_THRESHOLD = int(os.getenv("ALERT_THRESHOLD", 50))      # Количество атак
TIME_WINDOW_SEC = int(os.getenv("TIME_WINDOW_SEC", 60))      # За какое время (в секундах)
COOLDOWN_SEC = int(os.getenv("COOLDOWN_SEC", 300))        # Пауза между алертами (5 минут)
TELEGRAM_API_URL = os.getenv("TELEGRAM_API_URL", "https://api.telegram.org").rstrip("/")
KAFKA_BOOTSTRAP_SERVERS = os.getenv("KAFKA_BOOTSTRAP_SERVERS", "kafka:29092")


def send_telegram_alert(attack_count, top_ip):
    if not TELEGRAM_BOT_TOKEN or TELEGRAM_BOT_TOKEN == "ТВОЙ_ТОКЕН":
        print("Telegram alert skipped: Bot token is not configured.", flush=True)
        return
    if not TELEGRAM_CHAT_ID or TELEGRAM_CHAT_ID == "ТВОЙ_CHAT_ID":
        print("Telegram alert skipped: Chat ID is not configured.", flush=True)
        return

    message = (
        f"<b>КРИТИЧЕСКИЙ АЛЕРТ WAF</b> 🚨\n\n"
        f"Зафиксирована массовая атака!\n"
        f"<b>Инцидентов:</b> {attack_count} за последнюю минуту\n"
        f"<b>Главный атакующий IP:</b> {top_ip}\n\n"
        f"Срочно проверьте панель управления!"
    )
    url = f"{TELEGRAM_API_URL}/bot{TELEGRAM_BOT_TOKEN}/sendMessage"

    try:
        response = requests.post(
            url, 
            json={"chat_id": TELEGRAM_CHAT_ID, "text": message, "parse_mode": "HTML"},
            timeout=10
        )
        response.raise_for_status()
        print(f"Telegram alert sent successfully: {attack_count} attacks, top IP {top_ip}.", flush=True)
    except Exception as e:
        print(f"Failed to send Telegram alert: {e}", flush=True)

def connect_kafka():
    servers = [s.strip() for s in KAFKA_BOOTSTRAP_SERVERS.split(",")]
    print(f"Connecting to Kafka at {servers}...", flush=True)
    retries = 30
    while retries > 0:
        try:
            consumer = KafkaConsumer(
                'incidents',
                bootstrap_servers=servers,
                value_deserializer=lambda m: json.loads(m.decode('utf-8')),
                group_id='notification-service-group',
                auto_offset_reset='latest'
            )
            print("Successfully connected to Kafka.", flush=True)
            return consumer
        except Exception as e:
            retries -= 1
            print(f"Kafka connection failed. Retrying in 5 seconds ({retries} retries left)... Error: {e}", flush=True)
            time.sleep(5)
    print("Could not connect to Kafka. Exiting.", flush=True)
    sys.exit(1)

def main():
    print("Starting Alerting/Notification Service...", flush=True)
    print(f"Configurations:\n"
          f"  ALERT_THRESHOLD: {ALERT_THRESHOLD}\n"
          f"  TIME_WINDOW_SEC: {TIME_WINDOW_SEC}\n"
          f"  COOLDOWN_SEC: {COOLDOWN_SEC}\n"
          f"  TELEGRAM_CHAT_ID: {TELEGRAM_CHAT_ID}\n"
          f"  KAFKA_BOOTSTRAP_SERVERS: {KAFKA_BOOTSTRAP_SERVERS}", flush=True)

    consumer = connect_kafka()

    # Store tuples of (timestamp, attacker_ip)
    incident_timestamps = deque()
    last_alert_time = 0

    print("Alerting Service started. Listening for incidents...", flush=True)

    try:
        for message in consumer:
            incident = message.value
            current_time = time.time()
            attacker_ip = incident.get("attackerIp", "Unknown")

            # Добавляем текущую атаку
            incident_timestamps.append((current_time, attacker_ip))

            # Очищаем старые атаки (старше 1 минуты)
            while incident_timestamps and incident_timestamps[0][0] < current_time - TIME_WINDOW_SEC:
                incident_timestamps.popleft()

            # Проверяем порог и кулдаун
            current_count = len(incident_timestamps)
            if current_count >= ALERT_THRESHOLD:
                if current_time - last_alert_time >= COOLDOWN_SEC:
                    # Находим самый частый IP
                    ip_counts = Counter(ip for _, ip in incident_timestamps)
                    top_ip, _ = ip_counts.most_common(1)[0] if ip_counts else ("Unknown", 0)

                    print(f"Triggering ALERT! {current_count} attacks detected in the last minute.", flush=True)
                    send_telegram_alert(current_count, top_ip)

                    last_alert_time = current_time
                else:
                    # We are in cooldown. Log a message about skipped alert
                    remaining_cooldown = int(COOLDOWN_SEC - (current_time - last_alert_time))
                    print(f"Threshold exceeded ({current_count} attacks), but alert is suppressed due to Cooldown ({remaining_cooldown}s remaining).", flush=True)

    except KeyboardInterrupt:
        print("Stopping Alerting Service...", flush=True)
    finally:
        consumer.close()
        print("Kafka consumer closed. Service stopped.", flush=True)

if __name__ == "__main__":
    main()