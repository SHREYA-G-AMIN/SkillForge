import { useState } from 'react';
import { api } from '../api/client';
import { isValidUrl } from '../utils/validators';
import Alert from './Alert';
import FormField from './FormField';

/**
 * Generic add / edit / delete list used for projects, certifications and experience.
 * fields: [{ name, label, type: 'text'|'textarea'|'date'|'url', required, maxLength }]
 */
export default function EntityEditor({ resource, noun, items, fields, renderItem, onChanged }) {
  const blank = () => Object.fromEntries(fields.map((f) => [f.name, '']));
  const [form, setForm] = useState(blank);
  const [editingId, setEditingId] = useState(null);
  const [errors, setErrors] = useState({});
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const reset = () => {
    setForm(blank());
    setEditingId(null);
    setErrors({});
    setError('');
  };

  const startEdit = (item) => {
    setEditingId(item.id);
    setForm(Object.fromEntries(fields.map((f) => [f.name, item[f.name] ?? ''])));
    setErrors({});
    setError('');
  };

  const setValue = (name, value) => {
    setForm((prev) => ({ ...prev, [name]: value }));
    if (errors[name]) setErrors((prev) => ({ ...prev, [name]: '' }));
  };

  const validate = () => {
    const e = {};
    fields.forEach((f) => {
      const v = String(form[f.name] ?? '').trim();
      if (f.required && !v) e[f.name] = `${f.label} is required`;
      else if (f.maxLength && v.length > f.maxLength) e[f.name] = `Maximum ${f.maxLength} characters`;
      else if (f.type === 'url' && v && !isValidUrl(v)) e[f.name] = 'Enter a valid link starting with http:// or https://';
    });
    return e;
  };

  const submit = async (e) => {
    e.preventDefault();
    const v = validate();
    setErrors(v);
    if (Object.keys(v).length) return;

    const payload = Object.fromEntries(
      fields.map((f) => {
        const val = String(form[f.name] ?? '').trim();
        return [f.name, val === '' ? null : val];
      })
    );
    setBusy(true);
    setError('');
    try {
      if (editingId) await api.updateItem(resource, editingId, payload);
      else await api.createItem(resource, payload);
      await onChanged();
      reset();
    } catch (err) {
      setError(err.message);
      setErrors(err.fieldErrors || {});
    } finally {
      setBusy(false);
    }
  };

  const remove = async (item) => {
    if (!window.confirm(`Delete this ${noun}?`)) return;
    setBusy(true);
    setError('');
    try {
      await api.deleteItem(resource, item.id);
      if (editingId === item.id) reset();
      await onChanged();
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="card">
      <h2>{noun.charAt(0).toUpperCase() + noun.slice(1)}s</h2>
      <Alert>{error}</Alert>

      {items.length === 0 ? (
        <p className="muted">No {noun}s added yet.</p>
      ) : (
        <ul className="item-list">
          {items.map((item) => (
            <li key={item.id} className="item-row">
              <div>{renderItem(item)}</div>
              <div className="row-actions">
                <button className="btn btn-outline" disabled={busy} onClick={() => startEdit(item)}>Edit</button>
                <button className="btn btn-danger" disabled={busy} onClick={() => remove(item)}>Delete</button>
              </div>
            </li>
          ))}
        </ul>
      )}

      <form onSubmit={submit} noValidate className="sub-form">
        <h3>{editingId ? `Edit ${noun}` : `Add ${noun}`}</h3>
        <div className="form-grid">
          {fields.map((f) => (
            <FormField
              key={f.name}
              label={f.label}
              name={`${resource}-${f.name}`}
              as={f.type === 'textarea' ? 'textarea' : 'input'}
              type={f.type === 'textarea' ? undefined : f.type || 'text'}
              rows={f.type === 'textarea' ? 3 : undefined}
              required={f.required}
              maxLength={f.maxLength}
              value={form[f.name]}
              onChange={(e) => setValue(f.name, e.target.value)}
              error={errors[f.name]}
              className={f.type === 'textarea' ? 'full' : ''}
            />
          ))}
        </div>
        <div className="row-actions">
          <button className="btn btn-primary" disabled={busy}>{editingId ? 'Update' : 'Add'}</button>
          {editingId && <button type="button" className="btn btn-outline" onClick={reset}>Cancel</button>}
        </div>
      </form>
    </div>
  );
}
