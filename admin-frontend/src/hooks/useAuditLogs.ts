import { useState, useEffect, useCallback } from 'react';
import { getAuditLogs, type AuditLogResponseDto } from '../services/auditLogService';

const PAGE_SIZE = 20;

/**
 * Hook to manage system administrator audit logs fetching, searching, and pagination.
 */
export function useAuditLogs() {
  const [logs, setLogs] = useState<AuditLogResponseDto[]>([]);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [search, setSearch] = useState('');
  const [error, setError] = useState('');

  const fetchLogs = useCallback(
    async (p: number, silent = false) => {
      if (!silent) setLoading(true);
      else setRefreshing(true);
      setError('');
      try {
        const data = await getAuditLogs(p, PAGE_SIZE);
        setLogs(data.content ?? []);
        setTotalPages(data.total_pages ?? 0);
        setTotalElements(data.total_elements ?? 0);
        setPage(data.number);
      } catch {
        setError('Failed to fetch administrator audit logs.');
      } finally {
        setLoading(false);
        setRefreshing(false);
      }
    },
    []
  );

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    fetchLogs(0);
  }, [fetchLogs]);

  // Client-side local filtering based on action, rule_name, or admin_id within current page
  const filteredLogs = logs.filter((log) => {
    const q = search.trim().toLowerCase();
    if (!q) return true;
    return (
      log.rule_name.toLowerCase().includes(q) ||
      log.action.toLowerCase().includes(q) ||
      (log.admin_id ?? '').toLowerCase().includes(q)
    );
  });

  return {
    logs: filteredLogs,
    rawLogs: logs,
    totalPages,
    totalElements,
    page,
    loading,
    refreshing,
    search,
    setSearch,
    error,
    fetchLogs,
  };
}
