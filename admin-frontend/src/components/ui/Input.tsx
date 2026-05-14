import React from 'react';

interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  error?: string;
  icon?: React.ReactNode;
}

/**
 * Reusable styled Input component with label and error support.
 */
const Input: React.FC<InputProps> = ({ label, error, icon, className = '', id, ...props }) => {
  const inputId = id || label?.toLowerCase().replace(/\s+/g, '-');

  return (
    <div className="w-full">
      {label && (
        <label
          htmlFor={inputId}
          className="block text-sm font-medium text-cyber-200 mb-2"
        >
          {label}
        </label>
      )}
      <div className="relative">
        {icon && (
          <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-cyber-300">
            {icon}
          </div>
        )}
        <input
          id={inputId}
          className={`
            w-full rounded-lg border border-cyber-500 bg-cyber-800/60
            px-4 py-2.5 text-sm text-cyber-50
            placeholder-cyber-400
            focus:outline-none focus:ring-2 focus:ring-accent-blue/50 focus:border-accent-blue
            transition-all duration-200
            ${icon ? 'pl-10' : ''}
            ${error ? 'border-accent-rose focus:ring-accent-rose/50' : ''}
            ${className}
          `}
          {...props}
        />
      </div>
      {error && (
        <p className="mt-1.5 text-xs text-accent-rose">{error}</p>
      )}
    </div>
  );
};

export default Input;
