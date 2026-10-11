/** How filled-in a profile is, plus the single most useful next step. (Name and USN are always set at sign-up.) */
export function completeness(p) {
  const checks = [
    [!!p.phone, 'add your phone number'],
    [!!p.college, 'add your college'],
    [!!p.degree, 'add your degree'],
    [!!p.branch, 'add your branch'],
    [!!p.semester, 'pick your semester'],
    [p.cgpa !== null && p.cgpa !== undefined, 'add your CGPA'],
    [!!p.careerGoal, 'set a career goal'],
    [!!p.careerInterests, 'describe your career interests'],
    [!!p.resumeUrl, 'link your resume'],
    [p.skills.length > 0, 'add your first skill'],
    [p.projects.length > 0, 'add a project'],
    [p.certifications.length > 0, 'add a certification'],
    [p.experiences.length > 0, 'add your experience'],
  ];
  const done = checks.filter(([ok]) => ok).length;
  const next = checks.find(([ok]) => !ok);
  return { percent: Math.round((done / checks.length) * 100), next: next ? next[1] : null };
}