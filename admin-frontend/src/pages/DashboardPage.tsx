import React, { useEffect, useRef, useState } from 'react';
import {
  Activity,
  ShieldOff,
  BookOpen,
  Zap,
  TrendingUp,
  TrendingDown,
  Wifi,
  WifiOff,
} from 'lucide-react';
import {
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  BarChart,
  Bar,
  Legend,
} from 'recharts';

import Header from '../components/layout/Header';
import Card from '../components/ui/Card';
import {
  getIncidents,
  connectIncidentStream,
  type IncidentResponseDto,
} from '../services/incidentService';
import { getRules } from '../services/ruleService';

/* ===================================================================
   Helper utilities
=================================================================== */

const formatNumber = (n: number): string =>
  n >= 1_000_000
    ? `${(n / 1_000_000).toFixed(1)}M`
    : n >= 1_000
    ? `${(n / 1_000).toFixed(n >= 10_000 ? 0 : 1)}K`
    : n.toString();



export interface TimelineDataPoint {
  time: string;
  count: number;
  ml: number;
  static: number;
}

const TIMELINE_POINTS = 30;

/**
 * Group incidents by local-timezone **minute** bucket (HH:mm).
 * Returns fixed number of points (TIMELINE_POINTS) ending at current minute.
 */
const initTimelineData = (
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
 * Adds an incident to the timeline, creating or updating the bucket.
 */
const addIncidentToTimeline = (
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

/** Count occurrences of a string field */
const countBy = <K extends string>(
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

/** Top N entries by count */
const topN = <T extends { value: number }>(arr: T[], n: number): T[] =>
  arr.slice(0, n);

/* ===================================================================
   Chart colour palettes
=================================================================== */

const PIE_COLORS = ['#f43f5e', '#f59e0b', '#a855f7', '#06b6d4', '#3b82f6', '#10b981'];
const ACTION_COLOR: Record<string, string> = {
  BLOCK: '#f43f5e',
  LOG: '#f59e0b',
  ALLOW: '#10b981',
};
const DEFAULT_COLOR = '#7a92b5';

/* ===================================================================
   Sub-components
=================================================================== */

interface StatCardProps {
  title: string;
  value: string;
  subtitle?: string;
  icon: React.ReactNode;
  trend?: { value: string; positive: boolean };
  accentColor: string;
  delay: number;
  loading?: boolean;
}

const StatCard: React.FC<StatCardProps> = ({
  title,
  value,
  subtitle,
  icon,
  trend,
  accentColor,
  delay,
  loading,
}) => (
  <Card className="animate-slide-up" glow>
    <div style={{ animationDelay: `${delay}ms` }} className="animate-slide-up">
      <div className="flex items-start justify-between mb-4">
        <div
          className="w-11 h-11 rounded-xl flex items-center justify-center"
          style={{ backgroundColor: `${accentColor}15` }}
        >
          <span style={{ color: accentColor }}>{icon}</span>
        </div>
        {trend && (
          <span
            className={`flex items-center gap-1 text-xs font-semibold px-2 py-1 rounded-full ${
              trend.positive
                ? 'bg-emerald-500/10 text-emerald-400'
                : 'bg-rose-500/10 text-rose-400'
            }`}
          >
            {trend.positive ? <TrendingUp size={12} /> : <TrendingDown size={12} />}
            {trend.value}
          </span>
        )}
      </div>
      <p className="text-2xl font-bold text-white tracking-tight">
        {loading ? (
          <span className="inline-block w-20 h-7 rounded bg-cyber-700/50 animate-pulse" />
        ) : (
          value
        )}
      </p>
      <p className="text-sm text-cyber-300 mt-1">{title}</p>
      {subtitle && <p className="text-xs text-cyber-500 mt-0.5">{subtitle}</p>}
    </div>
  </Card>
);

/* ===== Custom Tooltip ===== */
const ChartTooltip = ({
  active,
  payload,
  label,
}: {
  active?: boolean;
  payload?: Array<{ name: string; value: number; color: string }>;
  label?: string;
}) => {
  if (!active || !payload?.length) return null;
  return (
    <div className="glass-card !p-3 !rounded-lg border border-cyber-500/30 shadow-xl">
      <p className="text-xs font-semibold text-cyber-200 mb-1.5">{label}</p>
      {payload.map((entry, i) => (
        <p key={i} className="text-xs" style={{ color: entry.color }}>
          {entry.name}: <span className="font-bold">{entry.value.toLocaleString()}</span>
        </p>
      ))}
    </div>
  );
};

/* ===== SSE Status Badge ===== */
const StreamBadge: React.FC<{ connected: boolean }> = ({ connected }) => (
  <span
    className={`flex items-center gap-1.5 text-xs font-semibold px-2.5 py-1 rounded-full ${
      connected
        ? 'bg-emerald-500/10 text-emerald-400'
        : 'bg-rose-500/10 text-rose-400'
    }`}
  >
    {connected ? <Wifi size={12} /> : <WifiOff size={12} />}
    {connected ? 'Live' : 'Reconnecting…'}
  </span>
);

/* ===================================================================
   Dashboard Page
=================================================================== */

const DashboardPage: React.FC = () => {
  const [incidents, setIncidents] = useState<IncidentResponseDto[]>([]);
  const [activeRules, setActiveRules] = useState<number>(0);
  const [timelineData, setTimelineData] = useState<TimelineDataPoint[]>([]);
  const [loading, setLoading] = useState(true);
  const [sseConnected, setSseConnected] = useState(false);
  const [totalBlockedRequests, setTotalBlockedRequests] = useState<number>(0);

  // Keep a stable ref to the AbortController so cleanup always cancels the
  // current connection even if React renders multiple times before unmount.
  const sseAbortRef = useRef<AbortController | null>(null);

  /* ── Initial data fetch ── */
  useEffect(() => {
    let cancelled = false;

    const load = async () => {
      try {
        const [incPage, rulesPage] = await Promise.all([
          getIncidents(0, 200),
          getRules(0, 200),
        ]);

        if (cancelled) return;

        setIncidents(incPage.content);
        setTotalBlockedRequests(incPage.total_elements ?? 0);
        setActiveRules(rulesPage.content.filter((r) => r.is_active).length);
        setTimelineData(initTimelineData(incPage.content, TIMELINE_POINTS));
      } catch (err) {
        console.error('[Dashboard] initial fetch failed', err);
      } finally {
        if (!cancelled) setLoading(false);
      }
    };

    load();
    return () => {
      cancelled = true;
    };
  }, []);

  /* ── SSE stream ── */
  useEffect(() => {
    // Open the stream.
    const ctrl = connectIncidentStream({
      onIncident: (newIncident) => {
        setSseConnected(true);
        // Prepend the new incident so the dashboard updates in real-time.
        setIncidents((prev) => [newIncident, ...prev]);
        setTotalBlockedRequests((prev) => prev + 1);
        setTimelineData((prev) => addIncidentToTimeline(prev, newIncident));
      },
      onError: () => {
        setSseConnected(false);
      },
    });

    sseAbortRef.current = ctrl;

    // Mark as connected optimistically; errors will flip it back.
    setSseConnected(true);

    /**
     * CLEANUP — called when the component unmounts (or if the effect re-runs).
     * Aborting the controller closes the underlying fetch/SSE connection and
     * prevents memory leaks / phantom state updates after unmount.
     */
    return () => {
      ctrl.abort();
      sseAbortRef.current = null;
      setSseConnected(false);
    };
  }, []); // Empty deps → runs once on mount, cleans up on unmount.

  /* ── Chart real-time progression ticking ── */
  useEffect(() => {
    const interval = setInterval(() => {
      setTimelineData((prev) => {
        if (prev.length === 0) return prev;
        const now = new Date();
        const key =
          `${String(now.getHours()).padStart(2, '0')}:` +
          `${String(now.getMinutes()).padStart(2, '0')}`;

        // Check if the current minute is already the latest point in our timeline
        const latestPoint = prev[prev.length - 1];
        if (latestPoint && latestPoint.time === key) {
          return prev;
        }

        // Otherwise, append a new minute point with 0 count
        const newPoint: TimelineDataPoint = {
          time: key,
          count: 0,
          ml: 0,
          static: 0,
        };

        const nextList = [...prev, newPoint];
        if (nextList.length > TIMELINE_POINTS) {
          nextList.shift();
        }
        return nextList;
      });
    }, 10000); // Check every 10 seconds to keep chart scrolling live

    return () => clearInterval(interval);
  }, []);

  // Bug 1 fix: group by snake_case field incident_type
  const attackTypeData = topN(
    countBy(incidents, 'incident_type').map((d, i) => ({
      ...d,
      color: PIE_COLORS[i % PIE_COLORS.length],
    })),
    6
  );

  // Bug 2 fix: group by snake_case field attacker_ip
  const blockedIPsData = countBy(incidents, 'attacker_ip')
    .map((d) => ({ ip: d.name, count: d.value }))
    .slice(0, 7);

  // Bug 2 fix: group by snake_case field action_taken
  const actionData = countBy(incidents, 'action_taken').map((d) => ({
    action: d.name,
    count: d.value,
    fill: ACTION_COLOR[d.name] ?? DEFAULT_COLOR,
  }));

  // Bug 3 fix: guard against undefined — only count as ML when field is
  // explicitly a number (not null and not undefined/missing)
  const mlCount = incidents.filter(
    (i) => i.confidence_score !== null && i.confidence_score !== undefined
  ).length;
  const staticCount = incidents.filter(
    (i) => i.confidence_score === null || i.confidence_score === undefined
  ).length;

  /* ── Render ── */
  return (
    <div className="min-h-screen">
      <Header title="Dashboard" />

      <div className="p-8 space-y-8">
        {/* ── Section A: Counter Cards ── */}
        <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-6">
          <StatCard
            title="Blocked Requests"
            value={formatNumber(totalBlockedRequests ?? 0)}
            subtitle="Total blocked requests"
            icon={<ShieldOff size={22} />}
            accentColor="#f43f5e"
            delay={0}
            loading={loading}
          />
          <StatCard
            title="Active Rules"
            value={activeRules.toString()}
            subtitle="Currently enforced"
            icon={<BookOpen size={22} />}
            accentColor="#a855f7"
            delay={100}
            loading={loading}
          />
          <StatCard
            title="ML Model Detections"
            value={formatNumber(mlCount)}
            subtitle={`Static rules: ${formatNumber(staticCount)}`}
            icon={<Activity size={22} />}
            accentColor="#3b82f6"
            delay={200}
            loading={loading}
          />
          <StatCard
            title="Redis Rate (Lua)"
            value="—"
            subtitle="Metric not yet available"
            icon={<Zap size={22} />}
            accentColor="#06b6d4"
            delay={300}
          />
        </div>

        {/* ── Section B: Charts ── */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">

          {/* 1. Incidents Timeline — Area Chart (full width) */}
          <Card className="animate-slide-up col-span-1 lg:col-span-2">
            <div className="flex items-center justify-between mb-1">
              <h3 className="text-base font-semibold text-white">
                Incidents Timeline
              </h3>
              <StreamBadge connected={sseConnected} />
            </div>
            <p className="text-xs text-cyber-400 mb-6">
              Incidents grouped by hour — updates live via SSE
            </p>
            {timelineData.length === 0 && !loading ? (
              <p className="text-cyber-400 text-sm text-center py-16">
                No incidents with timestamps yet.
              </p>
            ) : (
              <ResponsiveContainer width="100%" height={320}>
                <AreaChart data={timelineData}>
                  <defs>
                    <linearGradient id="incidentGrad" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#f43f5e" stopOpacity={0.25} />
                      <stop offset="95%" stopColor="#f43f5e" stopOpacity={0} />
                    </linearGradient>
                  </defs>
                  <CartesianGrid strokeDasharray="3 3" stroke="#1c2748" />
                  <XAxis
                    dataKey="time"
                    tick={{ fill: '#7a92b5', fontSize: 11 }}
                    axisLine={{ stroke: '#243158' }}
                    tickLine={false}
                  />
                  <YAxis
                    tick={{ fill: '#7a92b5', fontSize: 11 }}
                    axisLine={{ stroke: '#243158' }}
                    tickLine={false}
                    allowDecimals={false}
                  />
                  <Tooltip content={<ChartTooltip />} />
                  <Area
                    type="monotone"
                    dataKey="count"
                    name="Incidents"
                    stroke="#f43f5e"
                    strokeWidth={2.5}
                    fill="url(#incidentGrad)"
                    dot={false}
                    activeDot={{ r: 5, fill: '#f43f5e', stroke: '#0f1629', strokeWidth: 2 }}
                  />
                </AreaChart>
              </ResponsiveContainer>
            )}
          </Card>

          {/* 2. Top Attack Types — Pie Chart */}
          <Card className="animate-slide-up">
            <h3 className="text-base font-semibold text-white mb-1">
              Top Attack Types
            </h3>
            <p className="text-xs text-cyber-400 mb-4">
              Grouped by <code className="text-cyber-300">incident_type</code>
            </p>
            {attackTypeData.length === 0 && !loading ? (
              <p className="text-cyber-400 text-sm text-center py-16">No data.</p>
            ) : (
              <ResponsiveContainer width="100%" height={280}>
                <PieChart>
                  <Pie
                    data={attackTypeData}
                    cx="50%"
                    cy="50%"
                    innerRadius={65}
                    outerRadius={100}
                    paddingAngle={4}
                    dataKey="value"
                    strokeWidth={0}
                  >
                    {attackTypeData.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={entry.color} />
                    ))}
                  </Pie>
                  <Tooltip
                    contentStyle={{
                      background: '#0f1629',
                      border: '1px solid rgba(255,255,255,0.1)',
                      borderRadius: 8,
                      fontSize: 12,
                      color: '#e0e8f0',
                    }}
                    formatter={(value) => [`${value}`, 'Count']}
                  />
                  <Legend
                    formatter={(value: string) => (
                      <span className="text-xs text-cyber-200">{value}</span>
                    )}
                  />
                </PieChart>
              </ResponsiveContainer>
            )}
          </Card>

          {/* 3. Top Blocked IPs — Bar Chart */}
          <Card className="animate-slide-up">
            <h3 className="text-base font-semibold text-white mb-1">
              Top Blocked IPs
            </h3>
            <p className="text-xs text-cyber-400 mb-4">
              Top 7 sources by incident count
            </p>
            {blockedIPsData.length === 0 && !loading ? (
              <p className="text-cyber-400 text-sm text-center py-16">No data.</p>
            ) : (
              <ResponsiveContainer width="100%" height={280}>
                <BarChart data={blockedIPsData} layout="vertical" margin={{ left: 20 }}>
                  <CartesianGrid
                    strokeDasharray="3 3"
                    stroke="#1c2748"
                    horizontal={false}
                  />
                  <XAxis
                    type="number"
                    tick={{ fill: '#7a92b5', fontSize: 11 }}
                    axisLine={{ stroke: '#243158' }}
                    tickLine={false}
                    allowDecimals={false}
                  />
                  <YAxis
                    dataKey="ip"
                    type="category"
                    tick={{ fill: '#b0c4de', fontSize: 11 }}
                    axisLine={{ stroke: '#243158' }}
                    tickLine={false}
                    width={110}
                  />
                  <Tooltip content={<ChartTooltip />} />
                  <Bar
                    dataKey="count"
                    name="Incidents"
                    fill="#a855f7"
                    radius={[0, 6, 6, 0]}
                    barSize={20}
                  />
                </BarChart>
              </ResponsiveContainer>
            )}
          </Card>

          {/* 4. Action Distribution — Bar Chart (full width) */}
          <Card className="animate-slide-up col-span-1 lg:col-span-2">
            <h3 className="text-base font-semibold text-white mb-1">
              Action Distribution
            </h3>
            <p className="text-xs text-cyber-400 mb-4">
              Incidents grouped by <code className="text-cyber-300">action_taken</code>
            </p>
            {actionData.length === 0 && !loading ? (
              <p className="text-cyber-400 text-sm text-center py-16">No data.</p>
            ) : (
              <ResponsiveContainer width="100%" height={220}>
                <BarChart data={actionData} margin={{ left: 0 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#1c2748" />
                  <XAxis
                    dataKey="action"
                    tick={{ fill: '#b0c4de', fontSize: 12 }}
                    axisLine={{ stroke: '#243158' }}
                    tickLine={false}
                  />
                  <YAxis
                    tick={{ fill: '#7a92b5', fontSize: 11 }}
                    axisLine={{ stroke: '#243158' }}
                    tickLine={false}
                    allowDecimals={false}
                  />
                  <Tooltip content={<ChartTooltip />} />
                  <Bar
                    dataKey="count"
                    name="Incidents"
                    radius={[6, 6, 0, 0]}
                    barSize={48}
                    isAnimationActive
                  >
                    {actionData.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={entry.fill} />
                    ))}
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            )}
          </Card>

        </div>
      </div>
    </div>
  );
};

export default DashboardPage;
