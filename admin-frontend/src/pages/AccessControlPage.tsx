import React, { useState, useEffect, useCallback } from 'react';
import {
  Ban,
  CheckCircle2,
  AlertTriangle,
  Plus,
  Trash2,
  X,
  Search,
  Loader2,
  ChevronLeft,
  ChevronRight,
  RefreshCw,
  Shield,
  ShieldAlert,
  Calendar,
  FileText
} from 'lucide-react';
import Header from '../components/layout/Header';
import Card from '../components/ui/Card';
import Button from '../components/ui/Button';
import Input from '../components/ui/Input';
import {
  getManagedIps,
  addManagedIp,
  deleteManagedIp,
  type IpListType,
  type ManagedIpResponseDto
} from '../services/accessControlService';

const PAGE_SIZE = 10;
const IP_REGEX = /^([0-9]{1,3}\.){3}[0-9]{1,3}$|^([0-9a-fA-F]{0,4}:){2,7}[0-9a-fA-F]{0,4}$/;

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

/* ===== Add IP Modal ===== */

interface AddIpModalProps {
  defaultType: IpListType;
  onClose: () => void;
  onSaved: (saved: ManagedIpResponseDto) => void;
}

function AddIpModal({ defaultType, onClose, onSaved }: AddIpModalProps) {
  const [ipAddress, setIpAddress] = useState('');
  const [listType, setListType] = useState<IpListType>(defaultType);
  const [description, setDescription] = useState('');
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState<Record<string, string>>({});

  const validate = () => {
    const e: Record<string, string> = {};
    if (!ipAddress.trim()) {
      e.ipAddress = 'IP Address is required';
    } else if (!IP_REGEX.test(ipAddress.trim())) {
      e.ipAddress = 'Invalid IP Address format (IPv4 or IPv6 expected)';
    }
    if (description && description.length > 255) {
      e.description = 'Max 255 characters';
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
        description: description.trim() || null
      });
      onSaved(saved);
    } catch (err: any) {
      const errMsg = err.response?.data?.message || 'Failed to add IP address.';
      setErrors({ submit: errMsg });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    const handleEsc = (e: KeyboardEvent) => { if (e.key === 'Escape') onClose(); };
    window.addEventListener('keydown', handleEsc);
    return () => window.removeEventListener('keydown', handleEsc);
  }, [onClose]);

  const selectClass = 'w-full rounded-lg border border-cyber-500 bg-cyber-800/60 px-4 py-2.5 text-sm text-cyber-50 focus:outline-none focus:ring-2 focus:ring-accent-blue/50 focus:border-accent-blue transition-all cursor-pointer';

  return (
    <div className="fixed inset-0 z-40 flex items-center justify-center" onClick={(e) => { if (e.target === e.currentTarget) onClose(); }}>
      <div className="absolute inset-0 bg-cyber-900/80 backdrop-blur-sm" />
      <div className="relative z-50 w-full max-w-md mx-4 glass-card p-0 animate-slide-up overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-glass-border">
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 rounded-lg bg-accent-blue/10 flex items-center justify-center">
              <Shield size={16} className="text-accent-blue" />
            </div>
            <h2 className="text-base font-semibold text-white">Add IP Address</h2>
          </div>
          <button onClick={onClose} className="text-cyber-300 hover:text-white transition-colors"><X size={20} /></button>
        </div>

        {/* Form Body */}
        <form onSubmit={handleSubmit} className="px-6 py-5 space-y-4">
          <Input
            id="ip-address-input"
            label="IP Address"
            placeholder="e.g. 192.168.1.100 or 2001:db8::1"
            value={ipAddress}
            onChange={(e) => setIpAddress(e.target.value)}
            error={errors.ipAddress}
          />

          <div>
            <label className="block text-sm font-medium text-cyber-200 mb-2">List Type</label>
            <select
              id="list-type-select"
              value={listType}
              onChange={(e) => setListType(e.target.value as IpListType)}
              className={selectClass}
            >
              <option value="BLACKLIST" className="bg-cyber-800">BLACKLIST</option>
              <option value="WHITELIST" className="bg-cyber-800">WHITELIST</option>
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-cyber-200 mb-2">Description</label>
            <textarea
              id="description-input"
              placeholder="Provide context (e.g. Threat Intel Block / Corp VPN Whitelist)..."
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
              Cancel
            </Button>
            <Button type="submit" variant="primary" size="sm" isLoading={loading}>
              Add to List
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
}

/* ===== Delete Confirmation Modal ===== */

