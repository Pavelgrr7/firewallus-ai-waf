import React from 'react';
import { Shield, Construction } from 'lucide-react';
import Header from '../components/layout/Header';
import Card from '../components/ui/Card';

/**
 * RulesPage — placeholder for Rule Management feature.
 */
const RulesPage: React.FC = () => {
  return (
    <div className="min-h-screen">
      <Header title="Rule Management" />
      <div className="p-8">
        <Card className="animate-fade-in max-w-2xl mx-auto text-center py-16">
          <div className="w-16 h-16 mx-auto mb-6 rounded-2xl bg-accent-purple/10 flex items-center justify-center">
            <Shield size={32} className="text-accent-purple" />
          </div>
          <h3 className="text-xl font-bold text-white mb-2">Rule Management</h3>
          <div className="flex items-center justify-center gap-2 text-cyber-400 mb-4">
            <Construction size={16} />
            <span className="text-sm font-medium">Coming Soon</span>
          </div>
          <p className="text-sm text-cyber-400 max-w-md mx-auto">
            Manage WAF rules, create custom rule sets, and configure blocking policies.
            This feature is currently under development.
          </p>
        </Card>
      </div>
    </div>
  );
};

export default RulesPage;
