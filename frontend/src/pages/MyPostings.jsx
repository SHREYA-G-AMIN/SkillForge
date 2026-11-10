import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api/client';
import Alert from '../components/Alert';
import StatusBadge from '../components/StatusBadge';

export default function MyPostings() {
  const [postings, setPostings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [actionMsg, setActionMsg] = useState(null);

  const fetchPostings = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await api.myPostings();
      setPostings(data || []);
    } catch (err) {
      setError(err.message || 'Failed to load your postings');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPostings();
  }, []);

  const handleToggleStatus = async (id, currentStatus) => {
    const nextStatus = currentStatus === 'OPEN' ? 'CLOSED' : 'OPEN';
    setError(null);
    try {
      await api.setOpportunityStatus(id, nextStatus);
      setActionMsg(`Posting marked as ${nextStatus.toLowerCase()}`);
      setPostings((prev) =>
        prev.map((p) => (p.id === id ? { ...p, status: nextStatus } : p))
      );
    } catch (err) {
      setError(err.message || 'Failed to update posting status');
    }
  };

  const handleDelete = async (id, title) => {
    if (!window.confirm(`Are you sure you want to permanently delete "${title}"? All associated applicant records will also be removed.`)) {
      return;
    }
    setError(null);
    try {
      await api.deleteOpportunity(id);
      setActionMsg('Opportunity deleted successfully.');
      setPostings((prev) => prev.filter((p) => p.id !== id));
    } catch (err) {
      setError(err.message || 'Failed to delete opportunity');
    }
  };

  return (
    <div>
      <div className="page-head">
        <div>
          <h1>My Job Postings</h1>
          <p className="muted">Manage and track your active and closed recruitment opportunities</p>
        </div>
        <Link to="/recruiter/postings/new" className="btn btn-primary">
          + Post New Opportunity
        </Link>
      </div>

      {actionMsg && <Alert type="success">{actionMsg}</Alert>}
      {error && <Alert type="error">{error}</Alert>}

      {loading ? (
        <p className="muted center" style={{ padding: '3rem' }}>Loading postings...</p>
      ) : postings.length === 0 ? (
        <div className="panel center" style={{ padding: '3rem 1.5rem' }}>
          <p className="empty" style={{ fontSize: '1.1rem', marginBottom: '0.75rem' }}>
            You haven't posted any opportunities yet.
          </p>
          <Link to="/recruiter/postings/new" className="btn btn-primary">
            Create Your First Posting →
          </Link>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          {postings.map((p) => {
            const deadlineFormatted = p.deadline
              ? new Date(p.deadline).toLocaleDateString(undefined, { month: 'short', day: 'numeric', year: 'numeric' })
              : 'None';

            return (
              <div key={p.id} className="card" style={{ padding: '1.25rem' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '1rem' }}>
                  <div style={{ flex: '1 1 300px' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.35rem' }}>
                      <span style={{ textTransform: 'capitalize', fontSize: '0.8rem', fontWeight: 700, padding: '0.15rem 0.5rem', borderRadius: '6px', background: 'var(--primary-wash)', color: 'var(--primary-deep)' }}>
                        {p.type?.toLowerCase()}
                      </span>
                      <StatusBadge status={p.status} />
                    </div>

                    <h3 style={{ fontSize: '1.2rem', margin: '0 0 0.25rem' }}>
                      <Link to={`/opportunities/${p.id}`} style={{ color: 'var(--ink)', textDecoration: 'none' }}>
                        {p.title}
                      </Link>
                    </h3>
                    <p className="muted" style={{ margin: 0, fontSize: '0.9rem' }}>
                      {p.company} • 📍 {p.location} • ⏳ Deadline: {deadlineFormatted}
                    </p>
                  </div>

                  <div className="row-actions" style={{ flexWrap: 'wrap' }}>
                    <Link
                      to={`/recruiter/postings/${p.id}/applicants`}
                      className="btn btn-outline"
                      style={{ fontSize: '0.85rem', padding: '0.4rem 0.8rem' }}
                    >
                      👥 View Applicants ({p.applicantCount ?? 0})
                    </Link>

                    <Link
                      to={`/recruiter/postings/${p.id}/edit`}
                      className="btn btn-outline"
                      style={{ fontSize: '0.85rem', padding: '0.4rem 0.8rem' }}
                    >
                      ✏️ Edit
                    </Link>

                    <button
                      type="button"
                      className="btn btn-outline"
                      onClick={() => handleToggleStatus(p.id, p.status)}
                      style={{ fontSize: '0.85rem', padding: '0.4rem 0.8rem' }}
                    >
                      {p.status === 'OPEN' ? '🔒 Close' : '🔓 Reopen'}
                    </button>

                    <button
                      type="button"
                      className="btn btn-danger"
                      onClick={() => handleDelete(p.id, p.title)}
                      style={{ fontSize: '0.85rem', padding: '0.4rem 0.8rem' }}
                    >
                      🗑️ Delete
                    </button>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
