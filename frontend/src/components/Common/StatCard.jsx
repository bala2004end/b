import React from 'react';

export default function StatCard({ title, value, subtitle, icon: Icon, color = 'primary' }) {
  return (
    <div className="glass-card p-3 rounded-3 h-100 d-flex align-items-center justify-content-between">
      <div>
        <div className="text-muted small fw-medium mb-1">{title}</div>
        <div className="fs-3 fw-bold mb-0">{value}</div>
        {subtitle && <div className="text-muted small" style={{ fontSize: '0.75rem' }}>{subtitle}</div>}
      </div>
      {Icon && (
        <div className={`p-3 rounded-3 bg-${color} bg-opacity-25 text-${color} border border-${color} border-opacity-25`}>
          <Icon size={24} />
        </div>
      )}
    </div>
  );
}
