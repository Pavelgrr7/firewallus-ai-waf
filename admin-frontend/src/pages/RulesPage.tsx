import React, { useCallback, useEffect, useState } from 'react';
import {
  AlertTriangle,
  CheckCircle2,
  ChevronLeft,
  ChevronRight,
  Edit2,
  Loader2,
  Plus,
  RefreshCw,
  Search,
  Shield,
  Trash2,
  X,
  XCircle,
} from 'lucide-react';
import Header from '../components/layout/Header';
import Card from '../components/ui/Card';
import Button from '../components/ui/Button';
import Input from '../components/ui/Input';
import {
  createRule,
  deleteRule,
  disableRule,
  enableRule,
  getRules,
  updateRule,
  type Action,
  type Condition,
  type CreateRuleDto,
  type Operator,
  type RuleResponseDto,
  type Target,
  type UpdateRuleDto,
} from '../services/ruleService';

/* ===== Constants ===== */

const ACTIONS: Action[] = ['BLOCK', 'ALLOW', 'LOG'];
const TARGETS: Target[] = ['IP', 'URI', 'HEADER', 'METHOD'];
const OPERATORS: Operator[] = ['EQUALS', 'CONTAINS', 'REGEX'];

const ACTION_COLORS: Record<Action, string> = {
  BLOCK: 'text-accent-rose bg-accent-rose/10 border-accent-rose/30',
  ALLOW: 'text-accent-emerald bg-accent-emerald/10 border-accent-emerald/30',
  LOG: 'text-accent-amber bg-accent-amber/10 border-accent-amber/30',
};

const EMPTY_CONDITION: Condition = { target: 'IP', targetKey: null, operator: 'EQUALS', value: '' };

const PAGE_SIZE = 10;

/* ===== Sub-components ===== */

/** Inline toast notification */
function Toast({ message, type, onClose }: { message: string; type: 'success' | 'error'; onClose: () => void }) {
  useEffect(() => {
    const t = setTimeout(onClose, 4000);
    return () => clearTimeout(t);
  }, [onClose]);

  return (
    <div
      className={`fixed bottom-6 right-6 z-50 flex items-center gap-3 px-4 py-3 rounded-xl border shadow-2xl
        animate-slide-up backdrop-blur-md transition-all
        ${type === 'success'
          ? 'bg-accent-emerald/10 border-accent-emerald/40 text-accent-emerald'
          : 'bg-accent-rose/10 border-accent-rose/40 text-accent-rose'
        }`}
    >
      {type === 'success' ? <CheckCircle2 size={16} /> : <AlertTriangle size={16} />}
      <span className="text-sm font-medium">{message}</span>
      <button onClick={onClose} className="ml-2 opacity-60 hover:opacity-100 transition-opacity">
        <X size={14} />
      </button>
    </div>
  );
}

/** Toggle switch for isActive */
function Toggle({ checked, onChange, disabled }: { checked: boolean; onChange: () => void; disabled?: boolean }) {
  return (
    <button
      type="button"
      role="switch"
      aria-checked={checked}
      disabled={disabled}
      onClick={onChange}
      className={`relative inline-flex h-5 w-9 shrink-0 cursor-pointer rounded-full border-2 border-transparent
        transition-colors duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-accent-blue/50
        disabled:opacity-40 disabled:cursor-not-allowed
        ${checked ? 'bg-accent-emerald' : 'bg-cyber-500'}`}
    >
      <span
        className={`inline-block h-4 w-4 rounded-full bg-white shadow-lg transform transition-transform duration-200
          ${checked ? 'translate-x-4' : 'translate-x-0'}`}
      />
    </button>
  );
}

/* ===== Condition Row sub-component ===== */

