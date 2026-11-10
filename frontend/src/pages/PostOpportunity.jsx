import { useEffect, useState } from 'react';
import { useNavigate, useParams, Link } from 'react-router-dom';
import { api } from '../api/client';
import Alert from '../components/Alert';
import SkillRequirementEditor from '../components/SkillRequirementEditor';

export default function PostOpportunity() {
  const { id } = useParams();
  const isEdit = Boolean(id);
  const navigate = useNavigate();

  const [availableSkills, setAvailableSkills] = useState([]);
  const [submitting, setSubmitting] = useState(false);
  const [loading, setLoading] = useState(isEdit);
  const [serverError, setServerError] = useState(null);
  const [fieldErrors, setFieldErrors] = useState({});

  const [form, setForm] = useState({
    title: '',
    company: '',
    location: '',
    type: 'INTERNSHIP',
    description: '',
    deadline: '',
    minCgpa: '',
    eligibleBranches: '',
    eligibleYears: [3, 4],
    requiredSkills: [],
  });

  useEffect(() => {
    api.listSkills().then(setAvailableSkills).catch(() => {});
  }, []);

  useEffect(() => {
    if (isEdit) {
      setLoading(true);
      api.getOpportunity(id)
        .then((data) => {
          setForm({
            title: data.title || '',
            company: data.company || '',
            location: data.location || '',
            type: data.type || 'INTERNSHIP',
            description: data.description || '',
            deadline: data.deadline ? data.deadline.substring(0, 10) : '',
            minCgpa: data.minCgpa ?? '',
            eligibleBranches: data.eligibleBranches ? data.eligibleBranches.join(', ') : '',
            eligibleYears: data.eligibleYears || [],
            requiredSkills: data.requiredSkills?.map((s) => ({
              skillId: s.skillId,
              requiredLevel: s.requiredLevel,
            })) || [],
          });
        })
        .catch((err) => setServerError(err.message || 'Failed to load opportunity for editing'))
        .finally(() => setLoading(false));
    }
  }, [id, isEdit]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
    if (fieldErrors[name]) {
      setFieldErrors((prev) => {
        const copy = { ...prev };
        delete copy[name];
        return copy;
      });
    }
  };

  const handleYearToggle = (year) => {
    setForm((prev) => {
      const exists = prev.eligibleYears.includes(year);
      const next = exists
        ? prev.eligibleYears.filter((y) => y !== year)
        : [...prev.eligibleYears, year].sort();
      return { ...prev, eligibleYears: next };
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    setServerError(null);
    setFieldErrors({});

    const payload = {
      title: form.title.trim(),
      company: form.company.trim(),
      location: form.location.trim(),
      type: form.type,
      description: form.description.trim(),
      deadline: form.deadline || null,
      minCgpa: form.minCgpa !== '' ? Number(form.minCgpa) : null,
      eligibleBranches: form.eligibleBranches
        ? form.eligibleBranches.split(',').map((s) => s.trim()).filter(Boolean)
        : [],
      eligibleYears: form.eligibleYears,
      requiredSkills: form.requiredSkills,
    };

    try {
      if (isEdit) {
        await api.updateOpportunity(id, payload);
      } else {
        await api.createOpportunity(payload);
      }
      navigate('/recruiter/postings', { replace: true });
    } catch (err) {
      setServerError(err.message || 'Failed to save opportunity');
      if (err.fieldErrors) {
        setFieldErrors(err.fieldErrors);
      }
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) return <p className="muted center" style={{ padding: '3rem' }}>Loading opportunity data...</p>;

  return (
    <div>
      <div className="page-head">
        <div>
          <h1>{isEdit ? 'Edit Opportunity' : 'Post an Opportunity'}</h1>
          <p className="muted">
            {isEdit ? 'Update role details, eligibility criteria, and required skills' : 'Create an internship, full-time job, or hackathon listing for students'}
          </p>
        </div>
        <Link to="/recruiter/postings" className="btn btn-outline">
          Cancel
        </Link>
      </div>

      {serverError && <Alert type="error">{serverError}</Alert>}

      <form onSubmit={handleSubmit} className="panel">
        <div className="form-grid">
          {/* Title */}
          <div className={`field ${fieldErrors.title ? 'has-error' : ''}`}>
            <label htmlFor="opp-title">
              Job / Role Title <span className="req">*</span>
            </label>
            <input
              id="opp-title"
              name="title"
              type="text"
              placeholder="e.g. Backend Software Engineer Intern"
              value={form.title}
              onChange={handleChange}
              required
            />
            {fieldErrors.title && <span className="error-text">{fieldErrors.title}</span>}
          </div>

          {/* Type */}
          <div className={`field ${fieldErrors.type ? 'has-error' : ''}`}>
            <label htmlFor="opp-type">
              Opportunity Type <span className="req">*</span>
            </label>
            <select id="opp-type" name="type" value={form.type} onChange={handleChange} required>
              <option value="INTERNSHIP">Internship</option>
              <option value="JOB">Full-time Job</option>
              <option value="HACKATHON">Hackathon</option>
            </select>
            {fieldErrors.type && <span className="error-text">{fieldErrors.type}</span>}
          </div>

          {/* Company */}
          <div className={`field ${fieldErrors.company ? 'has-error' : ''}`}>
            <label htmlFor="opp-company">
              Company / Organization Name <span className="req">*</span>
            </label>
            <input
              id="opp-company"
              name="company"
              type="text"
              placeholder="e.g. Google India"
              value={form.company}
              onChange={handleChange}
              required
            />
            {fieldErrors.company && <span className="error-text">{fieldErrors.company}</span>}
          </div>

          {/* Location */}
          <div className={`field ${fieldErrors.location ? 'has-error' : ''}`}>
            <label htmlFor="opp-location">
              Location <span className="req">*</span>
            </label>
            <input
              id="opp-location"
              name="location"
              type="text"
              placeholder="e.g. Bangalore, Karnataka (or Remote)"
              value={form.location}
              onChange={handleChange}
              required
            />
            {fieldErrors.location && <span className="error-text">{fieldErrors.location}</span>}
          </div>

          {/* Deadline */}
          <div className={`field ${fieldErrors.deadline ? 'has-error' : ''}`}>
            <label htmlFor="opp-deadline">
              Application Deadline <span className="req">*</span>
            </label>
            <input
              id="opp-deadline"
              name="deadline"
              type="date"
              value={form.deadline}
              onChange={handleChange}
              required
            />
            {fieldErrors.deadline && <span className="error-text">{fieldErrors.deadline}</span>}
          </div>

          {/* Min CGPA */}
          <div className={`field ${fieldErrors.minCgpa ? 'has-error' : ''}`}>
            <label htmlFor="opp-cgpa">Minimum CGPA (0.0 - 10.0)</label>
            <input
              id="opp-cgpa"
              name="minCgpa"
              type="number"
              step="0.1"
              min="0"
              max="10"
              placeholder="e.g. 7.5 (leave blank for none)"
              value={form.minCgpa}
              onChange={handleChange}
            />
            {fieldErrors.minCgpa && <span className="error-text">{fieldErrors.minCgpa}</span>}
          </div>

          {/* Eligible Branches */}
          <div className={`field full ${fieldErrors.eligibleBranches ? 'has-error' : ''}`}>
            <label htmlFor="opp-branches">
              Eligible Branches (comma-separated, leave blank for all)
            </label>
            <input
              id="opp-branches"
              name="eligibleBranches"
              type="text"
              placeholder="e.g. Computer Science, Information Science, Electronics"
              value={form.eligibleBranches}
              onChange={handleChange}
            />
            {fieldErrors.eligibleBranches && <span className="error-text">{fieldErrors.eligibleBranches}</span>}
          </div>

          {/* Eligible Years */}
          <div className="field full">
            <label>Eligible Academic Years</label>
            <div style={{ display: 'flex', gap: '1.25rem', marginTop: '0.25rem', flexWrap: 'wrap' }}>
              {[1, 2, 3, 4].map((year) => (
                <label key={year} style={{ display: 'inline-flex', alignItems: 'center', gap: '0.4rem', fontWeight: 500, cursor: 'pointer' }}>
                  <input
                    type="checkbox"
                    checked={form.eligibleYears.includes(year)}
                    onChange={() => handleYearToggle(year)}
                    style={{ width: 'auto' }}
                  />
                  Year {year}
                </label>
              ))}
            </div>
          </div>

          {/* Description */}
          <div className={`field full ${fieldErrors.description ? 'has-error' : ''}`}>
            <label htmlFor="opp-desc">
              Full Description & Responsibilities <span className="req">*</span>
            </label>
            <textarea
              id="opp-desc"
              name="description"
              rows={6}
              placeholder="Describe the role responsibilities, project scope, requirements, perks, or stipend..."
              value={form.description}
              onChange={handleChange}
              required
            />
            {fieldErrors.description && <span className="error-text">{fieldErrors.description}</span>}
          </div>
        </div>

        {/* Skill Requirements Editor */}
        <SkillRequirementEditor
          value={form.requiredSkills}
          onChange={(newSkills) => setForm((prev) => ({ ...prev, requiredSkills: newSkills }))}
          availableSkills={availableSkills}
        />

        {/* Submit */}
        <div style={{ borderTop: '1px solid var(--line)', marginTop: '1.5rem', paddingTop: '1.25rem', display: 'flex', gap: '0.75rem', justifyContent: 'flex-end' }}>
          <Link to="/recruiter/postings" className="btn btn-outline">
            Cancel
          </Link>
          <button type="submit" className="btn btn-primary" disabled={submitting}>
            {submitting ? 'Saving...' : isEdit ? 'Update Opportunity' : 'Publish Opportunity'}
          </button>
        </div>
      </form>
    </div>
  );
}
