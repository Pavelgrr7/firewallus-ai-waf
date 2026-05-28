import { useState, useEffect, useCallback } from 'react';
import { useToast } from '../context/ToastContext';
import {
  deleteManagedIp,
  getManagedIps,
  type IpListType,
  type ManagedIpResponseDto,
} from '../services/accessControlService';

const PAGE_SIZE = 10;

/**
 * Hook to manage whitelists and blacklists IP data and actions.
 */
export function useManagedIps() {
  const [activeTab, setActiveTab] = useState<IpListType>('BLACKLIST');
  const [ips, setIps] = useState<ManagedIpResponseDto[]>([]);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [search, setSearch] = useState('');

  // Modals / Deletion
  const [addOpen, setAddOpen] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState<ManagedIpResponseDto | null>(null);
  const [deleting, setDeleting] = useState(false);
  const [deleteError, setDeleteError] = useState('');

  const { showToast } = useToast();

  const fetchIps = useCallback(
    async (type: IpListType, p: number, silent = false) => {
      if (!silent) setLoading(true);
      else setRefreshing(true);
      try {
        const data = await getManagedIps(p, PAGE_SIZE, type);
        setIps(data.content);
        setTotalPages(data.total_pages ?? 0);
        setTotalElements(data.total_elements ?? 0);
        setPage(data.number);
      } catch {
        showToast('Failed to load IP list.', 'error');
      } finally {
        setLoading(false);
        setRefreshing(false);
      }
    },
    [showToast]
  );

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    fetchIps(activeTab, 0);
  }, [activeTab, fetchIps]);

  const handleTabChange = (type: IpListType) => {
    setActiveTab(type);
    setSearch('');
  };

  const handleSaved = (saved: ManagedIpResponseDto) => {
    if (saved.list_type === activeTab) {
      fetchIps(activeTab, 0, true);
    } else {
      setActiveTab(saved.list_type);
    }
    setAddOpen(false);
    showToast(`IP ${saved.ip_address} added to ${saved.list_type}.`, 'success');
  };

  const handleDeleted = (id: string) => {
    const deleted = ips.find((x) => x.id === id);
    setIps((prev) => prev.filter((x) => x.id !== id));
    setTotalElements((n) => n - 1);
    if (ips.length === 1 && page > 0) {
      fetchIps(activeTab, page - 1);
    }
    setDeleteTarget(null);
    showToast(`IP ${deleted?.ip_address} removed from ${activeTab}.`, 'success');
  };

  const handleDeleteConfirm = async () => {
    if (!deleteTarget) return;
    setDeleting(true);
    setDeleteError('');
    try {
      await deleteManagedIp(deleteTarget.id);
      handleDeleted(deleteTarget.id);
    } catch {
      setDeleteError('Failed to delete IP. Please try again.');
    } finally {
      setDeleting(false);
    }
  };

  // Client-side local filtering
  const filteredIps = ips.filter(
    (ip) =>
      ip.ip_address.includes(search.trim()) ||
      (ip.description && ip.description.toLowerCase().includes(search.toLowerCase()))
  );

  return {
    ips: filteredIps,
    rawIps: ips,
    activeTab,
    totalPages,
    totalElements,
    page,
    loading,
    refreshing,
    search,
    setSearch,
    handleTabChange,
    fetchIps,
    handleSaved,
    handleDeleted,
    // modal states
    addOpen,
    setAddOpen,
    deleteTarget,
    setDeleteTarget,
    deleting,
    deleteError,
    setDeleteError,
    handleDeleteConfirm,
  };
}
