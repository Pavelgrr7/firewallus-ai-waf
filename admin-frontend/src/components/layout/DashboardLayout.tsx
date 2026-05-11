import React from 'react';
import { Outlet } from 'react-router-dom';
import Sidebar from './Sidebar';

/**
 * DashboardLayout — wraps all protected pages with Sidebar + content area.
 * The Header is rendered by individual pages so they can set their own title.
 */
const DashboardLayout: React.FC = () => {
  return (
    <div className="flex min-h-screen">
      <Sidebar />
      {/* Main content — offset by sidebar width */}
      <main className="flex-1 ml-64">
        <Outlet />
      </main>
    </div>
  );
};

export default DashboardLayout;
