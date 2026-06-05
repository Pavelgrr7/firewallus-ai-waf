import React, { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { AlertTriangle, Shield, X } from 'lucide-react';
import Button from '../../components/ui/Button';
import Input from '../../components/ui/Input';
import {
  addManagedIp,
  type IpListType,
  type ManagedIpResponseDto,
} from '../../services/accessControlService';

const IP_REGEX = /^([0-9]{1,3}\.){3}[0-9]{1,3}$|^([0-9a-fA-F]{0,4}:){2,7}[0-9a-fA-F]{0,4}$/;

interface AddIpModalProps {
  defaultType: IpListType;
  onClose: () => void;
  onSaved: (saved: ManagedIpResponseDto) => void;
}

/**
 * Modal dialog to add a managed IP to blacklist or whitelist.
 */
export default function AddIpModal({ defaultType, onClose, onSaved }: AddIpModalProps) {
  const { t } = useTranslation();
  const [ipAddress, setIpAddress] = useState('');
  const [listType, setListType] = useState<IpListType>(defaultType);
  const [description, setDescription] = useState('');
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState<Record<string, string>>({});

  const validate = () => {
    const e: Record<string, string> = {};
    if (!ipAddress.trim()) {
      e.ipAddress = t('access_control.modal.errors.ip_required');
    } else if (!IP_REGEX.test(ipAddress.trim())) {
      e.ipAddress = t('access_control.modal.errors.ip_invalid');
    }
    if (description && description.length > 255) {
      e.description = t('access_control.modal.errors.desc_max');
    }
    setErrors(e);
    return Object.keys(e).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validate()) return;
    setLoading(true);
    try {
      const saved = await addManagedIp({
        ip_address: ipAddress.trim(),
        list_type: listType,
        description: description.trim() || null,
      });
      onSaved(saved);
    } catch (err) {
      const axiosError = err as { response?: { data?: { message?: string } } };
      const errMsg = axiosError.response?.data?.message || t('access_control.modal.errors.add_failed');
      setErrors({ submit: errMsg });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    const handleEsc = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose();
    };
    window.addEventListener('keydown', handleEsc);
    return () => window.removeEventListener('keydown', handleEsc);
  }, [onClose]);

  const selectClass =
    'w-full rounded-lg border border-cyber-500 bg-cyber-800/60 px-4 py-2.5 text-sm text-cyber-550 focus:outline-none focus:ring-2 focus:ring-accent-blue/50 focus:border-accent-blue transition-all cursor-pointer text-white';

  return (
    <div
      className="fixed inset-0 z-40 flex items-center justify-center"
      onClick={(e) => {
        if (e.target === e.currentTarget) onClose();
      }}
    >
      <div className="absolute inset-0 bg-cyber-900/80 backdrop-blur-sm" />
      <div className="relative z-50 w-full max-w-md mx-4 glass-card p-0 animate-slide-up overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-glass-border">
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 rounded-lg bg-accent-blue/10 flex items-center justify-center">
              <Shield size={16} className="text-accent-blue" />
            </div>
            <h2 className="text-base font-semibold text-white">{t('access_control.modal.title')}</h2>
          </div>
          <button onClick={onClose} className="text-cyber-300 hover:text-white transition-colors cursor-pointer">
            <X size={20} />
          </button>
        </div>

        {/* Form Body */}
        <form onSubmit={handleSubmit} className="px-6 py-5 space-y-4">
          <Input
            id="ip-address-input"
            label={t('access_control.modal.ip_address')}
            placeholder={t('access_control.modal.ip_placeholder')}
            value={ipAddress}
            onChange={(e) => setIpAddress(e.target.value)}
            error={errors.ipAddress}
          />

          <div>
            <label className="block text-sm font-medium text-cyber-200 mb-2">{t('access_control.modal.list_type')}</label>
            <select
              id="list-type-select"
              value={listType}
              onChange={(e) => setListType(e.target.value as IpListType)}
              className={selectClass}
            >
              <option value="BLACKLIST" className="bg-cyber-800">
                BLACKLIST
              </option>
              <option value="WHITELIST" className="bg-cyber-800">
                WHITELIST
              </option>
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-cyber-200 mb-2">{t('access_control.modal.description')}</label>
            <textarea
              id="description-input"
              placeholder={t('access_control.modal.desc_placeholder')}
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              className="w-full h-20 rounded-lg border border-cyber-500 bg-cyber-800/60 px-4 py-2 text-sm text-cyber-50 placeholder-cyber-500 focus:outline-none focus:ring-2 focus:ring-accent-blue/50 focus:border-accent-blue transition-all"
            />
            {errors.description && <p className="text-xs text-accent-rose mt-1">{errors.description}</p>}
          </div>

          {errors.submit && (
            <p className="text-xs text-accent-rose flex items-center gap-1">
              <AlertTriangle size={12} /> {errors.submit}
            </p>
          )}

          <div className="flex justify-end gap-3 pt-2">
            <Button type="button" variant="ghost" size="sm" onClick={onClose} disabled={loading}>
              {t('common.cancel')}
            </Button>
            <Button type="submit" variant="primary" size="sm" isLoading={loading}>
              {t('access_control.modal.add_to_list')}
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
}
