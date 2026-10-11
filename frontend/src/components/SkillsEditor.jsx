import { useEffect, useState } from 'react';
import { api } from '../api/client';
import Alert from './Alert';

const LEVELS = ['BEGINNER', 'INTERMEDIATE', 'ADVANCED'];
const label = (l) => l.charAt(0) + l.slice(1).toLowerCase();

export default function SkillsEditor({ skills, onChanged }) {
  const [master, setMaster] = useState([]);
  const [skillId, setSkillId] = useState('');
  const [level, setLevel] = useState('BEGINNER');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    api.listSkills().then(setMaster).catch((err) => setError(err.message));
  }, []);

  const owned = new Set(skills.map((s) => s.skillId));
  const available = master.filter((s) => !owned.has(s.id));

  const run = async (action) => {
    setBusy(true);
    setError('');
    try {
      await action();
      await onChanged();
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  };

  const add = (e) => {
    e.preventDefault();
    if (!skillId) return setError('Choose a skill to add');
    run(async () => {
      await api.addSkill({ skillId: Number(skillId), level });
      setSkillId('');
    });
  };

  return (
    <div className="card">
      <h2>Technical skills</h2>
      <Alert>{error}</Alert>

      <form onSubmit={add} className="inline-form">
        <select value={skillId} onChange={(e) => setSkillId(e.target.value)} aria-label="Skill">
          <option value="">Select a skill...</option>
          {available.map((s) => (
            <option key={s.id} value={s.id}>{s.name} ({s.category})</option>
          ))}
        </select>
        <select value={level} onChange={(e) => setLevel(e.target.value)} aria-label="Proficiency">
          {LEVELS.map((l) => <option key={l} value={l}>{label(l)}</option>)}
        </select>
        <button className="btn btn-primary" disabled={busy}>Add</button>
      </form>

      {skills.length === 0 ? (
        <p className="muted">No skills added yet.</p>
      ) : (
        <ul className="item-list">
          {skills.map((s) => (
            <li key={s.id} className="item-row">
              <div><strong>{s.name}</strong> <span className="muted">{s.category}</span></div>
              <div className="row-actions">
                <select
                  value={s.level}
                  disabled={busy}
                  aria-label={`Level for ${s.name}`}
                  onChange={(e) => run(() => api.updateSkill(s.id, { level: e.target.value }))}
                >
                  {LEVELS.map((l) => <option key={l} value={l}>{label(l)}</option>)}
                </select>
                <button className="btn btn-danger" disabled={busy} onClick={() => run(() => api.deleteSkill(s.id))}>Remove</button>
              </div>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
