import { useState, useEffect } from 'react';
import {
  getIncidents,
  getIncidentStats,
  connectIncidentStream,
  type IncidentResponseDto,
} from '../services/incidentService';
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
  const [attackDistribution, setAttackDistribution] = useState<{ name: string; value: number }[]>([]);
  const [topBlockedIps, setTopBlockedIps] = useState<{ name: string; value: number }[]>([]);
  const [actionMetrics, setActionMetrics] = useState<{ name: string; value: number }[]>([]);

  const [sseConnected, setSseConnected] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadData = async () => {
      try {
        const [pageData, statsData] = await Promise.all([
          getIncidents(0, 1000), // load last 1000 items
          getIncidentStats(),
        ]);
        const list = pageData.content ?? [];
        setIncidents(list);

        setStats({
          total: statsData.total,
          mlBlocked: statsData.ml_blocked,
          staticBlocked: statsData.static_blocked,
          allowed: statsData.allowed,
        });

        setAttackDistribution(statsData.attack_distribution ?? []);
        setTopBlockedIps(statsData.top_blocked_ips ?? []);
        setActionMetrics(statsData.action_metrics ?? []);

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

        setAttackDistribution((prev) => {
          const type = newIncident.incident_type;
          const idx = prev.findIndex((x) => x.name === type);
          if (idx !== -1) {
            return prev.map((x, i) => (i === idx ? { ...x, value: x.value + 1 } : x));
          } else {
            return [...prev, { name: type, value: 1 }];
          }
        });

        setActionMetrics((prev) => {
          const action = newIncident.action_taken;
          const idx = prev.findIndex((x) => x.name === action);
          if (idx !== -1) {
            return prev.map((x, i) => (i === idx ? { ...x, value: x.value + 1 } : x));
          } else {
            return [...prev, { name: action, value: 1 }];
          }
        });

        if (newIncident.action_taken === 'BLOCK') {
          setTopBlockedIps((prev) => {
            const ip = newIncident.attacker_ip;
            const idx = prev.findIndex((x) => x.name === ip);
            let nextList = [];
            if (idx !== -1) {
              nextList = prev.map((x, i) => (i === idx ? { ...x, value: x.value + 1 } : x));
            } else {
              nextList = [...prev, { name: ip, value: 1 }];
            }
            return nextList.sort((a, b) => b.value - a.value).slice(0, 5);
          });
        }

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
    attackDistribution,
    topBlockedIps,
    actionMetrics,
    sseConnected,
    loading,
  };
}
