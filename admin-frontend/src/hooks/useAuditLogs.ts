import { useState, useEffect, useCallback, useRef } from 'react';
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
  const [search, setSearchState] = useState('');
  const [error, setError] = useState('');

  const searchRef = useRef('');

  const fetchLogs = useCallback(
    async (p: number, query?: string, silent = false) => {
      if (!silent) setLoading(true);
      else setRefreshing(true);
      setError('');
      try {
        const activeQuery = query !== undefined ? query : searchRef.current;
        const data = await getAuditLogs(p, PAGE_SIZE, activeQuery);
        setLogs(data.content ?? []);
        setTotalPages(data.total_pages ?? 0);
        setTotalElements(data.total_elements ?? 0);
        setPage(data.number);
        if (query !== undefined) {
          searchRef.current = query;
          setSearchState(query);
        }
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
    fetchLogs(0, '');
  }, [fetchLogs]);

  return {
    logs,
    totalPages,
    totalElements,
    page,
    loading,
    refreshing,
    search,
    setSearch: setSearchState,
    error,
    fetchLogs,
  };
}
