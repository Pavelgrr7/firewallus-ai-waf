import { useState, useEffect } from 'react';
import { useToast } from '../context/ToastContext';
import {
  getSettings,
  updateSettings,
  type SettingsResponseDto,
  type UpdateSettingsDto,
} from '../services/settingsService';

/**
 * Hook to manage WAF global configuration settings.
 */
export function useWafSettings() {
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

  const { showToast } = useToast();

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
    return () => {
      active = false;
    };
  }, [showToast]);

  const validate = () => {
    const e: Record<string, string> = {};
    if (rateLimitRequests < 1) e.rateLimitRequests = 'Must be at least 1 request';
    if (rateLimitWindowSec < 1) e.rateLimitWindowSec = 'Must be at least 1 second';
    if (alertThreshold < 1) e.alertThreshold = 'Threshold must be at least 1';

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
    } catch (err) {
      const axiosError = err as { response?: { data?: { message?: string } } };
      const errMsg = axiosError.response?.data?.message || 'Failed to save WAF settings.';
      showToast(errMsg, 'error');
    } finally {
      setSaving(false);
    }
  };

  return {
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
  };
}
