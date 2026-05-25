from enum import Enum

from pydantic import BaseModel


class HttpMethod(str, Enum):
    GET = "GET"
    POST = "POST"
    PUT = "PUT"
    PATCH = "PATCH"
    DELETE = "DELETE"
    OPTIONS = "OPTIONS"
    HEAD = "HEAD"
    TRACE = "TRACE"
    CONNECT = "CONNECT"
    UNKNOWN = "UNKNOWN"


class TrafficEvent(BaseModel):
    ip: str
    method: HttpMethod
    uri: str
