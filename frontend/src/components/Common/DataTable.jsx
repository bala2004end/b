import React from 'react';
import { Download, FileJson, Table as TableIcon } from 'lucide-react';
import { exportToCSV, exportToJSON } from '../../services/exportUtils';

export default function DataTable({ data, columns, title = 'Query Results' }) {
  if (!data || data.length === 0) {
    return (
      <div className="p-4 text-center glass-card rounded-3 text-muted">
        <TableIcon size={32} className="mb-2 opacity-50" />
        <div>No result rows returned for this execution.</div>
      </div>
    );
  }

  const cols = columns || Object.keys(data[0]);

  return (
    <div className="glass-card rounded-3 overflow-hidden my-3 border border-secondary border-opacity-25">
      {/* Table Header Controls */}
      <div className="d-flex align-items-center justify-content-between px-3 py-2 bg-secondary bg-opacity-25 border-bottom border-secondary border-opacity-25">
        <div className="d-flex align-items-center gap-2">
          <TableIcon size={16} className="text-info" />
          <span className="fw-semibold small">{title}</span>
          <span className="badge bg-primary bg-opacity-25 text-primary border border-primary border-opacity-25 rounded-pill small">
            {data.length} {data.length === 1 ? 'row' : 'rows'}
          </span>
        </div>

        <div className="d-flex align-items-center gap-2">
          <button 
            className="btn btn-outline-secondary btn-sm d-flex align-items-center gap-1 py-1 px-2.5"
            onClick={() => exportToCSV('query_export', data, cols)}
            title="Export as CSV"
          >
            <Download size={13} />
            <span style={{ fontSize: '0.75rem' }}>CSV</span>
          </button>

          <button 
            className="btn btn-outline-secondary btn-sm d-flex align-items-center gap-1 py-1 px-2.5"
            onClick={() => exportToJSON('query_export', data)}
            title="Export as JSON"
          >
            <FileJson size={13} />
            <span style={{ fontSize: '0.75rem' }}>JSON</span>
          </button>
        </div>
      </div>

      {/* Table Scroll Wrapper */}
      <div className="table-responsive" style={{ maxHeight: '380px' }}>
        <table className="table table-dark table-hover table-striped align-middle mb-0 font-sans" style={{ fontSize: '0.85rem' }}>
          <thead className="table-secondary sticky-top">
            <tr>
              <th scope="col" className="text-muted text-uppercase small" style={{ width: '50px' }}>#</th>
              {cols.map((col) => (
                <th key={col} scope="col" className="fw-semibold text-info">
                  {col}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {data.map((row, idx) => (
              <tr key={idx}>
                <td className="text-muted small">{idx + 1}</td>
                {cols.map((col) => (
                  <td key={col} className="text-light">
                    {row[col] !== undefined && row[col] !== null 
                      ? String(row[col]) 
                      : <span className="text-muted fst-italic">NULL</span>}
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
