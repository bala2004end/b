import React from 'react';
import { useAuth } from '../../context/AuthContext';
import { useTheme } from '../../context/ThemeContext';
import { Sun, Moon, LogOut, User as UserIcon, Cpu, Sparkles, Menu } from 'lucide-react';

/**
 * Header — responsive top navbar.
 * `onHamburgerClick` — triggers the mobile sidebar offcanvas.
 */
export default function Header({ onHamburgerClick }) {
  const { user, logout } = useAuth();
  const { theme, toggleTheme } = useTheme();

  return (
    <header
      className="navbar bg-dark bg-opacity-75 border-bottom border-secondary border-opacity-25 sticky-top"
      style={{ minHeight: 'var(--header-height)', zIndex: 1030 }}
      role="banner"
    >
      <div className="container-fluid px-3 py-0 d-flex align-items-center gap-2">

        {/* ── Hamburger (mobile/tablet only, hidden on lg+) ── */}
        <button
          type="button"
          className="hamburger-btn btn btn-outline-secondary border-secondary border-opacity-25 text-light"
          onClick={onHamburgerClick}
          aria-label="Open navigation menu"
          aria-controls="mobileSidebar"
          aria-expanded="false"
        >
          <Menu size={20} />
        </button>

        {/* ── Left: Gemini badge ──────────────────────────── */}
        <div className="d-flex align-items-center gap-2 flex-grow-1 min-w-0">
          <span className="badge bg-primary bg-opacity-25 text-primary border border-primary border-opacity-25 d-flex align-items-center gap-1 px-2 py-1 rounded-pill text-nowrap">
            <Cpu size={13} className="flex-shrink-0" />
            <span className="header-badge-text" style={{ fontSize: '0.78rem' }}>Gemini 1.5 Flash RAG Active</span>
          </span>
        </div>

        {/* ── Right: Theme toggle + User profile ─────────── */}
        <div className="d-flex align-items-center gap-2">
          {/* Theme Toggle */}
          <button 
            className="btn btn-outline-secondary btn-sm rounded-circle p-0 d-flex align-items-center justify-content-center flex-shrink-0"
            style={{ width: '34px', height: '34px' }}
            onClick={toggleTheme}
            title={`Switch to ${theme === 'dark' ? 'Light' : 'Dark'} mode`}
            aria-label={`Switch to ${theme === 'dark' ? 'Light' : 'Dark'} mode`}
          >
            {theme === 'dark'
              ? <Sun size={16} className="text-warning" />
              : <Moon size={16} className="text-info" />}
          </button>

          {/* User Profile Pill */}
          {user ? (
            <div className="dropdown">
              <button 
                className="btn btn-dark btn-sm rounded-pill border border-secondary border-opacity-25 d-flex align-items-center gap-2 px-2 px-sm-3 py-1"
                type="button"
                id="userDropdown"
                data-bs-toggle="dropdown"
                aria-expanded="false"
                aria-label="User menu"
              >
                <div
                  className="bg-primary text-white rounded-circle d-flex align-items-center justify-content-center flex-shrink-0 fw-semibold"
                  style={{ width: '24px', height: '24px', fontSize: '0.75rem' }}
                  aria-hidden="true"
                >
                  {user.username ? user.username.charAt(0).toUpperCase() : 'U'}
                </div>
                {/* Hide username on very small screens */}
                <span className="small fw-medium d-none d-sm-inline text-truncate" style={{ maxWidth: '100px' }}>
                  {user.username}
                </span>
              </button>
              <ul
                className="dropdown-menu dropdown-menu-end dropdown-menu-dark shadow border border-secondary border-opacity-25"
                aria-labelledby="userDropdown"
              >
                <li className="px-3 py-2 border-bottom border-secondary border-opacity-25">
                  <div className="fw-semibold small">{user.username}</div>
                  <div className="text-muted small" style={{ fontSize: '0.75rem' }}>
                    {user.email || 'user@aidb.io'}
                  </div>
                </li>
                <li>
                  <button
                    className="dropdown-item text-danger d-flex align-items-center gap-2 small py-2"
                    onClick={logout}
                  >
                    <LogOut size={14} /> Sign Out
                  </button>
                </li>
              </ul>
            </div>
          ) : (
            <span className="badge bg-secondary small">Guest</span>
          )}
        </div>
      </div>
    </header>
  );
}
