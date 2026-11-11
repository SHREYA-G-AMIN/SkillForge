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

  const isRecruiter = auth?.role === 'RECRUITER';

  return (
    <header className="navbar">
      <div className="navbar-inner">
        <Link to={isRecruiter ? '/recruiter/postings' : '/opportunities'} className="brand">
          Skill<span>Forge</span>
        </Link>

        <nav className="nav-links" aria-label="Main">
          {isRecruiter ? (
            <>
              <NavLink to="/recruiter/postings" end>My Postings</NavLink>
              <NavLink to="/recruiter/postings/new">+ Post Opportunity</NavLink>
            </>
          ) : (
            <>
              <NavLink to="/opportunities">Opportunities</NavLink>
              <NavLink to="/applications">My Applications</NavLink>
              <NavLink to="/saved">Saved</NavLink>
              <NavLink to="/profile" end>My Profile</NavLink>
              <NavLink to="/profile/edit">Edit Profile</NavLink>
            </>
          )}
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
              <p className="menu-email">
                <strong>{auth?.email}</strong>
                <span style={{ display: 'block', fontSize: '0.75rem', color: 'var(--primary)', fontWeight: 700 }}>
                  Role: {auth?.role}
                </span>
              </p>
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
