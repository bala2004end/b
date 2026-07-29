import React, { useState, useEffect } from 'react';
import api from '../services/api';
import { BarChart3, Zap, Clock, ShieldAlert, Sparkles, KeyRound } from 'lucide-react';

export default function Analytics() {
  const [analytics, setAnalytics] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchAnalytics();
  }, []);

  const fetchAnalytics = async () => {
    try {
      const res = await api.get('/analytics');
      setAnalytics(res.data);
    } catch (e) {
      console.error('Error fetching analytics', e);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="p-5 text-center">
        <div className="spinner-border text-primary" role="status"></div>
      </div>
    );
  }

  return (
    <div className="container-fluid p-0">
      <div className="mb-4">
        <h4 className="fw-bold gradient-text mb-1 d-flex align-items-center gap-2">
          <BarChart3 size={24} /> AI Database Analytics & Optimization Insights
        </h4>
        <p className="text-muted small">
          Slow query detection, execution metrics breakdown, and automated MySQL index recommendations.
        </p>
      </div>

      <div className="row g-4 mb-4">
        <div className="col-12 col-md-4">
          <div className="glass-card p-4 rounded-4 text-center">
            <Zap size={32} className="text-primary mb-2" />
            <h3 className="fw-bold text-light mb-0">{analytics?.totalQueriesExecuted || 0}</h3>
            <span className="text-muted small">Total Queries Executed</span>
          </div>
        </div>

        <div className="col-12 col-md-4">
          <div className="glass-card p-4 rounded-4 text-center">
            <Clock size={32} className="text-amber mb-2" />
            <h3 className="fw-bold text-light mb-0">{analytics?.avgExecutionTimeMs || 0} ms</h3>
            <span className="text-muted small">Average Latency</span>
          </div>
        </div>

        <div className="col-12 col-md-4">
          <div className="glass-card p-4 rounded-4 text-center">
            <ShieldAlert size={32} className="text-warning mb-2" />
            <h3 className="fw-bold text-light mb-0">{analytics?.slowQueriesCount || 0}</h3>
            <span className="text-muted small">Slow Queries Identified</span>
          </div>
        </div>
      </div>

      <div className="row g-4">
        {/* Index Recommendations */}
        <div className="col-12 col-lg-6">
          <div className="glass-card p-4 rounded-4 h-100">
            <h5 className="fw-semibold mb-3 d-flex align-items-center gap-2">
              <KeyRound className="text-info" size={20} /> AI Index Recommendations
            </h5>
            {analytics?.indexRecommendations && analytics.indexRecommendations.length > 0 ? (
              <ul className="list-group list-group-dark">
                {analytics.indexRecommendations.map((rec, i) => (
                  <li key={i} className="list-group-item bg-dark text-light border-secondary border-opacity-25 small font-monospace">
                    <Sparkles size={14} className="text-warning me-2" /> {rec}
                  </li>
                ))}
              </ul>
            ) : (
              <div className="text-muted small">No index recommendations currently required. All query paths optimized.</div>
            )}
          </div>
        </div>

        {/* Slow Query Logs */}
        <div className="col-12 col-lg-6">
          <div className="glass-card p-4 rounded-4 h-100">
            <h5 className="fw-semibold mb-3 d-flex align-items-center gap-2">
              <Clock className="text-warning" size={20} /> Slow Query Logs
            </h5>
            {analytics?.slowQueryLogs && analytics.slowQueryLogs.length > 0 ? (
              <div className="d-flex flex-column gap-2">
                {analytics.slowQueryLogs.map((log, i) => (
                  <div key={i} className="p-3 rounded-3 bg-dark border border-secondary border-opacity-25">
                    <div className="d-flex justify-content-between small text-muted mb-1">
                      <span>Query #{log.id}</span>
                      <span className="badge bg-warning bg-opacity-25 text-warning">{log.executionTimeMs} ms</span>
                    </div>
                    <code className="small text-info font-monospace">{log.query}</code>
                  </div>
                ))}
              </div>
            ) : (
              <div className="text-muted small">No slow queries (&gt; 50ms) detected in recent audit logs.</div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
