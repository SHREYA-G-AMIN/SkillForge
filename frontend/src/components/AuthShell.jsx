/** Split layout for Login and Register: skill-chip hero on the left, the form on the right. */
const CHIPS = [
  ['💻', 'Java', 'on-3', '-3deg'],
  ['⚛️', 'React', 'on-2', '2deg'],
  ['☁️', 'AWS', 'on-1', '-2deg'],
  ['🧠', 'Machine Learning', 'on-1', '3deg'],
  ['🗄️', 'SQL', 'on-2', '-1deg'],
  ['🐳', 'Docker', 'on-1', '2deg'],
];

export default function AuthShell({ children }) {
  return (
    <div className="auth-split">
      <aside className="auth-hero">
        <span className="brand brand-light">Skill<span>Forge</span></span>
        <div>
          <h1>Know your skills. Close the gaps.</h1>
          <p>Keep your skills, projects and career goal in one profile, so the right internships can find you.</p>
        </div>
        <ul className="hero-chips" aria-hidden="true">
          {CHIPS.map(([icon, name, level, tilt]) => (
            <li key={name} className={`hero-chip ${level}`} style={{ '--tilt': tilt }}>
              <span>{icon}</span> {name}
            </li>
          ))}
        </ul>
      </aside>
      <section className="auth-panel">
        <div className="auth-form">{children}</div>
      </section>
    </div>
  );
}