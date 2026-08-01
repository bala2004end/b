import React from 'react';

export default function StatCard({ title, value, subtitle, icon: Icon, color = 'primary' }) {
  return (
    <div className="glass-card p-3 rounded-3 h-100 d-flex align-items-center justify-content-between min-w-0">
      <div className="min-w-0 flex-grow-1 pe-2">
        <div className="text-muted small fw-medium mb-1 text-truncate">{title}</div>
        <div className="statcard-value mb-0 text-light">{value}</div>
        {subtitle && <div className="text-muted small text-truncate mt-1" style={{ fontSize: '0.75rem' }}>{subtitle}</div>}
      </div>
      {Icon && (
        <div className={`p-3 rounded-3 bg-${color} bg-opacity-25 text-${color} border border-${color} border-opacity-25 flex-shrink-0`}>
          <Icon size={24} />
        </div>
      )}
    </div>
  );
}
