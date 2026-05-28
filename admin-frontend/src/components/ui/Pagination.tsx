import { ChevronLeft, ChevronRight } from 'lucide-react';

interface PaginationProps {
  page: number; // 0-indexed
  totalPages: number;
  onPageChange: (p: number) => void;
  loading?: boolean;
  idPrefix: string;
}

/**
 * Reusable Pagination component with dynamic page range and custom ID prefix.
 */
export default function Pagination({
  page,
  totalPages,
  onPageChange,
  loading = false,
  idPrefix,
}: PaginationProps) {
  if (totalPages <= 1) return null;

  const startPage = Math.max(0, Math.min(page - 2, totalPages - 5));
  const pagesToShow = Math.min(totalPages, 5);

  return (
    <div className="flex items-center justify-between px-5 py-3.5 border-t border-glass-border">
      <span className="text-xs text-cyber-400 font-medium">
        Page {page + 1} of {totalPages}
      </span>
      <div className="flex items-center gap-1">
        {/* Previous Page Button */}
        <button
          id={`${idPrefix}-prev-page`}
          onClick={() => onPageChange(page - 1)}
          disabled={page === 0 || loading}
          className="p-1.5 rounded-md text-cyber-300 hover:text-white hover:bg-cyber-700 transition-all disabled:opacity-30 disabled:cursor-not-allowed cursor-pointer"
          title="Previous Page"
        >
          <ChevronLeft size={16} />
        </button>

        {/* Page Numbers */}
        {Array.from({ length: pagesToShow }, (_, i) => {
          const idx = startPage + i;
          const isActive = idx === page;
          return (
            <button
              key={idx}
              id={`${idPrefix}-page-${idx}`}
              onClick={() => onPageChange(idx)}
              className={`w-7 h-7 rounded-md text-xs font-semibold transition-all cursor-pointer
                ${isActive
                  ? 'bg-accent-blue text-white shadow-md shadow-accent-blue/20'
                  : 'text-cyber-300 hover:text-white hover:bg-cyber-700'
                }`}
            >
              {idx + 1}
            </button>
          );
        })}

        {/* Next Page Button */}
        <button
          id={`${idPrefix}-next-page`}
          onClick={() => onPageChange(page + 1)}
          disabled={page >= totalPages - 1 || loading}
          className="p-1.5 rounded-md text-cyber-300 hover:text-white hover:bg-cyber-700 transition-all disabled:opacity-30 disabled:cursor-not-allowed cursor-pointer"
          title="Next Page"
        >
          <ChevronRight size={16} />
        </button>
      </div>
    </div>
  );
}
