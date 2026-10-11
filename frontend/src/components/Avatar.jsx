import { useAuth } from '../context/AuthContext';

/** The user's chosen emoji in a round badge. size: 'sm' | 'xl' */
export default function Avatar({ size = 'sm' }) {
  const { emoji } = useAuth();
  return (
    <span className={`avatar avatar-${size}`} aria-hidden="true">
      {emoji}
    </span>
  );
}