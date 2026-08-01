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
    success: <CheckCircle size={16} className="text-success flex-shrink-0" />,
    error: <XCircle size={16} className="text-danger flex-shrink-0" />,
    warning: <AlertCircle size={16} className="text-warning flex-shrink-0" />,
  };

  const bgMap = {
    success: 'border-success border-opacity-50',
    error: 'border-danger border-opacity-50',
    warning: 'border-warning border-opacity-50',
  };

  return (
    <div
      className={`glass-card d-flex align-items-center gap-2 px-3 py-2 rounded-3 border ${bgMap[type]} w-100 shadow-sm`}
      style={{ fontSize: '0.85rem' }}
    >
      {iconMap[type]}
      <span className="flex-grow-1 text-wrap">{message}</span>
      <button
        className="btn btn-link p-0 text-muted flex-shrink-0"
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
    <div className="chat-container">

      {/* Toast notifications */}
      <div className="toast-stack" aria-live="polite" aria-atomic="false">
        {toasts.map(t => (
          <Toast key={t.id} message={t.message} type={t.type} onDismiss={() => dismissToast(t.id)} />
        ))}
      </div>

      {/* Messages area */}
      <div className="chat-messages-scroll">
        {messages.length === 0 ? (
          <div className="h-100 d-flex flex-column align-items-center justify-content-center text-center p-3">
            <div className="p-3 rounded-circle bg-primary bg-opacity-25 text-primary mb-3 flex-shrink-0">
              <Sparkles size={36} />
            </div>
            <h4 className="fw-bold gradient-text">How can I help with your Database today?</h4>
            <p className="text-muted small w-100" style={{ maxWidth: '460px' }}>
              Ask questions in plain English. The RAG engine will search schema embeddings,
              generate MySQL code, and execute safely.
            </p>
            <div className="suggestion-chips-wrap mt-3 w-100">
              {SUGGESTIONS.map((q, idx) => (
                <button
                  key={idx}
                  className="btn btn-outline-secondary btn-sm rounded-pill text-light border-secondary border-opacity-25 small text-wrap text-start"
                  onClick={() => handleSend(q)}
                  disabled={loading}
                >
                  {q}
                </button>
              ))}
            </div>
          </div>
        ) : (
          <div className="d-flex flex-column gap-3 py-3 w-100">
            {messages.map((msg) => (
              <div
                key={msg.id}
                className={`d-flex align-items-start chat-msg-gap gap-3 w-100 ${msg.sender === 'USER' ? 'justify-content-end' : ''}`}
              >
                {msg.sender === 'AI' && (
                  <div className="chat-msg-avatar p-2 rounded-circle bg-primary text-white flex-shrink-0">
                    <Bot size={18} />
                  </div>
                )}

                <div className={msg.sender === 'USER' ? 'chat-bubble-user' : 'chat-bubble-ai flex-grow-1'}>
                  {/* Header row */}
                  <div className="d-flex align-items-center justify-content-between flex-wrap gap-2 mb-1" style={{ fontSize: '0.75rem' }}>
                    <span className="fw-semibold text-opacity-75">
                      {msg.sender === 'USER' ? 'You' : 'AI Database Assistant'}
                    </span>
                    {msg.executionTimeMs != null && (
                      <span className="text-muted font-monospace text-nowrap">
                        {msg.executionTimeMs}ms · {msg.rowsReturned} rows
                      </span>
                    )}
                  </div>

                  {/* Error badge */}
                  {msg.sender === 'AI' && msg.isSuccess === false && (
                    <div className="d-flex align-items-center gap-1 mb-2 text-danger small">
                      <XCircle size={14} className="flex-shrink-0" />
                      <span>Query failed</span>
                    </div>
                  )}

                  {/* Message content */}
                  <div className="lh-base text-wrap" style={{ fontSize: '0.92rem' }}>{msg.content}</div>

                  {/* RAG context badge */}
                  {msg.retrievedTables?.length > 0 && (
                    <div className="mt-2 text-muted small d-flex align-items-center gap-1 flex-wrap" style={{ fontSize: '0.75rem' }}>
                      <Layers size={13} className="text-info flex-shrink-0" />
                      <span>RAG Retrieved:</span>
                      {msg.retrievedTables.map((tbl, i) => (
                        <span key={i} className="badge bg-secondary bg-opacity-50 font-monospace text-wrap">{tbl}</span>
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
                  <div className="chat-msg-avatar p-2 rounded-circle bg-secondary text-white flex-shrink-0">
                    <User size={18} />
                  </div>
                )}
              </div>
            ))}

            {/* Typing indicator */}
            {loading && (
              <div className="d-flex align-items-start chat-msg-gap gap-3 w-100">
                <div className="chat-msg-avatar p-2 rounded-circle bg-primary text-white flex-shrink-0">
                  <Bot size={18} />
                </div>
                <div className="chat-bubble-ai flex-grow-1">
                  <div className="d-flex align-items-center gap-2 flex-wrap">
                    <span className="small text-muted">Searching Vector DB & Generating SQL</span>
                    <div className="typing-indicator flex-shrink-0">
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
      <div className="chat-input-bar">
        <form
          onSubmit={(e) => { e.preventDefault(); handleSend(); }}
          className="glass-card p-2 rounded-4 mx-0"
          aria-label="Chat input form"
        >
          <div className="input-group align-items-center">
            <input
              ref={inputRef}
              id="chat-input"
              type="text"
              className="form-control bg-transparent text-light border-0 shadow-none px-2 px-sm-3 min-w-0"
              placeholder="Ask a question..."
              value={inputQuestion}
              onChange={(e) => setInputQuestion(e.target.value)}
              disabled={loading}
              maxLength={2000}
              autoComplete="off"
              aria-label="Your question"
            />

            <button
              type="button"
              className={`btn btn-link p-2 flex-shrink-0 ${isListening ? 'text-danger' : 'text-muted'}`}
              onClick={toggleVoiceInput}
              title={isListening ? 'Stop listening' : 'Voice input'}
              aria-label={isListening ? 'Stop voice input' : 'Start voice input'}
            >
              {isListening ? <MicOff size={20} /> : <Mic size={20} />}
            </button>

            <button
              type="submit"
              id="chat-send-btn"
              className="btn btn-primary bg-gradient rounded-3 px-2 px-sm-3 py-2 d-flex align-items-center justify-content-center gap-1 shadow-sm ms-1 flex-shrink-0"
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
          style={{ backgroundColor: 'rgba(0,0,0,0.7)', zIndex: 1055 }}
          role="dialog"
          aria-modal="true"
          aria-labelledby="explain-modal-title"
        >
          <div className="modal-dialog modal-fullscreen-sm-down modal-lg modal-dialog-centered modal-dialog-scrollable m-2 m-md-auto">
            <div className="modal-content bg-dark text-light border-secondary">
              <div className="modal-header border-secondary">
                <h5 id="explain-modal-title" className="modal-title d-flex align-items-center gap-2">
                  <HelpCircle size={20} className="text-info flex-shrink-0" />
                  <span className="text-truncate">EXPLAIN Execution Plan</span>
                </h5>
                <button
                  type="button"
                  className="btn-close btn-close-white flex-shrink-0"
                  onClick={() => setExplainModalData(null)}
                  aria-label="Close"
                />
              </div>
              <div className="modal-body overflow-x-hidden">
                <div className="font-monospace small text-info mb-3 p-2 bg-black rounded border border-secondary text-break overflow-x-auto" style={{ wordBreak: 'break-all' }}>
                  {explainModalData.sql}
                </div>
                <DataTable data={explainModalData.plan} title="Execution Plan" />
              </div>
              <div className="modal-footer border-secondary">
                <button
                  type="button"
                  className="btn btn-secondary btn-sm w-100 w-sm-auto"
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
