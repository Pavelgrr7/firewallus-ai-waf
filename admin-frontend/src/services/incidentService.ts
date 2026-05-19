import api from './authService';
import { fetchEventSource } from '@microsoft/fetch-event-source';

/* ===== Types ===== */

export interface IncidentResponseDto {
  incident_type: string;
  attacker_ip: string;
  target_uri: string;
  action_taken: string;
  confidence_score: number | null | undefined;
  timestamp: string | null;
}

export interface SpringPage<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

/* ===== REST ===== */

const BASE = '/v1/incidents';

/** GET /api/v1/incidents?size=200&page=0 */
export const getIncidents = (
  page = 0,
  size = 200
): Promise<SpringPage<IncidentResponseDto>> =>
  api
    .get<SpringPage<IncidentResponseDto>>(BASE, { params: { page, size } })
    .then((r) => r.data);

/* ===== SSE ===== */

export interface SseOptions {
  onIncident: (incident: IncidentResponseDto) => void;
  onError?: (err: unknown) => void;
}

/**
 * Opens an authenticated SSE connection to /api/v1/incidents/stream.
 *
 * Uses @microsoft/fetch-event-source so we can pass the Authorization header
 * (native EventSource does NOT support custom headers).
 *
 * Returns an AbortController whose .abort() method you should call on cleanup
 * (e.g. in a useEffect return function) to prevent memory leaks.
 */
export const connectIncidentStream = (options: SseOptions): AbortController => {
  const { onIncident, onError } = options;
  const controller = new AbortController();
  const token = localStorage.getItem('token');

  const url = `${window.location.origin}/api${BASE}/stream`;

  fetchEventSource(url, {
    method: 'GET',
    headers: {
      Authorization: token ? `Bearer ${token}` : '',
      Accept: 'text/event-stream',
    },
    signal: controller.signal,
    openWhenHidden: true, // keep alive even when the tab is hidden

    onmessage(event) {
      if (event.event === 'new-incident') {
        try {
          const incident: IncidentResponseDto = JSON.parse(event.data);
          onIncident(incident);
        } catch {
          // ignore malformed frames
        }
      }
    },

    onerror(err) {
      onError?.(err);
      // Rethrowing makes fetch-event-source stop retrying.
      // We do NOT rethrow here so it will auto-reconnect on transient failures.
    },
  });

  return controller;
};
