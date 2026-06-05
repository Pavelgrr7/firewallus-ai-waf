/* eslint-disable react-refresh/only-export-components */
import { X } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import type { Condition, Target, Operator } from '../../services/ruleService';

export const TARGETS: Target[] = ['IP', 'URI', 'HEADER', 'METHOD'];
export const OPERATORS: Operator[] = ['EQUALS', 'CONTAINS', 'REGEX'];

interface ConditionRowProps {
  cond: Condition;
  idx: number;
  onChange: (idx: number, c: Condition) => void;
  onRemove: (idx: number) => void;
  canRemove: boolean;
}

/**
 * Renders a single row of a WAF rule's conditions.
 */
export default function ConditionRow({
  cond,
  idx,
  onChange,
  onRemove,
  canRemove,
}: ConditionRowProps) {
  const { t } = useTranslation();
  const selClass =
    'w-full rounded-lg border border-cyber-500 bg-cyber-800/60 px-3 py-2 text-sm text-cyber-550 focus:outline-none focus:ring-2 focus:ring-accent-blue/50 focus:border-accent-blue transition-all cursor-pointer text-white';

  return (
    <div className="p-3 rounded-lg border border-cyber-600/50 bg-cyber-800/30 space-y-2">
      <div className="flex items-center justify-between">
        <span className="text-xs font-semibold text-cyber-300 uppercase tracking-wider">
          {t('rules.condition.condition_label', { num: idx + 1 })}
        </span>
        {canRemove && (
          <button
            type="button"
            onClick={() => onRemove(idx)}
            className="text-cyber-400 hover:text-accent-rose transition-colors cursor-pointer"
          >
            <X size={14} />
          </button>
        )}
      </div>
      <div className="grid grid-cols-2 gap-2">
        <div>
          <label className="block text-xs text-cyber-400 mb-1">{t('rules.condition.target')}</label>
          <select
            className={selClass}
            value={cond.target}
            onChange={(e) => {
              const t = e.target.value as Target;
              onChange(idx, {
                ...cond,
                target: t,
                target_key: t === 'HEADER' ? cond.target_key ?? '' : null,
              });
            }}
          >
            {TARGETS.map((targetVal) => (
              <option key={targetVal} value={targetVal} className="bg-cyber-800">
                {targetVal}
              </option>
            ))}
          </select>
        </div>
        <div>
          <label className="block text-xs text-cyber-400 mb-1">{t('rules.condition.operator')}</label>
          <select
            className={selClass}
            value={cond.operator}
            onChange={(e) =>
              onChange(idx, { ...cond, operator: e.target.value as Operator })
            }
          >
            {OPERATORS.map((o) => (
              <option key={o} value={o} className="bg-cyber-800">
                {o}
              </option>
            ))}
          </select>
        </div>
      </div>
      {cond.target === 'HEADER' && (
        <div>
          <label className="block text-xs text-cyber-400 mb-1">{t('rules.condition.header_name')}</label>
          <input
            className="w-full rounded-lg border border-cyber-500 bg-cyber-800/60 px-3 py-2 text-sm text-cyber-50 placeholder-cyber-500 focus:outline-none focus:ring-2 focus:ring-accent-blue/50 focus:border-accent-blue transition-all"
            placeholder={t('rules.condition.header_placeholder')}
            value={cond.target_key ?? ''}
            onChange={(e) => onChange(idx, { ...cond, target_key: e.target.value })}
          />
        </div>
      )}
      <div>
        <label className="block text-xs text-cyber-400 mb-1">{t('rules.condition.value')}</label>
        <input
          className="w-full rounded-lg border border-cyber-500 bg-cyber-800/60 px-3 py-2 text-sm text-cyber-50 placeholder-cyber-500 focus:outline-none focus:ring-2 focus:ring-accent-blue/50 focus:border-accent-blue transition-all"
          placeholder={t('rules.condition.value_placeholder')}
          value={cond.value}
          onChange={(e) => onChange(idx, { ...cond, value: e.target.value })}
        />
      </div>
    </div>
  );
}
