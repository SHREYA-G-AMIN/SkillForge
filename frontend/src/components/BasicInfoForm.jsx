import { useState } from 'react';
import { api } from '../api/client';
import { validateProfile } from '../utils/validators';
import Alert from './Alert';
import FormField from './FormField';

const toForm = (p) => ({
  name: p.name ?? '',
  usn: p.usn ?? '',
  phone: p.phone ?? '',
  college: p.college ?? '',
  degree: p.degree ?? '',
  branch: p.branch ?? '',
  semester: p.semester ?? '',
  cgpa: p.cgpa ?? '',
  careerGoal: p.careerGoal ?? '',
  careerInterests: p.careerInterests ?? '',
  resumeUrl: p.resumeUrl ?? '',
});

export default function BasicInfoForm({ profile, onSaved }) {
  const [form, setForm] = useState(() => toForm(profile));
  const [errors, setErrors] = useState({});
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [saving, setSaving] = useState(false);

  const onChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
    if (errors[e.target.name]) setErrors({ ...errors, [e.target.name]: '' });
    setSuccess('');
  };

  const onSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    const v = validateProfile(form);
    setErrors(v);
    if (Object.keys(v).length) return;

    setSaving(true);
    try {
      const saved = await api.updateProfile({
        name: form.name.trim(),
        usn: form.usn.trim(),
        phone: form.phone.trim(),
        college: form.college.trim(),
        degree: form.degree.trim(),
        branch: form.branch.trim(),
        semester: form.semester === '' ? null : Number(form.semester),
        cgpa: form.cgpa === '' ? null : Number(form.cgpa),
        careerGoal: form.careerGoal.trim(),
        careerInterests: form.careerInterests.trim(),
        resumeUrl: form.resumeUrl.trim(),
      });
      onSaved(saved);
      setSuccess('Profile saved.');
    } catch (err) {
      setError(err.message);
      setErrors(err.fieldErrors || {});
    } finally {
      setSaving(false);
    }
  };

  return (
    <form onSubmit={onSubmit} noValidate className="card">
      <h2>Basic information</h2>
      <Alert>{error}</Alert>
      <Alert type="success">{success}</Alert>

      <div className="form-grid">
        <FormField label="Full name" name="name" required value={form.name} onChange={onChange} error={errors.name} maxLength={100} />
        <FormField label="USN" name="usn" required value={form.usn} onChange={onChange} error={errors.usn} maxLength={30} />
        <FormField label="Email" name="email" value={profile.email} disabled readOnly hint="Your login email cannot be changed here." />
        <FormField label="Phone" name="phone" type="tel" inputMode="numeric" value={form.phone} onChange={onChange} error={errors.phone} hint="10 digits" />
        <FormField label="College" name="college" value={form.college} onChange={onChange} error={errors.college} maxLength={150} />
        <FormField label="Degree" name="degree" value={form.degree} onChange={onChange} error={errors.degree} placeholder="e.g. B.E." maxLength={100} />
        <FormField label="Branch" name="branch" value={form.branch} onChange={onChange} error={errors.branch} placeholder="e.g. Computer Science" maxLength={100} />
        <FormField label="Semester" name="semester" as="select" value={form.semester} onChange={onChange} error={errors.semester}>
          <option value="">Select</option>
          {[1, 2, 3, 4, 5, 6, 7, 8].map((n) => (
            <option key={n} value={n}>{n}</option>
          ))}
        </FormField>
        <FormField label="CGPA" name="cgpa" type="number" step="0.01" min="0" max="10" value={form.cgpa} onChange={onChange} error={errors.cgpa} />
        <FormField label="Career goal" name="careerGoal" value={form.careerGoal} onChange={onChange} error={errors.careerGoal} placeholder="e.g. Backend Developer" maxLength={100} />
      </div>

      <FormField label="Career interests" name="careerInterests" as="textarea" rows={3} value={form.careerInterests} onChange={onChange} error={errors.careerInterests} maxLength={500} hint="e.g. Cloud, AI, Web development" />
      <FormField label="Resume URL" name="resumeUrl" type="url" value={form.resumeUrl} onChange={onChange} error={errors.resumeUrl} placeholder="https://drive.google.com/..." hint="Link to your resume (Drive, GitHub, portfolio)" />

      <button className="btn btn-primary" disabled={saving}>{saving ? 'Saving...' : 'Save changes'}</button>
    </form>
  );
}
