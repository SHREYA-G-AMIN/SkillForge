import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api/client';
import Alert from '../components/Alert';
import OpportunityCard from '../components/OpportunityCard';

export default function SavedOpportunities() {
  const [bookmarks, setBookmarks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchBookmarks = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await api.listBookmarks();
      setBookmarks(data || []);
    } catch (err) {
      setError(err.message || 'Failed to load bookmarked opportunities');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchBookmarks();
  }, []);

  const handleToggleBookmark = async (id, isBookmarked) => {
    if (!isBookmarked) {
      await api.removeBookmark(id);
      // Remove from view immediately
      setBookmarks((prev) => prev.filter((b) => b.id !== id));
    }
  };

  return (
    <div>
      <div className="page-head">
        <div>
          <h1>Saved Opportunities</h1>
          <p className="muted">Your bookmarked internships, jobs, and hackathons</p>
        </div>
      </div>

      {error && <Alert type="error">{error}</Alert>}

      {loading ? (
        <p className="muted center" style={{ padding: '3rem' }}>Loading saved opportunities...</p>
      ) : bookmarks.length === 0 ? (
        <div className="panel center" style={{ padding: '3rem 1.5rem' }}>
          <p className="empty" style={{ fontSize: '1.1rem', marginBottom: '0.75rem' }}>
            You haven't bookmarked any opportunities yet.
          </p>
          <p className="muted" style={{ marginBottom: '1.25rem' }}>
            Click the star icon on any opportunity card to save it for quick access later.
          </p>
          <Link to="/opportunities" className="btn btn-primary">
            Explore Opportunities →
          </Link>
        </div>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(310px, 1fr))', gap: '1.25rem' }}>
          {bookmarks.map((opp) => (
            <OpportunityCard
              key={opp.id}
              opportunity={opp}
              onToggleBookmark={handleToggleBookmark}
            />
          ))}
        </div>
      )}
    </div>
  );
}
