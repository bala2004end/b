import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import StatCard from '../components/Common/StatCard';
import api from '../services/api';
import { 
  Database, 
  TableProperties, 
  MessageSquareCode, 
  Zap, 
  ShieldCheck, 
  Sparkles,
  ArrowRight,
  TrendingUp,
  Clock
} from 'lucide-react';

export default function Dashboard() {
  const [activeDb, setActiveDb] = useState(null);
  const [schema, setSchema] = useState(null);
  const [analytics, setAnalytics] = useState(null);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    loadDashboardData();
  }, []);

  const loadDashboardData = async () => {
    try {
      const connRes = await api.get('/connection/active');
      if (connRes.data) {
        setActiveDb(connRes.data);
      }
    } catch (e) {
      console.error('Failed loading dashboard data', e);
    } finally {
      setLoading(false);
    }
  };

  const sampleQuestions = [
    "How many employees joined this month?",
    "Which department has the highest salary?",
    "Show products with stock below 20.",
    "Which customers purchased more than ₹50000?"
  ];

  return (
    <div className="container-fluid p-0">
      {/* Welcome Banner */}
      <div className="glass-card p-4 rounded-4 mb-4 border border-primary border-opacity-25 bg-gradient">
        <div className="d-flex flex-column flex-md-row align-items-md-center justify-content-between gap-3">
          <div>
            <h4 className="fw-bold gradient-text mb-1 d-flex align-items-center gap-2">
              <Sparkles className="text-warning" size={24} /> AI Database Intelligence Platform
            </h4>
            <p className="text-muted mb-0 small">
              Connected to MySQL • Vector RAG Indexing active • Google Gemini 1.5 Flash LLM ready
            </p>
          </div>
          <button 
            className="btn btn-primary bg-gradient px-4 py-2 rounded-3 fw-semibold d-flex align-items-center gap-2 shadow-sm"
            onClick={() => navigate('/chat')}
          >
            <MessageSquareCode size={18} />
            <span>Launch AI Chat</span>
          </button>
        </div>
      </div>

      {/* Metrics Row */}
      <div className="row g-3 mb-4">
        <div className="col-12 col-sm-6 col-xl-3">
          <StatCard 
            title="Database Connection" 
            value={activeDb ? activeDb.databaseName : 'Not Connected'} 
            subtitle={activeDb ? `${activeDb.host}:${activeDb.port}` : 'Connect local MySQL'}
            icon={Database}
            color="info"
          />
        </div>
      </div>

      {/* Quick Launch Sample Queries */}
      <div className="row g-4 mb-4">
        <div className="col-12 col-lg-7">
          <div className="glass-card p-4 rounded-4 h-100">
            <h5 className="fw-semibold mb-3 d-flex align-items-center gap-2">
              <Sparkles className="text-info" size={20} /> Instant Sample Natural Language Prompts
            </h5>
            <p className="text-muted small mb-3">
              Click any question below to immediately ask the AI Database RAG Assistant:
            </p>

            <div className="d-flex flex-column gap-2">
              {sampleQuestions.map((q, idx) => (
                <div 
                  key={idx}
                  className="p-3 rounded-3 bg-dark bg-opacity-50 border border-secondary border-opacity-25 d-flex align-items-center justify-content-between hover-bg-secondary cursor-pointer transition-all"
                  onClick={() => navigate('/chat', { state: { question: q } })}
                >
                  <span className="small text-light fw-medium">{q}</span>
                  <ArrowRight size={16} className="text-primary" />
                </div>
              ))}
            </div>
          </div>
        </div>

        <div className="col-12 col-lg-5">
          <div className="glass-card p-4 rounded-4 h-100">
            <h5 className="fw-semibold mb-3 d-flex align-items-center gap-2">
              <ShieldCheck className="text-success" size={20} /> System Architecture & Security
            </h5>
            <ul className="list-unstyled d-flex flex-column gap-3 mb-0 small text-muted">
              <li className="d-flex align-items-start gap-2">
                <span className="badge bg-success bg-opacity-25 text-success rounded-circle p-1 mt-1">✓</span>
                <div>
                  <strong className="text-light">Vector RAG Pipeline:</strong> Automatic JDBC schema extraction into vector embeddings using cosine similarity.
                </div>
              </li>
              <li className="d-flex align-items-start gap-2">
                <span className="badge bg-success bg-opacity-25 text-success rounded-circle p-1 mt-1">✓</span>
                <div>
                  <strong className="text-light">Strict Read-Only Mode:</strong> Prevents DROP, DELETE, UPDATE, and dangerous DDL/DML statements.
                </div>
              </li>
              <li className="d-flex align-items-start gap-2">
                <span className="badge bg-success bg-opacity-25 text-success rounded-circle p-1 mt-1">✓</span>
                <div>
                  <strong className="text-light">Spring Security JWT:</strong> Stateless JWT token authorization for REST endpoints.
                </div>
              </li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  );
}
