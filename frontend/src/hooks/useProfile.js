import { useCallback, useEffect, useState } from 'react';
import { api } from '../api/client';

/** Loads the logged-in student's profile. `reload()` refreshes silently (no loading flash). */
export function useProfile() {
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const reload = useCallback(async () => {
    try {
      setProfile(await api.getProfile());
      setError('');
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    reload();
  }, [reload]);

  return { profile, setProfile, loading, error, reload };
}
