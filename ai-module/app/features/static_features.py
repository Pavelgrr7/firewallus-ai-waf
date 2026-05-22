import math
from urllib.parse import parse_qs, urlparse

import numpy as np

from app.schemas.traffic import HttpMethod, TrafficEvent

SUSPICIOUS_EXTENSIONS = {".php", ".env", ".bak", ".sql", ".conf", ".ini", ".log", ".old"}

SPECIAL_CHARS = ["'", '"', "<", ">", ";", "|", "\\", "`", "$", "!", "{", "}", "(", ")"]
SPECIAL_PATTERNS = ["--", "/*", "*/", "../", "0x", "%00", "%27", "%3C", "%3E"]

METHOD_LIST = list(HttpMethod)


def _shannon_entropy(s: str) -> float:
    if not s:
        return 0.0
    freq = {}
    for c in s:
        freq[c] = freq.get(c, 0) + 1
    length = len(s)
    return -sum((count / length) * math.log2(count / length) for count in freq.values())


def extract_static_features(event: TrafficEvent) -> np.ndarray:
    parsed = urlparse(event.uri)
    path = parsed.path
    query = parsed.query
    full_uri = event.uri

    features = []

    features.append(len(full_uri))
    features.append(path.count("/"))
    params = parse_qs(query)
    features.append(len(params))
    total_param_len = sum(len(v) for vals in params.values() for v in vals)
    features.append(total_param_len)

    for ch in SPECIAL_CHARS:
        features.append(full_uri.count(ch))
    for pat in SPECIAL_PATTERNS:
        features.append(full_uri.lower().count(pat))

    features.append(_shannon_entropy(full_uri))
    features.append(_shannon_entropy(path))
    features.append(_shannon_entropy(query) if query else 0.0)

    ext = path.rsplit(".", 1)[-1] if "." in path else ""
    features.append(1.0 if f".{ext}" in SUSPICIOUS_EXTENSIONS else 0.0)

    features.append(1.0 if any(c.isdigit() for c in path.split("/")[-1]) else 0.0)

    method_onehot = [1.0 if m == event.method else 0.0 for m in METHOD_LIST]
    features.extend(method_onehot)

    return np.array(features, dtype=np.float32)
