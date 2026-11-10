import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { api } from '../api/client';
import Alert from '../components/Alert';
import MatchRing from '../components/MatchRing';
import StatusBadge from '../components/StatusBadge';

export default function OpportunityDetails() {
  const { id } = useParams();
  const [opp, setOpp] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [successMsg, setSuccessMsg] = useState(null);
  const [applying, setApplying] = useState(false);
  const [bookmarking, setBookmarking] = useState(false);

  const fetchDetails = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await api.getOpportunity(id);
      setOpp(data);
    } catch (err) {
      setError(err.message || 'Failed to load opportunity details');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDetails();
  }, [id]);

  const handleApply = async () => {
    if (!window.confirm('Are you sure you want to submit your application for this opportunity?')) return;
    setApplying(true);
    setError(null);
    setSuccessMsg(null);
    try {
      await api.applyTo(id);
      setSuccessMsg('Application submitted successfully! Track its status under My Applications.');
      await fetchDetails();
    } catch (err) {
      setError(err.message || 'Failed to submit application');
    } finally {
      setApplying(false);
    }
  };

  const handleToggleBookmark = async () => {
    if (bookmarking || !opp) return;
    setBookmarking(true);
    const nextState = !opp.bookmarked;
    setOpp((prev) => ({ ...prev, bookmarked: nextState }));
    try {
      if (nextState) {
        await api.addBookmark(id);
      } else {
        await api.removeBookmark(id);
      }
    } catch {
      setOpp((prev) => ({ ...prev, bookmarked: !nextState }));
    } finally {
      setBookmarking(false);
    }
  };

  if (loading) return <p className="muted center" style={{ padding: '3rem' }}>Loading opportunity details...</p>;
  if (error && !opp) return <Alert type="error">{error}</Alert>;
  if (!opp) return <p className="empty center">Opportunity not found.</p>;

  const deadlineFormatted = opp.deadline
    ? new Date(opp.deadline).toLocaleDateString(undefined, { month: 'long', day: 'numeric', year: 'numeric' })
    : 'None';

  return (
    <div>
      <div style={{ marginBottom: '1rem' }}>
        <Link to="/opportunities" className="muted" style={{ textDecoration: 'none' }}>
          ← Back to Opportunities
        </Link>
      </div>

      {successMsg && <Alert type="success">{successMsg}</Alert>}
      {error && <Alert type="error">{error}</Alert>}

      {/* Header Panel */}
      <div className="card" style={{ padding: '1.75rem', marginBottom: '1.5rem' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '1rem' }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem', marginBottom: '0.4rem', flexWrap: 'wrap' }}>
              <span style={{ textTransform: 'capitalize', fontSize: '0.85rem', fontWeight: 700, padding: '0.2rem 0.6rem', borderRadius: '6px', background: 'var(--primary-wash)', color: 'var(--primary-deep)' }}>
                {opp.type?.toLowerCase()}
              </span>
              <StatusBadge status={opp.status} />
              {opp.applied && <StatusBadge status={opp.applicationStatus || 'APPLIED'} />}
            </div>

            <h1 style={{ fontSize: '1.85rem', marginBottom: '0.35rem' }}>{opp.title}</h1>
            <p className="muted" style={{ fontSize: '1.05rem', fontWeight: 600 }}>
              {opp.company} • 📍 {opp.location}
            </p>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
            {opp.matchPercentage !== null && (
              <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '0.25rem' }}>
                <MatchRing percentage={opp.matchPercentage} size={54} strokeWidth={5} />
                <span className="muted" style={{ fontSize: '0.75rem', fontWeight: 700 }}>Match</span>
              </div>
            )}

            <button
              type="button"
              className="btn btn-outline"
              onClick={handleToggleBookmark}
              disabled={bookmarking}
              style={{ fontSize: '1.25rem', padding: '0.45rem 0.85rem', color: opp.bookmarked ? 'var(--spark)' : 'inherit' }}
              title={opp.bookmarked ? 'Saved to bookmarks' : 'Bookmark this opportunity'}
            >
              {opp.bookmarked ? '★ Saved' : '☆ Save'}
            </button>
          </div>
        </div>

        {/* Action bar */}
        <div style={{ borderTop: '1px solid var(--line)', marginTop: '1.25rem', paddingTop: '1.25rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem' }}>
          <div style={{ display: 'flex', gap: '1.5rem', flexWrap: 'wrap', fontSize: '0.9rem' }}>
            <span>⏳ Deadline: <strong>{deadlineFormatted}</strong></span>
            {opp.minCgpa && <span>🎯 Min CGPA: <strong>{opp.minCgpa}</strong></span>}
            {opp.eligibleYears?.length > 0 && <span>🎓 Eligible Years: <strong>{opp.eligibleYears.join(', ')}</strong></span>}
          </div>

          <div>
            {opp.applied ? (
              <button type="button" className="btn btn-outline" disabled style={{ cursor: 'default' }}>
                ✓ Application Submitted ({opp.applicationStatus})
              </button>
            ) : (
              <button
                type="button"
                className="btn btn-primary"
                onClick={handleApply}
                disabled={applying || opp.status !== 'OPEN' || opp.eligible === false}
              >
                {applying ? 'Submitting...' : opp.eligible === false ? 'Ineligible to Apply' : 'Apply Now →'}
              </button>
            )}
          </div>
        </div>
      </div>

      {/* Grid: Description & Requirements vs Skills Match */}
      <div style={{ display: 'grid', gridTemplateColumns: 'minmax(0, 1.35fr) minmax(0, 1fr)', gap: '1.5rem' }}>
        <div>
          {/* Description */}
          <div className="panel">
            <h2>Role Description & Details</h2>
            <div style={{ whiteSpace: 'pre-wrap', lineHeight: 1.7, color: 'var(--ink)' }}>
              {opp.description}
            </div>
          </div>

          {/* Eligibility Criteria */}
          <div className="panel">
            <h2>Eligibility Requirements</h2>
            <dl className="facts" style={{ margin: 0 }}>
              <dt>Minimum CGPA</dt>
              <dd>{opp.minCgpa ? `${opp.minCgpa} / 10.0` : 'None specified'}</dd>

              <dt>Eligible Branches</dt>
              <dd>{opp.eligibleBranches?.length > 0 ? opp.eligibleBranches.join(', ') : 'Open to all branches'}</dd>

              <dt>Eligible Years</dt>
              <dd>{opp.eligibleYears?.length > 0 ? opp.eligibleYears.map((y) => `Year ${y}`).join(', ') : 'Open to all years'}</dd>

              <dt>Application Status</dt>
              <dd>{opp.status === 'OPEN' ? 'Accepting applications' : 'Applications closed'}</dd>
            </dl>

            {opp.eligibilityReasons?.length > 0 && (
              <div style={{ marginTop: '1rem', padding: '0.75rem', borderRadius: '10px', background: '#fdf1f0', border: '1px solid #f1c3c1' }}>
                <p style={{ fontWeight: 700, color: 'var(--danger)', margin: '0 0 0.35rem', fontSize: '0.9rem' }}>
                  Why you might be ineligible:
                </p>
                <ul style={{ margin: 0, paddingLeft: '1.2rem', color: '#9a2723', fontSize: '0.85rem' }}>
                  {opp.eligibilityReasons.map((r, i) => (
                    <li key={i}>{r}</li>
                  ))}
                </ul>
              </div>
            )}
          </div>
        </div>

        {/* Skill Match Breakdown Column */}
        <div>
          <div className="panel">
            <h2>Required Skills Breakdown</h2>

            {/* Matched Skills */}
            <div style={{ marginBottom: '1.25rem' }}>
              <h3 style={{ fontSize: '0.95rem', color: 'var(--mint)', display: 'flex', alignItems: 'center', gap: '0.4rem', marginBottom: '0.5rem' }}>
                ✓ Matched Skills ({opp.matchedSkills?.length || 0})
              </h3>
              {opp.matchedSkills?.length === 0 ? (
                <p className="empty" style={{ fontSize: '0.85rem' }}>None of your profile skills meet the required level yet.</p>
              ) : (
                <ul className="stack" style={{ margin: 0 }}>
                  {opp.matchedSkills?.map((s) => (
                    <li key={s.skillId} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <div>
                        <strong>{s.skillName}</strong>
                        <div className="meta-line" style={{ fontSize: '0.8rem', margin: 0 }}>
                          Required Level: {s.requiredLevel} • Your Level: {s.studentLevel ?? 0}
                        </div>
                      </div>
                      <div className="pips" title={`Required level ${s.requiredLevel}`}>
                        {[1, 2, 3, 4, 5].map((lvl) => (
                          <i key={lvl} className={lvl <= s.requiredLevel ? 'on' : ''} />
                        ))}
                      </div>
                    </li>
                  ))}
                </ul>
              )}
            </div>

            {/* Missing Skills */}
            <div>
              <h3 style={{ fontSize: '0.95rem', color: 'var(--danger)', display: 'flex', alignItems: 'center', gap: '0.4rem', marginBottom: '0.5rem' }}>
                ⚠️ Missing / Skill Gap ({opp.missingSkills?.length || 0})
              </h3>
              {opp.missingSkills?.length === 0 ? (
                <p className="muted" style={{ fontSize: '0.85rem' }}>You meet all skill requirements for this role! 🎉</p>
              ) : (
                <ul className="stack" style={{ margin: 0 }}>
                  {opp.missingSkills?.map((s) => (
                    <li key={s.skillId} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <div>
                        <strong style={{ color: 'var(--ink)' }}>{s.skillName}</strong>
                        <div className="meta-line" style={{ fontSize: '0.8rem', margin: 0 }}>
                          Requires Level {s.requiredLevel} {s.studentLevel ? `(You have Level ${s.studentLevel})` : '(Not in profile)'}
                        </div>
                      </div>
                      <div className="pips" title={`Required level ${s.requiredLevel}`}>
                        {[1, 2, 3, 4, 5].map((lvl) => (
                          <i key={lvl} className={lvl <= s.requiredLevel ? 'on' : ''} />
                        ))}
                      </div>
                    </li>
                  ))}
                </ul>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