function ConditionRow({ cond, idx, onChange, onRemove, canRemove }: {
  cond: Condition; idx: number;
  onChange: (idx: number, c: Condition) => void;
  onRemove: (idx: number) => void;
  canRemove: boolean;
}) {
  const sel = 'w-full rounded-lg border border-cyber-500 bg-cyber-800/60 px-3 py-2 text-sm text-cyber-50 focus:outline-none focus:ring-2 focus:ring-accent-blue/50 focus:border-accent-blue transition-all cursor-pointer';
  return (
    <div className="p-3 rounded-lg border border-cyber-600/50 bg-cyber-800/30 space-y-2">
      <div className="flex items-center justify-between">
        <span className="text-xs font-semibold text-cyber-300 uppercase tracking-wider">Condition {idx + 1}</span>
        {canRemove && (
          <button type="button" onClick={() => onRemove(idx)} className="text-cyber-400 hover:text-accent-rose transition-colors">
            <X size={14} />
          </button>
        )}
      </div>
      <div className="grid grid-cols-2 gap-2">
        <div>
          <label className="block text-xs text-cyber-400 mb-1">Target</label>
          <select className={sel} value={cond.target} onChange={e => {
            const t = e.target.value as Target;
            onChange(idx, { ...cond, target: t, targetKey: t === 'HEADER' ? (cond.targetKey ?? '') : null });
          }}>
            {TARGETS.map(t => <option key={t} value={t} className="bg-cyber-800">{t}</option>)}
          </select>
        </div>
        <div>
          <label className="block text-xs text-cyber-400 mb-1">Operator</label>
          <select className={sel} value={cond.operator} onChange={e => onChange(idx, { ...cond, operator: e.target.value as Operator })}>
            {OPERATORS.map(o => <option key={o} value={o} className="bg-cyber-800">{o}</option>)}
          </select>
        </div>
      </div>
      {cond.target === 'HEADER' && (
        <div>
          <label className="block text-xs text-cyber-400 mb-1">Header Name</label>
          <input
            className="w-full rounded-lg border border-cyber-500 bg-cyber-800/60 px-3 py-2 text-sm text-cyber-50 placeholder-cyber-500 focus:outline-none focus:ring-2 focus:ring-accent-blue/50 focus:border-accent-blue transition-all"
            placeholder="e.g. User-Agent"
            value={cond.targetKey ?? ''}
            onChange={e => onChange(idx, { ...cond, targetKey: e.target.value })}
          />
        </div>
      )}
      <div>
        <label className="block text-xs text-cyber-400 mb-1">Value</label>
        <input
          className="w-full rounded-lg border border-cyber-500 bg-cyber-800/60 px-3 py-2 text-sm text-cyber-50 placeholder-cyber-500 focus:outline-none focus:ring-2 focus:ring-accent-blue/50 focus:border-accent-blue transition-all"
          placeholder="e.g. sqlmap, /admin/*, 192.168.1.1"
          value={cond.value}
          onChange={e => onChange(idx, { ...cond, value: e.target.value })}
        />
      </div>
    </div>
  );
}

/* ===== Modal: Create / Edit Rule ===== */

interface ModalProps {
  rule: RuleResponseDto | null;
  onClose: () => void;
  onSaved: (rule: RuleResponseDto) => void;
}

