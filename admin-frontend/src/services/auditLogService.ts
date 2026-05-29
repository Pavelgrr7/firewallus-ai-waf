import api from './authService';
import type { Page } from './ruleService';

export type AuditAction = 'CREATE_RULE' | 'UPDATE_RULE' | 'DELETE_RULE' | 'ENABLE_RULE' | 'DISABLE_RULE' | 'LOGIN';

export interface AuditLogResponseDto {
  id: string;
  admin_id: string | null;
  action: AuditAction;
  rule_id: number | null;
  rule_name: string;
  timestamp: string | null;
}

const BASE = '/v1/audit-logs';

/** GET /api/v1/audit-logs?page=&size=&search= */
export const getAuditLogs = (page = 0, size = 20, search?: string): Promise<Page<AuditLogResponseDto>> =>
  api.get<Page<AuditLogResponseDto>>(BASE, { params: { page, size, search } }).then((r) => r.data);