function DeleteModal({ ipInfo, onClose, onDeleted }: { ipInfo: ManagedIpResponseDto; onClose: () => void; onDeleted: (id: string) => void }) {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleDelete = async () => {
    setLoading(true);
    try {
      await deleteManagedIp(ipInfo.id);
      onDeleted(ipInfo.id);
    } catch {
      setError('Failed to remove IP address. Please try again.');
      setLoading(false);
    }
  };

  useEffect(() => {
    const handleEsc = (e: KeyboardEvent) => { if (e.key === 'Escape') onClose(); };
    window.addEventListener('keydown', handleEsc);
    return () => window.removeEventListener('keydown', handleEsc);
  }, [onClose]);

  return (
    <div className="fixed inset-0 z-40 flex items-center justify-center" onClick={(e) => { if (e.target === e.currentTarget) onClose(); }}>
      <div className="absolute inset-0 bg-cyber-900/80 backdrop-blur-sm" />
      <div className="relative z-50 w-full max-w-md mx-4 glass-card p-6 animate-slide-up">
        <div className="flex items-center gap-3 mb-4">
          <div className="w-10 h-10 rounded-xl bg-accent-rose/10 flex items-center justify-center">
            <Trash2 size={18} className="text-accent-rose" />
          </div>
          <div>
            <h2 className="text-base font-semibold text-white">Remove IP Address</h2>
            <p className="text-xs text-cyber-300">This will remove the bypass/block rule instantly.</p>
          </div>
        </div>

        <p className="text-sm text-cyber-200 mb-5">
          Are you sure you want to remove <span className="font-semibold text-white">"{ipInfo.ip_address}"</span> from the{' '}
          <span className="font-bold text-white">{ipInfo.list_type}</span>?
        </p>

        {error && (
          <p className="text-xs text-accent-rose mb-4 flex items-center gap-1">
            <AlertTriangle size={12} /> {error}
          </p>
        )}

        <div className="flex justify-end gap-3">
          <Button variant="ghost" size="sm" onClick={onClose} disabled={loading}>
            Cancel
            </Button>
          <Button variant="danger" size="sm" isLoading={loading} onClick={handleDelete}>
            Remove
          </Button>
        </div>
      </div>
    </div>
  );
}

/* ===== Main Access Control Page ===== */

