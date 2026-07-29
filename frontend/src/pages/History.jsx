import React, { useState, useEffect } from 'react';
import api from '../services/api';
import { History as HistoryIcon, Clock, CheckCircle2, XCircle, Download } from 'lucide-react';
import { exportToCSV } from '../services/exportUtils';

export default function History() {
  const [historyLogs, setHistoryLogs] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchHistory();
  }, []);

  const fetchHistory = async () => {
    try {
      const res = await api.get('/chat/history');
      setHistoryLogs(res.data || []);
    } catch (e) {
      console.error('Error loading history', e);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container-fluid p-0">
      <div className="d-flex align-items-center justify-content-between mb-4">
        <div>
          <h4 className="fw-bold gradient-text mb-1 d-flex align-items-center gap-2">
            <HistoryIcon size={24} /> Execution Audit Logs & History
          </h4>
          <p className="text-muted small">
            Full audit log of generated SQL queries, execution metrics, and latency history.
          </p>
        </div>

        {historyLogs.length > 0 && (
          <button 
            className="btn btn-outline-secondary btn-sm d-flex align-items-center gap-1.5 px-3 py-2"
            onClick={() => exportToCSV('audit_history', historyLogs)}
          >
            <Download size={14} /> Export CSV
          </button>
        )}
      </div>

      {loading ? (
        <div className="p-5 text-center">
          <div className="spinner-border text-primary" role="status"></div>
        </div>
      ) : historyLogs.length === 0 ? (
        <div className="glass-card p-5 text-center rounded-4">
          <HistoryIcon size={48} className="text-muted mb-3 opacity-50" />
          <h5>No Execution History</h5>
          <p className="text-muted small">Executions performed through AI Chat will be audited here.</p>
        </div>
      ) : (
        <div className="glass-card rounded-4 overflow-hidden border border-secondary border-opacity-25">
          <div className="table-responsive">
            <table className="table table-dark table-hover align-middle mb-0 font-sans" style={{ fontSize: '0.85rem' }}>
              <thead className="table-secondary">
                <tr>
                  <th style={{ width: '60px' }}>Status</th>
                  <th>SQL Query</th>
                  <th>Execution Duration</th>
                  <th>Rows</th>
                  <th>Timestamp</th>
                </tr>
              </thead>
              <tbody>
                {historyLogs.map((log) => (
                  <tr key={log.id}>
                    <td>
                      {log.isSuccess ? (
                        <CheckCircle2 size={18} className="text-success" />
                      ) : (
                        <XCircle size={18} className="text-danger" />
                      )}
                    </td>
                    <td className="font-monospace text-info small" style={{ maxWidth: '400px' }}>
                      <div className="text-truncate">{log.generatedSql}</div>
                    </td>
                    <td>
                      <span className="badge bg-secondary font-monospace d-inline-flex align-items-center gap-1">
                        <Clock size={12} /> {log.executionTimeMs || 0} ms
                      </span>
                    </td>
                    <td><strong className="text-light">{log.rowsReturned || 0}</strong></td>
                    <td className="text-muted small">
                      {log.timestamp ? new Date(log.timestamp).toLocaleString() : 'Just now'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
}
