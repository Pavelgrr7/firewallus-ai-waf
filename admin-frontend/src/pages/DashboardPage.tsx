import React, { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import {
  Activity,
  AlertOctagon,
  ArrowRight,
  Shield,
  ShieldAlert,
  ShieldCheck,
  Zap,
} from 'lucide-react';
import {
  Area,
  AreaChart,
  Bar,
  BarChart,
  Cell,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';
import Header from '../components/layout/Header';
import Card from '../components/ui/Card';
import { useDashboardData } from '../hooks/useDashboardData';
import { formatNumber } from '../utils/dashboardHelpers';
import { getRedisRate } from '../services/metricsService';

const DashboardPage: React.FC = () => {
  const { t } = useTranslation();
  const {
    incidents,
    timeline,
    stats,
    attackDistribution,
    topBlockedIps,
    actionMetrics,
    sseConnected,
    loading,
  } = useDashboardData();

  const [redisRate, setRedisRate] = useState<string | null>(null);

  useEffect(() => {
    const fetchRate = async () => {
      try {
        const data = await getRedisRate();
        setRedisRate(data.rate);
      } catch (err) {
        console.error('Failed to fetch Redis rate', err);
      }
    };

    fetchRate();
    const interval = setInterval(fetchRate, 3000);

    return () => {
      clearInterval(interval);
    };
  }, []);

  // Stats are aggregated in real-time on the backend
  const attackTypes = attackDistribution;
  const topIps = topBlockedIps;
  const actionCounts = actionMetrics;

  // Chart colors
  const PIE_COLORS = ['#f43f5e', '#a855f7', '#06b6d4', '#f59e0b', '#3b82f6', '#10b981'];

  return (
    <div className="min-h-screen">
      <Header title={t('dashboard.title')} />

      <div className="p-6 space-y-6">
        {/* Status bar */}
        <div className="flex items-center justify-between animate-fade-in bg-cyber-800/40 border border-glass-border px-4 py-3 rounded-xl">
          <div className="flex items-center gap-2">
            <Activity size={16} className="text-accent-blue" />
            <span className="text-sm font-semibold text-white">{t('header.system_status')}</span>
          </div>
          <div className="flex items-center gap-2">
            <span className="text-xs text-cyber-300">{t('header.live_feed')}</span>
            <div className="flex items-center gap-1.5 bg-cyber-900/60 px-2.5 py-1 rounded-lg border border-glass-border">
              <span
                className={`w-2 h-2 rounded-full ${
                  sseConnected ? 'bg-accent-emerald animate-pulse' : 'bg-accent-rose'
                }`}
              />
              <span className="text-[10px] font-mono font-bold text-white uppercase">
                {sseConnected ? t('header.connected') : t('header.disconnected')}
              </span>
            </div>
          </div>
        </div>

        {/* Stats Grid */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 animate-slide-up">
          <StatCard
            title={t('dashboard.stats.total_requests')}
            value={formatNumber(stats.total)}
            subtitle={t('dashboard.stats.total_requests_sub')}
            icon={<Shield size={22} />}
            accentColor="#3b82f6"
            delay={0}
          />
          <StatCard
            title={t('dashboard.stats.ml_blocked')}
            value={formatNumber(stats.mlBlocked)}
            subtitle={t('dashboard.stats.ml_blocked_sub')}
            icon={<ShieldAlert size={22} />}
            accentColor="#a855f7"
            delay={100}
          />
          <StatCard
            title={t('dashboard.stats.static_blocked')}
            value={formatNumber(stats.staticBlocked)}
            subtitle={t('dashboard.stats.static_blocked_sub')}
            icon={<AlertOctagon size={22} />}
            accentColor="#f43f5e"
            delay={200}
          />
          <StatCard
            title={t('dashboard.stats.redis_rate')}
            value={redisRate !== null ? `${redisRate} ops/sec` : '—'}
            subtitle={t('dashboard.stats.redis_rate_sub')}
            icon={<Zap size={22} />}
            accentColor="#06b6d4"
            delay={300}
          />
        </div>

        {/* Incident timeline graph */}
        <Card className="animate-slide-up [animation-delay:150ms] p-5">
          <div className="flex items-center justify-between mb-6">
            <div>
              <h3 className="text-sm font-bold text-white uppercase tracking-wider">
                {t('dashboard.timeline.title')}
              </h3>
              <p className="text-xs text-cyber-400">{t('dashboard.timeline.subtitle')}</p>
            </div>
            <div className="flex items-center gap-4 text-xs font-mono">
              <div className="flex items-center gap-1.5">
                <span className="w-2.5 h-1.5 rounded-full bg-accent-purple" />
                <span className="text-cyber-300">{t('dashboard.timeline.ai_engine')}</span>
              </div>
              <div className="flex items-center gap-1.5">
                <span className="w-2.5 h-1.5 rounded-full bg-accent-rose" />
                <span className="text-cyber-300">{t('dashboard.timeline.signatures')}</span>
              </div>
            </div>
          </div>
          <div className="h-64">
            {loading ? (
              <div className="w-full h-full flex items-center justify-center text-cyber-400">
                {t('dashboard.timeline.loading')}
              </div>
            ) : (
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart data={timeline} margin={{ top: 0, right: 0, left: -20, bottom: 0 }}>
                  <defs>
                    <linearGradient id="colorMl" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#a855f7" stopOpacity={0.2} />
                      <stop offset="95%" stopColor="#a855f7" stopOpacity={0} />
                    </linearGradient>
                    <linearGradient id="colorStatic" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#f43f5e" stopOpacity={0.2} />
                      <stop offset="95%" stopColor="#f43f5e" stopOpacity={0} />
                    </linearGradient>
                  </defs>
                  <XAxis
                    dataKey="time"
                    stroke="#4a6085"
                    fontSize={10}
                    tickLine={false}
                    axisLine={false}
                  />
                  <YAxis
                    stroke="#4a6085"
                    fontSize={10}
                    tickLine={false}
                    axisLine={false}
                    allowDecimals={false}
                  />
                  <Tooltip content={<ChartTooltip />} />
                  <Area
                    type="monotone"
                    dataKey="ml"
                    name={t('dashboard.timeline.ai_engine')}
                    stroke="#a855f7"
                    strokeWidth={2}
                    fillOpacity={1}
                    fill="url(#colorMl)"
                  />
                  <Area
                    type="monotone"
                    dataKey="static"
                    name={t('dashboard.timeline.signatures')}
                    stroke="#f43f5e"
                    strokeWidth={2}
                    fillOpacity={1}
                    fill="url(#colorStatic)"
                  />
                </AreaChart>
              </ResponsiveContainer>
            )}
          </div>
        </Card>

        {/* Grid charts */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Attack Types Distribution */}
          <Card className="animate-slide-up [animation-delay:200ms] p-5">
            <h3 className="text-sm font-bold text-white uppercase tracking-wider mb-5">
              {t('dashboard.distribution.title')}
            </h3>
            <div className="h-56 flex items-center justify-center">
              {loading ? (
                <div className="text-cyber-400">{t('dashboard.distribution.loading')}</div>
              ) : attackTypes.length === 0 ? (
                <div className="text-xs text-cyber-400">{t('dashboard.distribution.no_data')}</div>
              ) : (
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart>
                    <Pie
                      data={attackTypes}
                      cx="50%"
                      cy="50%"
                      innerRadius={55}
                      outerRadius={75}
                      paddingAngle={3}
                      dataKey="value"
                    >
                      {attackTypes.map((_, index) => (
                        <Cell
                          key={`cell-${index}`}
                          fill={PIE_COLORS[index % PIE_COLORS.length]}
                          stroke="rgba(15, 22, 41, 0.8)"
                          strokeWidth={2}
                        />
                      ))}
                    </Pie>
                    <Tooltip content={<ChartTooltip />} />
                  </PieChart>
                </ResponsiveContainer>
              )}
            </div>
            {/* Legend */}
            <div className="grid grid-cols-2 gap-2 mt-4 text-[11px] font-medium max-h-24 overflow-y-auto pr-1">
              {attackTypes.slice(0, 6).map((type, idx) => (
                <div key={type.name} className="flex items-center gap-1.5">
                  <span
                    className="w-2 h-2 rounded-full shrink-0"
                    style={{ backgroundColor: PIE_COLORS[idx % PIE_COLORS.length] }}
                  />
                  <span className="text-cyber-300 truncate">{type.name}</span>
                  <span className="text-white ml-auto font-semibold font-mono">{type.value}</span>
                </div>
              ))}
            </div>
          </Card>

          {/* Top Blocked IPs */}
          <Card className="animate-slide-up [animation-delay:250ms] p-5">
            <h3 className="text-sm font-bold text-white uppercase tracking-wider mb-5">
              {t('dashboard.top_ips.title')}
            </h3>
            <div className="h-56">
              {loading ? (
                <div className="w-full h-full flex items-center justify-center text-cyber-400">
                  {t('dashboard.top_ips.loading')}
                </div>
              ) : topIps.length === 0 ? (
                <div className="w-full h-full flex items-center justify-center text-xs text-cyber-400">
                  {t('dashboard.top_ips.no_data')}
                </div>
              ) : (
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={topIps} layout="vertical" margin={{ left: -10, right: 10 }}>
                    <XAxis type="number" stroke="#4a6085" fontSize={10} hide />
                    <YAxis
                      dataKey="name"
                      type="category"
                      stroke="#7a92b5"
                      fontSize={10}
                      tickLine={false}
                      axisLine={false}
                      width={90}
                    />
                    <Tooltip content={<ChartTooltip />} />
                    <Bar dataKey="value" name={t('dashboard.top_ips.blocks')} fill="#a855f7" radius={[0, 4, 4, 0]} barSize={12}>
                      {topIps.map((_, idx) => (
                        <Cell key={idx} fill="#a855f7" opacity={1 - idx * 0.15} />
                      ))}
                    </Bar>
                  </BarChart>
                </ResponsiveContainer>
              )}
            </div>
          </Card>

          {/* WAF Decision Actions distribution */}
          <Card className="animate-slide-up [animation-delay:300ms] p-5">
            <h3 className="text-sm font-bold text-white uppercase tracking-wider mb-5">
              {t('dashboard.actions_chart.title')}
            </h3>
            <div className="h-56">
              {loading ? (
                <div className="w-full h-full flex items-center justify-center text-cyber-400">
                  {t('dashboard.actions_chart.loading')}
                </div>
              ) : actionCounts.length === 0 ? (
                <div className="w-full h-full flex items-center justify-center text-xs text-cyber-400">
                  {t('dashboard.actions_chart.no_data')}
                </div>
              ) : (
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={actionCounts} margin={{ left: -20 }}>
                    <XAxis
                      dataKey="name"
                      stroke="#7a92b5"
                      fontSize={10}
                      tickLine={false}
                      axisLine={false}
                    />
                    <YAxis
                      stroke="#4a6085"
                      fontSize={10}
                      tickLine={false}
                      axisLine={false}
                      allowDecimals={false}
                    />
                    <Tooltip content={<ChartTooltip />} />
                    <Bar dataKey="value" name={t('dashboard.actions_chart.count')} radius={[4, 4, 0, 0]} barSize={25}>
                      {actionCounts.map((entry, index) => {
                        const color =
                          entry.name === 'BLOCK'
                            ? '#f43f5e'
                            : entry.name === 'ALLOW'
                            ? '#10b981'
                            : '#f59e0b';
                        return <Cell key={index} fill={color} />;
                      })}
                    </Bar>
                  </BarChart>
                </ResponsiveContainer>
              )}
            </div>
          </Card>
        </div>

        {/* Recent logs overview */}
        <Card className="animate-slide-up [animation-delay:350ms] p-5">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h3 className="text-sm font-bold text-white uppercase tracking-wider">
                {t('dashboard.incidents.title')}
              </h3>
              <p className="text-xs text-cyber-400">{t('dashboard.incidents.subtitle')}</p>
            </div>
            <a
              href="/audit-logs"
              className="flex items-center gap-1 text-xs text-accent-blue hover:text-accent-cyan hover:underline transition-all"
            >
              {t('dashboard.incidents.view_full')} <ArrowRight size={13} />
            </a>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-xs">
              <thead>
                <tr className="border-b border-glass-border">
                  <th className="text-left py-2 font-semibold text-cyber-300">{t('dashboard.incidents.cols.time')}</th>
                  <th className="text-left py-2 font-semibold text-cyber-300">{t('dashboard.incidents.cols.ip')}</th>
                  <th className="text-left py-2 font-semibold text-cyber-300">{t('dashboard.incidents.cols.uri')}</th>
                  <th className="text-left py-2 font-semibold text-cyber-300">{t('dashboard.incidents.cols.classification')}</th>
                  <th className="text-right py-2 font-semibold text-cyber-300">{t('dashboard.incidents.cols.result')}</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-glass-border/40">
                {loading ? (
                  <tr>
                    <td colSpan={5} className="text-center py-6 text-cyber-400">
                      {t('dashboard.incidents.loading')}
                    </td>
                  </tr>
                ) : incidents.length === 0 ? (
                  <tr>
                    <td colSpan={5} className="text-center py-6 text-cyber-400">
                      {t('dashboard.incidents.no_incidents')}
                    </td>
                  </tr>
                ) : (
                  incidents.slice(0, 5).map((inc, i) => {
                    const isBlock = inc.action_taken === 'BLOCK';
                    const isMl =
                      inc.confidence_score !== null && inc.confidence_score !== undefined;

                    return (
                      <tr key={i} className="hover:bg-cyber-700/10">
                        <td className="py-2.5 font-mono text-cyber-400">
                          {inc.timestamp ? new Date(inc.timestamp).toLocaleTimeString() : '—'}
                        </td>
                        <td className="py-2.5 font-mono font-medium text-white">
                          {inc.attacker_ip}
                        </td>
                        <td className="py-2.5 font-mono text-cyber-300 max-w-[200px] truncate">
                          {inc.target_uri}
                        </td>
                        <td className="py-2.5">
                          <span
                            className={`inline-flex items-center gap-1 px-2 py-0.5 rounded text-[10px] font-semibold border
                            ${
                              isMl
                                ? 'text-accent-purple bg-accent-purple/10 border-accent-purple/20'
                                : 'text-accent-rose bg-accent-rose/10 border-accent-rose/20'
                            }`}
                          >
                            {isMl && <ShieldCheck size={10} />}
                            {inc.incident_type}
                          </span>
                        </td>
                        <td className="py-2.5 text-right">
                          <span
                            className={`inline-flex items-center px-1.5 py-0.5 rounded text-[10px] font-bold border
                            ${
                              isBlock
                                ? 'text-accent-rose bg-accent-rose/10 border-accent-rose/30'
                                : 'text-accent-emerald bg-accent-emerald/10 border-accent-emerald/30'
                            }`}
                          >
                            {inc.action_taken}
                          </span>
                        </td>
                      </tr>
                    );
                  })
                )}
              </tbody>
            </table>
          </div>
        </Card>
      </div>
    </div>
  );
};

/* ===== Local Page-specific Layout Subcomponents ===== */

interface StatCardProps {
  title: string;
  value: string;
  subtitle: string;
  icon: React.ReactNode;
  accentColor: string;
  delay: number;
}

const StatCard: React.FC<StatCardProps> = ({
  title,
  value,
  subtitle,
  icon,
  accentColor,
  delay,
}) => {
  return (
    <Card
      className="p-5 flex items-center justify-between border-l-2"
      style={{
        borderLeftColor: accentColor,
        animationDelay: `${delay}ms`,
      }}
    >
      <div className="space-y-1.5">
        <span className="text-[11px] font-semibold text-cyber-400 uppercase tracking-wider">
          {title}
        </span>
        <div className="text-2xl font-bold text-white tracking-tight font-mono">{value}</div>
        <span className="text-xs text-cyber-300 block">{subtitle}</span>
      </div>
      <div
        className="w-11 h-11 rounded-xl flex items-center justify-center shrink-0"
        style={{
          backgroundColor: `${accentColor}10`,
          color: accentColor,
        }}
      >
        {icon}
      </div>
    </Card>
  );
};

interface CustomTooltipProps {
  active?: boolean;
  payload?: Array<{
    color?: string;
    name?: string;
    value?: string | number;
  }>;
  label?: string | number;
}

const ChartTooltip: React.FC<CustomTooltipProps> = ({ active, payload, label }) => {
  if (active && payload && payload.length) {
    return (
      <div className="bg-cyber-800/90 border border-cyber-500/50 p-2.5 rounded-lg shadow-xl backdrop-blur-md">
        <p className="text-[10px] font-mono font-semibold text-cyber-400 mb-1">{label}</p>
        <div className="space-y-0.5">
          {payload.map((item, i: number) => (
            <div key={i} className="flex items-center gap-2 text-xs">
              <span className="w-1.5 h-1.5 rounded-full" style={{ backgroundColor: item.color }} />
              <span className="text-cyber-200">{item.name}:</span>
              <span className="font-semibold text-white ml-auto">{item.value}</span>
            </div>
          ))}
        </div>
      </div>
    );
  }
  return null;
};

export default DashboardPage;
