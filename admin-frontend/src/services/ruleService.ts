import api from './authService';

/* ===== Enums ===== */

export type Action = 'BLOCK' | 'ALLOW' | 'LOG';
export type Target = 'IP' | 'URI' | 'HEADER' | 'METHOD';
export type Operator = 'EQUALS' | 'CONTAINS' | 'REGEX';

/* ===== Domain Objects ===== */

export interface Condition {
  target: Target;
  /** Only populated when target === 'HEADER' */
  target_key?: string | null;
  operator: Operator;
  value: string;
}

/* ===== DTOs ===== */

export interface RuleResponseDto {
  id: number;
  name: string;
  is_active: boolean;
  action: Action;
  conditions: Condition[];
}

export interface CreateRuleDto {
  name: string;
  action: Action;
  conditions: Condition[];
  is_active: boolean;
}

export interface UpdateRuleDto {
  name?: string;
  action?: Action;
  conditions?: Condition[];
}

export interface Page<T> {
  content: T[];
  total_elements: number;
  total_pages: number;
  number: number; // current page (0-indexed)
  size: number;
}

/* ===== API Calls ===== */

const BASE = '/v1/rules';

/** GET /api/v1/rules?page=&size= */
export const getRules = (page = 0, size = 10): Promise<Page<RuleResponseDto>> =>
  api.get<Page<RuleResponseDto>>(BASE, { params: { page, size } }).then((r) => r.data);

/** GET /api/v1/rules/:id */
export const getRuleById = (id: number): Promise<RuleResponseDto> =>
  api.get<RuleResponseDto>(`${BASE}/${id}`).then((r) => r.data);

/** POST /api/v1/rules */
export const createRule = (dto: CreateRuleDto): Promise<RuleResponseDto> =>
  api.post<RuleResponseDto>(BASE, dto).then((r) => r.data);

/** PATCH /api/v1/rules/:id */
export const updateRule = (id: number, dto: UpdateRuleDto): Promise<RuleResponseDto> =>
  api.patch<RuleResponseDto>(`${BASE}/${id}`, dto).then((r) => r.data);

/** DELETE /api/v1/rules/:id */
export const deleteRule = (id: number): Promise<void> =>
  api.delete(`${BASE}/${id}`).then(() => undefined);

/** POST /api/v1/rules/:id/enable */
export const enableRule = (id: number): Promise<RuleResponseDto> =>
  api.post<RuleResponseDto>(`${BASE}/${id}/enable`).then((r) => r.data);

/** POST /api/v1/rules/:id/disable */
export const disableRule = (id: number): Promise<RuleResponseDto> =>
  api.post<RuleResponseDto>(`${BASE}/${id}/disable`).then((r) => r.data);

/** POST /api/v1/rules/seed-defaults */
export const seedDefaultRules = (): Promise<{ message: string }> =>
  api.post<{ message: string }>(`${BASE}/seed-defaults`).then((r) => r.data);
