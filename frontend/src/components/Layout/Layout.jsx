import React, { useState, useEffect } from 'react';
import Sidebar from './Sidebar';
import Header from './Header';
import api from '../../services/api';

export default function Layout({ children }) {
  const [activeDb, setActiveDb] = useState(null);
  const [isReadOnly, setIsReadOnly] = useState(true);
  // Controls whether the mobile offcanvas sidebar is open
  const [mobileSidebarOpen, setMobileSidebarOpen] = useState(false);

  useEffect(() => {
    fetchActiveConnection();
  }, []);

  const fetchActiveConnection = async () => {
    try {
      const res = await api.get('/connection/active');
      if (res.data) {
        setActiveDb(res.data);
        setIsReadOnly(res.data.isReadOnly ?? true);
      }
    } catch (e) {
      console.log('No active connection fetched yet');
    }
  };

  return (
    <div className="app-shell">
      {/* ── Desktop Sidebar (lg+) ── hidden via CSS on mobile ── */}
      <aside className="app-sidebar-desktop">
        <Sidebar activeDb={activeDb} isReadOnly={isReadOnly} />
      </aside>

      {/* ── Mobile Offcanvas Sidebar (< lg) ─────────────────── */}
      {/* Backdrop */}
      {mobileSidebarOpen && (
        <div
          className="offcanvas-backdrop fade show"
          onClick={() => setMobileSidebarOpen(false)}
          style={{ zIndex: 1044 }}
          aria-hidden="true"
        />
      )}

      {/* Offcanvas panel */}
      <div
        className={`offcanvas offcanvas-start sidebar-offcanvas${mobileSidebarOpen ? ' show' : ''}`}
        tabIndex="-1"
        id="mobileSidebar"
        aria-labelledby="mobileSidebarLabel"
        style={{
          visibility: mobileSidebarOpen ? 'visible' : 'hidden',
          zIndex: 1045,
          transition: 'transform 0.3s ease-in-out',
        }}
        aria-hidden={!mobileSidebarOpen}
        role="dialog"
        aria-modal="true"
      >
        <div className="offcanvas-header px-3 py-2 border-bottom border-secondary border-opacity-25">
          <h6 id="mobileSidebarLabel" className="offcanvas-title visually-hidden">
            Navigation
          </h6>
          <button
            type="button"
            className="btn-close btn-close-white ms-auto"
            aria-label="Close navigation"
            onClick={() => setMobileSidebarOpen(false)}
          />
        </div>
        <div className="offcanvas-body p-0">
          <Sidebar
            activeDb={activeDb}
            isReadOnly={isReadOnly}
            onNavClick={() => setMobileSidebarOpen(false)}
          />
        </div>
      </div>

      {/* ── Main Content Column ──────────────────────────────── */}
      <div className="app-main-col">
        <Header onHamburgerClick={() => setMobileSidebarOpen(true)} />
        <main className="app-main-content">
          {children}
        </main>
      </div>
    </div>
  );
}
