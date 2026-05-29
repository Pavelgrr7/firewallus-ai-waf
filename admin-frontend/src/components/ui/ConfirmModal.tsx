import React, { useEffect } from 'react';
import { AlertTriangle, Trash2 } from 'lucide-react';
import Button from './Button';

interface ConfirmModalProps {
  isOpen: boolean;
  title: string;
  subtitle?: string;
  description: React.ReactNode;
  confirmLabel?: string;
  cancelLabel?: string;
  variant?: 'danger' | 'primary' | 'secondary';
  isLoading?: boolean;
  error?: string;
  onConfirm: () => void;
  onClose: () => void;
}

/**
 * Reusable Confirmation Modal dialog for dangerous/destructive actions (like deletions).
 */
export default function ConfirmModal({
  isOpen,
  title,
  subtitle,
  description,
  confirmLabel = 'Confirm',
  cancelLabel = 'Cancel',
  variant = 'danger',
  isLoading = false,
  error,
  onConfirm,
  onClose,
}: ConfirmModalProps) {
  useEffect(() => {
    if (!isOpen) return;
    const handleEsc = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose();
    };
    window.addEventListener('keydown', handleEsc);
    return () => window.removeEventListener('keydown', handleEsc);
  }, [isOpen, onClose]);

  if (!isOpen) return null;

  return (
    <div
      className="fixed inset-0 z-40 flex items-center justify-center"
      onClick={(e) => {
        if (e.target === e.currentTarget) onClose();
      }}
    >
      <div className="absolute inset-0 bg-cyber-900/80 backdrop-blur-sm" />
      <div className="relative z-50 w-full max-w-md mx-4 glass-card p-6 animate-slide-up">
        <div className="flex items-center gap-3 mb-4">
          <div
            className={`w-10 h-10 rounded-xl flex items-center justify-center ${
              variant === 'danger' ? 'bg-accent-rose/10' : 'bg-accent-blue/10'
            }`}
          >
            <Trash2
              size={18}
              className={variant === 'danger' ? 'text-accent-rose' : 'text-accent-blue'}
            />
          </div>
          <div>
            <h2 className="text-base font-semibold text-white">{title}</h2>
            {subtitle && <p className="text-xs text-cyber-300">{subtitle}</p>}
          </div>
        </div>

        <div className="text-sm text-cyber-200 mb-5">{description}</div>

        {error && (
          <p className="text-xs text-accent-rose mb-4 flex items-center gap-1">
            <AlertTriangle size={12} /> {error}
          </p>
        )}

        <div className="flex justify-end gap-3">
          <Button variant="ghost" size="sm" onClick={onClose} disabled={isLoading}>
            {cancelLabel}
          </Button>
          <Button variant={variant} size="sm" isLoading={isLoading} onClick={onConfirm}>
            {confirmLabel}
          </Button>
        </div>
      </div>
    </div>
  );
}
