import React from 'react';
import {
  Edit2,
  Loader2,
  Plus,
  RefreshCw,
  Search,
  ShieldAlert,
  Sliders,
  Trash2,
} from 'lucide-react';
import Header from '../components/layout/Header';
import Card from '../components/ui/Card';
import Button from '../components/ui/Button';
import Input from '../components/ui/Input';
import Toggle from '../components/ui/Toggle';
import Pagination from '../components/ui/Pagination';
import ConfirmModal from '../components/ui/ConfirmModal';
import RuleModal from '../features/rules/RuleModal';
import { useRulesFetch } from '../hooks/useRulesFetch';

const RulesPage: React.FC = () => {
  const {
    rules,
    rawRules,
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
  } = useRulesFetch();

  return (
    <div className="min-h-screen">
      <Header title="WAF Protection Rules" />

      <div className="p-6 space-y-5">
        {/* Title area */}
        <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 animate-fade-in">
          <div>
            <h2 className="text-lg font-bold text-white">Signature Rules</h2>
            <p className="text-xs text-cyber-300 mt-0.5">
              {loading
                ? 'Loading…'
                : `${totalElements} rule${totalElements !== 1 ? 's' : ''} configured`}
            </p>
          </div>
          <div className="flex items-center gap-2">
            <button
              id="rules-refresh-btn"
              onClick={() => fetchRules(page, true)}
              disabled={refreshing || loading}
              className="p-2 rounded-lg text-cyber-300 hover:text-white hover:bg-cyber-700 transition-all disabled:opacity-40 cursor-pointer"
              title="Refresh"
            >
              <RefreshCw size={16} className={refreshing ? 'animate-spin' : ''} />
            </button>
            {rawRules.length === 0 && !loading && (
              <Button
                id="rules-seed-btn"
                variant="secondary"
                size="sm"
                onClick={handleSeedDefaults}
                disabled={seedingDefaults}
              >
                {seedingDefaults ? (
                  <>
                    <Loader2 size={16} className="mr-1.5 animate-spin" /> Load Default Rules
                  </>
                ) : (
                  <>
                    <Sliders size={16} className="mr-1.5" /> Load Default Rules
                  </>
                )}
              </Button>
            )}
            <Button
              id="rules-add-btn"
              variant="primary"
              size="sm"
              onClick={() => setCreateOpen(true)}
            >
              <Plus size={16} className="mr-1.5" /> Add Rule
            </Button>
          </div>
        </div>

        {/* Search */}
        <div className="flex gap-2 max-w-md animate-fade-in">
          <div className="flex-1">
            <Input
              id="rules-search"
              placeholder="Search by name, action or condition value…"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              icon={<Search size={15} />}
            />
          </div>
        </div>

        {/* Rules Table */}
        <Card className="animate-slide-up p-0 overflow-hidden">
          {loading ? (
            <div className="flex items-center justify-center py-24 gap-3 text-cyber-300">
              <Loader2 size={22} className="animate-spin" />
              <span className="text-sm">Loading signature rules…</span>
            </div>
          ) : rules.length === 0 ? (
            <div className="text-center py-24 text-cyber-400">
              <div className="w-14 h-14 mx-auto mb-4 rounded-2xl bg-cyber-850 border border-glass-border flex items-center justify-center">
                <ShieldAlert size={28} className="text-cyber-500" />
              </div>
              <p className="text-sm font-medium">No rules match your filters</p>
              {search && <p className="text-xs text-cyber-400 mt-1">Try clearing your search term.</p>}
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-glass-border">
                    <th className="text-left px-5 py-3.5 text-xs font-semibold text-cyber-300 uppercase tracking-wider">
                      Status
                    </th>
                    <th className="text-left px-5 py-3.5 text-xs font-semibold text-cyber-300 uppercase tracking-wider">
                      Rule Name
                    </th>
                    <th className="text-left px-5 py-3.5 text-xs font-semibold text-cyber-300 uppercase tracking-wider">
                      Action
                    </th>
                    <th className="text-left px-5 py-3.5 text-xs font-semibold text-cyber-300 uppercase tracking-wider hidden md:table-cell">
                      Conditions Count
                    </th>
                    <th className="text-right px-5 py-3.5 text-xs font-semibold text-cyber-300 uppercase tracking-wider">
                      Actions
                    </th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-glass-border/50">
                  {rules.map((rule) => {
                    const isToggling = toggling.has(rule.id);
                    return (
                      <tr
                        key={rule.id}
                        className="group hover:bg-cyber-700/20 transition-colors duration-150"
                      >
                        <td className="px-5 py-4 w-20">
                          <Toggle
                            checked={rule.is_active}
                            disabled={isToggling}
                            onChange={() => handleToggleActive(rule)}
                          />
                        </td>
                        <td className="px-5 py-4 font-medium text-white max-w-xs truncate">
                          {rule.name}
                        </td>
                        <td className="px-5 py-4">
                          <span
                            className={`inline-flex items-center px-2 py-0.5 rounded text-[10px] font-bold border
                            ${
                              rule.action === 'BLOCK'
                                ? 'text-accent-rose bg-accent-rose/10 border-accent-rose/30'
                                : rule.action === 'ALLOW'
                                ? 'text-accent-emerald bg-accent-emerald/10 border-accent-emerald/30'
                                : 'text-accent-yellow bg-accent-yellow/10 border-accent-yellow/30'
                            }`}
                          >
                            {rule.action}
                          </span>
                        </td>
                        <td className="px-5 py-4 text-cyber-400 font-mono text-xs hidden md:table-cell">
                          {rule.conditions.length} condition{rule.conditions.length !== 1 ? 's' : ''}
                        </td>
                        <td className="px-5 py-4 text-right">
                          <div className="flex items-center justify-end gap-1.5 opacity-0 group-hover:opacity-100 transition-opacity">
                            <button
                              id={`rules-edit-${rule.id}`}
                              onClick={() => setEditRule(rule)}
                              className="p-1.5 rounded-md text-cyber-300 hover:text-white hover:bg-cyber-700 transition-all cursor-pointer"
                              title="Edit Rule"
                            >
                              <Edit2 size={14} />
                            </button>
                            <button
                              id={`rules-delete-${rule.id}`}
                              onClick={() => setDeleteTarget(rule)}
                              className="p-1.5 rounded-md text-cyber-300 hover:text-accent-rose hover:bg-accent-rose/10 transition-all cursor-pointer"
                              title="Delete Rule"
                            >
                              <Trash2 size={14} />
                            </button>
                          </div>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}

          {/* Pagination */}
          <Pagination
            page={page}
            totalPages={totalPages}
            onPageChange={(p) => fetchRules(p)}
            loading={loading}
            idPrefix="rules"
          />
        </Card>
      </div>

      {/* Create & Edit Modals */}
      {(createOpen || editRule) && (
        <RuleModal
          rule={editRule}
          onClose={() => {
            setCreateOpen(false);
            setEditRule(null);
          }}
          onSaved={handleSaved}
        />
      )}

      {/* Delete confirmation Modal */}
      <ConfirmModal
        isOpen={!!deleteTarget}
        title="Delete Signature Rule"
        subtitle="This action is irreversible."
        description={
          <>
            Are you sure you want to delete rule{' '}
            <span className="font-semibold text-white">"{deleteTarget?.name}"</span>? Any traffic
            matching this signature will no longer be intercepted.
          </>
        }
        confirmLabel="Delete"
        cancelLabel="Cancel"
        variant="danger"
        isLoading={deleting}
        error={deleteError}
        onConfirm={handleDeleteConfirm}
        onClose={() => {
          setDeleteTarget(null);
          setDeleteError('');
        }}
      />
    </div>
  );
};

export default RulesPage;
