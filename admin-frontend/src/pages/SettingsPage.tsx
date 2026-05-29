import React from 'react';
import {
  Loader2,
  Save,
  Send,
  Sliders,
} from 'lucide-react';
import Header from '../components/layout/Header';
import Card from '../components/ui/Card';
import Button from '../components/ui/Button';
import Input from '../components/ui/Input';
import { useWafSettings } from '../hooks/useWafSettings';

const SettingsPage: React.FC = () => {
  const {
    rateLimitRequests,
    setRateLimitRequests,
    rateLimitWindowSec,
    setRateLimitWindowSec,
    tgBotToken,
    setTgBotToken,
    tgChatId,
    setTgChatId,
    alertThreshold,
    setAlertThreshold,
    loading,
    saving,
    errors,
    handleSave,
  } = useWafSettings();

  return (
    <div className="min-h-screen">
      <Header title="WAF Settings" />

      <div className="p-6 max-w-4xl mx-auto space-y-6">
        {/* Title row */}
        <div className="flex items-center justify-between animate-fade-in">
          <div>
            <h2 className="text-lg font-bold text-white">WAF Configurations</h2>
            <p className="text-xs text-cyber-300 mt-0.5">
              Tune rate limiting parameters and critical alerting channels.
            </p>
          </div>
        </div>

        {loading ? (
          <div className="flex items-center justify-center py-32 gap-3 text-cyber-300">
            <Loader2 size={24} className="animate-spin text-accent-blue" />
            <span className="text-sm">Fetching settings data...</span>
          </div>
        ) : (
          <form onSubmit={handleSave} className="space-y-6 animate-slide-up">
            {/* Rate Limiting Settings */}
            <Card className="p-6">
              <div className="flex items-center gap-3 border-b border-glass-border pb-4 mb-5">
                <div className="w-8 h-8 rounded-lg bg-accent-blue/10 flex items-center justify-center">
                  <Sliders size={16} className="text-accent-blue" />
                </div>
                <div>
                  <h3 className="text-base font-bold text-white">Rate Limiting</h3>
                  <p className="text-xs text-cyber-400">Control incoming traffic thresholds dynamically.</p>
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <Input
                  id="settings-rate-requests"
                  label="Requests Limit"
                  type="number"
                  placeholder="e.g. 100"
                  value={rateLimitRequests}
                  onChange={(e) => setRateLimitRequests(Number(e.target.value))}
                  error={errors.rateLimitRequests}
                />
                <Input
                  id="settings-rate-window"
                  label="Rate Limit Window (seconds)"
                  type="number"
                  placeholder="e.g. 60"
                  value={rateLimitWindowSec}
                  onChange={(e) => setRateLimitWindowSec(Number(e.target.value))}
                  error={errors.rateLimitWindowSec}
                />
              </div>
            </Card>

            {/* Telegram Integration Settings */}
            <Card className="p-6">
              <div className="flex items-center gap-3 border-b border-glass-border pb-4 mb-5">
                <div className="w-8 h-8 rounded-lg bg-accent-purple/10 flex items-center justify-center">
                  <Send size={16} className="text-accent-purple" />
                </div>
                <div>
                  <h3 className="text-base font-bold text-white">Telegram Integration & Alerts</h3>
                  <p className="text-xs text-cyber-400">Send notifications automatically when sliding window thresholds are breached.</p>
                </div>
              </div>

              <div className="space-y-5">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <Input
                    id="settings-tg-token"
                    label="Bot Token"
                    placeholder="Enter Telegram Bot Token"
                    value={tgBotToken}
                    onChange={(e) => setTgBotToken(e.target.value)}
                    error={errors.tgBotToken}
                  />
                  <Input
                    id="settings-tg-chat"
                    label="Chat ID"
                    placeholder="Enter Telegram Chat ID"
                    value={tgChatId}
                    onChange={(e) => setTgChatId(e.target.value)}
                    error={errors.tgChatId}
                  />
                </div>
                <div className="w-full md:w-1/2 md:pr-3">
                  <Input
                    id="settings-alert-threshold"
                    label="Incident Alert Threshold (attacks/min)"
                    type="number"
                    placeholder="e.g. 50"
                    value={alertThreshold}
                    onChange={(e) => setAlertThreshold(Number(e.target.value))}
                    error={errors.alertThreshold}
                  />
                </div>
              </div>
            </Card>

            {/* Submit Button */}
            <div className="flex items-center justify-end gap-4">
              <Button
                id="settings-save-btn"
                type="submit"
                variant="primary"
                size="md"
                isLoading={saving}
                className="gap-2"
              >
                <Save size={16} />
                Save Configurations
              </Button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
};

export default SettingsPage;
