import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import { Sparkles, Database, Lock, Mail, User, ArrowRight } from 'lucide-react';

export default function Login() {
  const [isRegister, setIsRegister] = useState(false);
  const [username, setUsername] = useState('admin');
  const [email, setEmail] = useState('admin@aidb.io');
  const [password, setPassword] = useState('admin123');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const { login, register } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      if (isRegister) {
        await register(username, email, password);
      } else {
        await login(username, password);
      }
      navigate('/');
    } catch (err) {
      setError(err.response?.data?.message || 'Authentication failed. Check credentials.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-vh-100 d-flex align-items-center justify-content-center bg-dark p-3" style={{ background: 'radial-gradient(circle at center, #1e293b 0%, #0f172a 100%)' }}>
      <div className="glass-card p-4 p-md-5 rounded-4 shadow-lg border border-secondary border-opacity-25" style={{ maxWidth: '440px', width: '100%' }}>
        {/* Brand Header */}
        <div className="text-center mb-4">
          <div className="d-inline-flex p-3 rounded-circle bg-primary bg-gradient text-white mb-3 shadow">
            <Sparkles size={32} />
          </div>
          <h3 className="fw-bold gradient-text mb-1">AI Database Assistant</h3>
          <p className="text-muted small">RAG Powered Natural Language SQL Engine</p>
        </div>

        {error && (
          <div className="alert alert-danger py-2 px-3 small rounded-3 mb-3 border-0 bg-danger bg-opacity-25 text-white">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="mb-3">
            <label className="form-label text-muted small fw-semibold">Username</label>
            <div className="input-group">
              <span className="input-group-text bg-dark text-muted border-secondary border-opacity-25">
                <User size={16} />
              </span>
              <input
                type="text"
                className="form-control bg-dark text-light border-secondary border-opacity-25"
                placeholder="Enter username"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                required
              />
            </div>
          </div>

          {isRegister && (
            <div className="mb-3">
              <label className="form-label text-muted small fw-semibold">Email Address</label>
              <div className="input-group">
                <span className="input-group-text bg-dark text-muted border-secondary border-opacity-25">
                  <Mail size={16} />
                </span>
                <input
                  type="email"
                  className="form-control bg-dark text-light border-secondary border-opacity-25"
                  placeholder="name@company.com"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                />
              </div>
            </div>
          )}

          <div className="mb-4">
            <label className="form-label text-muted small fw-semibold">Password</label>
            <div className="input-group">
              <span className="input-group-text bg-dark text-muted border-secondary border-opacity-25">
                <Lock size={16} />
              </span>
              <input
                type="password"
                className="form-control bg-dark text-light border-secondary border-opacity-25"
                placeholder="••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>
          </div>

          <button
            type="submit"
            className="btn btn-primary bg-gradient w-100 py-2.5 rounded-3 fw-semibold d-flex align-items-center justify-content-center gap-2 shadow-sm"
            disabled={loading}
          >
            {loading ? (
              <span className="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
            ) : (
              <>
                <span>{isRegister ? 'Create Account' : 'Sign In'}</span>
                <ArrowRight size={16} />
              </>
            )}
          </button>
        </form>

        <div className="text-center mt-4 pt-3 border-top border-secondary border-opacity-25">
          <button
            type="button"
            className="btn btn-link text-info text-decoration-none small p-0"
            onClick={() => setIsRegister(!isRegister)}
          >
            {isRegister ? 'Already have an account? Sign In' : "Don't have an account? Sign Up"}
          </button>
        </div>
      </div>
    </div>
  );
}
