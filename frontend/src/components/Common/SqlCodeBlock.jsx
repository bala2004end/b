import React, { useState } from 'react';
import { Copy, Check, Play, HelpCircle, Bookmark, Sparkles } from 'lucide-react';

export default function SqlCodeBlock({ sql, onExecute, onExplain, onSave }) {
  const [copied, setCopied] = useState(false);

  const handleCopy = () => {
    navigator.clipboard.writeText(sql);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <div className="position-relative my-2 rounded-3 border border-info border-opacity-25 bg-dark">
      {/* Code Header Toolbar */}
      <div className="sql-toolbar">
        <span className="small font-monospace text-info d-flex align-items-center gap-1.5 fw-semibold flex-shrink-0">
          <Sparkles size={14} /> Generated MySQL Query
        </span>
        <div className="sql-toolbar-actions">
          <button 
            className="btn btn-dark btn-sm text-light p-1 px-2 border-0 d-flex align-items-center gap-1 small flex-shrink-0"
            onClick={handleCopy}
            title="Copy SQL to Clipboard"
          >
            {copied ? <Check size={14} className="text-success" /> : <Copy size={14} />}
            <span style={{ fontSize: '0.75rem' }}>{copied ? 'Copied' : 'Copy'}</span>
          </button>

          {onExecute && (
            <button 
              className="btn btn-success btn-sm p-1 px-2 d-flex align-items-center gap-1 small flex-shrink-0"
              onClick={() => onExecute(sql)}
              title="Run Query"
            >
              <Play size={13} />
              <span style={{ fontSize: '0.75rem' }}>Execute</span>
            </button>
          )}

          {onExplain && (
            <button 
              className="btn btn-outline-info btn-sm p-1 px-2 d-flex align-items-center gap-1 small flex-shrink-0"
              onClick={() => onExplain(sql)}
              title="Explain Execution Plan"
            >
              <HelpCircle size={13} />
              <span style={{ fontSize: '0.75rem' }}>Explain</span>
            </button>
          )}

          {onSave && (
            <button 
              className="btn btn-outline-warning btn-sm p-1 px-2 d-flex align-items-center gap-1 small flex-shrink-0"
              onClick={() => onSave(sql)}
              title="Bookmark Query"
            >
              <Bookmark size={13} />
              <span style={{ fontSize: '0.75rem' }}>Save</span>
            </button>
          )}
        </div>
      </div>

      {/* Code Block Display */}
      {/* Note: .sql-codeblock class provides overflow-x: auto and white-space: pre in index.css */}
      <pre className="sql-codeblock">
        <code>{sql}</code>
      </pre>
    </div>
  );
}
