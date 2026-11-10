export default function SkillRequirementEditor({ value = [], onChange, availableSkills = [] }) {
  const addRequirement = () => {
    // Pick the first skill that isn't already selected
    const selectedIds = new Set(value.map((v) => Number(v.skillId)));
    const unselected = availableSkills.find((s) => !selectedIds.has(s.id));
    const nextSkillId = unselected ? unselected.id : availableSkills[0]?.id;
    if (nextSkillId) {
      onChange([...value, { skillId: nextSkillId, requiredLevel: 3 }]);
    }
  };

  const updateRequirement = (index, field, val) => {
    const updated = value.map((item, idx) => {
      if (idx === index) {
        return { ...item, [field]: val };
      }
      return item;
    });
    onChange(updated);
  };

  const removeRequirement = (index) => {
    onChange(value.filter((_, idx) => idx !== index));
  };

  return (
    <div className="sub-form" style={{ marginTop: '1rem', paddingTop: '1rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.75rem' }}>
        <h3 style={{ margin: 0, fontSize: '1rem' }}>Required Skills & Proficiency</h3>
        <button
          type="button"
          className="btn btn-outline"
          onClick={addRequirement}
          style={{ padding: '0.3rem 0.75rem', fontSize: '0.85rem' }}
        >
          + Add skill
        </button>
      </div>

      {value.length === 0 ? (
        <p className="empty" style={{ fontSize: '0.9rem', fontStyle: 'italic' }}>
          No specific skills required. Click "+ Add skill" to specify competencies.
        </p>
      ) : (
        <ul className="item-list" style={{ padding: 0, margin: 0 }}>
          {value.map((req, idx) => (
            <li key={idx} className="item-row" style={{ display: 'flex', gap: '0.75rem', alignItems: 'center', flexWrap: 'wrap' }}>
              <div style={{ flex: '1 1 200px' }}>
                <label htmlFor={`skill-select-${idx}`} className="sr-only" style={{ display: 'none' }}>
                  Select Skill
                </label>
                <select
                  id={`skill-select-${idx}`}
                  value={req.skillId}
                  onChange={(e) => updateRequirement(idx, 'skillId', Number(e.target.value))}
                  style={{ width: '100%' }}
                >
                  {availableSkills.map((s) => (
                    <option key={s.id} value={s.id}>
                      {s.name} {s.category ? `(${s.category})` : ''}
                    </option>
                  ))}
                </select>
              </div>

              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <label htmlFor={`level-select-${idx}`} style={{ fontSize: '0.85rem', fontWeight: 600 }}>
                  Min Level:
                </label>
                <select
                  id={`level-select-${idx}`}
                  value={req.requiredLevel}
                  onChange={(e) => updateRequirement(idx, 'requiredLevel', Number(e.target.value))}
                  style={{ width: 'auto' }}
                >
                  <option value={1}>Level 1 (Beginner)</option>
                  <option value={2}>Level 2 (Basic)</option>
                  <option value={3}>Level 3 (Intermediate)</option>
                  <option value={4}>Level 4 (Proficient)</option>
                  <option value={5}>Level 5 (Advanced)</option>
                </select>
              </div>

              <div className="pips" title={`Level ${req.requiredLevel} of 5`}>
                {[1, 2, 3, 4, 5].map((lvl) => (
                  <i key={lvl} className={lvl <= req.requiredLevel ? 'on' : ''} />
                ))}
              </div>

              <button
                type="button"
                className="btn btn-danger"
                onClick={() => removeRequirement(idx)}
                style={{ padding: '0.35rem 0.65rem', fontSize: '0.85rem' }}
                aria-label="Remove skill requirement"
              >
                ✕
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
