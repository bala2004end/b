import React, { useState, useEffect, useRef } from 'react';
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
  RefreshCw, 
  Database, 
  Layers, 
  Bot, 
  User, 
  HelpCircle,
  Bookmark
} from 'lucide-react';

export default function AIChat() {
  const [messages, setMessages] = useState([]);
  const [inputQuestion, setInputQuestion] = useState('');
  const [loading, setLoading] = useState(false);
  const [isListening, setIsListening] = useState(false);
  const [conversationId, setConversationId] = useState(null);
  const [explainModalData, setExplainModalData] = useState(null);

  const messagesEndRef = useRef(null);
  const location = useLocation();

  useEffect(() => {
    // If prompt passed from Dashboard
    if (location.state?.question) {
      handleSend(location.state.question);
    }
  }, [location.state]);

  useEffect(() => {
    scrollToBottom();
  }, [messages, loading]);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  const handleSend = async (questionToSend) => {
    const q = questionToSend || inputQuestion;
    if (!q || !q.trim()) return;

    const userMsg = {
      id: Date.now(),
      sender: 'USER',
      content: q
    };

    setMessages(prev => [...prev, userMsg]);
    setInputQuestion('');
    setLoading(true);

    try {
      const res = await api.post('/chat', {
        conversationId,
        question: q
      });

      if (res.data.conversationId) {
        setConversationId(res.data.conversationId);
      }

      const aiMsg = {
        id: res.data.messageId || Date.now() + 1,
        sender: 'AI',
        content: res.data.explanation,
        generatedSql: res.data.generatedSql,
        executionTimeMs: res.data.executionTimeMs,
        rowsReturned: res.data.rowsReturned,
        isSuccess: res.data.isSuccess,
        errorMessage: res.data.errorMessage,
        data: res.data.data,
        columns: res.data.columns,
        chartType: res.data.chartType,
        retrievedTables: res.data.retrievedTables
      };

      setMessages(prev => [...prev, aiMsg]);
    } catch (err) {
      const errorMsg = {
        id: Date.now() + 1,
        sender: 'AI',
        content: 'Error: ' + (err.response?.data?.message || err.message || 'Failed to process question.'),
        isSuccess: false
      };
      setMessages(prev => [...prev, errorMsg]);
    } finally {
      setLoading(false);
    }
  };

  // Voice Input Speech Recognition
  const toggleVoiceInput = () => {
    if (!('webkitSpeechRecognition' in window) && !('SpeechRecognition' in window)) {
      alert('Speech recognition is not supported in this browser.');
      return;
    }

    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
    const recognition = new SpeechRecognition();

    if (!isListening) {
      recognition.start();
      setIsListening(true);
      recognition.onresult = (event) => {
        const transcript = event.results[0][0].transcript;
        setInputQuestion(transcript);
        setIsListening(false);
      };
      recognition.onerror = () => setIsListening(false);
      recognition.onend = () => setIsListening(false);
    } else {
      setIsListening(false);
    }
  };

  const handleExplain = async (sql) => {
    try {
      const res = await api.post('/chat/explain', { sqlQuery: sql });
      setExplainModalData({ sql, plan: res.data });
    } catch (e) {
      alert('Failed to get execution plan: ' + e.message);
    }
  };

  const handleSaveQuery = async (sql) => {
    try {
      await api.post('/saved-queries', {
        title: 'Saved Query (' + new Date().toLocaleTimeString() + ')',
        sqlQuery: sql,
        category: 'Chat'
      });
      alert('Query saved to bookmarks!');
    } catch (e) {
      alert('Failed to save query');
    }
  };

  return (
    <div className="container-fluid p-0 chat-container">
      {/* Messages Scroll Area */}
      <div className="chat-messages-scroll pe-2">
        {messages.length === 0 ? (
          <div className="h-100 d-flex flex-column align-items-center justify-content-center text-center p-4">
            <div className="p-3 rounded-circle bg-primary bg-opacity-25 text-primary mb-3">
              <Sparkles size={36} />
            </div>
            <h4 className="fw-bold gradient-text">How can I help with your Database today?</h4>
            <p className="text-muted small" style={{ maxWidth: '460px' }}>
              Ask questions in plain English. The RAG engine will search schema embeddings, generate MySQL code, and execute safely.
            </p>

            <div className="d-flex flex-wrap gap-2 justify-content-center mt-3" style={{ maxWidth: '600px' }}>
              {[
                "How many employees joined this month?",
                "Which department has the highest salary?",
                "Show products with stock below 20.",
                "Which customers purchased more than ₹50000?"
              ].map((q, idx) => (
                <button
                  key={idx}
                  className="btn btn-outline-secondary btn-sm rounded-pill text-light py-1.5 px-3 border-secondary border-opacity-25 hover-bg-secondary small"
                  onClick={() => handleSend(q)}
                >
                  {q}
                </button>
              ))}
            </div>
          </div>
        ) : (
          <div className="d-flex flex-column gap-3 py-3">
            {messages.map((msg) => (
              <div key={msg.id} className={`d-flex align-items-start gap-3 ${msg.sender === 'USER' ? 'justify-content-end' : ''}`}>
                {msg.sender === 'AI' && (
                  <div className="p-2 rounded-circle bg-primary text-white flex-shrink-0">
                    <Bot size={18} />
                  </div>
                )}

                <div className={msg.sender === 'USER' ? 'chat-bubble-user' : 'chat-bubble-ai w-100'}>
                  {/* Sender Name */}
                  <div className="d-flex align-items-center justify-content-between mb-1" style={{ fontSize: '0.75rem' }}>
                    <span className="fw-semibold text-opacity-75">{msg.sender === 'USER' ? 'You' : 'AI Database Assistant'}</span>
                    {msg.executionTimeMs && (
                      <span className="text-muted font-monospace">{msg.executionTimeMs} ms • {msg.rowsReturned} rows</span>
                    )}
                  </div>

                  {/* Message Content */}
                  <div className="lh-base" style={{ fontSize: '0.92rem' }}>{msg.content}</div>

                  {/* RAG Context Schema Badge */}
                  {msg.retrievedTables && msg.retrievedTables.length > 0 && (
                    <div className="mt-2 text-muted small d-flex align-items-center gap-1.5" style={{ fontSize: '0.75rem' }}>
                      <Layers size={13} className="text-info" />
                      <span>RAG Retrieved Schema: </span>
                      {msg.retrievedTables.map((tbl, i) => (
                        <span key={i} className="badge bg-secondary bg-opacity-50 font-monospace">{tbl}</span>
                      ))}
                    </div>
                  )}

                  {/* Generated SQL Code Block */}
                  {msg.generatedSql && (
                    <SqlCodeBlock 
                      sql={msg.generatedSql}
                      onExecute={() => handleSend("Re-run: " + msg.generatedSql)}
                      onExplain={handleExplain}
                      onSave={handleSaveQuery}
                    />
                  )}

                  {/* Visual Analytics Chart */}
                  {msg.chartType && msg.chartType !== 'NONE' && (
                    <AiChart data={msg.data} chartType={msg.chartType} columns={msg.columns} />
                  )}

                  {/* Result Data Table */}
                  {msg.data && msg.data.length > 0 && (
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

            {/* Typing Animation Loading Indicator */}
            {loading && (
              <div className="d-flex align-items-start gap-3">
                <div className="p-2 rounded-circle bg-primary text-white flex-shrink-0">
                  <Bot size={18} />
                </div>
                <div className="chat-bubble-ai">
                  <div className="d-flex align-items-center gap-2">
                    <span className="small text-muted">Searching Vector DB & Generating SQL</span>
                    <div className="typing-indicator">
                      <div className="typing-dot"></div>
                      <div className="typing-dot"></div>
                      <div className="typing-dot"></div>
                    </div>
                  </div>
                </div>
              </div>
            )}
            <div ref={messagesEndRef} />
          </div>
        )}
      </div>

      {/* Input Bar */}
      <div className="pt-3 border-top border-secondary border-opacity-25 mt-auto">
        <form onSubmit={(e) => { e.preventDefault(); handleSend(); }} className="glass-card p-2 rounded-4">
          <div className="input-group align-items-center">
            <input
              type="text"
              className="form-control bg-transparent text-light border-0 shadow-none px-3"
              placeholder="Ask a question in plain English (e.g. Which department has highest salary?)..."
              value={inputQuestion}
              onChange={(e) => setInputQuestion(e.target.value)}
              disabled={loading}
            />

            <button
              type="button"
              className={`btn btn-link p-2 ${isListening ? 'text-danger animate-pulse' : 'text-muted'}`}
              onClick={toggleVoiceInput}
              title="Voice Speech Input"
            >
              {isListening ? <MicOff size={20} /> : <Mic size={20} />}
            </button>

            <button
              type="submit"
              className="btn btn-primary bg-gradient rounded-3 px-3 py-2 d-flex align-items-center gap-1.5 shadow-sm ms-1"
              disabled={loading || !inputQuestion.trim()}
            >
              <Send size={16} />
              <span className="d-none d-sm-inline fw-semibold small">Send</span>
            </button>
          </div>
        </form>
      </div>

      {/* EXPLAIN Execution Plan Modal */}
      {explainModalData && (
        <div className="modal fade show d-block backdrop-blur" style={{ backgroundColor: 'rgba(0,0,0,0.7)' }}>
          <div className="modal-dialog modal-lg modal-dialog-centered">
            <div className="modal-content bg-dark text-light border-secondary">
              <div className="modal-header border-secondary">
                <h5 className="modal-title d-flex align-items-center gap-2">
                  <HelpCircle size={20} className="text-info" /> EXPLAIN Query Execution Plan
                </h5>
                <button type="button" className="btn-close btn-close-white" onClick={() => setExplainModalData(null)}></button>
              </div>
              <div className="modal-body">
                <div className="font-monospace small text-info mb-3 p-2 bg-dark rounded border border-secondary">
                  {explainModalData.sql}
                </div>
                <DataTable data={explainModalData.plan} title="MySQL Execution Plan Breakdown" />
              </div>
              <div className="modal-footer border-secondary">
                <button type="button" className="btn btn-secondary btn-sm" onClick={() => setExplainModalData(null)}>Close</button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
