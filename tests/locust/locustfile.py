import random
from locust import HttpUser, task, between

def get_random_ip():
    """Генерирует случайный IP для подмены (чтобы WAF банил точечно)"""
    return f"203.0.113.{random.randint(1, 250)}"

class NormalUser(HttpUser):
    # Обычный юзер делает запросы с паузой 1-3 секунды
    wait_time = between(1.0, 3.0)
    # Вес: на каждых 4 обычных юзеров будет создаваться 1 хакер
    weight = 4 

    def on_start(self):
        # Присваиваем "юзеру" его личный IP на время сессии
        self.user_ip = get_random_ip()
        self.headers = {"X-Forwarded-For": self.user_ip}

    @task(3)
    def browse_homepage(self):
        # Обычный GET запрос (метаданные выглядят легитимно)
        self.client.get("/", headers=self.headers, name="[Normal] GET /")

    @task(2)
    def view_profile(self):
        self.client.get("/api/v1/profile", headers=self.headers, name="[Normal] GET /profile")

    @task(1)
    def login_attempt(self):
        # Легитимный POST запрос
        self.client.post("/api/v1/auth", 
                         json={"username": "user", "password": "password"},
                         headers=self.headers,
                         name="[Normal] POST /auth")

class Attacker(HttpUser):
    # Хакер спамит запросами быстрее (пауза 0.5 - 1.5 сек)
    wait_time = between(0.5, 1.5)
    weight = 1

    def on_start(self):
        self.hacker_ip = get_random_ip()
        self.headers = {"X-Forwarded-For": self.hacker_ip}

    @task(2)
    def sql_injection(self):
        # Атака через URI
        payload = "' OR '1'='1"
        self.client.get(f"/api/v1/users?id={payload}", headers=self.headers, name="[Attack] SQLi")

    @task(2)
    def path_traversal(self):
        # Попытка прочитать системные файлы
        payload = "../../../../etc/passwd"
        self.client.get(f"/api/v1/download?file={payload}", headers=self.headers, name="[Attack] Path Traversal")

    @task(1)
    def xss_attack(self):
        # Обфусцированный пейлоад в URI
        payload = "%3Cscript%3Ealert%281%29%3C%2Fscript%3E"
        self.client.get(f"/api/v1/search?q={payload}", headers=self.headers, name="[Attack] XSS")