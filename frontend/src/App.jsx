import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { ThemeProvider } from './context/ThemeContext';
import Layout from './components/Layout/Layout';

import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import DatabaseConnection from './pages/DatabaseConnection';
import SchemaBrowser from './pages/SchemaBrowser';
import AIChat from './pages/AIChat';
import SavedQueries from './pages/SavedQueries';
import History from './pages/History';
import Analytics from './pages/Analytics';
import Settings from './pages/Settings';

// Protected Route Component
const PrivateRoute = ({ children }) => {
  const { user, loading } = useAuth();

  if (loading) {
    return (
      <div className="min-vh-100 d-flex align-items-center justify-content-center bg-dark">
        <div className="spinner-border text-primary" role="status"></div>
      </div>
    );
  }

  return user ? children : <Navigate to="/login" replace />;
};

export default function App() {
  return (
    <ThemeProvider>
      <AuthProvider>
        <BrowserRouter>
          <Routes>
            {/* Public Login Route */}
            <Route path="/login" element={<Login />} />

            {/* Protected Application Routes */}
            <Route
              path="/*"
              element={
                <PrivateRoute>
                  <Layout>
                    <Routes>
                      <Route path="/" element={<Dashboard />} />
                      <Route path="/chat" element={<AIChat />} />
                      <Route path="/connection" element={<DatabaseConnection />} />
                      <Route path="/schema" element={<SchemaBrowser />} />
                      <Route path="/saved-queries" element={<SavedQueries />} />
                      <Route path="/history" element={<History />} />
                      <Route path="/analytics" element={<Analytics />} />
                      <Route path="/settings" element={<Settings />} />
                    </Route>
                  </Layout>
                </PrivateRoute>
              }
            />
          </Routes>
        </BrowserRouter>
      </AuthProvider>
    </ThemeProvider>
  );
}
