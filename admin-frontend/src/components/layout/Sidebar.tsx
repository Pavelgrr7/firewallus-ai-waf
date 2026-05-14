import React from 'react';
import { NavLink } from 'react-router-dom';
import { LayoutDashboard, Shield, Settings, ShieldCheck } from 'lucide-react';

interface NavItem {
  label: string;
  path: string;
  icon: React.ReactNode;
  badge?: string;
}

const navItems: NavItem[] = [
  { label: 'Dashboard', path: '/', icon: <LayoutDashboard size={20} /> },
  { label: 'Rule Management', path: '/rules', icon: <Shield size={20} />, badge: 'Soon' },
  { label: 'Settings', path: '/settings', icon: <Settings size={20} />, badge: 'Soon' },
];

/**
 * Sidebar — fixed left navigation with links and branding.
 */
const Sidebar: React.FC = () => {
  return (
    <aside className="fixed left-0 top-0 h-screen w-64 bg-cyber-800/80 backdrop-blur-xl border-r border-glass-border flex flex-col z-30">
      {/* Brand */}
      <div className="flex items-center gap-3 px-6 py-6 border-b border-glass-border">
        <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-accent-blue to-accent-cyan flex items-center justify-center shadow-lg shadow-accent-blue/20">
          <ShieldCheck size={22} className="text-white" />
        </div>
        <div>
          <h1 className="text-base font-bold text-white tracking-tight">Firewallus</h1>
          <p className="text-[11px] text-cyber-300 font-medium tracking-wider uppercase">WAF Admin</p>
        </div>
      </div>

      {/* Navigation Links */}
      <nav className="flex-1 px-3 py-4 space-y-1">
        {navItems.map((item) => (
          <NavLink
            key={item.path}
            to={item.path}
            end={item.path === '/'}
            className={({ isActive }) =>
              `flex items-center gap-3 px-4 py-2.5 rounded-lg text-sm font-medium transition-all duration-200 group ${
                isActive
                  ? 'bg-accent-blue/15 text-accent-blue border border-accent-blue/20 shadow-sm'
                  : 'text-cyber-200 hover:bg-cyber-700/60 hover:text-white border border-transparent'
              }`
            }
          >
            <span className="group-hover:scale-110 transition-transform duration-200">
              {item.icon}
            </span>
            <span>{item.label}</span>
            {item.badge && (
              <span className="ml-auto text-[10px] font-semibold uppercase tracking-wide bg-cyber-600 text-cyber-300 px-2 py-0.5 rounded-full">
                {item.badge}
              </span>
            )}
          </NavLink>
        ))}
      </nav>

      {/* Footer */}
      <div className="px-6 py-4 border-t border-glass-border">
        <p className="text-[11px] text-cyber-400">© 2026 Firewallus</p>
        <p className="text-[10px] text-cyber-500">AI-Powered WAF v1.0</p>
      </div>
    </aside>
  );
};

export default Sidebar;
