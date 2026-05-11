import React from 'react';
import { Settings, Construction } from 'lucide-react';
import Header from '../components/layout/Header';
import Card from '../components/ui/Card';

/**
 * SettingsPage — placeholder for Settings feature.
 */
const SettingsPage: React.FC = () => {
  return (
    <div className="min-h-screen">
      <Header title="Settings" />
      <div className="p-8">
        <Card className="animate-fade-in max-w-2xl mx-auto text-center py-16">
          <div className="w-16 h-16 mx-auto mb-6 rounded-2xl bg-accent-amber/10 flex items-center justify-center">
            <Settings size={32} className="text-accent-amber" />
          </div>
          <h3 className="text-xl font-bold text-white mb-2">Settings</h3>
          <div className="flex items-center justify-center gap-2 text-cyber-400 mb-4">
            <Construction size={16} />
            <span className="text-sm font-medium">Coming Soon</span>
          </div>
          <p className="text-sm text-cyber-400 max-w-md mx-auto">
            Configure system settings, notification preferences, user management,
            and integration options. This feature is currently under development.
          </p>
        </Card>
      </div>
    </div>
  );
};

export default SettingsPage;
