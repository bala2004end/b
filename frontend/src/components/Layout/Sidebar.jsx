import React from 'react';
import { NavLink } from 'react-router-dom';
import { 
  LayoutDashboard, 
  Database, 
  MessageSquareCode, 
  TableProperties, 
  Bookmark, 
  History, 
  BarChart3, 
  Settings,
  Sparkles,
  ShieldCheck,
  ShieldAlert
} from 'lucide-react';

export default function Sidebar({ activeDb, isReadOnly }) {
  const navItems = [
    { path: '/', label: 'Dashboard', icon: LayoutDashboard },
    { path: '/chat', label: 'AI Chat', icon: MessageSquareCode },
    { path: '/connection', label: 'DB Connection', icon: Database },
    { path: '/schema', label: 'Schema Browser', icon: TableProperties },
    { path: '/saved-queries', label: 'Saved Queries', icon: Bookmark },
    { path: '/history', label: 'Query History', icon: History },
    { path: '/analytics', label: 'Analytics & Insights', icon: BarChart3 },
    { path: '/settings', label: 'Settings', icon: Settings },
  ];

  return (
    <aside className="d-flex flex-column p-3 text-white bg-dark border-end border-secondary border-opacity-25" style={{ width: '260px', minHeight: '100vh' }}>
      {/* Brand Header */}
      <div className="d-flex align-items-center gap-2 mb-4 px-2">
        <div className="p-2 rounded-3 bg-primary bg-gradient text-white shadow-sm">
          <Sparkles size={22} />
        </div>
        <div>
          <h6 className="fw-bold mb-0 gradient-text fs-5">AI DB Assistant</h6>
          <small className="text-muted" style={{ fontSize: '0.72rem' }}>LangChain4j + RAG</small>
        </div>
      </div>

      {/* Connection Status Pill */}
      <div className="mb-4 p-2.5 rounded-3 glass-card border border-opacity-10">
        <div className="d-flex align-items-center justify-content-between mb-1">
          <span className="small text-muted fw-semibold">Active Database</span>
          <span className={`badge ${activeDb ? 'bg-success badge-glow' : 'bg-secondary'}`}>
            {activeDb ? 'CONNECTED' : 'DISCONNECTED'}
          </span>
        </div>
        <div className="fw-medium text-truncate text-info small">
          {activeDb ? activeDb.databaseName : 'No DB Connected'}
        </div>
        <div className="d-flex align-items-center justify-content-between mt-2 pt-2 border-top border-secondary border-opacity-25" style={{ fontSize: '0.75rem' }}>
          <span className="text-muted">Mode:</span>
          <span className={`d-inline-flex align-items-center gap-1 ${isReadOnly ? 'text-success' : 'text-warning'}`}>
            {isReadOnly ? <ShieldCheck size={14} /> : <ShieldAlert size={14} />}
            {isReadOnly ? 'Read Only' : 'Write Allowed'}
          </span>
        </div>
      </div>

      {/* Navigation Links */}
      <ul className="nav nav-pills flex-column mb-auto gap-1">
        {navItems.map((item) => {
          const Icon = item.icon;
          return (
            <li key={item.path} className="nav-item">
              <NavLink
                to={item.path}
                className={({ isActive }) =>
                  `nav-link d-flex align-items-center gap-3 px-3 py-2.5 rounded-3 transition-all ${
                    isActive
                      ? 'active bg-primary bg-gradient text-white fw-semibold shadow-sm'
                      : 'text-light text-opacity-75 hover-bg-secondary'
                  }`
                }
              >
                <Icon size={18} />
                <span>{item.label}</span>
              </NavLink>
            </li>
          );
        })}
      </ul>

      {/* Footer System Meta */}
      <div className="pt-3 border-top border-secondary border-opacity-25 mt-3 text-center text-muted small">
        <div>Spring Boot 3.3 • Java 21</div>
        <div className="text-opacity-50" style={{ fontSize: '0.7rem' }}>Gemini RAG Engine v1.0</div>
      </div>
    </aside>
  );
}
