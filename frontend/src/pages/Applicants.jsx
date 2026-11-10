import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { api } from '../api/client';
import Alert from '../components/Alert';
import MatchRing from '../components/MatchRing';
import StatusBadge from '../components/StatusBadge';

export default function Applicants() {
  const { id } = useParams();
  const [opportunity, setOpportunity] = useState(null);
  const [applicants, setApplicants] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [actionSuccess, setActionSuccess] = useState(null);
  const [updatingId, setUpdatingId] = useState(null);

  const fetchData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [oppData, appsData] = await Promise.all([
        api.getOpportunity(id),
        api.getApplicants(id),
      ]);
      setOpportunity(oppData);
      setApplicants(appsData || []);
    } catch (err) {
      setError(err.message || 'Failed to load applicant list');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [id]);

  const handleStatusChange = async (appId, nextStatus) => {
    if (!nextStatus) return;
    setUpdatingId(appId);
    setError(null);
    setActionSuccess(null);

    // Save previous state for rollback
    const prevApplicants = [...applicants];

    // Optimistic UI update
    setApplicants((prev) =>
      prev.map((a) => (a.applicationId === appId ? { ...a, status: nextStatus } : a))
    );

    try {
      const updatedItem = await api.updateApplicationStatus(appId, nextStatus);
      // Update with server response
      setApplicants((prev) =>
        prev.map((a) => (a.applicationId === appId ? updatedItem : a))
      );
      setActionSuccess(`Application status successfully updated to ${nextStatus}.`);
    } catch (err) {
      // Rollback on failure
      setApplicants(prevApplicants);
      setError(err.message || 'Failed to update application status');
    } finally {
      setUpdatingId(null);
    }
  };

  if (loading) return <p className="muted center" style={{ padding: '3rem' }}>Loading applicant roster...</p>;

  return (
    <div>
      <div style={{ marginBottom: '1rem' }}>
        <Link to="/recruiter/postings" className="muted" style={{ textDecoration: 'none' }}>
          ← Back to My Postings
        </Link>
      </div>

      <div className="page-head">
        <div>
          <h1>Applicants: {opportunity?.title || `Posting #${id}`}</h1>
          <p className="muted">
            {opportunity?.company} • Total Applicants: {applicants.length}
          </p>
        </div>
      </div>

      {actionSuccess && <Alert type="success">{actionSuccess}</Alert>}
      {error && <Alert type="error">{error}</Alert>}

      {applicants.length === 0 ? (
        <div className="panel center" style={{ padding: '3rem 1.5rem' }}>
          <p className="empty" style={{ fontSize: '1.1rem', marginBottom: '0.5rem' }}>No candidates have applied yet.</p>
          <p className="muted">Candidates who apply will appear here along with their match score and skill breakdown.</p>
        </div>
      ) : (
        <div className="panel" style={{ padding: 0, overflowX: 'auto' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', minWidth: '680px' }}>
            <thead>
              <tr style={{ background: 'var(--canvas)', borderBottom: '1px solid var(--line)', fontSize: '0.85rem', color: 'var(--ink-soft)' }}>
                <th style={{ padding: '0.85rem 1rem' }}>Candidate</th>
                <th style={{ padding: '0.85rem 1rem' }}>Academics</th>
                <th style={{ padding: '0.85rem 1rem' }}>Match %</th>
                <th style={{ padding: '0.85rem 1rem' }}>Missing Skills</th>
                <th style={{ padding: '0.85rem 1rem' }}>Applied On</th>
                <th style={{ padding: '0.85rem 1rem' }}>Current Status</th>
                <th style={{ padding: '0.85rem 1rem' }}>Action</th>
              </tr>
            </thead>
            <tbody>
              {applicants.map((a) => {
                const appliedDate = a.appliedAt
                  ? new Date(a.appliedAt).toLocaleDateString(undefined, { month: 'short', day: 'numeric', year: 'numeric' })
                  : '';
                const legalOptions = a.legalNextStatuses || [];

                return (
                  <tr key={a.applicationId} style={{ borderBottom: '1px solid var(--line)', fontSize: '0.9rem' }}>
                    {/* Candidate */}
                    <td style={{ padding: '0.9rem 1rem' }}>
                      <strong style={{ display: 'block', color: 'var(--ink)' }}>{a.studentName}</strong>
                      <span className="muted" style={{ fontSize: '0.8rem' }}>{a.email}</span>
                    </td>

                    {/* Academics */}
                    <td style={{ padding: '0.9rem 1rem' }}>
                      <div>CGPA: <strong>{a.cgpa ?? 'N/A'}</strong></div>
                      <div className="muted" style={{ fontSize: '0.8rem' }}>
                        {a.branch || 'Branch N/A'} {a.year ? `• Year ${a.year}` : ''}
                      </div>
                    </td>

                    {/* Match % */}
                    <td style={{ padding: '0.9rem 1rem' }}>
                      <MatchRing percentage={a.matchPercentage} size={38} strokeWidth={3.5} />
                    </td>

                    {/* Missing Skills */}
                    <td style={{ padding: '0.9rem 1rem', maxWidth: '200px' }}>
                      {a.missingSkills?.length > 0 ? (
                        <div style={{ display: 'flex', gap: '0.25rem', flexWrap: 'wrap' }}>
                          {a.missingSkills.map((sk, idx) => (
                            <span
                              key={idx}
                              style={{
                                fontSize: '0.75rem',
                                padding: '0.1rem 0.4rem',
                                background: '#fdf1f0',
                                color: 'var(--danger)',
                                border: '1px solid #f1c3c1',
                                borderRadius: '4px',
                              }}
                            >
                              {sk}
                            </span>
                          ))}
                        </div>
                      ) : (
                        <span style={{ fontSize: '0.8rem', color: 'var(--mint)', fontWeight: 600 }}>
                          ✓ Full Match
                        </span>
                      )}
                    </td>

                    {/* Applied Date */}
                    <td style={{ padding: '0.9rem 1rem', whiteSpace: 'nowrap' }} className="muted">
                      {appliedDate}
                    </td>

                    {/* Status Badge */}
                    <td style={{ padding: '0.9rem 1rem' }}>
                      <StatusBadge status={a.status} />
                    </td>

                    {/* Status Dropdown */}
                    <td style={{ padding: '0.9rem 1rem' }}>
                      {legalOptions.length > 0 ? (
                        <select
                          disabled={updatingId === a.applicationId}
                          value=""
                          onChange={(e) => handleStatusChange(a.applicationId, e.target.value)}
                          style={{ fontSize: '0.85rem', padding: '0.35rem 0.5rem', width: 'auto' }}
                          aria-label={`Change status for ${a.studentName}`}
                        >
                          <option value="" disabled>
                            Move to...
                          </option>
                          {legalOptions.map((opt) => (
                            <option key={opt} value={opt}>
                              → {opt}
                            </option>
                          ))}
                        </select>
                      ) : (
                        <span className="muted" style={{ fontSize: '0.8rem', fontStyle: 'italic' }}>
                          Terminal
                        </span>
                      )}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
