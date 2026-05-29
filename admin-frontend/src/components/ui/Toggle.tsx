

interface ToggleProps {
  checked: boolean;
  onChange: () => void;
  disabled?: boolean;
}

/**
 * Reusable Toggle switch component.
 */
export default function Toggle({ checked, onChange, disabled }: ToggleProps) {
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
