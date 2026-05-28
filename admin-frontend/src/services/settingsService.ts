import api from './authService';

export interface SettingsResponseDto {
  rate_limit_requests: number;
  rate_limit_window_sec: number;
  tg_bot_token: string | null;
  tg_chat_id: string | null;
  alert_threshold: number;
}

export interface UpdateSettingsDto {
  rate_limit_requests?: number;
  rate_limit_window_sec?: number;
  tg_bot_token?: string | null;
  tg_chat_id?: string | null;
  alert_threshold?: number;
}

const BASE = '/v1/settings';

/** GET /api/v1/settings */
export const getSettings = (): Promise<SettingsResponseDto> =>
  api.get<SettingsResponseDto>(BASE).then((r) => r.data);

/** PATCH /api/v1/settings */
export const updateSettings = (dto: UpdateSettingsDto): Promise<SettingsResponseDto> =>
  api.patch<SettingsResponseDto>(BASE, dto).then((r) => r.data);
