import React from 'react';
import { LogOut, Bell, User } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import Button from '../ui/Button';

interface HeaderProps {
  title: string;
}

/**
 * Header — top bar showing page title, user info, and logout.
 */
const Header: React.FC<HeaderProps> = ({ title }) => {
  const { user, logout } = useAuth();

  return (
    <header className="sticky top-0 z-20 bg-cyber-900/70 backdrop-blur-xl border-b border-glass-border">
      <div className="flex items-center justify-between px-8 py-4">
        {/* Page Title */}
        <div>
          <h2 className="text-xl font-bold text-white">{title}</h2>
          <p className="text-xs text-cyber-400 mt-0.5">
            {new Date().toLocaleDateString('en-US', {
              weekday: 'long',
              year: 'numeric',
              month: 'long',
              day: 'numeric',
            })}
          </p>
        </div>

        {/* Right Side */}
        <div className="flex items-center gap-4">
          {/* Notifications Bell */}
          <button
            className="relative p-2 rounded-lg text-cyber-300 hover:text-white hover:bg-cyber-700 transition-all duration-200"
            title="Notifications"
          >
            <Bell size={18} />
            <span className="absolute top-1.5 right-1.5 w-2 h-2 bg-accent-rose rounded-full animate-pulse" />
          </button>

          {/* User Badge */}
          <div className="flex items-center gap-3 px-3 py-1.5 rounded-lg bg-cyber-800/50 border border-glass-border">
            <div className="w-7 h-7 rounded-full bg-gradient-to-br from-accent-blue to-accent-purple flex items-center justify-center">
              <User size={14} className="text-white" />
            </div>
            <div className="hidden sm:block">
              <p className="text-sm font-medium text-white leading-tight">
                {user?.username || 'Admin'}
              </p>
              <p className="text-[10px] text-cyber-400 uppercase tracking-wide">
                {user?.role || 'admin'}
              </p>
            </div>
          </div>

          {/* Logout */}
          <Button variant="ghost" size="sm" onClick={logout} className="gap-2">
            <LogOut size={16} />
            <span className="hidden sm:inline">Logout</span>
          </Button>
        </div>
      </div>
    </header>
  );
};

export default Header;
