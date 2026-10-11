import { Link } from 'react-router-dom';
import Alert from '../components/Alert';
import Avatar from '../components/Avatar';
import { useProfile } from '../hooks/useProfile';
import { completeness } from '../utils/completeness';

const LEVEL_NUM = { BEGINNER: 1, INTERMEDIATE: 2, ADVANCED: 3 };
const pretty = (l) => l.charAt(0) + l.slice(1).toLowerCase();

function Pips({ level }) {
  const n = LEVEL_NUM[level] || 1;
  return (
    <span className="pips" role="img" aria-label={pretty(level)}>
      {[1, 2, 3].map((i) => <i key={i} className={i <= n ? 'on' : ''} />)}
    </span>
  );
}

function Row({ label, children }) {
  const empty = children === null || children === undefined || children === '';
  return (
    <>
      <dt>{label}</dt>
      <dd className={empty ? 'muted' : ''}>{empty ? 'Not added' : children}</dd>
    </>
  );
}

function Empty({ children }) {
  return (
    <p className="empty">
      {children} <Link to="/profile/edit">Add it now</Link>
    </p>
  );
}

export default function Profile() {
  const { profile, loading, error } = useProfile();

  if (loading) return <p className="muted">Loading your profile...</p>;
  if (error) return <Alert>{error}</Alert>;

  const { percent, next } = completeness(profile);
  const firstName = profile.name.trim().split(' ')[0];
  const tags = [profile.college, profile.degree, profile.branch, profile.semester ? `Semester ${profile.semester}` : null].filter(Boolean);

  return (
    <>
      <header className="profile-hero">
        <Avatar size="xl" />
        <div className="hero-id">
          <p className="greeting">Hi, {firstName} 👋</p>
          <h1>{profile.name}</h1>
          <p className="hero-goal">
            {profile.careerGoal ? `Aiming to become a ${profile.careerGoal}` : 'Set a career goal so your profile points somewhere.'}
          </p>
          {tags.length > 0 && (
            <ul className="hero-tags">
              {tags.map((t) => <li key={t}>{t}</li>)}
            </ul>
          )}
        </div>
        <div className="hero-side">
          <div className="meter" role="img" aria-label={`Profile ${percent}% complete`}>
            <div className="meter-bar"><div style={{ width: `${percent}%` }} /></div>
            <p><strong>{percent}%</strong> complete</p>
            {next && <p className="meter-next">Next: {next}</p>}
          </div>
          <Link to="/profile/edit" className="btn btn-spark">Edit profile</Link>
        </div>
      </header>

      <div className="profile-grid">
        <aside className="panel">
          <h2>About</h2>
          <dl className="facts">
            <Row label="Email">{profile.email}</Row>
            <Row label="USN">{profile.usn}</Row>
            <Row label="Phone">{profile.phone}</Row>
            <Row label="CGPA">{profile.cgpa}</Row>
            <Row label="Interests">{profile.careerInterests}</Row>
            <Row label="Resume">
              {profile.resumeUrl && <a href={profile.resumeUrl} target="_blank" rel="noopener noreferrer">Open resume</a>}
            </Row>
          </dl>
        </aside>

        <div className="main-col">
          <section className="panel">
            <h2>Skills</h2>
            {profile.skills.length === 0 ? (
              <Empty>No skills yet. Skills are how opportunities get matched to you.</Empty>
            ) : (
              <ul className="skill-list">
                {profile.skills.map((s) => (
                  <li key={s.id} className="skill-pill">
                    {s.name} <Pips level={s.level} />
                  </li>
                ))}
              </ul>
            )}
          </section>

          <section className="panel">
            <h2>Projects</h2>
            {profile.projects.length === 0 ? (
              <Empty>No projects yet.</Empty>
            ) : (
              <ul className="stack">
                {profile.projects.map((p) => (
                  <li key={p.id}>
                    <h3>{p.title}</h3>
                    {p.techUsed && <p className="meta-line">{p.techUsed}</p>}
                    {p.description && <p>{p.description}</p>}
                    {p.link && <a href={p.link} target="_blank" rel="noopener noreferrer">View project</a>}
                  </li>
                ))}
              </ul>
            )}
          </section>

          <section className="panel">
            <h2>Certifications</h2>
            {profile.certifications.length === 0 ? (
              <Empty>No certifications yet.</Empty>
            ) : (
              <ul className="stack">
                {profile.certifications.map((c) => (
                  <li key={c.id}>
                    <h3>{c.name}</h3>
                    <p className="meta-line">{[c.issuer, c.issueDate].filter(Boolean).join(', ')}</p>
                  </li>
                ))}
              </ul>
            )}
          </section>

          <section className="panel">
            <h2>Experience</h2>
            {profile.experiences.length === 0 ? (
              <Empty>No experience yet. Internships and part-time work count.</Empty>
            ) : (
              <ol className="timeline">
                {profile.experiences.map((x) => (
                  <li key={x.id}>
                    <h3>{x.role}, {x.company}</h3>
                    {x.duration && <p className="meta-line">{x.duration}</p>}
                    {x.description && <p>{x.description}</p>}
                  </li>
                ))}
              </ol>
            )}
          </section>
        </div>
      </div>
    </>
  );
}