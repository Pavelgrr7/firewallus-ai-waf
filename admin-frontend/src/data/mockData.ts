/* ===== Mock data for Dashboard charts and counters ===== */

/** Counter cards data */
export const counterCards = {
  totalTraffic: 1_245_000,
  blockedRequests: 15_430,
  activeRules: 42,
  redisRate: 850, // req/s
};

/** Traffic vs Blocks — line chart (last 24 hours) */
export const trafficData = [
  { hour: '00:00', traffic: 4200, blocked: 120 },
  { hour: '01:00', traffic: 3100, blocked: 95 },
  { hour: '02:00', traffic: 2800, blocked: 78 },
  { hour: '03:00', traffic: 2200, blocked: 65 },
  { hour: '04:00', traffic: 2500, blocked: 82 },
  { hour: '05:00', traffic: 3200, blocked: 110 },
  { hour: '06:00', traffic: 4800, blocked: 180 },
  { hour: '07:00', traffic: 6500, blocked: 250 },
  { hour: '08:00', traffic: 8900, blocked: 420 },
  { hour: '09:00', traffic: 12000, blocked: 580 },
  { hour: '10:00', traffic: 15200, blocked: 720 },
  { hour: '11:00', traffic: 16800, blocked: 810 },
  { hour: '12:00', traffic: 14500, blocked: 650 },
  { hour: '13:00', traffic: 15800, blocked: 700 },
  { hour: '14:00', traffic: 17200, blocked: 850 },
  { hour: '15:00', traffic: 16500, blocked: 780 },
  { hour: '16:00', traffic: 14000, blocked: 620 },
  { hour: '17:00', traffic: 11500, blocked: 480 },
  { hour: '18:00', traffic: 9800, blocked: 380 },
  { hour: '19:00', traffic: 8200, blocked: 310 },
  { hour: '20:00', traffic: 7500, blocked: 270 },
  { hour: '21:00', traffic: 6800, blocked: 220 },
  { hour: '22:00', traffic: 5500, blocked: 170 },
  { hour: '23:00', traffic: 4800, blocked: 140 },
];

/** Top Attack Types — pie chart */
export const attackTypes = [
  { name: 'SQL Injection', value: 40, color: '#f43f5e' },
  { name: 'XSS', value: 30, color: '#f59e0b' },
  { name: 'Bad Bot', value: 20, color: '#a855f7' },
  { name: 'Other', value: 10, color: '#06b6d4' },
];

/** Top Blocked IPs — bar chart */
export const blockedIPs = [
  { ip: '192.168.1.42', count: 1840, country: 'CN' },
  { ip: '10.0.0.195', count: 1520, country: 'RU' },
  { ip: '172.16.0.88', count: 1100, country: 'BR' },
  { ip: '203.0.113.12', count: 890, country: 'IN' },
  { ip: '198.51.100.7', count: 670, country: 'US' },
];

/** Action Distribution — ML Model vs Static Rules */
export const actionDistribution = [
  { category: 'Block', mlModel: 320, staticRules: 480 },
  { category: 'Allow', mlModel: 890, staticRules: 1200 },
  { category: 'Challenge', mlModel: 150, staticRules: 60 },
  { category: 'Rate Limit', mlModel: 210, staticRules: 180 },
  { category: 'Log Only', mlModel: 430, staticRules: 120 },
];
