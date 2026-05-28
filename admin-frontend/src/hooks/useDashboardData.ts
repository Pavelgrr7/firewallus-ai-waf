import { useState, useEffect } from 'react';
import { getIncidents, connectIncidentStream, type IncidentResponseDto } from '../services/incidentService';
import {
  initTimelineData,
  addIncidentToTimeline,
  type TimelineDataPoint,
} from '../utils/dashboardHelpers';

/**
 * Hook to load initial WAF incidents and handle real-time SSE events.
 */
export function useDashboardData() {
  const [incidents, setIncidents] = useState<IncidentResponseDto[]>([]);
  const [timeline, setTimeline] = useState<TimelineDataPoint[]>([]);
  const [stats, setStats] = useState({
    total: 0,
    mlBlocked: 0,
    staticBlocked: 0,
    allowed: 0,
  });

  const [sseConnected, setSseConnected] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadData = async () => {
      try {
        const pageData = await getIncidents(0, 1000); // load last 1000 items
        const list = pageData.content ?? [];
        setIncidents(list);

        const total = pageData.total_elements ?? list.length;
        const mlBlocked = list.filter(
          (x) =>
            x.action_taken === 'BLOCK' &&
            x.confidence_score !== null &&
            x.confidence_score !== undefined
        ).length;
        const staticBlocked = list.filter(
          (x) =>
            x.action_taken === 'BLOCK' &&
            (x.confidence_score === null || x.confidence_score === undefined)
        ).length;
        const allowed = list.filter(
          (x) => x.action_taken === 'ALLOW' || x.action_taken === 'LOG'
        ).length;

        setStats({ total, mlBlocked, staticBlocked, allowed });
        setTimeline(initTimelineData(list));
      } catch (err) {
        console.error('Failed to load WAF incident history', err);
      } finally {
        if (loading) setLoading(false);
      }
    };
    loadData();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    if (loading) return;

    const controller = connectIncidentStream({
      onIncident: (newIncident) => {
        setIncidents((prev) => {
          const next = [newIncident, ...prev];
          return next.slice(0, 1000);
        });

        setStats((prev) => {
          const isBlock = newIncident.action_taken === 'BLOCK';
          const isMl =
            newIncident.confidence_score !== null &&
            newIncident.confidence_score !== undefined;

          return {
            total: prev.total + 1,
            mlBlocked: prev.mlBlocked + (isBlock && isMl ? 1 : 0),
            staticBlocked: prev.staticBlocked + (isBlock && !isMl ? 1 : 0),
            allowed: prev.allowed + (!isBlock ? 1 : 0),
          };
        });

        setTimeline((prev) => addIncidentToTimeline(prev, newIncident));
      },
      onOpen: () => {
        setSseConnected(true);
      },
      onClose: () => {
        setSseConnected(false);
      },
      onError: (err) => {
        console.error('SSE connection error:', err);
        setSseConnected(false);
      },
    });

    return () => {
      controller.abort();
    };
  }, [loading]);

  return {
    incidents,
    timeline,
    stats,
    sseConnected,
    loading,
  };
}
