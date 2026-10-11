// Single place that talks to the Spring Boot backend.
const BASE_URL = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080').replace(/\/$/, '');
const STORAGE_KEY = 'skillforge_auth';

export class ApiError extends Error {
  constructor(status, message, fieldErrors) {
    super(message);
    this.status = status;
    this.fieldErrors = fieldErrors || {};
  }
}

/* ---- token storage (localStorage: simple, but readable by any script on the page, so keep the app XSS-free) ---- */
export const getStoredAuth = () => {
  try {
    return JSON.parse(localStorage.getItem(STORAGE_KEY));
  } catch {
    return null;
  }
};
export const storeAuth = (auth) => localStorage.setItem(STORAGE_KEY, JSON.stringify(auth));
export const clearAuth = () => localStorage.removeItem(STORAGE_KEY);

let onUnauthorized = () => {};
export const setUnauthorizedHandler = (fn) => {
  onUnauthorized = fn;
};

async function request(path, { method = 'GET', body, auth = true } = {}) {
  const headers = {};
  if (body !== undefined) headers['Content-Type'] = 'application/json';
  const stored = getStoredAuth();
  const sentToken = auth && stored?.token;
  if (sentToken) headers.Authorization = `Bearer ${stored.token}`;

  let res;
  try {
    res = await fetch(`${BASE_URL}${path}`, {
      method,
      headers,
      body: body !== undefined ? JSON.stringify(body) : undefined,
    });
  } catch {
    throw new ApiError(0, 'Cannot reach the server. Please check that the backend is running.');
  }

  const text = await res.text();
  let data = null;
  if (text) {
    try {
      data = JSON.parse(text);
    } catch {
      /* non-JSON body */
    }
  }

  if (!res.ok) {
    // A 401 on a request that carried a token means the session expired or was revoked.
    if (res.status === 401 && sentToken) onUnauthorized();
    const fallback = res.status === 401 ? 'Please log in to continue.' : 'Something went wrong. Please try again.';
    throw new ApiError(res.status, data?.message || fallback, data?.errors);
  }
  return data;
}

export const api = {
  // auth
  register: (payload) => request('/api/auth/register', { method: 'POST', body: payload, auth: false }),
  login: (payload) => request('/api/auth/login', { method: 'POST', body: payload, auth: false }),
  logout: () => request('/api/auth/logout', { method: 'POST' }),

  // profile
  getProfile: () => request('/api/profile/me'),
  updateProfile: (payload) => request('/api/profile/me', { method: 'PUT', body: payload }),

  // master skills + the student's skills
  listSkills: () => request('/api/skills'),
  addSkill: (payload) => request('/api/profile/skills', { method: 'POST', body: payload }),
  updateSkill: (id, payload) => request(`/api/profile/skills/${id}`, { method: 'PUT', body: payload }),
  deleteSkill: (id) => request(`/api/profile/skills/${id}`, { method: 'DELETE' }),

  // projects | certifications | experience share the same shape
  createItem: (resource, payload) => request(`/api/profile/${resource}`, { method: 'POST', body: payload }),
  updateItem: (resource, id, payload) => request(`/api/profile/${resource}/${id}`, { method: 'PUT', body: payload }),
  deleteItem: (resource, id) => request(`/api/profile/${resource}/${id}`, { method: 'DELETE' }),
};
