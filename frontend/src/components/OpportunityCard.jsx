import { useState } from 'react';
import { Link } from 'react-router-dom';
import MatchRing from './MatchRing';
import StatusBadge from './StatusBadge';

export default function OpportunityCard({ opportunity, onToggleBookmark, showMatch = true }) {
  const [bookmarked, setBookmarked] = useState(opportunity.bookmarked);
  const [busy, setBusy] = useState(false);

  const handleBookmark = async (e) => {
    e.preventDefault();
    e.stopPropagation();
    if (busy || !onToggleBookmark) return;
    setBusy(true);
    const nextState = !bookmarked;
    setBookmarked(nextState); // optimistic update
    try {
      await onToggleBookmark(opportunity.id, nextState);
    } catch {
      setBookmarked(!nextState); // rollback on error
    } finally {
      setBusy(false);
    }
  };

  const deadlineFormatted = opportunity.deadline
    ? new Date(opportunity.deadline).toLocaleDateString(undefined, { month: 'short', day: 'numeric', year: 'numeric' })
    : 'No deadline';

  return (
    <article className="card opportunity-card" style={{ display: 'flex', flexDirection: 'column', justifyContent: 'space-between', position: 'relative' }}>
      <div>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: '0.75rem', marginBottom: '0.65rem' }}>
          <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center', flexWrap: 'wrap' }}>
            <span className="opp-type-tag" style={{ textTransform: 'capitalize', fontSize: '0.8rem', fontWeight: 700, padding: '0.15rem 0.55rem', borderRadius: '6px', background: 'var(--primary-wash)', color: 'var(--primary-deep)' }}>
              {opportunity.type?.toLowerCase()}
            </span>
            <StatusBadge status={opportunity.status} />
            {opportunity.eligible === false && (
              <span style={{ fontSize: '0.75rem', fontWeight: 700, padding: '0.15rem 0.55rem', borderRadius: '6px', background: '#fdf1f0', color: 'var(--danger)', border: '1px solid #f1c3c1' }}>
                Ineligible
              </span>
            )}
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            {showMatch && opportunity.matchPercentage !== null && opportunity.matchPercentage !== undefined && (
              <MatchRing percentage={opportunity.matchPercentage} size={42} strokeWidth={4} />
            )}
            {onToggleBookmark && (
              <button
                type="button"
                className="bookmark-btn"
                onClick={handleBookmark}
                disabled={busy}
                aria-label={bookmarked ? 'Remove from saved' : 'Save opportunity'}
                style={{
                  background: 'none',
                  border: 'none',
                  fontSize: '1.25rem',
                  cursor: busy ? 'wait' : 'pointer',
                  padding: '0.2rem',
                  color: bookmarked ? 'var(--spark)' : 'var(--ink-soft)',
                  lineHeight: 1,
                }}
              >
                {bookmarked ? '★' : '☆'}
              </button>
            )}
          </div>
        </div>

        <h3 style={{ fontSize: '1.15rem', marginBottom: '0.25rem' }}>
          <Link to={`/opportunities/${opportunity.id}`} style={{ textDecoration: 'none', color: 'var(--ink)' }}>
            {opportunity.title}
          </Link>
        </h3>
        <p className="muted" style={{ fontWeight: 600, fontSize: '0.92rem', marginBottom: '0.5rem' }}>
          {opportunity.company} • 📍 {opportunity.location}
        </p>

        {opportunity.minCgpa && (
          <p className="muted" style={{ fontSize: '0.85rem', marginBottom: '0.5rem' }}>
            Min CGPA: <strong>{opportunity.minCgpa}</strong>
          </p>
        )}
      </div>

      <div style={{ borderTop: '1px solid var(--line)', paddingTop: '0.75rem', marginTop: '0.75rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: '0.85rem' }}>
        <span className="muted">⏳ Deadline: {deadlineFormatted}</span>
        <Link to={`/opportunities/${opportunity.id}`} className="btn btn-outline" style={{ padding: '0.35rem 0.75rem', fontSize: '0.85rem' }}>
          View details →
        </Link>
      </div>
    </article>
  );
}
