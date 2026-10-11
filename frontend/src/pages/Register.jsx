import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Alert from '../components/Alert';
import AuthShell from '../components/AuthShell';
import FormField from '../components/FormField';
import { useAuth } from '../context/AuthContext';
import { validateRegister } from '../utils/validators';

export default function Register() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ name: '', usn: '', email: '', password: '', confirmPassword: '' });
  const [errors, setErrors] = useState({});
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const onChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
    if (errors[e.target.name]) setErrors({ ...errors, [e.target.name]: '' });
  };

  const onSubmit = async (e) => {
    e.preventDefault();
    setError('');
    const v = validateRegister(form);
    setErrors(v);
    if (Object.keys(v).length) return;

    setLoading(true);
    try {
      await register({
        name: form.name.trim(),
        usn: form.usn.trim(),
        email: form.email.trim(),
        password: form.password,
      });
      navigate('/profile/edit', { replace: true }); // let the student complete their profile
    } catch (err) {
      setError(err.message);
      setErrors(err.fieldErrors || {});
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthShell>
      <form onSubmit={onSubmit} noValidate>
        <h2 className="auth-title">Create your account</h2>
        <p className="muted">It takes a minute. You can fill in the rest of your profile afterwards.</p>

        <Alert>{error}</Alert>

        <FormField label="Full name" name="name" autoComplete="name" value={form.name} onChange={onChange} error={errors.name} required />
        <FormField label="USN" name="usn" value={form.usn} onChange={onChange} error={errors.usn} required />
        <FormField label="Email" name="email" type="email" autoComplete="email" value={form.email} onChange={onChange} error={errors.email} required />
        <FormField label="Password" name="password" type="password" autoComplete="new-password" value={form.password} onChange={onChange} error={errors.password} hint="At least 8 characters" required />
        <FormField label="Confirm password" name="confirmPassword" type="password" autoComplete="new-password" value={form.confirmPassword} onChange={onChange} error={errors.confirmPassword} required />

        <button className="btn btn-primary btn-block" disabled={loading}>{loading ? 'Creating account...' : 'Create account'}</button>
        <p className="muted center">
          Already registered? <Link to="/login">Log in</Link>
        </p>
      </form>
    </AuthShell>
  );
}