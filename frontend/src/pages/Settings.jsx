import React, { useState, useEffect } from 'react';
import api from '../services/api';
import { Settings as SettingsIcon, Key, ShieldCheck, Cpu, CheckCircle2 } from 'lucide-react';

export default function Settings() {
  const [apiKey, setApiKey] = useState('');
  const [modelName, setModelName] = useState('gemini-1.5-flash');
  const [isReadOnly, setIsReadOnly] = useState(true);
  const [saved, setSaved] = useState(false);

  useEffect(() => {
    fetchSettings();
  }, []);

  const fetchSettings = async () => {
    try {
      const res = await api.get('/settings');
      if (res.data) {
        setApiKey(res.data.geminiApiKey || '');
        setModelName(res.data.modelName || 'gemini-1.5-flash');
        setIsReadOnly(res.data.isReadOnly ?? true);
      }
    } catch (e) {
      console.error('Error fetching settings', e);
    }
  };

  const handleSave = async (e) => {
    e.preventDefault();
    try {
      await api.post('/settings', {
        geminiApiKey: apiKey,
        modelName,
        isReadOnly
      });
      setSaved(true);
      setTimeout(() => setSaved(false), 3000);
    } catch (e) {
      alert('Failed to update settings');
    }
  };

  return (
    <div className="container-fluid p-0" style={{ maxWidth: '800px' }}>
      <div className="mb-4">
        <h4 className="fw-bold gradient-text mb-1 d-flex align-items-center gap-2">
          <SettingsIcon size={24} /> System & AI Model Settings
        </h4>
        <p className="text-muted small">
          Configure Google Gemini API key, model selection, RAG parameters, and security guardrails.
        </p>
      </div>

      {saved && (
        <div className="alert alert-success d-flex align-items-center gap-2 border-0 bg-success bg-opacity-25 text-white mb-4">
          <CheckCircle2 size={18} /> Settings successfully updated!
        </div>
      )}

      <div className="glass-card p-4 rounded-4">
        <form onSubmit={handleSave}>
          <div className="mb-4">
            <label className="form-label text-muted small fw-semibold d-flex align-items-center gap-1.5">
              <Key size={16} className="text-warning" /> Google Gemini API Key
            </label>
            <input
              type="password"
              className="form-control bg-dark text-light border-secondary border-opacity-25"
              placeholder="AIzaSy..."
              value={apiKey}
              onChange={(e) => setApiKey(e.target.value)}
            />
            <div className="form-text text-muted small">
              Optional override. If left blank, the application will use the default system environment key or intelligent rule fallback.
            </div>
          </div>

          <div className="mb-4">
            <label className="form-label text-muted small fw-semibold d-flex align-items-center gap-1.5">
              <Cpu size={16} className="text-info" /> LLM Model Provider Selection
            </label>
            <select
              className="form-select bg-dark text-light border-secondary border-opacity-25"
              value={modelName}
              onChange={(e) => setModelName(e.target.value)}
            >
              <option value="gemini-1.5-flash">Google Gemini 1.5 Flash (Recommended - High Speed)</option>
              <option value="gemini-1.5-pro">Google Gemini 1.5 Pro (Advanced Reasoning)</option>
            </select>
          </div>

          <div className="form-check form-switch mb-4 p-3 rounded-3 bg-dark bg-opacity-50 border border-secondary border-opacity-25 ms-0 d-flex align-items-center justify-content-between">
            <div>
              <label className="form-check-label text-light fw-semibold small d-flex align-items-center gap-1.5" htmlFor="readOnlyToggle">
                <ShieldCheck size={16} className="text-success" /> Strict Read-Only Execution Mode
              </label>
              <div className="text-muted" style={{ fontSize: '0.75rem' }}>Prevents accidental mutation or deletion of database records</div>
            </div>
            <input
              className="form-check-input ms-0"
              type="checkbox"
              id="readOnlyToggle"
              checked={isReadOnly}
              onChange={(e) => setIsReadOnly(e.target.checked)}
            />
          </div>

          <button
            type="submit"
            className="btn btn-primary bg-gradient px-4 py-2 rounded-3 fw-semibold shadow-sm"
          >
            Save Settings
          </button>
        </form>
      </div>
    </div>
  );
}
