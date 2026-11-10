import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api/client';
import Alert from '../components/Alert';
import StatusBadge from '../components/StatusBadge';

const STAGES = ['APPLIED', 'SHORTLISTED', 'INTERVIEW', 'SELECTED'];

export default function MyApplications() {
  const [applications, setApplications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchApplications = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await api.myApplications();
      setApplications(data || []);
    } catch (err) {
      setError(err.message || 'Failed to load your applications');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchApplications();
  }, []);

  return (
    <div>
      <div className="page-head">
        <div>
          <h1>My Applications</h1>
          <p className="muted">Track the progress and status of your submitted applications</p>
        </div>
      </div>

      {error && <Alert type="error">{error}</Alert>}

      {loading ? (
        <p className="muted center" style={{ padding: '3rem' }}>Loading applications...</p>
      ) : applications.length === 0 ? (
        <div className="panel center" style={{ padding: '3rem 1.5rem' }}>
          <p className="empty" style={{ fontSize: '1.1rem', marginBottom: '0.75rem' }}>You haven't applied to any opportunities yet.</p>
          <Link to="/opportunities" className="btn btn-primary">
            Explore Opportunities →
          </Link>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
          {applications.map((app) => {
            const isRejected = app.status === 'REJECTED';
            const appliedDate = app.appliedAt
              ? new Date(app.appliedAt).toLocaleDateString(undefined, { month: 'short', day: 'numeric', year: 'numeric' })
              : '';

            return (
              <div key={app.id} className="card" style={{ padding: '1.4rem' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '0.75rem', marginBottom: '1.25rem' }}>
                  <div>
                    <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center', marginBottom: '0.35rem' }}>
                      <span style={{ textTransform: 'capitalize', fontSize: '0.8rem', fontWeight: 700, padding: '0.15rem 0.5rem', borderRadius: '6px', background: 'var(--primary-wash)', color: 'var(--primary-deep)' }}>
                        {app.type?.toLowerCase()}
                      </span>
                      <StatusBadge status={app.status} />
                    </div>

                    <h3 style={{ fontSize: '1.2rem', margin: '0 0 0.2rem' }}>
                      <Link to={`/opportunities/${app.opportunityId}`} style={{ color: 'var(--ink)', textDecoration: 'none' }}>
                        {app.opportunityTitle}
                      </Link>
                    </h3>
                    <p className="muted" style={{ margin: 0, fontSize: '0.92rem' }}>
                      {app.company} • 📍 {app.location} • Applied on {appliedDate}
                    </p>
                  </div>

                  <Link to={`/opportunities/${app.opportunityId}`} className="btn btn-outline" style={{ fontSize: '0.85rem', padding: '0.4rem 0.8rem' }}>
                    View Posting →
                  </Link>
                </div>

                {/* Status Timeline */}
                <div style={{ borderTop: '1px solid var(--line)', paddingTop: '1.2rem' }}>
                  <h4 style={{ fontSize: '0.85rem', textTransform: 'uppercase', letterSpacing: '0.05em', color: 'var(--ink-soft)', marginBottom: '0.9rem' }}>
                    Application Status Progress
                  </h4>

                  {isRejected ? (
                    <div style={{ padding: '0.75rem 1rem', background: '#fdf1f0', border: '1px solid #f1c3c1', borderRadius: '10px', display: 'flex', alignItems: 'center', gap: '0.65rem' }}>
                      <span style={{ fontSize: '1.2rem', color: 'var(--danger)' }}>✕</span>
                      <div>
                        <strong style={{ color: 'var(--danger)' }}>Application Not Shortlisted (Rejected)</strong>
                        <p className="muted" style={{ margin: 0, fontSize: '0.85rem' }}>
                          Unfortunately, the recruiter decided not to move forward with your application at this time.
                        </p>
                      </div>
                    </div>
                  ) : (
                    <div className="status-tracker" style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '0.5rem', position: 'relative' }}>
                      {STAGES.map((stage, idx) => {
                        const currentIdx = STAGES.indexOf(app.status);
                        const isCompleted = idx <= currentIdx;
                        const isCurrent = idx === currentIdx;

                        return (
                          <div
                            key={stage}
                            style={{
                              padding: '0.6rem 0.5rem',
                              textAlign: 'center',
                              borderRadius: '8px',
                              background: isCurrent ? 'var(--primary-wash)' : isCompleted ? '#eaf8f3' : 'var(--canvas)',
                              border: isCurrent ? '1.5px solid var(--primary)' : isCompleted ? '1px solid #b4e5d5' : '1px solid var(--line)',
                              transition: 'all 0.2s ease',
                            }}
                          >
                            <div style={{ fontSize: '0.75rem', fontWeight: 800, color: isCurrent ? 'var(--primary-deep)' : isCompleted ? 'var(--mint)' : 'var(--ink-soft)' }}>
                              {idx + 1}. {stage}
                            </div>
                            <div style={{ fontSize: '0.7rem', marginTop: '0.15rem', color: isCurrent ? 'var(--primary)' : 'var(--ink-soft)' }}>
                              {isCurrent ? 'Current' : isCompleted ? 'Completed' : 'Pending'}
                            </div>
                          </div>
                        );
                      })}
                    </div>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
