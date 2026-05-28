import React from 'react';
import {
  Calendar,
  History,
  Layers,
  Loader2,
  RefreshCw,
  Search,
  User,
} from 'lucide-react';
import Header from '../components/layout/Header';
import Card from '../components/ui/Card';
import Button from '../components/ui/Button';
import Input from '../components/ui/Input';
import Pagination from '../components/ui/Pagination';
import { useAuditLogs } from '../hooks/useAuditLogs';
import type { AuditAction } from '../services/auditLogService';

const ACTION_BADGES: Record<AuditAction, string> = {
  CREATE_RULE: 'text-accent-emerald bg-accent-emerald/10 border-accent-emerald/30',
  UPDATE_RULE: 'text-accent-blue bg-accent-blue/10 border-accent-blue/30',
  DELETE_RULE: 'text-accent-rose bg-accent-rose/10 border-accent-rose/30',
  ENABLE_RULE: 'text-teal-400 bg-teal-400/10 border-teal-400/30',
  DISABLE_RULE: 'text-accent-amber bg-accent-amber/10 border-accent-amber/30',
  LOGIN: 'text-accent-purple bg-accent-purple/10 border-accent-purple/30',
};

const AuditLogsPage: React.FC = () => {
  const {
    logs,
    totalPages,
    totalElements,
    page,
    loading,
    refreshing,
    search,
    setSearch,
    error,
    fetchLogs,
  } = useAuditLogs();

  return (
    <div className="min-h-screen">
      <Header title="System Audit Logs" />

      <div className="p-6 space-y-5">
        {/* Top bar */}
        <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 animate-fade-in">
          <div>
            <h2 className="text-lg font-bold text-white">Administrator Actions</h2>
            <p className="text-xs text-cyber-300 mt-0.5 font-mono">
              {loading ? 'Fetching log events...' : `${totalElements} events recorded`}
            </p>
          </div>
          <div className="flex items-center gap-2">
            <button
              id="audit-refresh-btn"
              onClick={() => fetchLogs(page, true)}
              disabled={refreshing || loading}
              className="p-2 rounded-lg text-cyber-300 hover:text-white hover:bg-cyber-700 transition-all disabled:opacity-40"
              title="Refresh Logs"
            >
              <RefreshCw size={16} className={refreshing ? 'animate-spin' : ''} />
            </button>
          </div>
        </div>

        {/* Search Filter */}
        <div className="max-w-md animate-fade-in">
          <Input
            id="audit-search"
            placeholder="Search by action, rule name, or admin ID…"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            icon={<Search size={15} />}
          />
        </div>

        {/* Logs Table Card */}
        <Card className="animate-slide-up p-0 overflow-hidden">
          {loading ? (
            <div className="flex items-center justify-center py-24 gap-3 text-cyber-300">
              <Loader2 size={22} className="animate-spin" />
              <span className="text-sm">Retrieving audit trail...</span>
            </div>
          ) : error ? (
            <div className="text-center py-24 text-accent-rose space-y-2">
              <History size={36} className="mx-auto opacity-50" />
              <p className="text-sm font-semibold">{error}</p>
              <Button variant="secondary" size="sm" onClick={() => fetchLogs(page)}>
                Retry
              </Button>
            </div>
          ) : logs.length === 0 ? (
            <div className="text-center py-24">
              <div className="w-14 h-14 mx-auto mb-4 rounded-2xl bg-cyber-800 flex items-center justify-center">
                <History size={28} className="text-cyber-400" />
              </div>
              <p className="text-sm font-medium text-cyber-200">
                {search ? 'No logs match your search query' : 'Audit logs are empty'}
              </p>
              <p className="text-xs text-cyber-400 mt-1">
                Security-sensitive operations performed by administrators will appear here.
              </p>
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-glass-border">
                    <th className="text-left px-5 py-3.5 text-xs font-semibold text-cyber-300 uppercase tracking-wider">
                      Action Type
                    </th>
                    <th className="text-left px-5 py-3.5 text-xs font-semibold text-cyber-300 uppercase tracking-wider">
                      Target Rule
                    </th>
                    <th className="text-left px-5 py-3.5 text-xs font-semibold text-cyber-300 uppercase tracking-wider">
                      Admin ID
                    </th>
                    <th className="text-left px-5 py-3.5 text-xs font-semibold text-cyber-300 uppercase tracking-wider hidden md:table-cell">
                      Timestamp
                    </th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-glass-border/50">
                  {logs.map((log) => (
                    <tr
                      key={log.id}
                      className="hover:bg-cyber-700/20 transition-colors duration-150"
                    >
                      {/* Action Badge */}
                      <td className="px-5 py-4">
                        <span
                          className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold border uppercase tracking-wider
                          ${ACTION_BADGES[log.action] ?? 'text-cyber-200 bg-cyber-700/40 border-cyber-500/30'}`}
                        >
                          {log.action.replace('_', ' ')}
                        </span>
                      </td>

                      {/* Target Rule Name */}
                      <td className="px-5 py-4 text-white font-medium">
                        <div className="flex items-center gap-2">
                          <Layers size={13} className="text-cyber-400 shrink-0" />
                          <span>{log.rule_name}</span>
                          {log.rule_id && (
                            <span className="text-[10px] bg-cyber-800 text-cyber-300 px-1.5 py-0.5 rounded font-mono">
                              ID: {log.rule_id}
                            </span>
                          )}
                        </div>
                      </td>

                      {/* Admin ID */}
                      <td className="px-5 py-4 text-cyber-300 font-mono text-xs">
                        <div className="flex items-center gap-1.5">
                          <User size={12} className="text-cyber-400 shrink-0" />
                          <span>{log.admin_id || 'System Event'}</span>
                        </div>
                      </td>

                      {/* Localized Timestamp */}
                      <td className="px-5 py-4 text-cyber-300 hidden md:table-cell">
                        <div className="flex items-center gap-2">
                          <Calendar size={13} className="text-cyber-400 shrink-0" />
                          <span>{log.timestamp ? new Date(log.timestamp).toLocaleString() : 'Unknown'}</span>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}

          {/* Pagination */}
          <Pagination
            page={page}
            totalPages={totalPages}
            onPageChange={(p) => fetchLogs(p)}
            loading={loading}
            idPrefix="audit"
          />
        </Card>
      </div>
    </div>
  );
};

export default AuditLogsPage;
