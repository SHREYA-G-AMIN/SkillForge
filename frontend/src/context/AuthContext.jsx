import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { api, clearAuth, getStoredAuth, setUnauthorizedHandler, storeAuth } from '../api/client';
import { DEFAULT_EMOJI } from '../utils/emojis';

const AuthContext = createContext(null);

const emojiKey = (email) => `skillforge_emoji:${email}`;
const readEmoji = (email) => {
  try {
    return (email && localStorage.getItem(emojiKey(email))) || DEFAULT_EMOJI;
  } catch {
    return DEFAULT_EMOJI;
  }
};

export function AuthProvider({ children }) {
  const [auth, setAuth] = useState(() => getStoredAuth()); // { token, email, role } | null
  const [sessionExpired, setSessionExpired] = useState(false);
  const [emoji, setEmojiState] = useState(() => readEmoji(getStoredAuth()?.email));

  // Each user keeps their own avatar emoji (saved in this browser).
  useEffect(() => setEmojiState(readEmoji(auth?.email)), [auth?.email]);

  const setEmoji = useCallback(
    (value) => {
      setEmojiState(value);
      try {
        if (auth?.email) localStorage.setItem(emojiKey(auth.email), value);
      } catch {
        /* storage unavailable: keep it for this session only */
      }
    },
    [auth?.email]
  );

  const persist = (res) => {
    const value = { token: res.token, email: res.email, role: res.role };
    storeAuth(value);
    setAuth(value);
    setSessionExpired(false);
  };

  const login = useCallback(async (credentials) => persist(await api.login(credentials)), []);
  const register = useCallback(async (details) => persist(await api.register(details)), []);

  const logout = useCallback(async () => {
    try {
      await api.logout(); // revokes the token on the server
    } catch {
      /* even if the call fails, always sign out locally */
    }
    clearAuth();
    setAuth(null);
  }, []);

  // Any 401 on an authenticated request (expired / revoked token) signs the user out.
  useEffect(() => {
    setUnauthorizedHandler(() => {
      clearAuth();
      setAuth(null);
      setSessionExpired(true);
    });
  }, []);

  const value = useMemo(
    () => ({ auth, isAuthenticated: !!auth, sessionExpired, emoji, setEmoji, login, register, logout }),
    [auth, sessionExpired, emoji, setEmoji, login, register, logout]
  );
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export const useAuth = () => useContext(AuthContext);