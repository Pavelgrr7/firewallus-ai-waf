import React from 'react';
import { LogOut, User } from 'lucide-react';
import { useTranslation } from 'react-i18next';
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
  const { t, i18n } = useTranslation();
  const lang = (i18n.language || 'en').toUpperCase();

  const changeLanguage = (newLang: 'EN' | 'RU') => {
    i18n.changeLanguage(newLang.toLowerCase());
    localStorage.setItem('lng', newLang);
  };

  return (
    <header className="sticky top-0 z-20 bg-cyber-900/70 backdrop-blur-xl border-b border-glass-border">
      <div className="flex items-center justify-between px-8 py-4">
        {/* Page Title */}
        <div>
          <h2 className="text-xl font-bold text-white">{title}</h2>
          <p className="text-xs text-cyber-400 mt-0.5">
            {new Date().toLocaleDateString(lang === 'RU' ? 'ru-RU' : 'en-US', {
              weekday: 'long',
              year: 'numeric',
              month: 'long',
              day: 'numeric',
            })}
          </p>
        </div>

        {/* Right Side */}
        <div className="flex items-center gap-4">
          {/* Language Switcher */}
          <div className="flex items-center bg-cyber-800/60 p-0.5 rounded-lg border border-glass-border">
            <button
              onClick={() => changeLanguage('EN')}
              className={`px-2.5 py-1 text-xs font-bold rounded-md transition-all duration-200 cursor-pointer ${
                lang === 'EN'
                  ? 'bg-accent-blue/20 text-accent-blue border border-accent-blue/30 shadow-[0_0_10px_rgba(59,130,246,0.15)]'
                  : 'text-cyber-400 hover:text-white hover:bg-cyber-700/30 border border-transparent'
              }`}
            >
              EN
            </button>
            <button
              onClick={() => changeLanguage('RU')}
              className={`px-2.5 py-1 text-xs font-bold rounded-md transition-all duration-200 cursor-pointer ${
                lang === 'RU'
                  ? 'bg-accent-blue/20 text-accent-blue border border-accent-blue/30 shadow-[0_0_10px_rgba(59,130,246,0.15)]'
                  : 'text-cyber-400 hover:text-white hover:bg-cyber-700/30 border border-transparent'
              }`}
            >
              RU
            </button>
          </div>

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
            <span className="hidden sm:inline">{t('header.logout')}</span>
          </Button>
        </div>
      </div>
    </header>
  );
};

export default Header;
