import type { IncidentResponseDto } from '../services/incidentService';

export interface TimelineDataPoint {
  time: string;
  count: number;
  ml: number;
  static: number;
}

export const TIMELINE_POINTS = 30;

/**
 * Formats numbers into human-readable strings (e.g. 1.2M, 15K, 450).
 */
export const formatNumber = (n: number): string =>
  n >= 1_000_000
    ? `${(n / 1_000_000).toFixed(1)}M`
    : n >= 1_000
    ? `${(n / 1_000).toFixed(n >= 10_000 ? 0 : 1)}K`
    : n.toString();

/**
 * Group incidents by local-timezone minute bucket (HH:mm).
 * Returns fixed number of points (TIMELINE_POINTS) ending at current minute.
 */
export const initTimelineData = (
  incidents: IncidentResponseDto[],
  pointCount = TIMELINE_POINTS
): TimelineDataPoint[] => {
  const data: TimelineDataPoint[] = [];
  const now = new Date();

  for (let i = pointCount - 1; i >= 0; i--) {
    const d = new Date(now.getTime() - i * 60 * 1000);
    const key =
      `${String(d.getHours()).padStart(2, '0')}:` +
      `${String(d.getMinutes()).padStart(2, '0')}`;

    const matches = incidents.filter((inc) => {
      if (!inc.timestamp) return false;
      const incDate = new Date(inc.timestamp);
      const incKey =
        `${String(incDate.getHours()).padStart(2, '0')}:` +
        `${String(incDate.getMinutes()).padStart(2, '0')}`;
      return incKey === key;
    });

    const ml = matches.filter(
      (inc) => inc.confidence_score !== null && inc.confidence_score !== undefined
    ).length;
    const staticD = matches.length - ml;

    data.push({
      time: key,
      count: matches.length,
      ml,
      static: staticD,
    });
  }

  return data;
};

/**
 * Adds a new incident to the timeline, updating the existing minute bucket or creating a new one.
 */
export const addIncidentToTimeline = (
  prev: TimelineDataPoint[],
  inc: IncidentResponseDto
): TimelineDataPoint[] => {
  if (!inc.timestamp) return prev;
  const d = new Date(inc.timestamp);
  const key =
    `${String(d.getHours()).padStart(2, '0')}:` +
    `${String(d.getMinutes()).padStart(2, '0')}`;

  const isMl = inc.confidence_score !== null && inc.confidence_score !== undefined;

  const idx = prev.findIndex((pt) => pt.time === key);
  if (idx !== -1) {
    return prev.map((pt, i) => {
      if (i === idx) {
        return {
          ...pt,
          count: pt.count + 1,
          ml: pt.ml + (isMl ? 1 : 0),
          static: pt.static + (isMl ? 0 : 1),
        };
      }
      return pt;
    });
  } else {
    const newPoint: TimelineDataPoint = {
      time: key,
      count: 1,
      ml: isMl ? 1 : 0,
      static: isMl ? 0 : 1,
    };
    const nextList = [...prev, newPoint];
    if (nextList.length > TIMELINE_POINTS) {
      nextList.shift();
    }
    return nextList;
  }
};

/**
 * Counts occurrences of a specific key field inside incident items.
 */
export const countBy = <K extends string>(
  incidents: IncidentResponseDto[],
  field: keyof IncidentResponseDto
): { name: K; value: number }[] => {
  const map: Record<string, number> = {};
  for (const inc of incidents) {
    const key = String(inc[field] ?? 'Unknown');
    map[key] = (map[key] ?? 0) + 1;
  }
  return Object.entries(map)
    .sort(([, a], [, b]) => b - a)
    .map(([name, value]) => ({ name: name as K, value }));
};

/**
 * Returns top N elements from array.
 */
export const topN = <T extends { value: number }>(arr: T[], n: number): T[] =>
  arr.slice(0, n);
