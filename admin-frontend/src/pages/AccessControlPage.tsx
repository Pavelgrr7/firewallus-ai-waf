import React from 'react';
import {
  Loader2,
  Plus,
  RefreshCw,
  Search,
  ShieldAlert,
  Trash2,
  Globe,
  FileText,
  Calendar,
} from 'lucide-react';
import Header from '../components/layout/Header';
import Card from '../components/ui/Card';
import Button from '../components/ui/Button';
import Input from '../components/ui/Input';
import ConfirmModal from '../components/ui/ConfirmModal';
import Pagination from '../components/ui/Pagination';
import AddIpModal from '../features/access-control/AddIpModal';
import { useManagedIps } from '../hooks/useManagedIps';

const AccessControlPage: React.FC = () => {
  const {
    ips,
    activeTab,
    totalPages,
    totalElements,
    page,
    loading,
    refreshing,
    search,
    setSearch,
    handleTabChange,
    fetchIps,
    handleSaved,
    // modal states
    addOpen,
    setAddOpen,
    deleteTarget,
    setDeleteTarget,
    deleting,
    deleteError,
    setDeleteError,
    handleDeleteConfirm,
  } = useManagedIps();

  return (
    <div className="min-h-screen">
      <Header title="Access Control" />

      <div className="p-6 space-y-5">
        {/* Top Header Section */}
        <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 animate-fade-in">
          <div>
            <h2 className="text-lg font-bold text-white">IP Lists</h2>
            <p className="text-xs text-cyber-300 mt-0.5">
              {loading
                ? 'Loading…'
                : `${totalElements} address${totalElements !== 1 ? 'es' : ''} in current list`}
            </p>
          </div>
          <div className="flex items-center gap-2">
            <button
              id="acl-refresh-btn"
              onClick={() => fetchIps(activeTab, page, true)}
              disabled={refreshing || loading}
              className="p-2 rounded-lg text-cyber-300 hover:text-white hover:bg-cyber-700 transition-all disabled:opacity-40 cursor-pointer"
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

        {/* Tab & Search controls */}
        <div className="flex flex-col md:flex-row gap-4 justify-between items-stretch md:items-center animate-fade-in">
          {/* Tabs */}
          <div className="flex bg-cyber-800/80 p-1 rounded-xl border border-glass-border self-start shrink-0">
            <button
              id="acl-tab-blacklist"
              onClick={() => handleTabChange('BLACKLIST')}
              className={`px-4 py-1.5 rounded-lg text-xs font-semibold tracking-wider transition-all cursor-pointer
                ${
                  activeTab === 'BLACKLIST'
                    ? 'bg-accent-rose text-white shadow-lg shadow-accent-rose/25'
                    : 'text-cyber-300 hover:text-white'
                }`}
            >
              BLACKLIST
            </button>
            <button
              id="acl-tab-whitelist"
              onClick={() => handleTabChange('WHITELIST')}
              className={`px-4 py-1.5 rounded-lg text-xs font-semibold tracking-wider transition-all cursor-pointer
                ${
                  activeTab === 'WHITELIST'
                    ? 'bg-accent-emerald text-white shadow-lg shadow-accent-emerald/25'
                    : 'text-cyber-300 hover:text-white'
                }`}
            >
              WHITELIST
            </button>
          </div>

          {/* Search bar */}
          <div className="flex-1 max-w-md">
            <Input
              id="acl-search"
              placeholder="Search by IP address or description…"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              icon={<Search size={15} />}
            />
          </div>
        </div>

        {/* Table data */}
        <Card className="animate-slide-up p-0 overflow-hidden">
          {loading ? (
            <div className="flex items-center justify-center py-24 gap-3 text-cyber-300">
              <Loader2 size={22} className="animate-spin" />
              <span className="text-sm">Loading addresses…</span>
            </div>
          ) : ips.length === 0 ? (
            <div className="text-center py-24">
              <div
                className={`w-14 h-14 mx-auto mb-4 rounded-2xl flex items-center justify-center
                ${activeTab === 'BLACKLIST' ? 'bg-accent-rose/10' : 'bg-accent-emerald/10'}`}
              >
                {activeTab === 'BLACKLIST' ? (
                  <ShieldAlert size={28} className="text-accent-rose" />
                ) : (
                  <Globe size={28} className="text-accent-emerald" />
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
                    <th className="text-left px-5 py-3.5 text-xs font-semibold text-cyber-300 uppercase tracking-wider">
                      IP Address
                    </th>
                    <th className="text-left px-5 py-3.5 text-xs font-semibold text-cyber-300 uppercase tracking-wider">
                      List Type
                    </th>
                    <th className="text-left px-5 py-3.5 text-xs font-semibold text-cyber-300 uppercase tracking-wider">
                      Description
                    </th>
                    <th className="text-left px-5 py-3.5 text-xs font-semibold text-cyber-300 uppercase tracking-wider hidden md:table-cell">
                      Added At
                    </th>
                    <th className="text-right px-5 py-3.5 text-xs font-semibold text-cyber-300 uppercase tracking-wider">
                      Actions
                    </th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-glass-border/50">
                  {ips.map((ip) => (
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
                        <span
                          className={`inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-medium border
                          ${
                            ip.list_type === 'BLACKLIST'
                              ? 'text-accent-rose bg-accent-rose/10 border-accent-rose/30'
                              : 'text-accent-emerald bg-accent-emerald/10 border-accent-emerald/30'
                          }`}
                        >
                          <span
                            className={`w-1.5 h-1.5 rounded-full
                            ${ip.list_type === 'BLACKLIST' ? 'bg-accent-rose' : 'bg-accent-emerald'}`}
                          />
                          {ip.list_type}
                        </span>
                      </td>

                      {/* Description */}
                      <td className="px-5 py-4 text-cyber-200">
                        <div
                          className="flex items-center gap-2 max-w-[300px] truncate"
                          title={ip.description ?? ''}
                        >
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
                            {ip.timestamp ? new Date(ip.timestamp).toLocaleString() : 'System Seeding'}
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

          {/* Pagination */}
          <Pagination
            page={page}
            totalPages={totalPages}
            onPageChange={(p) => fetchIps(activeTab, p)}
            loading={loading}
            idPrefix="acl"
          />
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

      {/* Confirm Delete Modal */}
      <ConfirmModal
        isOpen={!!deleteTarget}
        title={`Remove from ${activeTab}`}
        subtitle="This action is irreversible."
        description={
          <>
            Are you sure you want to remove IP address{' '}
            <span className="font-mono font-semibold text-white">
              {deleteTarget?.ip_address}
            </span>{' '}
            from the {activeTab.toLowerCase()}?
          </>
        }
        confirmLabel="Remove"
        cancelLabel="Cancel"
        variant="danger"
        isLoading={deleting}
        error={deleteError}
        onConfirm={handleDeleteConfirm}
        onClose={() => {
          setDeleteTarget(null);
          setDeleteError('');
        }}
      />
    </div>
  );
};

export default AccessControlPage;
