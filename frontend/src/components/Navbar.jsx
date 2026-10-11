import { useEffect, useRef, useState } from 'react';
import { Link, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { AVATAR_EMOJIS } from '../utils/emojis';
import Avatar from './Avatar';

export default function Navbar() {
  const { auth, emoji, setEmoji, logout } = useAuth();
  const navigate = useNavigate();
  const [open, setOpen] = useState(false);
  const menuRef = useRef(null);

  useEffect(() => {
    if (!open) return undefined;
    const onClick = (e) => menuRef.current && !menuRef.current.contains(e.target) && setOpen(false);
    const onKey = (e) => e.key === 'Escape' && setOpen(false);
    document.addEventListener('mousedown', onClick);
    document.addEventListener('keydown', onKey);
    return () => {
      document.removeEventListener('mousedown', onClick);
      document.removeEventListener('keydown', onKey);
    };
  }, [open]);

  const handleLogout = async () => {
    setOpen(false);
    await logout();
    navigate('/login', { replace: true });
  };

  return (
    <header className="navbar">
      <div className="navbar-inner">
        <Link to="/profile" className="brand">Skill<span>Forge</span></Link>

        <nav className="nav-links" aria-label="Main">
          <NavLink to="/profile" end>My profile</NavLink>
          <NavLink to="/profile/edit">Edit profile</NavLink>
        </nav>

        <div className="user-menu" ref={menuRef}>
          <button
            className="avatar-btn"
            aria-haspopup="true"
            aria-expanded={open}
            aria-label="Account menu"
            onClick={() => setOpen((v) => !v)}
          >
            <Avatar />
            <span className="nav-email">{auth?.email}</span>
          </button>

          {open && (
            <div className="menu">
              <p className="menu-email">{auth?.email}</p>
              <p className="menu-title">Choose your emoji</p>
              <div className="emoji-grid">
                {AVATAR_EMOJIS.map((e) => (
                  <button key={e} className="emoji-opt" aria-pressed={emoji === e} aria-label={`Use ${e}`} onClick={() => setEmoji(e)}>
                    {e}
                  </button>
                ))}
              </div>
              <button className="menu-logout" onClick={handleLogout}>Log out</button>
            </div>
          )}
        </div>
      </div>
    </header>
  );
}