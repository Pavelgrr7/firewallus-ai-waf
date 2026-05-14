import React from 'react';

interface CardProps {
  children: React.ReactNode;
  className?: string;
  /** Add the animated glow border effect */
  glow?: boolean;
}

/**
 * Reusable glass-morphism card container.
 */
const Card: React.FC<CardProps> = ({ children, className = '', glow = false }) => {
  return (
    <div
      className={`
        glass-card p-6
        ${glow ? 'glow-blue' : ''}
        transition-all duration-300 hover:border-cyber-400/30
        ${className}
      `}
    >
      {children}
    </div>
  );
};

export default Card;
