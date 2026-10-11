import { useState } from 'react';
import { Link } from 'react-router-dom';
import Alert from '../components/Alert';
import BasicInfoForm from '../components/BasicInfoForm';
import EntityEditor from '../components/EntityEditor';
import SkillsEditor from '../components/SkillsEditor';
import { useProfile } from '../hooks/useProfile';

const PROJECT_FIELDS = [
  { name: 'title', label: 'Title', required: true, maxLength: 255 },
  { name: 'techUsed', label: 'Technologies used', maxLength: 255 },
  { name: 'link', label: 'Project link', type: 'url', maxLength: 255 },
  { name: 'description', label: 'Description', type: 'textarea', maxLength: 1000 },
];
const CERT_FIELDS = [
  { name: 'name', label: 'Certification name', required: true, maxLength: 255 },
  { name: 'issuer', label: 'Issuing organisation', maxLength: 255 },
  { name: 'issueDate', label: 'Issue date', type: 'date' },
];
const EXPERIENCE_FIELDS = [
  { name: 'company', label: 'Company', required: true, maxLength: 255 },
  { name: 'role', label: 'Role', required: true, maxLength: 255 },
  { name: 'duration', label: 'Duration', maxLength: 255 },
  { name: 'description', label: 'Description', type: 'textarea', maxLength: 1000 },
];

const TABS = [
  ['basic', 'Basic info'],
  ['skills', 'Skills'],
  ['projects', 'Projects'],
  ['certifications', 'Certifications'],
  ['experience', 'Experience'],
];

export default function ProfileEdit() {
  const { profile, setProfile, loading, error, reload } = useProfile();
  const [tab, setTab] = useState('basic');

  if (loading) return <p className="muted">Loading your profile...</p>;
  if (error) return <Alert>{error}</Alert>;

  return (
    <>
      <div className="page-head">
        <h1>Edit profile</h1>
        <Link to="/profile" className="btn btn-outline">View profile</Link>
      </div>

      <div className="tabs" role="tablist">
        {TABS.map(([key, text]) => (
          <button key={key} role="tab" aria-selected={tab === key} className={tab === key ? 'tab active' : 'tab'} onClick={() => setTab(key)}>
            {text}
          </button>
        ))}
      </div>

      {tab === 'basic' && <BasicInfoForm profile={profile} onSaved={setProfile} />}
      {tab === 'skills' && <SkillsEditor skills={profile.skills} onChanged={reload} />}
      {tab === 'projects' && (
        <EntityEditor
          resource="projects" noun="project" items={profile.projects} fields={PROJECT_FIELDS} onChanged={reload}
          renderItem={(p) => (
            <>
              <strong>{p.title}</strong>
              {p.techUsed && <span className="meta">{p.techUsed}</span>}
            </>
          )}
        />
      )}
      {tab === 'certifications' && (
        <EntityEditor
          resource="certifications" noun="certification" items={profile.certifications} fields={CERT_FIELDS} onChanged={reload}
          renderItem={(c) => (
            <>
              <strong>{c.name}</strong>
              <span className="meta">{[c.issuer, c.issueDate].filter(Boolean).join(', ')}</span>
            </>
          )}
        />
      )}
      {tab === 'experience' && (
        <EntityEditor
          resource="experience" noun="experience" items={profile.experiences} fields={EXPERIENCE_FIELDS} onChanged={reload}
          renderItem={(x) => (
            <>
              <strong>{x.role}</strong> at {x.company}
              {x.duration && <span className="meta">{x.duration}</span>}
            </>
          )}
        />
      )}
    </>
  );
}