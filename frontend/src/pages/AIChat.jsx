import React, { useState, useEffect, useRef, useCallback } from 'react';
import { useLocation } from 'react-router-dom';
import api from '../services/api';
import SqlCodeBlock from '../components/Common/SqlCodeBlock';
import DataTable from '../components/Common/DataTable';
import AiChart from '../components/Charts/AiChart';
import {
  Send,
  Sparkles,
  Mic,
  MicOff,
  Layers,
  Bot,
  User,
  HelpCircle,
  CheckCircle,
  XCircle,
  AlertCircle,
} from 'lucide-react';

// ── Toast Notification Component ─────────────────────────────────────────────
function Toast({ message, type = 'success', onDismiss }) {
  useEffect(() => {
    const timer = setTimeout(onDismiss, 4000);
    return () => clearTimeout(timer);
  }, [onDismiss]);

  const iconMap = {
    success: <CheckCircle size={16} className="text-success" />,
    error: <XCircle size={16} className="text-danger" />,
    warning: <AlertCircle size={16} className="text-warning" />,
  };

  const bgMap = {
    success: 'border-success border-opacity-50',
    error: 'border-danger border-opacity-50',
    warning: 'border-warning border-opacity-50',
  };

  return (
    <div
      className={`glass-card d-flex align-items-center gap-2 px-3 py-2 rounded-3 border ${bgMap[type]}`}
      style={{ minWidth: '280px', maxWidth: '400px', fontSize: '0.85rem' }}
    >
      {iconMap[type]}
      <span className="flex-grow-1">{message}</span>
      <button
        className="btn btn-link p-0 text-muted"
        onClick={onDismiss}
        style={{ lineHeight: 1 }}
        aria-label="Dismiss notification"
      >
        ×
      </button>
    </div>
  );
}

// ── Suggestion Chips ──────────────────────────────────────────────────────────
const SUGGESTIONS = [
  'How many employees joined this month?',
  'Which department has the highest salary?',
  'Show products with stock below 20.',
  'Which customers purchased more than ₹50,000?',
];

