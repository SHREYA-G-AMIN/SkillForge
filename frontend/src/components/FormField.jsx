/** Label + input/textarea/select + inline error. `as` = 'input' (default) | 'textarea' | 'select'. */
export default function FormField({ label, name, error, hint, as = 'input', children, className = '', ...props }) {
  const Control = as;
  const id = `field-${name}`;
  return (
    <div className={`field ${error ? 'has-error' : ''} ${className}`}>
      <label htmlFor={id}>
        {label}
        {props.required && <span className="req"> *</span>}
      </label>
      <Control id={id} name={name} aria-invalid={!!error} aria-describedby={error ? `${id}-err` : undefined} {...props}>
        {children}
      </Control>
      {hint && !error && <small className="muted">{hint}</small>}
      {error && (
        <small id={`${id}-err`} className="error-text">
          {error}
        </small>
      )}
    </div>
  );
}
