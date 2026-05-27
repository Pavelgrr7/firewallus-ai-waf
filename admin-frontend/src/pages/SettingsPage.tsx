import React, { useState, useEffect } from 'react';
import {
  Save,
  Loader2,
  AlertTriangle,
  CheckCircle2,
  X,
  Send,
  Sliders
} from 'lucide-react';
import Header from '../components/layout/Header';
import Card from '../components/ui/Card';
import Button from '../components/ui/Button';
import Input from '../components/ui/Input';
import {
  getSettings,
  updateSettings,
  type SettingsResponseDto,
  type UpdateSettingsDto
} from '../services/settingsService';

/* ===== Inline Sub-components ===== */

function Toast({ message, type, onClose }: { message: string; type: 'success' | 'error'; onClose: () => void }) {
  useEffect(() => {
    const t = setTimeout(onClose, 4000);
    return () => clearTimeout(t);
  }, [onClose]);

  return (
    <div
      className={`fixed bottom-6 right-6 z-50 flex items-center gap-3 px-4 py-3 rounded-xl border shadow-2xl
        animate-slide-up backdrop-blur-md transition-all
        ${type === 'success'
          ? 'bg-accent-emerald/10 border-accent-emerald/40 text-accent-emerald'
          : 'bg-accent-rose/10 border-accent-rose/40 text-accent-rose'
        }`}
    >
      {type === 'success' ? <CheckCircle2 size={16} /> : <AlertTriangle size={16} />}
      <span className="text-sm font-medium">{message}</span>
      <button onClick={onClose} className="ml-2 opacity-60 hover:opacity-100 transition-opacity">
        <X size={14} />
      </button>
    </div>
  );
}

const SettingsPage: React.FC = () => {
  // Original settings to identify modifications
  const [original, setOriginal] = useState<SettingsResponseDto | null>(null);

  // Form Fields
  const [rateLimitRequests, setRateLimitRequests] = useState<number>(100);
  const [rateLimitWindowSec, setRateLimitWindowSec] = useState<number>(60);
  const [tgBotToken, setTgBotToken] = useState<string>('');
  const [tgChatId, setTgChatId] = useState<string>('');
  const [alertThreshold, setAlertThreshold] = useState<number>(50);

  // Status
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' } | null>(null);

  const showToast = (message: string, type: 'success' | 'error') => setToast({ message, type });

  // Fetch settings on mount
  useEffect(() => {
    let active = true;
    const fetchSettingsData = async () => {
      try {
        const data = await getSettings();
        if (active) {
          setOriginal(data);
          setRateLimitRequests(data.rate_limit_requests);
          setRateLimitWindowSec(data.rate_limit_window_sec);
          setTgBotToken(data.tg_bot_token || '');
          setTgChatId(data.tg_chat_id || '');
          setAlertThreshold(data.alert_threshold);
        }
      } catch {
        if (active) showToast('Failed to load WAF settings.', 'error');
      } finally {
        if (active) setLoading(false);
      }
    };
    fetchSettingsData();
    return () => { active = false; };
  }, []);

  const validate = () => {
    const e: Record<string, string> = {};
    if (rateLimitRequests < 1) e.rateLimitRequests = 'Must be at least 1 request';
    if (rateLimitWindowSec < 1) e.rateLimitWindowSec = 'Must be at least 1 second';
    if (alertThreshold < 1) e.alertThreshold = 'Threshold must be at least 1';

    // If bot token is provided, suggest Chat ID is also required and vice versa
    if (tgBotToken && !tgChatId) {
      e.tgChatId = 'Chat ID is required if Bot Token is specified';
    }
    if (tgChatId && !tgBotToken) {
      e.tgBotToken = 'Bot Token is required if Chat ID is specified';
    }

    setErrors(e);
    return Object.keys(e).length === 0;
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validate()) return;
    setSaving(true);

    // Build diff DTO
    const payload: UpdateSettingsDto = {};
    if (rateLimitRequests !== original?.rate_limit_requests) {
      payload.rate_limit_requests = rateLimitRequests;
    }
    if (rateLimitWindowSec !== original?.rate_limit_window_sec) {
      payload.rate_limit_window_sec = rateLimitWindowSec;
    }
    const cleanToken = tgBotToken.trim() || null;
    if (cleanToken !== (original?.tg_bot_token || null)) {
      payload.tg_bot_token = cleanToken;
    }
    const cleanChatId = tgChatId.trim() || null;
    if (cleanChatId !== (original?.tg_chat_id || null)) {
      payload.tg_chat_id = cleanChatId;
    }
    if (alertThreshold !== original?.alert_threshold) {
      payload.alert_threshold = alertThreshold;
    }

    // If nothing changed, skip API call
    if (Object.keys(payload).length === 0) {
      showToast('No modifications detected.', 'success');
      setSaving(false);
      return;
    }

    try {
      const updated = await updateSettings(payload);
      setOriginal(updated);
      setRateLimitRequests(updated.rate_limit_requests);
      setRateLimitWindowSec(updated.rate_limit_window_sec);
      setTgBotToken(updated.tg_bot_token || '');
      setTgChatId(updated.tg_chat_id || '');
      setAlertThreshold(updated.alert_threshold);
      showToast('WAF configuration saved successfully.', 'success');
    } catch (err: any) {
      const errMsg = err.response?.data?.message || 'Failed to save WAF settings.';
      showToast(errMsg, 'error');
    } finally {
      setSaving(false);
    }
  };

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

      {/* Toast alert popup */}
      {toast && (
        <Toast
          message={toast.message}
          type={toast.type}
          onClose={() => setToast(null)}
        />
      )}
    </div>
  );
};

export default SettingsPage;
