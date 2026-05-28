import api from './authService';
import type { Page } from './ruleService';

export type IpListType = 'BLACKLIST' | 'WHITELIST';

export interface ManagedIpResponseDto {
  id: string;
  ip_address: string;
  description: string | null;
  timestamp: string | null;
  list_type: IpListType;
}

export interface CreateManagedIpDto {
  ip_address: string;
  list_type: IpListType;
  description?: string | null;
}

const BASE = '/v1/access-control';

/** GET /api/v1/access-control?page=&size=&listType= */
export const getManagedIps = (
  page = 0,
  size = 20,
  listType?: IpListType
): Promise<Page<ManagedIpResponseDto>> => {
  const params: Record<string, any> = { page, size };
  if (listType) {
    params.listType = listType;
  }
  return api.get<Page<ManagedIpResponseDto>>(BASE, { params }).then((r) => r.data);
};

/** POST /api/v1/access-control */
export const addManagedIp = (dto: CreateManagedIpDto): Promise<ManagedIpResponseDto> =>
  api.post<ManagedIpResponseDto>(BASE, dto).then((r) => r.data);

/** DELETE /api/v1/access-control/:id */
export const deleteManagedIp = (id: string): Promise<void> =>
  api.delete(`${BASE}/${id}`).then(() => undefined);
