import React from 'react';
import { useAuth } from '../../context/AuthContext';
import { useTheme } from '../../context/ThemeContext';
import { Sun, Moon, LogOut, User as UserIcon, Cpu, Sparkles } from 'lucide-react';

export default function Header() {
  const { user, logout } = useAuth();
  const { theme, toggleTheme } = useTheme();

  return (
    <header className="navbar navbar-expand bg-dark bg-opacity-75 border-bottom border-secondary border-opacity-25 px-4 py-2 sticky-top backdrop-blur">
      <div className="container-fluid p-0 d-flex align-items-center justify-content-between">
        <div className="d-flex align-items-center gap-2">
          <span className="badge bg-primary bg-opacity-25 text-primary border border-primary border-opacity-25 d-flex align-items-center gap-1 px-2.5 py-1.5 rounded-pill">
            <Cpu size={14} /> Gemini 1.5 Flash RAG Active
          </span>
        </div>

        <div className="d-flex align-items-center gap-3">
          {/* Theme Toggle Button */}
          <button 
            className="btn btn-outline-secondary btn-sm rounded-circle p-2 d-flex align-items-center justify-content-center"
            onClick={toggleTheme}
            title={`Switch to ${theme === 'dark' ? 'Light' : 'Dark'} mode`}
          >
            {theme === 'dark' ? <Sun size={16} className="text-warning" /> : <Moon size={16} className="text-info" />}
          </button>

          {/* User Profile Pill */}
          {user ? (
            <div className="dropdown">
              <button 
                className="btn btn-dark btn-sm rounded-pill border border-secondary border-opacity-25 d-flex align-items-center gap-2 px-3 py-1.5"
                type="button"
                data-bs-toggle="dropdown"
                aria-expanded="false"
              >
                <div className="bg-primary text-white rounded-circle d-flex align-items-center justify-content-center" style={{ width: '24px', height: '24px', fontSize: '0.75rem' }}>
                  {user.username ? user.username.charAt(0).toUpperCase() : 'U'}
                </div>
                <span className="small fw-medium">{user.username}</span>
              </button>
              <ul className="dropdown-menu dropdown-menu-end dropdown-menu-dark shadow border border-secondary border-opacity-25">
                <li className="px-3 py-2 border-bottom border-secondary border-opacity-25">
                  <div className="fw-semibold small">{user.username}</div>
                  <div className="text-muted small" style={{ fontSize: '0.75rem' }}>{user.email || 'user@aidb.io'}</div>
                </li>
                <li>
                  <button className="dropdown-menu-item dropdown-item text-danger d-flex align-items-center gap-2 small py-2" onClick={logout}>
                    <LogOut size={14} /> Sign Out
                  </button>
                </li>
              </ul>
            </div>
          ) : (
            <span className="badge bg-secondary small">Guest Mode</span>
          )}
        </div>
      </div>
    </header>
  );
}