function RuleModal({ rule, onClose, onSaved }: ModalProps) {
  const isEdit = rule !== null;
  const [name, setName] = useState(rule?.name ?? '');
  const [action, setAction] = useState<Action>(rule?.action ?? 'BLOCK');
  const [conditions, setConditions] = useState<Condition[]>(
    rule?.conditions?.length ? rule.conditions : [{ ...EMPTY_CONDITION }]
  );
  const [is_active, setIsActive] = useState(rule?.is_active ?? true);
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState<Record<string, string>>({});

  const updateCondition = (idx: number, c: Condition) =>
    setConditions(prev => prev.map((p, i) => i === idx ? c : p));
  const addCondition = () => setConditions(prev => [...prev, { ...EMPTY_CONDITION }]);
  const removeCondition = (idx: number) => setConditions(prev => prev.filter((_, i) => i !== idx));

  const validate = () => {
    const e: Record<string, string> = {};
    if (!name.trim()) e.name = 'Name is required';
    else if (name.length > 64) e.name = 'Max 64 characters';
    if (conditions.some(c => !c.value.trim() || (c.target === 'HEADER' && !c.targetKey?.trim())))
      e.conditions = 'All condition fields are required';
    setErrors(e);
    return Object.keys(e).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validate()) return;
    setLoading(true);
    try {
      let saved: RuleResponseDto;
      if (isEdit) {
        const dto: UpdateRuleDto = { name: name.trim(), action, conditions };
        saved = await updateRule(rule!.id, dto);
      } else {
        const dto: CreateRuleDto = { name: name.trim(), action, conditions, is_active };
        saved = await createRule(dto);
      }
      onSaved(saved);
    } catch {
      setErrors({ submit: 'Failed to save rule. Please try again.' });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    const handler = (e: KeyboardEvent) => { if (e.key === 'Escape') onClose(); };
    window.addEventListener('keydown', handler);
    return () => window.removeEventListener('keydown', handler);
  }, [onClose]);

  const selClass = 'w-full rounded-lg border border-cyber-500 bg-cyber-800/60 px-4 py-2.5 text-sm text-cyber-50 focus:outline-none focus:ring-2 focus:ring-accent-blue/50 focus:border-accent-blue transition-all cursor-pointer';

  return (
    <div className="fixed inset-0 z-40 flex items-center justify-center" onClick={(e) => { if (e.target === e.currentTarget) onClose(); }}>
      <div className="absolute inset-0 bg-cyber-900/80 backdrop-blur-sm" />
      <div className="relative z-50 w-full max-w-xl mx-4 glass-card p-0 animate-slide-up overflow-hidden flex flex-col max-h-[90vh]">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-glass-border shrink-0">
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 rounded-lg bg-accent-purple/10 flex items-center justify-center">
              <Shield size={16} className="text-accent-purple" />
            </div>
            <h2 className="text-base font-semibold text-white">{isEdit ? 'Edit Rule' : 'New Rule'}</h2>
          </div>
          <button onClick={onClose} className="text-cyber-300 hover:text-white transition-colors"><X size={20} /></button>
        </div>
        {/* Body */}
        <form onSubmit={handleSubmit} className="px-6 py-5 space-y-4 overflow-y-auto">
          <Input id="rule-name" label="Rule Name" placeholder="e.g. Block SQLMap Scanners"
            value={name} onChange={(e) => setName(e.target.value)} error={errors.name} maxLength={64} />
          {/* Action */}
          <div>
            <label className="block text-sm font-medium text-cyber-200 mb-2">Action</label>
            <select id="rule-action" value={action} onChange={(e) => setAction(e.target.value as Action)} className={selClass}>
              {ACTIONS.map(a => <option key={a} value={a} className="bg-cyber-800">{a}</option>)}
            </select>
          </div>
          {/* Conditions */}
          <div>
            <div className="flex items-center justify-between mb-2">
              <label className="text-sm font-medium text-cyber-200">
                Conditions <span className="text-cyber-400 text-xs">(AND logic)</span>
              </label>
              <button type="button" onClick={addCondition} className="flex items-center gap-1 text-xs text-accent-blue hover:text-accent-cyan transition-colors">
                <Plus size={13} /> Add
              </button>
            </div>
            <div className="space-y-2">
              {conditions.map((c, i) => (
                <ConditionRow key={i} cond={c} idx={i} onChange={updateCondition} onRemove={removeCondition} canRemove={conditions.length > 1} />
              ))}
            </div>
            {errors.conditions && <p className="text-xs text-accent-rose mt-1 flex items-center gap-1"><AlertTriangle size={12} />{errors.conditions}</p>}
          </div>
          {/* Active toggle (create only) */}
          {!isEdit && (
            <div className="flex items-center justify-between py-1">
              <span className="text-sm font-medium text-cyber-200">Enable immediately</span>
              <Toggle checked={is_active} onChange={() => setIsActive(v => !v)} />
            </div>
          )}
          {errors.submit && <p className="text-xs text-accent-rose flex items-center gap-1"><AlertTriangle size={12} />{errors.submit}</p>}
          <div className="flex justify-end gap-3 pt-2">
            <Button type="button" variant="ghost" size="sm" onClick={onClose} disabled={loading}>Cancel</Button>
            <Button type="submit" variant="primary" size="sm" isLoading={loading}>{isEdit ? 'Save Changes' : 'Create Rule'}</Button>
          </div>
        </form>
      </div>
    </div>
  );
}

/* ===== Modal: Delete Confirmation ===== */

