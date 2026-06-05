import React, { Suspense } from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { ToastProvider } from './context/ToastContext';
import ProtectedRoute from './routes/ProtectedRoute';
import DashboardLayout from './components/layout/DashboardLayout';
import LoginPage from './pages/LoginPage';
import DashboardPage from './pages/DashboardPage';
import RulesPage from './pages/RulesPage';
import SettingsPage from './pages/SettingsPage';
import AccessControlPage from './pages/AccessControlPage';
import AuditLogsPage from './pages/AuditLogsPage';

/**
 * App — root component with routing and auth context.
 */
const App: React.FC = () => {
  return (
    <Suspense
      fallback={
        <div className="min-h-screen bg-cyber-950 flex flex-col items-center justify-center gap-3">
          <div className="w-8 h-8 border-2 border-accent-blue border-t-transparent rounded-full animate-spin" />
          <span className="text-sm text-cyber-300 font-medium">Loading...</span>
        </div>
      }
    >
      <AuthProvider>
        <ToastProvider>
          <BrowserRouter>
            <Routes>
              {/* Public Route */}
              <Route path="/login" element={<LoginPage />} />

              {/* Protected Routes — wrapped in DashboardLayout */}
              <Route
                element={
                  <ProtectedRoute>
                    <DashboardLayout />
                  </ProtectedRoute>
                }
              >
                <Route path="/" element={<DashboardPage />} />
                <Route path="/rules" element={<RulesPage />} />
                <Route path="/access-control" element={<AccessControlPage />} />
                <Route path="/audit-logs" element={<AuditLogsPage />} />
                <Route path="/settings" element={<SettingsPage />} />
              </Route>
            </Routes>
          </BrowserRouter>
        </ToastProvider>
      </AuthProvider>
    </Suspense>
  );
};

export default App;
