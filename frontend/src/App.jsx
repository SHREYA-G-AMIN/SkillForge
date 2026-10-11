import { Navigate, Outlet, Route, Routes } from 'react-router-dom';
import Layout from './components/Layout';
import { useAuth } from './context/AuthContext';
import Login from './pages/Login';
import Profile from './pages/Profile';
import ProfileEdit from './pages/ProfileEdit';
import Register from './pages/Register';

/** Login/Register are only for logged-out users. */
function PublicOnly() {
  const { isAuthenticated } = useAuth();
  return isAuthenticated ? <Navigate to="/profile" replace /> : <Outlet />;
}

export default function App() {
  const { isAuthenticated } = useAuth();
  return (
    <Routes>
      <Route element={<PublicOnly />}>
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
      </Route>

      <Route element={<Layout />}>
        <Route path="/profile" element={<Profile />} />
        <Route path="/profile/edit" element={<ProfileEdit />} />
      </Route>

      <Route path="*" element={<Navigate to={isAuthenticated ? '/profile' : '/login'} replace />} />
    </Routes>
  );
}