function DeleteModal({ rule, onClose, onDeleted }: { rule: RuleResponseDto; onClose: () => void; onDeleted: (id: number) => void }) {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleDelete = async () => {
    setLoading(true);
    try {
      await deleteRule(rule.id);
      onDeleted(rule.id);
    } catch {
      setError('Failed to delete rule. Please try again.');
      setLoading(false);
    }
  };

  useEffect(() => {
    const handler = (e: KeyboardEvent) => { if (e.key === 'Escape') onClose(); };
    window.addEventListener('keydown', handler);
    return () => window.removeEventListener('keydown', handler);
  }, [onClose]);

  return (
    <div
      className="fixed inset-0 z-40 flex items-center justify-center"
      onClick={(e) => { if (e.target === e.currentTarget) onClose(); }}
    >
      <div className="absolute inset-0 bg-cyber-900/80 backdrop-blur-sm" />
      <div className="relative z-50 w-full max-w-md mx-4 glass-card p-6 animate-slide-up">
        <div className="flex items-center gap-3 mb-4">
          <div className="w-10 h-10 rounded-xl bg-accent-rose/10 flex items-center justify-center">
            <Trash2 size={18} className="text-accent-rose" />
          </div>
          <div>
            <h2 className="text-base font-semibold text-white">Delete Rule</h2>
            <p className="text-xs text-cyber-300">This action cannot be undone.</p>
          </div>
        </div>
        <p className="text-sm text-cyber-200 mb-5">
          Are you sure you want to delete rule{' '}
          <span className="font-semibold text-white">"{rule.name}"</span>?
        </p>
        {error && (
          <p className="text-xs text-accent-rose mb-4 flex items-center gap-1">
            <AlertTriangle size={12} /> {error}
          </p>
        )}
        <div className="flex justify-end gap-3">
          <Button variant="ghost" size="sm" onClick={onClose} disabled={loading}>Cancel</Button>
          <Button variant="danger" size="sm" isLoading={loading} onClick={handleDelete}>Delete</Button>
        </div>
      </div>
    </div>
  );
}

/* ===== Main Page ===== */

