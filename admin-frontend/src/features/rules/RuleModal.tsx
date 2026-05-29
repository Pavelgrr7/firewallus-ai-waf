import React, { useState, useEffect } from 'react';
import { AlertTriangle, Plus, Shield, X } from 'lucide-react';
import Button from '../../components/ui/Button';
import Input from '../../components/ui/Input';
import Toggle from '../../components/ui/Toggle';
import ConditionRow from './ConditionRow';
import {
  createRule,
  updateRule,
  type Action,
  type Condition,
  type CreateRuleDto,
  type RuleResponseDto,
  type UpdateRuleDto,
} from '../../services/ruleService';

const ACTIONS: Action[] = ['BLOCK', 'ALLOW', 'LOG'];
const EMPTY_CONDITION: Condition = { target: 'IP', target_key: null, operator: 'EQUALS', value: '' };

interface RuleModalProps {
  rule: RuleResponseDto | null;
  onClose: () => void;
  onSaved: (rule: RuleResponseDto) => void;
}

/**
 * Modal dialog for creating or editing WAF rules.
 */
export default function RuleModal({ rule, onClose, onSaved }: RuleModalProps) {
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
    setConditions((prev) => prev.map((p, i) => (i === idx ? c : p)));
  const addCondition = () => setConditions((prev) => [...prev, { ...EMPTY_CONDITION }]);
  const removeCondition = (idx: number) => setConditions((prev) => prev.filter((_, i) => i !== idx));

  const validate = () => {
    const e: Record<string, string> = {};
    if (!name.trim()) e.name = 'Name is required';
    else if (name.length > 64) e.name = 'Max 64 characters';
    if (
      conditions.some(
        (c) => !c.value.trim() || (c.target === 'HEADER' && !c.target_key?.trim())
      )
    ) {
      e.conditions = 'All condition fields are required';
    }
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
    const handler = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose();
    };
    window.addEventListener('keydown', handler);
    return () => window.removeEventListener('keydown', handler);
  }, [onClose]);

  const selectClass =
    'w-full rounded-lg border border-cyber-500 bg-cyber-800/60 px-4 py-2.5 text-sm text-cyber-50 focus:outline-none focus:ring-2 focus:ring-accent-blue/50 focus:border-accent-blue transition-all cursor-pointer';

  return (
    <div
      className="fixed inset-0 z-40 flex items-center justify-center"
      onClick={(e) => {
        if (e.target === e.currentTarget) onClose();
      }}
    >
      <div className="absolute inset-0 bg-cyber-900/80 backdrop-blur-sm" />
      <div className="relative z-50 w-full max-w-xl mx-4 glass-card p-0 animate-slide-up overflow-hidden flex flex-col max-h-[90vh]">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-glass-border shrink-0">
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 rounded-lg bg-accent-purple/10 flex items-center justify-center">
              <Shield size={16} className="text-accent-purple" />
            </div>
            <h2 className="text-base font-semibold text-white">
              {isEdit ? 'Edit Rule' : 'New Rule'}
            </h2>
          </div>
          <button onClick={onClose} className="text-cyber-300 hover:text-white transition-colors cursor-pointer">
            <X size={20} />
          </button>
        </div>
        {/* Body */}
        <form onSubmit={handleSubmit} className="px-6 py-5 space-y-4 overflow-y-auto">
          <Input
            id="rule-name"
            label="Rule Name"
            placeholder="e.g. Block SQLMap Scanners"
            value={name}
            onChange={(e) => setName(e.target.value)}
            error={errors.name}
            maxLength={64}
          />
          {/* Action */}
          <div>
            <label className="block text-sm font-medium text-cyber-200 mb-2">Action</label>
            <select
              id="rule-action"
              value={action}
              onChange={(e) => setAction(e.target.value as Action)}
              className={selectClass}
            >
              {ACTIONS.map((a) => (
                <option key={a} value={a} className="bg-cyber-800">
                  {a}
                </option>
              ))}
            </select>
          </div>
          {/* Conditions */}
          <div>
            <div className="flex items-center justify-between mb-2">
              <label className="text-sm font-medium text-cyber-200">
                Conditions <span className="text-cyber-400 text-xs">(AND logic)</span>
              </label>
              <button
                type="button"
                onClick={addCondition}
                className="flex items-center gap-1 text-xs text-accent-blue hover:text-accent-cyan transition-colors cursor-pointer"
              >
                <Plus size={13} /> Add
              </button>
            </div>
            <div className="space-y-2">
              {conditions.map((c, i) => (
                <ConditionRow
                  key={i}
                  cond={c}
                  idx={i}
                  onChange={updateCondition}
                  onRemove={removeCondition}
                  canRemove={conditions.length > 1}
                />
              ))}
            </div>
            {errors.conditions && (
              <p className="text-xs text-accent-rose mt-1 flex items-center gap-1">
                <AlertTriangle size={12} />
                {errors.conditions}
              </p>
            )}
          </div>
          {/* Active toggle (create only) */}
          {!isEdit && (
            <div className="flex items-center justify-between py-1">
              <span className="text-sm font-medium text-cyber-200">Enable immediately</span>
              <Toggle checked={is_active} onChange={() => setIsActive((v) => !v)} />
            </div>
          )}
          {errors.submit && (
            <p className="text-xs text-accent-rose flex items-center gap-1">
              <AlertTriangle size={12} />
              {errors.submit}
            </p>
          )}
          <div className="flex justify-end gap-3 pt-2">
            <Button type="button" variant="ghost" size="sm" onClick={onClose} disabled={loading}>
              Cancel
            </Button>
            <Button type="submit" variant="primary" size="sm" isLoading={loading}>
              {isEdit ? 'Save Changes' : 'Create Rule'}
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
}
