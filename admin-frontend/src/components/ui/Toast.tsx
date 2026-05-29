import { useEffect } from 'react';
import { CheckCircle2, AlertTriangle, X } from 'lucide-react';

export interface ToastProps {
  message: string;
  type: 'success' | 'error';
  onClose: () => void;
}

/**
 * Reusable inline/popup Toast notification component.
 */
export default function Toast({ message, type, onClose }: ToastProps) {
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
      <button onClick={onClose} className="ml-2 opacity-60 hover:opacity-100 transition-opacity cursor-pointer">
        <X size={14} />
      </button>
    </div>
  );
}
