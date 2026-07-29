import React, { useState, useEffect } from 'react';
import api from '../services/api';
import { TableProperties, Key, Link2, Search, Database, Layers } from 'lucide-react';

export default function SchemaBrowser() {
  const [schema, setSchema] = useState(null);
  const [selectedTable, setSelectedTable] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchSchema();
  }, []);

  const fetchSchema = async () => {
    try {
      const res = await api.get('/schema');
      if (res.data) {
        setSchema(res.data);
        if (res.data.tables && res.data.tables.length > 0) {
          setSelectedTable(res.data.tables[0]);
        }
      }
    } catch (e) {
      console.error('Error fetching schema', e);
    } finally {
      setLoading(false);
    }
  };

  const filteredTables = schema?.tables?.filter(t => 
    t.tableName.toLowerCase().includes(searchTerm.toLowerCase())
  ) || [];

  if (loading) {
    return (
      <div className="p-5 text-center">
        <div className="spinner-border text-primary" role="status"></div>
        <div className="mt-2 text-muted small">Loading database schema...</div>
      </div>
    );
  }

  if (!schema) {
    return (
      <div className="glass-card p-5 text-center rounded-4">
        <Database size={48} className="text-muted mb-3 opacity-50" />
        <h5>No Schema Indexed</h5>
        <p className="text-muted small">Please connect to a MySQL database first to view schema metadata.</p>
      </div>
    );
  }

  return (
    <div className="container-fluid p-0">
      <div className="mb-4">
        <h4 className="fw-bold gradient-text mb-1 d-flex align-items-center gap-2">
          <TableProperties size={24} /> Interactive Database Schema Browser
        </h4>
        <p className="text-muted small">
          Inspected metadata for <strong>{schema.databaseName}</strong> ({schema.totalTables} Tables, {schema.totalViews} Views).
        </p>
      </div>

      <div className="row g-4">
        {/* Table List Sidebar */}
        <div className="col-12 col-md-4 col-xl-3">
          <div className="glass-card p-3 rounded-4 h-100">
            <div className="mb-3">
              <div className="input-group input-group-sm">
                <span className="input-group-text bg-dark text-muted border-secondary border-opacity-25">
                  <Search size={14} />
                </span>
                <input
                  type="text"
                  className="form-control bg-dark text-light border-secondary border-opacity-25"
                  placeholder="Filter tables..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                />
              </div>
            </div>

            <div className="d-flex flex-column gap-1 overflow-auto" style={{ maxHeight: '600px' }}>
              {filteredTables.map((tbl) => (
                <button
                  key={tbl.tableName}
                  className={`btn btn-sm text-start py-2 px-3 rounded-3 d-flex align-items-center justify-content-between transition-all ${
                    selectedTable?.tableName === tbl.tableName
                      ? 'btn-primary bg-gradient shadow-sm'
                      : 'btn-dark text-light border-0 hover-bg-secondary'
                  }`}
                  onClick={() => setSelectedTable(tbl)}
                >
                  <span className="fw-medium font-monospace small">{tbl.tableName}</span>
                  <span className="badge bg-dark bg-opacity-50 text-muted small">{tbl.columns.length} cols</span>
                </button>
              ))}
            </div>
          </div>
        </div>

        {/* Column Details Panel */}
        <div className="col-12 col-md-8 col-xl-9">
          {selectedTable ? (
            <div className="glass-card p-4 rounded-4">
              <div className="d-flex align-items-center justify-content-between border-bottom border-secondary border-opacity-25 pb-3 mb-4">
                <div>
                  <h5 className="fw-bold font-monospace text-info mb-1">{selectedTable.tableName}</h5>
                  <span className="badge bg-secondary small">{selectedTable.tableType}</span>
                </div>
                <div className="text-muted small">
                  {selectedTable.columns.length} Columns • {selectedTable.foreignKeys.length} Relationships
                </div>
              </div>

              {/* Columns Table */}
              <h6 className="fw-semibold mb-3 d-flex align-items-center gap-1.5 small text-uppercase text-muted">
                <Layers size={16} /> Column Definitions
              </h6>
              <div className="table-responsive rounded-3 border border-secondary border-opacity-25 mb-4">
                <table className="table table-dark table-hover align-middle mb-0 font-sans" style={{ fontSize: '0.85rem' }}>
                  <thead className="table-secondary">
                    <tr>
                      <th>Column Name</th>
                      <th>Data Type</th>
                      <th>Key Type</th>
                      <th>Nullable</th>
                      <th>FK References</th>
                    </tr>
                  </thead>
                  <tbody>
                    {selectedTable.columns.map((col) => (
                      <tr key={col.columnName}>
                        <td className="fw-semibold text-light font-monospace">{col.columnName}</td>
                        <td className="text-info">{col.dataType}({col.columnSize})</td>
                        <td>
                          {col.isPrimaryKey && (
                            <span className="badge bg-warning bg-opacity-25 text-warning border border-warning border-opacity-25 d-inline-flex align-items-center gap-1">
                              <Key size={12} /> PRIMARY
                            </span>
                          )}
                          {col.isForeignKey && (
                            <span className="badge bg-info bg-opacity-25 text-info border border-info border-opacity-25 d-inline-flex align-items-center gap-1 ms-1">
                              <Link2 size={12} /> FOREIGN
                            </span>
                          )}
                          {!col.isPrimaryKey && !col.isForeignKey && <span className="text-muted">-</span>}
                        </td>
                        <td>{col.isNullable ? <span className="text-success">YES</span> : <span className="text-muted">NO</span>}</td>
                        <td className="text-muted font-monospace small">
                          {col.fkReferencedTable ? `${col.fkReferencedTable}(${col.fkReferencedColumn})` : '-'}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              {/* Foreign Keys / Relationships */}
              {selectedTable.foreignKeys.length > 0 && (
                <div>
                  <h6 className="fw-semibold mb-3 d-flex align-items-center gap-1.5 small text-uppercase text-muted">
                    <Link2 size={16} /> Foreign Key Relationships
                  </h6>
                  <ul className="list-group list-group-dark">
                    {selectedTable.foreignKeys.map((fk, idx) => (
                      <li key={idx} className="list-group-item bg-dark text-light border-secondary border-opacity-25 small font-monospace">
                        <span className="text-info">{fk.fkColumnName}</span> → <span className="text-warning">{fk.pkTableName}({fk.pkColumnName})</span>
                      </li>
                    ))}
                  </ul>
                </div>
              )}
            </div>
          ) : (
            <div className="glass-card p-5 text-center rounded-4 text-muted">
              Select a table from the sidebar to inspect its columns and relationships.
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
