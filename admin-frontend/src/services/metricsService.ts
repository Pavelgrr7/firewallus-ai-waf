import api from './authService';

export interface RedisRateResponse {
  rate: string;
}

const BASE = '/v1/metrics';

/**
 * GET /api/v1/metrics/redis-rate
 * Returns the instantaneous operations per second from Redis stats.
 */
export const getRedisRate = (): Promise<RedisRateResponse> =>
  api.get<RedisRateResponse>(`${BASE}/redis-rate`).then((r) => r.data);
