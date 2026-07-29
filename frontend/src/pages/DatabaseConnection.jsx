import React, { useState, useEffect } from 'react';
import api from '../services/api';
import { Database, CheckCircle2, AlertCircle, RefreshCw, ShieldCheck, Sparkles } from 'lucide-react';

export default function DatabaseConnection() {
  const [host, setHost] = useState('localhost');
  const [port, setPort] = useState(3306);
  const [username, setUsername] = useState('root');
  const [password, setPassword] = useState('root123');
  const [databaseName, setDatabaseName] = useState('sample_company_db');
  const [isReadOnly, setIsReadOnly] = useState(true);

  const [testResult, setTestResult] = useState(null);
  const [connectResult, setConnectResult] = useState(null);
  const [loadingTest, setLoadingTest] = useState(false);
  const [loadingConnect, setLoadingConnect] = useState(false);
  const [indexingProgress, setIndexingProgress] = useState(0);

  useEffect(() => {
    fetchActiveConnection();
  }, []);

  const fetchActiveConnection = async () => {
    try {
      const res = await api.get('/connection/active');
      if (res.data) {
        setHost(res.data.host || 'localhost');
        setPort(res.data.port || 3306);
        setUsername(res.data.username || 'root');
        setDatabaseName(res.data.databaseName || 'sample_company_db');
        setIsReadOnly(res.data.isReadOnly ?? true);
        setConnectResult({
          connected: true,
          message: 'Currently connected to ' + res.data.databaseName,
          databaseName: res.data.databaseName,
          host: res.data.host,
          port: res.data.port,
          isReadOnly: res.data.isReadOnly
        });
      }
    } catch (e) {
      console.log('No active connection yet');
    }
  };

  const handleTestConnection = async (e) => {
    e.preventDefault();
    setTestResult(null);
    setLoadingTest(true);

    try {
      const res = await api.post('/connection/test', {
        host,
        port: parseInt(port),
        username,
        password,
        databaseName,
        isReadOnly
      });
      setTestResult(res.data);
    } catch (err) {
      setTestResult({
        connected: false,
        message: err.response?.data?.message || 'Connection test failed.'
      });
    } finally {
      setLoadingTest(false);
    }
  };

  const handleConnectAndIndex = async (e) => {
    e.preventDefault();
    setConnectResult(null);
    setLoadingConnect(true);
    setIndexingProgress(20);

    const interval = setInterval(() => {
      setIndexingProgress(prev => (prev < 90 ? prev + 15 : prev));
    }, 400);

    try {
      const res = await api.post('/connection/connect', {
        host,
        port: parseInt(port),
        username,
        password,
        databaseName,
        isReadOnly
      });
      clearInterval(interval);
      setIndexingProgress(100);
      setConnectResult(res.data);
    } catch (err) {
      clearInterval(interval);
      setIndexingProgress(0);
      setConnectResult({
        connected: false,
        message: err.response?.data?.message || 'Connection failed.'
      });
    } finally {
      setLoadingConnect(false);
    }
  };

  return (
    <div className="container-fluid p-0">
      <div className="mb-4">
        <h4 className="fw-bold gradient-text mb-1 d-flex align-items-center gap-2">
          <Database size={24} /> MySQL Database Connection & RAG Schema Indexing
        </h4>
        <p className="text-muted small">
          Connect your target MySQL database. Spring Boot will inspect tables, columns, keys, and index metadata into Vector Memory.
        </p>
      </div>

      <div className="row g-4">
        <div className="col-12 col-lg-7">
          <div className="glass-card p-4 rounded-4">
            <h5 className="fw-semibold mb-3">Connection Credentials</h5>

            <form onSubmit={handleConnectAndIndex}>
              <div className="row g-3 mb-3">
                <div className="col-12 col-md-8">
                  <label className="form-label text-muted small fw-semibold">Host / Server Address</label>
                  <input
                    type="text"
                    className="form-control bg-dark text-light border-secondary border-opacity-25"
                    value={host}
                    onChange={(e) => setHost(e.target.value)}
                    placeholder="localhost or 127.0.0.1"
                    required
                  />
                </div>

                <div className="col-12 col-md-4">
                  <label className="form-label text-muted small fw-semibold">Port</label>
                  <input
                    type="number"
                    className="form-control bg-dark text-light border-secondary border-opacity-25"
                    value={port}
                    onChange={(e) => setPort(e.target.value)}
                    required
                  />
                </div>
              </div>

              <div className="row g-3 mb-3">
                <div className="col-12 col-md-6">
                  <label className="form-label text-muted small fw-semibold">Username</label>
                  <input
                    type="text"
                    className="form-control bg-dark text-light border-secondary border-opacity-25"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    required
                  />
                </div>

                <div className="col-12 col-md-6">
                  <label className="form-label text-muted small fw-semibold">Password</label>
                  <input
                    type="password"
                    className="form-control bg-dark text-light border-secondary border-opacity-25"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                  />
                </div>
              </div>

              <div className="mb-4">
                <label className="form-label text-muted small fw-semibold">Database Name</label>
                <input
                  type="text"
                  className="form-control bg-dark text-light border-secondary border-opacity-25"
                  value={databaseName}
                  onChange={(e) => setDatabaseName(e.target.value)}
                  placeholder="sample_company_db"
                  required
                />
              </div>

              <div className="form-check form-switch mb-4 p-3 rounded-3 bg-dark bg-opacity-50 border border-secondary border-opacity-25 ms-0 d-flex align-items-center justify-content-between">
                <div>
                  <label className="form-check-label text-light fw-semibold small d-flex align-items-center gap-1.5" htmlFor="readOnlySwitch">
                    <ShieldCheck size={16} className="text-success" /> Enforce Read-Only Mode (Recommended)
                  </label>
                  <div className="text-muted" style={{ fontSize: '0.75rem' }}>Forbids DROP, DELETE, UPDATE, and ALTER queries</div>
                </div>
                <input
                  className="form-check-input ms-0"
                  type="checkbox"
                  id="readOnlySwitch"
                  checked={isReadOnly}
                  onChange={(e) => setIsReadOnly(e.target.checked)}
                />
              </div>

              {loadingConnect && (
                <div className="mb-3">
                  <div className="d-flex justify-content-between small text-muted mb-1">
                    <span>Extracting Metadata & Indexing Vector Memory...</span>
                    <span>{indexingProgress}%</span>
                  </div>
                  <div className="progress bg-dark border border-secondary border-opacity-25" style={{ height: '8px' }}>
                    <div 
                      className="progress-bar bg-primary progress-bar-striped progress-bar-animated" 
                      role="progressbar" 
                      style={{ width: `${indexingProgress}%` }}
                    ></div>
                  </div>
                </div>
              )}

              <div className="d-flex align-items-center gap-3">
                <button
                  type="button"
                  className="btn btn-outline-secondary px-4 py-2 rounded-3 fw-medium"
                  onClick={handleTestConnection}
                  disabled={loadingTest || loadingConnect}
                >
                  {loadingTest ? <span className="spinner-border spinner-border-sm me-2"></span> : null}
                  Test Connection
                </button>

                <button
                  type="submit"
                  className="btn btn-primary bg-gradient px-4 py-2 rounded-3 fw-semibold d-flex align-items-center gap-2 shadow-sm"
                  disabled={loadingConnect}
                >
                  {loadingConnect ? <RefreshCw className="animate-spin" size={16} /> : <Sparkles size={16} />}
                  Connect & Extract Vector Schema
                </button>
              </div>
            </form>
          </div>
        </div>

        <div className="col-12 col-lg-5">
          {/* Test Result Banner */}
          {testResult && (
            <div className={`p-4 rounded-4 glass-card mb-4 border ${testResult.connected ? 'border-success' : 'border-danger'}`}>
              <div className="d-flex align-items-center gap-2 mb-2">
                {testResult.connected ? <CheckCircle2 className="text-success" size={20} /> : <AlertCircle className="text-danger" size={20} />}
                <h6 className="fw-semibold mb-0">{testResult.connected ? 'Connection Succeeded' : 'Connection Failed'}</h6>
              </div>
              <p className="small text-muted mb-0">{testResult.message}</p>
            </div>
          )}

          {/* Connection Status Card */}
          {connectResult && (
            <div className={`p-4 rounded-4 glass-card border ${connectResult.connected ? 'border-success' : 'border-danger'}`}>
              <div className="d-flex align-items-center justify-content-between mb-3">
                <div className="d-flex align-items-center gap-2">
                  <Database size={20} className="text-info" />
                  <h6 className="fw-semibold mb-0">Active Vector Connection</h6>
                </div>
                <span className="badge bg-success badge-glow">ACTIVE</span>
              </div>

              <ul className="list-unstyled small d-flex flex-column gap-2 mb-0 text-muted">
                <li className="d-flex justify-content-between">
                  <span>Database:</span>
                  <strong className="text-light">{connectResult.databaseName}</strong>
                </li>
                <li className="d-flex justify-content-between">
                  <span>Host & Port:</span>
                  <strong className="text-light">{connectResult.host}:{connectResult.port}</strong>
                </li>
                <li className="d-flex justify-content-between">
                  <span>Mode:</span>
                  <strong className={connectResult.isReadOnly ? 'text-success' : 'text-warning'}>
                    {connectResult.isReadOnly ? 'Read-Only Protected' : 'Write Allowed'}
                  </strong>
                </li>
                {connectResult.totalTables && (
                  <li className="d-flex justify-content-between">
                    <span>Tables Indexed:</span>
                    <strong className="text-primary">{connectResult.totalTables} Tables</strong>
                  </li>
                )}
                {connectResult.totalEmbeddings && (
                  <li className="d-flex justify-content-between">
                    <span>Vector Embeddings:</span>
                    <strong className="text-info">{connectResult.totalEmbeddings} Chunks</strong>
                  </li>
                )}
              </ul>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
