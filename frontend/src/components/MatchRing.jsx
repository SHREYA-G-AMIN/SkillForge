export default function MatchRing({ percentage = 0, size = 44, strokeWidth = 4 }) {
  const radius = (size - strokeWidth * 2) / 2;
  const circumference = 2 * Math.PI * radius;
  const validPct = Math.max(0, Math.min(100, Math.round(percentage ?? 0)));
  const offset = circumference - (validPct / 100) * circumference;

  let color = 'var(--danger)';
  if (validPct >= 70) color = 'var(--mint)';
  else if (validPct >= 40) color = 'var(--spark)';

  return (
    <div
      className="match-ring"
      style={{ width: size, height: size, position: 'relative', display: 'inline-grid', placeItems: 'center', flexShrink: 0 }}
      title={`Match Score: ${validPct}%`}
      aria-label={`Match score ${validPct}%`}
    >
      <svg width={size} height={size} style={{ transform: 'rotate(-90deg)' }}>
        <circle
          cx={size / 2}
          cy={size / 2}
          r={radius}
          stroke="var(--line)"
          strokeWidth={strokeWidth}
          fill="none"
        />
        <circle
          cx={size / 2}
          cy={size / 2}
          r={radius}
          stroke={color}
          strokeWidth={strokeWidth}
          strokeDasharray={circumference}
          strokeDashoffset={offset}
          strokeLinecap="round"
          fill="none"
          style={{ transition: 'stroke-dashoffset 0.4s ease' }}
        />
      </svg>
      <span
        style={{
          position: 'absolute',
          fontSize: size <= 44 ? '0.72rem' : '0.85rem',
          fontWeight: 800,
          color: 'var(--ink)',
        }}
      >
        {validPct}%
      </span>
    </div>
  );
}
