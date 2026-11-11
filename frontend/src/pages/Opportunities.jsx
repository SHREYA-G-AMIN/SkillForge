import { useEffect, useState } from 'react';
import { api } from '../api/client';
import Alert from '../components/Alert';
import OpportunityCard from '../components/OpportunityCard';

const TYPES = [
  { label: 'All Opportunities', value: '' },
  { label: 'Internships', value: 'INTERNSHIP' },
  { label: 'Full-time Jobs', value: 'JOB' },
  { label: 'Hackathons', value: 'HACKATHON' },
];

export default function Opportunities() {
  const [opportunities, setOpportunities] = useState([]);
  const [skills, setSkills] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Filters
  const [q, setQ] = useState('');
  const [type, setType] = useState('');
  const [location, setLocation] = useState('');
  const [skillId, setSkillId] = useState('');

  // Fetch master skills once for filter
  useEffect(() => {
    api.listSkills().then(setSkills).catch(() => {});
  }, []);

  const loadData = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await api.listOpportunities({
        q: q.trim() || undefined,
        type: type || undefined,
        location: location.trim() || undefined,
        skillId: skillId || undefined,
      });
      setOpportunities(data || []);
    } catch (err) {
      setError(err.message || 'Failed to load opportunities');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [type, skillId]);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    loadData();
  };

  const handleToggleBookmark = async (id, isBookmarked) => {
    if (isBookmarked) {
      await api.addBookmark(id);
    } else {
      await api.removeBookmark(id);
    }
  };

  return (
    <div>
      <div className="page-head">
        <div>
          <h1>Explore Opportunities</h1>
          <p className="muted">Discover jobs, internships, and hackathons tailored to your skills</p>
        </div>
      </div>

      {error && <Alert type="error">{error}</Alert>}

      {/* Tabs */}
      <div className="tabs" role="tablist" aria-label="Opportunity types">
        {TYPES.map((t) => (
          <button
            key={t.value}
            role="tab"
            aria-selected={type === t.value}
            className={`tab ${type === t.value ? 'active' : ''}`}
            onClick={() => setType(t.value)}
          >
            {t.label}
          </button>
        ))}
      </div>

      {/* Search & Filter Bar */}
      <form onSubmit={handleSearchSubmit} className="card" style={{ padding: '1rem', marginBottom: '1.5rem' }}>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '0.75rem', alignItems: 'flex-end' }}>
          <div className="field" style={{ margin: 0 }}>
            <label htmlFor="search-q">Search</label>
            <input
              id="search-q"
              type="text"
              placeholder="Title, company, or keyword..."
              value={q}
              onChange={(e) => setQ(e.target.value)}
            />
          </div>

          <div className="field" style={{ margin: 0 }}>
            <label htmlFor="filter-loc">Location</label>
            <input
              id="filter-loc"
              type="text"
              placeholder="e.g. Bangalore, Remote..."
              value={location}
              onChange={(e) => setLocation(e.target.value)}
            />
          </div>

          <div className="field" style={{ margin: 0 }}>
            <label htmlFor="filter-skill">Required Skill</label>
            <select
              id="filter-skill"
              value={skillId}
              onChange={(e) => setSkillId(e.target.value)}
            >
              <option value="">All Skills</option>
              {skills.map((s) => (
                <option key={s.id} value={s.id}>{s.name}</option>
              ))}
            </select>
          </div>

          <div>
            <button type="submit" className="btn btn-primary" style={{ width: '100%' }}>
              Filter
            </button>
          </div>
        </div>
      </form>

      {/* Grid */}
      {loading ? (
        <p className="muted center" style={{ padding: '2rem' }}>Loading opportunities...</p>
      ) : opportunities.length === 0 ? (
        <div className="panel center" style={{ padding: '3rem 1.5rem' }}>
          <p className="empty" style={{ fontSize: '1.1rem', marginBottom: '0.5rem' }}>No opportunities found</p>
          <p className="muted">Try adjusting your filters or search terms.</p>
        </div>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(310px, 1fr))', gap: '1.25rem' }}>
          {opportunities.map((opp) => (
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
