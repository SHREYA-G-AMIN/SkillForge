export default function StatusBadge({ status }) {
  if (!status) return null;

  const config = {
    // Opportunity statuses
    OPEN: { bg: '#eaf8f3', color: '#14654f', border: '#b4e5d5', label: 'Open' },
    CLOSED: { bg: '#f1f3fa', color: '#565c80', border: '#e1e5f2', label: 'Closed' },
    // Application statuses
    APPLIED: { bg: 'var(--primary-wash)', color: 'var(--primary-deep)', border: '#cfd0f5', label: 'Applied' },
    SHORTLISTED: { bg: '#fff8e6', color: '#8c6b00', border: '#fce299', label: 'Shortlisted' },
    INTERVIEW: { bg: '#eaf4ff', color: '#095aba', border: '#b8d6fc', label: 'Interview' },
    SELECTED: { bg: '#eaf8f3', color: '#14654f', border: '#b4e5d5', label: 'Selected' },
    REJECTED: { bg: '#fdf1f0', color: '#9a2723', border: '#f1c3c1', label: 'Rejected' },
  }[status.toUpperCase()] || { bg: '#f1f3fa', color: '#565c80', border: '#e1e5f2', label: status };

  return (
    <span
      style={{
        display: 'inline-flex',
        alignItems: 'center',
        padding: '0.2rem 0.65rem',
        borderRadius: '999px',
        fontSize: '0.8rem',
        fontWeight: 700,
        backgroundColor: config.bg,
        color: config.color,
        border: `1px solid ${config.border}`,
        lineHeight: 1.2,
        whiteSpace: 'nowrap',
      }}
    >
      {config.label}
    </span>
  );
}
