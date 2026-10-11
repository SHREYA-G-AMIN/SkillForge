// Client-side validation mirrors the backend rules (the backend remains the source of truth).
const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const URL_RE = /^https?:\/\/\S+$/i;

export const isValidUrl = (v) => URL_RE.test(v);

export function validateLogin({ email, password }) {
  const e = {};
  if (!email.trim()) e.email = 'Email is required';
  else if (!EMAIL_RE.test(email.trim())) e.email = 'Enter a valid email address';
  if (!password) e.password = 'Password is required';
    if (!password) e.password = 'Password is required';
  else if (password.length < 8) e.password = 'Password must be at least 8 characters';
  return e;
}

export function validateRegister({ name, usn, email, password, confirmPassword }) {
  const e = {};
  if (!name.trim()) e.name = 'Name is required';
  if (!usn.trim()) e.usn = 'USN is required';
  if (!email.trim()) e.email = 'Email is required';
  else if (!EMAIL_RE.test(email.trim())) e.email = 'Enter a valid email address';
  if (!password) e.password = 'Password is required';
  else if (password.length < 8) e.password = 'Password must be at least 8 characters';
  else if (password.length > 72) e.password = 'Password must be at most 72 characters';
  if (confirmPassword !== password) e.confirmPassword = 'Passwords do not match';
  return e;
}

export function validateProfile(f) {
  const e = {};
  if (!f.name.trim()) e.name = 'Name is required';
  if (!f.usn.trim()) e.usn = 'USN is required';
  if (f.phone.trim() && !/^[0-9]{10}$/.test(f.phone.trim())) e.phone = 'Phone must be 10 digits';
  if (f.cgpa !== '') {
    const n = Number(f.cgpa);
    if (Number.isNaN(n) || n < 0 || n > 10) e.cgpa = 'CGPA must be between 0 and 10';
  }
  if (f.careerInterests.length > 500) e.careerInterests = 'Maximum 500 characters';
  if (f.resumeUrl.trim() && !isValidUrl(f.resumeUrl.trim())) e.resumeUrl = 'Enter a valid link starting with http:// or https://';
  return e;
}