const RulesPage: React.FC = () => {
  const [rules, setRules] = useState<RuleResponseDto[]>([]);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [search, setSearch] = useState('');

  // Modal state
  const [createOpen, setCreateOpen] = useState(false);
  const [editRule, setEditRule] = useState<RuleResponseDto | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<RuleResponseDto | null>(null);

  // Toast state
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' } | null>(null);

  // Toggle loading per-row
  const [toggling, setToggling] = useState<Set<number>>(new Set());

  const showToast = (message: string, type: 'success' | 'error') => setToast({ message, type });

  const fetchRules = useCallback(async (p: number, silent = false) => {
    if (!silent) setLoading(true);
    else setRefreshing(true);
    try {
      const data = await getRules(p, PAGE_SIZE);
      setRules(data.content);
      setTotalPages(data.totalPages);
      setTotalElements(data.totalElements);
      setPage(data.number);
    } catch {
      showToast('Failed to load rules.', 'error');
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, []);

  useEffect(() => {
    fetchRules(0);
  }, [fetchRules]);

  /* ----- Handlers ----- */

  const handleToggleActive = async (rule: RuleResponseDto) => {
    setToggling((prev) => new Set(prev).add(rule.id));
    try {
      const updated = rule.is_active ? await disableRule(rule.id) : await enableRule(rule.id);
      setRules((prev) => prev.map((r) => (r.id === updated.id ? updated : r)));
      showToast(`Rule "${updated.name}" ${updated.is_active ? 'enabled' : 'disabled'}.`, 'success');
    } catch {
      showToast('Failed to update rule status.', 'error');
    } finally {
      setToggling((prev) => { const s = new Set(prev); s.delete(rule.id); return s; });
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

  /* ----- Filtered (client-side search on current page) ----- */

  const filtered = rules.filter((r) => {
    const q = search.toLowerCase();
    return (
      r.name.toLowerCase().includes(q) ||
      r.action.toLowerCase().includes(q) ||
      r.conditions.some(c => c.value.toLowerCase().includes(q) || (c.targetKey ?? '').toLowerCase().includes(q))
    );
  });

  /* ----- Render ----- */

  return (
    <div className="min-h-screen">
      <Header title="Rule Management" />

      <div className="p-6 space-y-5">
        {/* Top bar */}
        <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 animate-fade-in">
          <div>
            <h2 className="text-lg font-bold text-white">WAF Rules</h2>
            <p className="text-xs text-cyber-300 mt-0.5">
              {loading ? 'Loading…' : `${totalElements} rule${totalElements !== 1 ? 's' : ''} total`}
            </p>
          </div>
          <div className="flex items-center gap-2">
            <button
              id="rules-refresh-btn"
              onClick={() => fetchRules(page, true)}
              disabled={refreshing || loading}
              className="p-2 rounded-lg text-cyber-300 hover:text-white hover:bg-cyber-700 transition-all disabled:opacity-40"
              title="Refresh"
            >
              <RefreshCw size={16} className={refreshing ? 'animate-spin' : ''} />
            </button>
            <Button
              id="rules-create-btn"
              variant="primary"
              size="sm"
              onClick={() => setCreateOpen(true)}
            >
              <Plus size={16} className="mr-1.5" />
              New Rule
            </Button>
          </div>
        </div>

        {/* Search */}
        <div className="animate-fade-in">
          <Input
            id="rules-search"
            placeholder="Search by name, type, or condition…"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            icon={<Search size={15} />}
          />
        </div>

        {/* Table card */}
        <Card className="animate-slide-up p-0 overflow-hidden">
          {loading ? (
            <div className="flex items-center justify-center py-24 gap-3 text-cyber-300">
              <Loader2 size={22} className="animate-spin" />
              <span className="text-sm">Loading rules…</span>
            </div>
          ) : filtered.length === 0 ? (
            <div className="text-center py-24">
              <div className="w-14 h-14 mx-auto mb-4 rounded-2xl bg-accent-purple/10 flex items-center justify-center">
                <Shield size={28} className="text-accent-purple" />
              </div>
              <p className="text-sm font-medium text-cyber-200">
                {search ? 'No rules match your search' : 'No rules yet'}
              </p>
              {!search && (
                <p className="text-xs text-cyber-400 mt-1">
                  Click <span className="text-accent-blue">New Rule</span> to create your first WAF rule.
                </p>
              )}
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-glass-border">
                    <th className="text-left px-5 py-3.5 text-xs font-semibold text-cyber-300 uppercase tracking-wider">Name</th>
                    <th className="text-left px-5 py-3.5 text-xs font-semibold text-cyber-300 uppercase tracking-wider">Action</th>
                    <th className="text-left px-5 py-3.5 text-xs font-semibold text-cyber-300 uppercase tracking-wider hidden md:table-cell">Conditions</th>
                    <th className="text-center px-5 py-3.5 text-xs font-semibold text-cyber-300 uppercase tracking-wider">Status</th>
                    <th className="text-right px-5 py-3.5 text-xs font-semibold text-cyber-300 uppercase tracking-wider">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-glass-border/50">
                  {filtered.map((rule) => (
                    <tr
                      key={rule.id}
                      className="group hover:bg-cyber-700/20 transition-colors duration-150"
                    >
                      {/* Name */}
                      <td className="px-5 py-4">
                        <div className="flex items-center gap-2.5">
                          <div className={`w-1.5 h-6 rounded-full shrink-0 transition-colors
                            ${rule.is_active ? 'bg-accent-emerald' : 'bg-cyber-500'}`}
                          />
                          <span className="font-medium text-white">{rule.name}</span>
                        </div>
                      </td>

                      {/* Action badge */}
                      <td className="px-5 py-4">
                        <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border
                          ${ACTION_COLORS[rule.action] ?? 'text-cyber-200 bg-cyber-700/40 border-cyber-500/30'}`}>
                          {rule.action}
                        </span>
                      </td>

                      {/* Conditions summary */}
                      <td className="px-5 py-4 hidden md:table-cell">
                        <div className="flex flex-col gap-1">
                          {rule.conditions.slice(0, 2).map((c, i) => (
                            <span key={i} className="text-xs font-mono text-cyber-300 bg-cyber-800/60 px-2 py-0.5 rounded-md truncate max-w-[220px] block">
                              {c.target}{c.targetKey ? `:${c.targetKey}` : ''} {c.operator} {c.value}
                            </span>
                          ))}
                          {rule.conditions.length > 2 && (
                            <span className="text-xs text-cyber-400">+{rule.conditions.length - 2} more</span>
                          )}
                        </div>
                      </td>

                      {/* Toggle */}
                      <td className="px-5 py-4 text-center">
                        <div className="flex items-center justify-center gap-2">
                          <Toggle
                            checked={rule.is_active}
                            onChange={() => handleToggleActive(rule)}
                            disabled={toggling.has(rule.id)}
                          />
                          {toggling.has(rule.id) ? (
                            <Loader2 size={12} className="animate-spin text-cyber-300" />
                          ) : rule.is_active ? (
                            <CheckCircle2 size={13} className="text-accent-emerald" />
                          ) : (
                            <XCircle size={13} className="text-cyber-400" />
                          )}
                        </div>
                      </td>

                      {/* Actions */}
                      <td className="px-5 py-4 text-right">
                        <div className="flex items-center justify-end gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                          <button
                            id={`rule-edit-${rule.id}`}
                            onClick={() => setEditRule(rule)}
                            className="p-1.5 rounded-md text-cyber-300 hover:text-accent-blue hover:bg-accent-blue/10 transition-all"
                            title="Edit"
                          >
                            <Edit2 size={14} />
                          </button>
                          <button
                            id={`rule-delete-${rule.id}`}
                            onClick={() => setDeleteTarget(rule)}
                            className="p-1.5 rounded-md text-cyber-300 hover:text-accent-rose hover:bg-accent-rose/10 transition-all"
                            title="Delete"
                          >
                            <Trash2 size={14} />
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}

          {/* Pagination footer */}
          {!loading && totalPages > 1 && (
            <div className="flex items-center justify-between px-5 py-3.5 border-t border-glass-border">
              <span className="text-xs text-cyber-400">
                Page {page + 1} of {totalPages}
              </span>
              <div className="flex items-center gap-1">
                <button
                  id="rules-prev-page"
                  onClick={() => fetchRules(page - 1)}
                  disabled={page === 0 || loading}
                  className="p-1.5 rounded-md text-cyber-300 hover:text-white hover:bg-cyber-700 transition-all disabled:opacity-30 disabled:cursor-not-allowed"
                >
                  <ChevronLeft size={16} />
                </button>
                {Array.from({ length: Math.min(totalPages, 5) }, (_, i) => {
                  const p0 = Math.max(0, Math.min(page - 2, totalPages - 5));
                  const idx = p0 + i;
                  return (
                    <button
                      key={idx}
                      id={`rules-page-${idx}`}
                      onClick={() => fetchRules(idx)}
                      className={`w-7 h-7 rounded-md text-xs font-medium transition-all
                        ${idx === page
                          ? 'bg-accent-blue text-white'
                          : 'text-cyber-300 hover:text-white hover:bg-cyber-700'
                        }`}
                    >
                      {idx + 1}
                    </button>
                  );
                })}
                <button
                  id="rules-next-page"
                  onClick={() => fetchRules(page + 1)}
                  disabled={page >= totalPages - 1 || loading}
                  className="p-1.5 rounded-md text-cyber-300 hover:text-white hover:bg-cyber-700 transition-all disabled:opacity-30 disabled:cursor-not-allowed"
                >
                  <ChevronRight size={16} />
                </button>
              </div>
            </div>
          )}
        </Card>
      </div>

      {/* Modals */}
      {(createOpen || editRule) && (
        <RuleModal
          rule={editRule}
          onClose={() => { setCreateOpen(false); setEditRule(null); }}
          onSaved={handleSaved}
        />
      )}
      {deleteTarget && (
        <DeleteModal
          rule={deleteTarget}
          onClose={() => setDeleteTarget(null)}
          onDeleted={handleDeleted}
        />
      )}

      {/* Toast */}
      {toast && (
        <Toast
          message={toast.message}
          type={toast.type}
          onClose={() => setToast(null)}
        />
      )}
    </div>
  );
};

export default RulesPage;
