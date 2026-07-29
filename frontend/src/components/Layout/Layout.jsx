import React, { useState, useEffect } from 'react';
import Sidebar from './Sidebar';
import Header from './Header';
import api from '../../services/api';

export default function Layout({ children }) {
  const [activeDb, setActiveDb] = useState(null);
  const [isReadOnly, setIsReadOnly] = useState(true);

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
    <div className="d-flex min-vh-100 bg-dark text-light">
      <Sidebar activeDb={activeDb} isReadOnly={isReadOnly} />
      <div className="d-flex flex-column flex-grow-1 overflow-hidden">
        <Header />
        <main className="flex-grow-1 p-4 overflow-auto">
          {children}
        </main>
      </div>
    </div>
  );
}
