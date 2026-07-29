import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import { Bookmark, Play, Trash2, Copy, Check, Sparkles } from 'lucide-react';

export default function SavedQueries() {
  const [queries, setQueries] = useState([]);
  const [loading, setLoading] = useState(true);
  const [copiedId, setCopiedId] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    fetchSavedQueries();
  }, []);

  const fetchSavedQueries = async () => {
    try {
      const res = await api.get('/saved-queries');
      setQueries(res.data || []);
    } catch (e) {
      console.error('Error fetching saved queries', e);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this saved query bookmark?')) return;
    try {
      await api.delete(`/saved-queries/${id}`);
      setQueries(prev => prev.filter(q => q.id !== id));
    } catch (e) {
      alert('Failed to delete query');
    }
  };

  const handleCopy = (id, sql) => {
    navigator.clipboard.writeText(sql);
    setCopiedId(id);
    setTimeout(() => setCopiedId(null), 2000);
  };

  return (
    <div className="container-fluid p-0">
      <div className="mb-4">
        <h4 className="fw-bold gradient-text mb-1 d-flex align-items-center gap-2">
          <Bookmark size={24} /> Saved SQL Bookmarks
        </h4>
        <p className="text-muted small">
          Access bookmarked queries for instant one-click execution or copying.
        </p>
      </div>

      {loading ? (
        <div className="p-5 text-center">
          <div className="spinner-border text-primary" role="status"></div>
        </div>
      ) : queries.length === 0 ? (
        <div className="glass-card p-5 text-center rounded-4">
          <Bookmark size={48} className="text-muted mb-3 opacity-50" />
          <h5>No Bookmarked Queries Saved</h5>
          <p className="text-muted small">Bookmark SQL queries directly from the AI Chat response blocks.</p>
        </div>
      ) : (
        <div className="row g-3">
          {queries.map((q) => (
            <div key={q.id} className="col-12 col-md-6 col-xl-4">
              <div className="glass-card p-4 rounded-4 h-100 d-flex flex-column justify-content-between">
                <div>
                  <div className="d-flex align-items-center justify-content-between mb-2">
                    <h6 className="fw-bold text-info mb-0">{q.title}</h6>
                    <span className="badge bg-secondary small">{q.category || 'General'}</span>
                  </div>

                  <pre className="sql-codeblock my-3 small" style={{ maxHeight: '120px', overflowY: 'auto' }}>
                    <code>{q.sqlQuery}</code>
                  </pre>
                </div>

                <div className="d-flex align-items-center justify-content-between pt-3 border-top border-secondary border-opacity-25 mt-3">
                  <div className="d-flex gap-1">
                    <button
                      className="btn btn-outline-secondary btn-sm p-1.5 px-2.5 d-flex align-items-center gap-1"
                      onClick={() => handleCopy(q.id, q.sqlQuery)}
                      title="Copy SQL"
                    >
                      {copiedId === q.id ? <Check size={14} className="text-success" /> : <Copy size={14} />}
                      <span style={{ fontSize: '0.75rem' }}>{copiedId === q.id ? 'Copied' : 'Copy'}</span>
                    </button>

                    <button
                      className="btn btn-danger btn-sm p-1.5 px-2 text-white opacity-75 hover-opacity-100"
                      onClick={() => handleDelete(q.id)}
                      title="Delete Bookmark"
                    >
                      <Trash2 size={14} />
                    </button>
                  </div>

                  <button
                    className="btn btn-success bg-gradient btn-sm px-3 py-1.5 fw-semibold d-flex align-items-center gap-1 shadow-sm"
                    onClick={() => navigate('/chat', { state: { question: 'Execute: ' + q.sqlQuery } })}
                  >
                    <Play size={13} />
                    <span style={{ fontSize: '0.75rem' }}>Run Query</span>
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
