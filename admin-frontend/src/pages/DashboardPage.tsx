import React from 'react';
import {
  Activity,
  ShieldOff,
  BookOpen,
  Zap,
  TrendingUp,
  TrendingDown,
} from 'lucide-react';
import {
  LineChart,
  Line,
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
  RadarChart,
  PolarGrid,
  PolarAngleAxis,
  PolarRadiusAxis,
  Radar,
} from 'recharts';

import Header from '../components/layout/Header';
import Card from '../components/ui/Card';
import {
  counterCards,
  trafficData,
  attackTypes,
  blockedIPs,
  actionDistribution,
} from '../data/mockData';

/* ===== Helper: format large numbers ===== */
const formatNumber = (n: number): string =>
  n >= 1_000_000
    ? `${(n / 1_000_000).toFixed(1)}M`
    : n >= 1_000
    ? `${(n / 1_000).toFixed(n >= 10_000 ? 0 : 1)}K`
    : n.toString();

/* ===== Counter Card Component ===== */
interface StatCardProps {
  title: string;
  value: string;
  subtitle?: string;
  icon: React.ReactNode;
  trend?: { value: string; positive: boolean };
  accentColor: string;
  delay: number;
}

const StatCard: React.FC<StatCardProps> = ({
  title,
  value,
  subtitle,
  icon,
  trend,
  accentColor,
  delay,
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
      <p className="text-2xl font-bold text-white tracking-tight">{value}</p>
      <p className="text-sm text-cyber-300 mt-1">{title}</p>
      {subtitle && <p className="text-xs text-cyber-500 mt-0.5">{subtitle}</p>}
    </div>
  </Card>
);

/* ===== Custom Chart Tooltip ===== */
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

/* ===== Dashboard Page ===== */
const DashboardPage: React.FC = () => {
  return (
    <div className="min-h-screen">
      <Header title="Dashboard" />

      <div className="p-8 space-y-8">
        {/* Section A: Counter Cards */}
        <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-6">
          <StatCard
            title="Total Traffic"
            value={formatNumber(counterCards.totalTraffic)}
            subtitle="HTTP requests analyzed"
            icon={<Activity size={22} />}
            trend={{ value: '+12.5%', positive: true }}
            accentColor="#3b82f6"
            delay={0}
          />
          <StatCard
            title="Blocked Requests"
            value={formatNumber(counterCards.blockedRequests)}
            subtitle="Attacks prevented"
            icon={<ShieldOff size={22} />}
            trend={{ value: '+3.2%', positive: false }}
            accentColor="#f43f5e"
            delay={100}
          />
          <StatCard
            title="Active Rules"
            value={counterCards.activeRules.toString()}
            subtitle="Currently enforced"
            icon={<BookOpen size={22} />}
            trend={{ value: '+2', positive: true }}
            accentColor="#a855f7"
            delay={200}
          />
          <StatCard
            title="Redis Rate (Lua)"
            value={`${counterCards.redisRate} req/s`}
            subtitle="Current throughput"
            icon={<Zap size={22} />}
            trend={{ value: '+8.1%', positive: true }}
            accentColor="#06b6d4"
            delay={300}
          />
        </div>

        {/* Section B: Charts */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* 1. Traffic vs Blocks — Line Chart */}
          <Card className="animate-slide-up col-span-1 lg:col-span-2">
            <h3 className="text-base font-semibold text-white mb-1">
              Traffic vs Blocked Requests
            </h3>
            <p className="text-xs text-cyber-400 mb-6">Last 24 hours</p>
            <ResponsiveContainer width="100%" height={320}>
              <LineChart data={trafficData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#1c2748" />
                <XAxis
                  dataKey="hour"
                  tick={{ fill: '#7a92b5', fontSize: 11 }}
                  axisLine={{ stroke: '#243158' }}
                  tickLine={false}
                />
                <YAxis
                  tick={{ fill: '#7a92b5', fontSize: 11 }}
                  axisLine={{ stroke: '#243158' }}
                  tickLine={false}
                  tickFormatter={(v: number) => formatNumber(v)}
                />
                <Tooltip content={<ChartTooltip />} />
                <Line
                  type="monotone"
                  dataKey="traffic"
                  name="Traffic"
                  stroke="#3b82f6"
                  strokeWidth={2.5}
                  dot={false}
                  activeDot={{ r: 5, fill: '#3b82f6', stroke: '#0f1629', strokeWidth: 2 }}
                />
                <Line
                  type="monotone"
                  dataKey="blocked"
                  name="Blocked"
                  stroke="#f43f5e"
                  strokeWidth={2.5}
                  dot={false}
                  activeDot={{ r: 5, fill: '#f43f5e', stroke: '#0f1629', strokeWidth: 2 }}
                />
                <Legend
                  wrapperStyle={{ paddingTop: 16, fontSize: 12, color: '#b0c4de' }}
                />
              </LineChart>
            </ResponsiveContainer>
          </Card>

          {/* 2. Top Attack Types — Pie Chart */}
          <Card className="animate-slide-up">
            <h3 className="text-base font-semibold text-white mb-1">
              Top Attack Types
            </h3>
            <p className="text-xs text-cyber-400 mb-4">Distribution of blocked threats</p>
            <ResponsiveContainer width="100%" height={280}>
              <PieChart>
                <Pie
                  data={attackTypes}
                  cx="50%"
                  cy="50%"
                  innerRadius={65}
                  outerRadius={100}
                  paddingAngle={4}
                  dataKey="value"
                  strokeWidth={0}
                >
                  {attackTypes.map((entry, index) => (
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
                  formatter={(value) => [`${value}%`, '']}
                />
                <Legend
                  formatter={(value: string) => (
                    <span className="text-xs text-cyber-200">{value}</span>
                  )}
                />
              </PieChart>
            </ResponsiveContainer>
          </Card>

          {/* 3. Top Blocked IPs — Bar Chart */}
          <Card className="animate-slide-up">
            <h3 className="text-base font-semibold text-white mb-1">
              Top Blocked IPs
            </h3>
            <p className="text-xs text-cyber-400 mb-4">Top 5 sources by blocked count</p>
            <ResponsiveContainer width="100%" height={280}>
              <BarChart
                data={blockedIPs}
                layout="vertical"
                margin={{ left: 20 }}
              >
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
                  name="Blocked"
                  fill="#a855f7"
                  radius={[0, 6, 6, 0]}
                  barSize={20}
                />
              </BarChart>
            </ResponsiveContainer>
          </Card>

          {/* 4. Action Distribution — Radar Chart */}
          <Card className="animate-slide-up col-span-1 lg:col-span-2">
            <h3 className="text-base font-semibold text-white mb-1">
              Action Distribution
            </h3>
            <p className="text-xs text-cyber-400 mb-4">ML Model vs Static Rules</p>
            <ResponsiveContainer width="100%" height={320}>
              <RadarChart data={actionDistribution} cx="50%" cy="50%" outerRadius="70%">
                <PolarGrid stroke="#243158" />
                <PolarAngleAxis
                  dataKey="category"
                  tick={{ fill: '#b0c4de', fontSize: 12 }}
                />
                <PolarRadiusAxis
                  tick={{ fill: '#7a92b5', fontSize: 10 }}
                  axisLine={false}
                />
                <Radar
                  name="ML Model"
                  dataKey="mlModel"
                  stroke="#06b6d4"
                  fill="#06b6d4"
                  fillOpacity={0.2}
                  strokeWidth={2}
                />
                <Radar
                  name="Static Rules"
                  dataKey="staticRules"
                  stroke="#f59e0b"
                  fill="#f59e0b"
                  fillOpacity={0.15}
                  strokeWidth={2}
                />
                <Legend
                  wrapperStyle={{ paddingTop: 16, fontSize: 12, color: '#b0c4de' }}
                />
                <Tooltip
                  contentStyle={{
                    background: '#0f1629',
                    border: '1px solid rgba(255,255,255,0.1)',
                    borderRadius: 8,
                    fontSize: 12,
                    color: '#e0e8f0',
                  }}
                />
              </RadarChart>
            </ResponsiveContainer>
          </Card>
        </div>
      </div>
    </div>
  );
};

export default DashboardPage;