const AccessControlPage: React.FC = () => {
  const [activeTab, setActiveTab] = useState<IpListType>('BLACKLIST');
  const [ips, setIps] = useState<ManagedIpResponseDto[]>([]);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [search, setSearch] = useState('');

  // Modals & Toast State
  const [addOpen, setAddOpen] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState<ManagedIpResponseDto | null>(null);
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' } | null>(null);

  const showToast = (message: string, type: 'success' | 'error') => setToast({ message, type });

  const fetchIps = useCallback(async (tab: IpListType, p: number, silent = false) => {
    if (!silent) setLoading(true);
    else setRefreshing(true);
    try {
      const data = await getManagedIps(p, PAGE_SIZE, tab);
      setIps(data.content);
      setTotalPages(data.totalPages);
      setTotalElements(data.totalElements);
      setPage(data.number);
    } catch {
      showToast('Failed to retrieve access control list.', 'error');
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, []);

  useEffect(() => {
    fetchIps(activeTab, 0);
  }, [activeTab, fetchIps]);

  const handleSaved = (saved: ManagedIpResponseDto) => {
    showToast(`IP "${saved.ip_address}" added to ${saved.list_type}.`, 'success');
    setAddOpen(false);
    if (saved.list_type === activeTab) {
      fetchIps(activeTab, 0, true);
    } else {
      setActiveTab(saved.list_type);
    }
  };

  const handleDeleted = (id: string) => {
    const deletedIp = ips.find((ip) => ip.id === id);
    showToast(`IP "${deletedIp?.ip_address}" removed successfully.`, 'success');
    setDeleteTarget(null);
    if (ips.length === 1 && page > 0) {
      fetchIps(activeTab, page - 1);
    } else {
      fetchIps(activeTab, page, true);
    }
  };

  // Client-side search matching IP or Description
  const filtered = ips.filter((ip) => {
    const q = search.toLowerCase();
    return (
      ip.ip_address.toLowerCase().includes(q) ||
      (ip.description ?? '').toLowerCase().includes(q)
    );
  });

  return (
    <div className="min-h-screen">
      <Header title="Access Control List (ACL)" />

      <div className="p-6 space-y-5">
        {/* Top Header Row */}
        <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 animate-fade-in">
          <div>
            <h2 className="text-lg font-bold text-white">Managed IP Directory</h2>
            <p className="text-xs text-cyber-300 mt-0.5 font-mono">
              {loading ? 'Querying database...' : `${totalElements} IP address rules found`}
            </p>
          </div>
          <div className="flex items-center gap-2">
            <button
              id="acl-refresh-btn"
              onClick={() => fetchIps(activeTab, page, true)}
              disabled={refreshing || loading}
              className="p-2 rounded-lg text-cyber-300 hover:text-white hover:bg-cyber-700 transition-all disabled:opacity-40"
              title="Refresh"
            >
              <RefreshCw size={16} className={refreshing ? 'animate-spin' : ''} />
            </button>
            <Button
              id="acl-add-btn"
              variant="primary"
              size="sm"
              onClick={() => setAddOpen(true)}
            >
              <Plus size={16} className="mr-1.5" />
              Add IP Address
            </Button>
          </div>
        </div>

        {/* Tab switch and Search filters */}
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 animate-fade-in">
          {/* Custom Tabs */}
          <div className="flex items-center p-1 rounded-xl bg-cyber-800/80 border border-glass-border max-w-fit">
            <button
              id="acl-tab-blacklist"
              onClick={() => setActiveTab('BLACKLIST')}
              className={`flex items-center gap-2 px-4 py-2 rounded-lg text-xs font-semibold uppercase tracking-wider transition-all duration-200 cursor-pointer
                ${activeTab === 'BLACKLIST'
                  ? 'bg-accent-rose/10 text-accent-rose border border-accent-rose/30 shadow-md'
                  : 'text-cyber-300 hover:text-white border border-transparent'}`}
            >
              <Ban size={14} />
              Blacklist
            </button>
            <button
              id="acl-tab-whitelist"
              onClick={() => setActiveTab('WHITELIST')}
              className={`flex items-center gap-2 px-4 py-2 rounded-lg text-xs font-semibold uppercase tracking-wider transition-all duration-200 cursor-pointer
                ${activeTab === 'WHITELIST'
                  ? 'bg-accent-emerald/10 text-accent-emerald border border-accent-emerald/30 shadow-md'
                  : 'text-cyber-300 hover:text-white border border-transparent'}`}
            >
              <CheckCircle2 size={14} />
              Whitelist
            </button>
          </div>

          {/* Search box */}
          <div className="w-full md:max-w-md">
            <Input
              id="acl-search"
              placeholder="Search by IP address or description…"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              icon={<Search size={15} />}
            />
          </div>
        </div>

        {/* Main List Card */}
        <Card className="animate-slide-up p-0 overflow-hidden">
          {loading ? (
            <div className="flex items-center justify-center py-24 gap-3 text-cyber-300">
              <Loader2 size={22} className="animate-spin" />
              <span className="text-sm">Querying active list...</span>
            </div>
          ) : filtered.length === 0 ? (
            <div className="text-center py-24">
              <div className={`w-14 h-14 mx-auto mb-4 rounded-2xl flex items-center justify-center
                ${activeTab === 'BLACKLIST' ? 'bg-accent-rose/10' : 'bg-accent-emerald/10'}`}>
                {activeTab === 'BLACKLIST' ? (
                  <ShieldAlert size={28} className="text-accent-rose" />
                ) : (
                  <Shield size={28} className="text-accent-emerald" />
                )}
              </div>
              <p className="text-sm font-medium text-cyber-200">
                {search ? 'No matches found in active filter' : `No IPs in your ${activeTab.toLowerCase()} yet`}
              </p>
              {!search && (
                <p className="text-xs text-cyber-400 mt-1">
                  Click <span className="text-accent-blue">Add IP Address</span> to register a custom bypass/block rule.
                </p>
              )}
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-glass-border">
                    <th className="text-left px-5 py-3.5 text-xs font-semibold text-cyber-300 uppercase tracking-wider">IP Address</th>
                    <th className="text-left px-5 py-3.5 text-xs font-semibold text-cyber-300 uppercase tracking-wider">List Type</th>
                    <th className="text-left px-5 py-3.5 text-xs font-semibold text-cyber-300 uppercase tracking-wider">Description</th>
                    <th className="text-left px-5 py-3.5 text-xs font-semibold text-cyber-300 uppercase tracking-wider hidden md:table-cell">Added At</th>
                    <th className="text-right px-5 py-3.5 text-xs font-semibold text-cyber-300 uppercase tracking-wider">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-glass-border/50">
                  {filtered.map((ip) => (
                    <tr
                      key={ip.id}
                      className="group hover:bg-cyber-700/20 transition-colors duration-150"
                    >
                      {/* IP Address */}
                      <td className="px-5 py-4 font-mono font-medium text-white">
                        {ip.ip_address}
                      </td>

                      {/* Type Badge */}
                      <td className="px-5 py-4">
                        <span className={`inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-medium border
                          ${ip.list_type === 'BLACKLIST'
                            ? 'text-accent-rose bg-accent-rose/10 border-accent-rose/30'
                            : 'text-accent-emerald bg-accent-emerald/10 border-accent-emerald/30'
                          }`}
                        >
                          <span className={`w-1.5 h-1.5 rounded-full
                            ${ip.list_type === 'BLACKLIST' ? 'bg-accent-rose' : 'bg-accent-emerald'}`}
                          />
                          {ip.list_type}
                        </span>
                      </td>

                      {/* Description */}
                      <td className="px-5 py-4 text-cyber-200">
                        <div className="flex items-center gap-2 max-w-[300px] truncate" title={ip.description ?? ''}>
                          {ip.description ? (
                            <>
                              <FileText size={12} className="text-cyber-400 shrink-0" />
                              <span>{ip.description}</span>
                            </>
                          ) : (
                            <span className="text-cyber-500 italic">No description provided</span>
                          )}
                        </div>
                      </td>

                      {/* Timestamp */}
                      <td className="px-5 py-4 text-cyber-300 hidden md:table-cell">
                        <div className="flex items-center gap-2">
                          <Calendar size={13} className="text-cyber-400 shrink-0" />
                          <span>
                            {ip.timestamp
                              ? new Date(ip.timestamp).toLocaleString()
                              : 'System Seeding'}
                          </span>
                        </div>
                      </td>

                      {/* Actions */}
                      <td className="px-5 py-4 text-right">
                        <button
                          id={`acl-delete-${ip.id}`}
                          onClick={() => setDeleteTarget(ip)}
                          className="p-1.5 rounded-md text-cyber-300 hover:text-accent-rose hover:bg-accent-rose/10 transition-all opacity-0 group-hover:opacity-100 cursor-pointer"
                          title="Remove Rule"
                        >
                          <Trash2 size={14} />
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}

          {/* Pagination Footer */}
          {!loading && totalPages > 1 && (
            <div className="flex items-center justify-between px-5 py-3.5 border-t border-glass-border">
              <span className="text-xs text-cyber-400 font-medium">
                Page {page + 1} of {totalPages}
              </span>
              <div className="flex items-center gap-1">
                <button
                  id="acl-prev-page"
                  onClick={() => fetchIps(activeTab, page - 1)}
                  disabled={page === 0 || loading}
                  className="p-1.5 rounded-md text-cyber-300 hover:text-white hover:bg-cyber-700 transition-all disabled:opacity-30 disabled:cursor-not-allowed cursor-pointer"
                >
                  <ChevronLeft size={16} />
                </button>
                {Array.from({ length: Math.min(totalPages, 5) }, (_, i) => {
                  const p0 = Math.max(0, Math.min(page - 2, totalPages - 5));
                  const idx = p0 + i;
                  return (
                    <button
                      key={idx}
                      id={`acl-page-${idx}`}
                      onClick={() => fetchIps(activeTab, idx)}
                      className={`w-7 h-7 rounded-md text-xs font-semibold transition-all cursor-pointer
                        ${idx === page
                          ? 'bg-accent-blue text-white shadow-md shadow-accent-blue/20'
                          : 'text-cyber-300 hover:text-white hover:bg-cyber-700'
                        }`}
                    >
                      {idx + 1}
                    </button>
                  );
                })}
                <button
                  id="acl-next-page"
                  onClick={() => fetchIps(activeTab, page + 1)}
                  disabled={page >= totalPages - 1 || loading}
                  className="p-1.5 rounded-md text-cyber-300 hover:text-white hover:bg-cyber-700 transition-all disabled:opacity-30 disabled:cursor-not-allowed cursor-pointer"
                >
                  <ChevronRight size={16} />
                </button>
              </div>
            </div>
          )}
        </Card>
      </div>

      {/* Add IP Modal */}
      {addOpen && (
        <AddIpModal
          defaultType={activeTab}
          onClose={() => setAddOpen(false)}
          onSaved={handleSaved}
        />
      )}

      {/* Delete Confirmation Modal */}
      {deleteTarget && (
        <DeleteModal
          ipInfo={deleteTarget}
          onClose={() => setDeleteTarget(null)}
          onDeleted={handleDeleted}
        />
      )}

      {/* Toast Notification */}
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

export default AccessControlPage;
