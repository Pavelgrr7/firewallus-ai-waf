import { useState, useEffect, useCallback } from 'react';
import { useToast } from '../context/ToastContext';
import {
  deleteRule,
  disableRule,
  enableRule,
  getRules,
  seedDefaultRules,
  type RuleResponseDto,
} from '../services/ruleService';

const PAGE_SIZE = 10;

/**
 * Hook to manage WAF rules state, filtering, pagination, and actions.
 */
export function useRulesFetch() {
  const [rules, setRules] = useState<RuleResponseDto[]>([]);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [search, setSearch] = useState('');

  // Modals / Deletion
  const [createOpen, setCreateOpen] = useState(false);
  const [editRule, setEditRule] = useState<RuleResponseDto | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<RuleResponseDto | null>(null);
  const [deleting, setDeleting] = useState(false);
  const [deleteError, setDeleteError] = useState('');

  // Individual rule toggles
  const [toggling, setToggling] = useState<Set<number>>(new Set());
  const [seedingDefaults, setSeedingDefaults] = useState(false);

  const { showToast } = useToast();

  const fetchRules = useCallback(async (p: number, silent = false) => {
    if (!silent) setLoading(true);
    else setRefreshing(true);
    try {
      const data = await getRules(p, PAGE_SIZE);
      setRules(data.content);
      setTotalPages(data.total_pages ?? 0);
      setTotalElements(data.total_elements ?? 0);
      setPage(data.number);
    } catch {
      showToast('Failed to load WAF rules.', 'error');
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, [showToast]);

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    fetchRules(0);
  }, [fetchRules]);

  const handleSeedDefaults = async () => {
    setSeedingDefaults(true);
    try {
      await seedDefaultRules();
      showToast('Default rules successfully loaded.', 'success');
      fetchRules(0);
    } catch {
      showToast('Failed to load default rules.', 'error');
    } finally {
      setSeedingDefaults(false);
    }
  };

  const handleToggleActive = async (rule: RuleResponseDto) => {
    setToggling((prev) => new Set(prev).add(rule.id));
    try {
      const updated = rule.is_active ? await disableRule(rule.id) : await enableRule(rule.id);
      setRules((prev) => prev.map((r) => (r.id === updated.id ? updated : r)));
      showToast(`Rule "${updated.name}" ${updated.is_active ? 'enabled' : 'disabled'}.`, 'success');
    } catch {
      showToast('Failed to update rule status.', 'error');
    } finally {
      setToggling((prev) => {
        const s = new Set(prev);
        s.delete(rule.id);
        return s;
      });
    }
  };

  const handleSaved = (saved: RuleResponseDto) => {
    const isNew = !rules.some((r) => r.id === saved.id);
    if (isNew) {
      fetchRules(page, true);
      showToast(`Rule "${saved.name}" created.`, 'success');
    } else {
      setRules((prev) => prev.map((r) => (r.id === saved.id ? saved : r)));
      showToast(`Rule "${saved.name}" updated.`, 'success');
    }
    setCreateOpen(false);
    setEditRule(null);
  };

  const handleDeleted = (id: number) => {
    const deleted = rules.find((r) => r.id === id);
    setRules((prev) => prev.filter((r) => r.id !== id));
    setTotalElements((n) => n - 1);
    if (rules.length === 1 && page > 0) fetchRules(page - 1);
    setDeleteTarget(null);
    showToast(`Rule "${deleted?.name}" deleted.`, 'success');
  };

  const handleDeleteConfirm = async () => {
    if (!deleteTarget) return;
    setDeleting(true);
    setDeleteError('');
    try {
      await deleteRule(deleteTarget.id);
      handleDeleted(deleteTarget.id);
    } catch {
      setDeleteError('Failed to delete rule. Please try again.');
    } finally {
      setDeleting(false);
    }
  };

  // Client-side local filtering on search term
  const filteredRules = rules.filter((r) => {
    const q = search.toLowerCase();
    return (
      r.name.toLowerCase().includes(q) ||
      r.action.toLowerCase().includes(q) ||
      r.conditions.some(
        (c) =>
          c.value.toLowerCase().includes(q) ||
          (c.target_key ?? '').toLowerCase().includes(q)
      )
    );
  });

  return {
    rules: filteredRules,
    rawRules: rules,
    totalPages,
    totalElements,
    page,
    loading,
    refreshing,
    search,
    setSearch,
    fetchRules,
    toggling,
    handleToggleActive,
    handleSaved,
    handleDeleted,
    handleSeedDefaults,
    seedingDefaults,
    // modal states
    createOpen,
    setCreateOpen,
    editRule,
    setEditRule,
    deleteTarget,
    setDeleteTarget,
    deleting,
    deleteError,
    setDeleteError,
    handleDeleteConfirm,
  };
}