export default function AIChat() {
  const [messages, setMessages] = useState([]);
  const [inputQuestion, setInputQuestion] = useState('');
  const [loading, setLoading] = useState(false);
  const [isListening, setIsListening] = useState(false);
  const [conversationId, setConversationId] = useState(null);
  const [explainModalData, setExplainModalData] = useState(null);
  const [toasts, setToasts] = useState([]);

  const messagesEndRef = useRef(null);
  const inputRef = useRef(null);
  const recognitionRef = useRef(null);
  const location = useLocation();

  // ── Toast helpers ──────────────────────────────────────────────────────────
  const addToast = useCallback((message, type = 'success') => {
    const id = Date.now();
    setToasts(prev => [...prev, { id, message, type }]);
  }, []);

  const dismissToast = useCallback((id) => {
    setToasts(prev => prev.filter(t => t.id !== id));
  }, []);

  // ── Effects ────────────────────────────────────────────────────────────────
  useEffect(() => {
    if (location.state?.question) {
      handleSend(location.state.question);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []); // Only on mount

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, loading]);

  // ── Send handler ───────────────────────────────────────────────────────────
  const handleSend = async (questionToSend) => {
    const q = (questionToSend ?? inputQuestion).trim();
    if (!q || loading) return;

    const userMsg = { id: Date.now(), sender: 'USER', content: q };
    setMessages(prev => [...prev, userMsg]);
    setInputQuestion('');
    setLoading(true);

    try {
      const { data } = await api.post('/chat', { conversationId, question: q });

      if (data.conversationId) setConversationId(data.conversationId);

      setMessages(prev => [
        ...prev,
        {
          id: data.messageId || Date.now() + 1,
          sender: 'AI',
          content: data.explanation,
          generatedSql: data.generatedSql,
          executionTimeMs: data.executionTimeMs,
          rowsReturned: data.rowsReturned,
          isSuccess: data.isSuccess,
          errorMessage: data.errorMessage,
          data: data.data,
          columns: data.columns,
          chartType: data.chartType,
          retrievedTables: data.retrievedTables,
        },
      ]);
    } catch (err) {
      const errMsg = err.response?.data?.message || err.message || 'Failed to process question.';
      setMessages(prev => [
        ...prev,
        { id: Date.now() + 1, sender: 'AI', content: errMsg, isSuccess: false },
      ]);
    } finally {
      setLoading(false);
      inputRef.current?.focus();
    }
  };

  // ── Voice input ────────────────────────────────────────────────────────────
  const toggleVoiceInput = () => {
    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
    if (!SpeechRecognition) {
      addToast('Speech recognition is not supported in this browser.', 'warning');
      return;
    }

    if (isListening) {
      recognitionRef.current?.stop();
      setIsListening(false);
      return;
    }

    const recognition = new SpeechRecognition();
    recognition.lang = 'en-US';
    recognitionRef.current = recognition;
    recognition.start();
    setIsListening(true);

    recognition.onresult = (event) => {
      setInputQuestion(event.results[0][0].transcript);
      setIsListening(false);
    };
    recognition.onerror = (e) => {
      addToast(`Speech recognition error: ${e.error}`, 'warning');
      setIsListening(false);
    };
    recognition.onend = () => setIsListening(false);
  };

  // ── Explain handler ────────────────────────────────────────────────────────
  const handleExplain = async (sql) => {
    try {
      const { data } = await api.post('/chat/explain', { sqlQuery: sql });
      setExplainModalData({ sql, plan: data });
    } catch (e) {
      addToast(`Failed to get execution plan: ${e.response?.data?.message || e.message}`, 'error');
    }
  };

  // ── Save query handler ─────────────────────────────────────────────────────
  const handleSaveQuery = async (sql) => {
    try {
      await api.post('/saved-queries', {
        title: `Saved Query (${new Date().toLocaleTimeString()})`,
        sqlQuery: sql,
        category: 'Chat',
      });
      addToast('Query saved to bookmarks!', 'success');
    } catch (e) {
      addToast(`Failed to save query: ${e.response?.data?.message || e.message}`, 'error');
    }
  };

  // ── Render ─────────────────────────────────────────────────────────────────
  return (
    <div className="container-fluid p-0 chat-container" style={{ position: 'relative' }}>

      {/* Toast notifications */}
      <div
        style={{
          position: 'fixed', top: '1rem', right: '1rem',
          zIndex: 9999, display: 'flex', flexDirection: 'column', gap: '0.5rem',
        }}
        aria-live="polite"
        aria-atomic="false"
      >
        {toasts.map(t => (
          <Toast key={t.id} message={t.message} type={t.type} onDismiss={() => dismissToast(t.id)} />
        ))}
      </div>

      {/* Messages area */}
      <div className="chat-messages-scroll pe-2">
        {messages.length === 0 ? (
          <div className="h-100 d-flex flex-column align-items-center justify-content-center text-center p-4">
            <div className="p-3 rounded-circle bg-primary bg-opacity-25 text-primary mb-3">
              <Sparkles size={36} />
            </div>
            <h4 className="fw-bold gradient-text">How can I help with your Database today?</h4>
            <p className="text-muted small" style={{ maxWidth: '460px' }}>
              Ask questions in plain English. The RAG engine will search schema embeddings,
              generate MySQL code, and execute safely.
            </p>
            <div className="d-flex flex-wrap gap-2 justify-content-center mt-3" style={{ maxWidth: '600px' }}>
              {SUGGESTIONS.map((q, idx) => (
                <button
                  key={idx}
                  className="btn btn-outline-secondary btn-sm rounded-pill text-light border-secondary border-opacity-25 small"
                  onClick={() => handleSend(q)}
                  disabled={loading}
                >
                  {q}
                </button>
              ))}
            </div>
          </div>
        ) : (
          <div className="d-flex flex-column gap-3 py-3">
            {messages.map((msg) => (
              <div
                key={msg.id}
                className={`d-flex align-items-start gap-3 ${msg.sender === 'USER' ? 'justify-content-end' : ''}`}
              >
                {msg.sender === 'AI' && (
                  <div className="p-2 rounded-circle bg-primary text-white flex-shrink-0">
                    <Bot size={18} />
                  </div>
                )}

                <div className={msg.sender === 'USER' ? 'chat-bubble-user' : 'chat-bubble-ai w-100'}>
                  {/* Header row */}
                  <div className="d-flex align-items-center justify-content-between mb-1" style={{ fontSize: '0.75rem' }}>
                    <span className="fw-semibold text-opacity-75">
                      {msg.sender === 'USER' ? 'You' : 'AI Database Assistant'}
                    </span>
                    {msg.executionTimeMs != null && (
                      <span className="text-muted font-monospace">
                        {msg.executionTimeMs}ms · {msg.rowsReturned} rows
                      </span>
                    )}
                  </div>

                  {/* Error badge */}
                  {msg.sender === 'AI' && msg.isSuccess === false && (
                    <div className="d-flex align-items-center gap-1 mb-2 text-danger small">
                      <XCircle size={14} />
                      <span>Query failed</span>
                    </div>
                  )}

                  {/* Message content */}
                  <div className="lh-base" style={{ fontSize: '0.92rem' }}>{msg.content}</div>

                  {/* RAG context badge */}
                  {msg.retrievedTables?.length > 0 && (
                    <div className="mt-2 text-muted small d-flex align-items-center gap-1 flex-wrap" style={{ fontSize: '0.75rem' }}>
                      <Layers size={13} className="text-info" />
                      <span>RAG Retrieved:</span>
                      {msg.retrievedTables.map((tbl, i) => (
                        <span key={i} className="badge bg-secondary bg-opacity-50 font-monospace">{tbl}</span>
                      ))}
                    </div>
                  )}

                  {/* SQL code block */}
                  {msg.generatedSql && (
                    <SqlCodeBlock
                      sql={msg.generatedSql}
                      onExecute={() => handleSend(msg.generatedSql)}
                      onExplain={handleExplain}
                      onSave={handleSaveQuery}
                    />
                  )}

                  {/* Chart */}
                  {msg.chartType && msg.chartType !== 'NONE' && (
                    <AiChart data={msg.data} chartType={msg.chartType} columns={msg.columns} />
                  )}

                  {/* Data table */}
                  {msg.data?.length > 0 && (
                    <DataTable data={msg.data} columns={msg.columns} />
                  )}
                </div>

                {msg.sender === 'USER' && (
                  <div className="p-2 rounded-circle bg-secondary text-white flex-shrink-0">
                    <User size={18} />
                  </div>
                )}
              </div>
            ))}

            {/* Typing indicator */}
            {loading && (
              <div className="d-flex align-items-start gap-3">
                <div className="p-2 rounded-circle bg-primary text-white flex-shrink-0">
                  <Bot size={18} />
                </div>
                <div className="chat-bubble-ai">
                  <div className="d-flex align-items-center gap-2">
                    <span className="small text-muted">Searching Vector DB & Generating SQL</span>
                    <div className="typing-indicator">
                      <div className="typing-dot" />
                      <div className="typing-dot" />
                      <div className="typing-dot" />
                    </div>
                  </div>
                </div>
              </div>
            )}
            <div ref={messagesEndRef} />
          </div>
        )}
      </div>

      {/* Input bar */}
      <div className="pt-3 border-top border-secondary border-opacity-25 mt-auto">
        <form
          onSubmit={(e) => { e.preventDefault(); handleSend(); }}
          className="glass-card p-2 rounded-4"
          aria-label="Chat input form"
        >
          <div className="input-group align-items-center">
            <input
              ref={inputRef}
              id="chat-input"
              type="text"
              className="form-control bg-transparent text-light border-0 shadow-none px-3"
              placeholder="Ask a question in plain English…"
              value={inputQuestion}
              onChange={(e) => setInputQuestion(e.target.value)}
              disabled={loading}
              maxLength={2000}
              autoComplete="off"
              aria-label="Your question"
            />

            <button
              type="button"
              className={`btn btn-link p-2 ${isListening ? 'text-danger' : 'text-muted'}`}
              onClick={toggleVoiceInput}
              title={isListening ? 'Stop listening' : 'Voice input'}
              aria-label={isListening ? 'Stop voice input' : 'Start voice input'}
            >
              {isListening ? <MicOff size={20} /> : <Mic size={20} />}
            </button>

            <button
              type="submit"
              id="chat-send-btn"
              className="btn btn-primary bg-gradient rounded-3 px-3 py-2 d-flex align-items-center gap-1 shadow-sm ms-1"
              disabled={loading || !inputQuestion.trim()}
              aria-label="Send message"
            >
              <Send size={16} />
              <span className="d-none d-sm-inline fw-semibold small">Send</span>
            </button>
          </div>
        </form>
      </div>

      {/* EXPLAIN Modal */}
      {explainModalData && (
        <div
          className="modal fade show d-block"
          style={{ backgroundColor: 'rgba(0,0,0,0.7)' }}
          role="dialog"
          aria-modal="true"
          aria-labelledby="explain-modal-title"
        >
          <div className="modal-dialog modal-lg modal-dialog-centered modal-dialog-scrollable">
            <div className="modal-content bg-dark text-light border-secondary">
              <div className="modal-header border-secondary">
                <h5 id="explain-modal-title" className="modal-title d-flex align-items-center gap-2">
                  <HelpCircle size={20} className="text-info" />
                  EXPLAIN Execution Plan
                </h5>
                <button
                  type="button"
                  className="btn-close btn-close-white"
                  onClick={() => setExplainModalData(null)}
                  aria-label="Close"
                />
              </div>
              <div className="modal-body">
                <div className="font-monospace small text-info mb-3 p-2 bg-black rounded border border-secondary" style={{ wordBreak: 'break-all' }}>
                  {explainModalData.sql}
                </div>
                <DataTable data={explainModalData.plan} title="MySQL Execution Plan" />
              </div>
              <div className="modal-footer border-secondary">
                <button
                  type="button"
                  className="btn btn-secondary btn-sm"
                  onClick={() => setExplainModalData(null)}
                >
                  Close
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
