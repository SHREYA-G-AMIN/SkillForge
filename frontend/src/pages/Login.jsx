import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Alert from '../components/Alert';
import AuthShell from '../components/AuthShell';
import FormField from '../components/FormField';
import { useAuth } from '../context/AuthContext';
import { validateLogin } from '../utils/validators';

export default function Login() {
  const { login, sessionExpired } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ email: '', password: '' });
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
    const v = validateLogin(form);
    setErrors(v);
    if (Object.keys(v).length) return;

    setLoading(true);
    try {
      await login({ email: form.email.trim(), password: form.password });
      navigate('/profile', { replace: true });
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
        <h2 className="auth-title">Welcome back 👋</h2>
        <p className="muted">Log in to see and update your profile.</p>

        {sessionExpired && <Alert type="info">Your session has ended. Please log in again.</Alert>}
        <Alert>{error}</Alert>

        <FormField label="Email" name="email" type="email" autoComplete="email" value={form.email} onChange={onChange} error={errors.email} required />
        <FormField label="Password" name="password" type="password" autoComplete="current-password" value={form.password} onChange={onChange} error={errors.password} required />

        <button className="btn btn-primary btn-block" disabled={loading}>{loading ? 'Logging in...' : 'Log in'}</button>
        <p className="muted center">
          New here? <Link to="/register">Create an account</Link>
        </p>
      </form>
    </AuthShell>
  );
}
