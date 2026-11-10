import { Navigate, Outlet, Route, Routes } from 'react-router-dom';
import Layout from './components/Layout';
import { useAuth } from './context/AuthContext';
import Login from './pages/Login';
import Profile from './pages/Profile';
import ProfileEdit from './pages/ProfileEdit';
import Register from './pages/Register';

// Person 3 pages
import Opportunities from './pages/Opportunities';
import OpportunityDetails from './pages/OpportunityDetails';
import MyApplications from './pages/MyApplications';
import SavedOpportunities from './pages/SavedOpportunities';
import MyPostings from './pages/MyPostings';
import PostOpportunity from './pages/PostOpportunity';
import Applicants from './pages/Applicants';

/** Login/Register are only for logged-out users. */
function PublicOnly() {
  const { isAuthenticated, auth } = useAuth();
  if (isAuthenticated) {
    return <Navigate to={auth?.role === 'RECRUITER' ? '/recruiter/postings' : '/opportunities'} replace />;
  }
  return <Outlet />;
}

/** Route guard that ensures the user has a specific role. */
function RequireRole({ role }) {
  const { auth } = useAuth();
  if (auth?.role !== role && auth?.role !== 'ADMIN') {
    return <Navigate to="/profile" replace />;
  }
  return <Outlet />;
}

export default function App() {
  const { isAuthenticated, auth } = useAuth();

  return (
    <Routes>
      <Route element={<PublicOnly />}>
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
      </Route>

      <Route element={<Layout />}>
        {/* Common Authenticated Routes */}
        <Route path="/profile" element={<Profile />} />
        <Route path="/profile/edit" element={<ProfileEdit />} />

        {/* Student Routes */}
        <Route element={<RequireRole role="STUDENT" />}>
          <Route path="/opportunities" element={<Opportunities />} />
          <Route path="/opportunities/:id" element={<OpportunityDetails />} />
          <Route path="/applications" element={<MyApplications />} />
          <Route path="/saved" element={<SavedOpportunities />} />
        </Route>

        {/* Recruiter Routes */}
        <Route element={<RequireRole role="RECRUITER" />}>
          <Route path="/recruiter/postings" element={<MyPostings />} />
          <Route path="/recruiter/postings/new" element={<PostOpportunity />} />
          <Route path="/recruiter/postings/:id/edit" element={<PostOpportunity />} />
          <Route path="/recruiter/postings/:id/applicants" element={<Applicants />} />
        </Route>
      </Route>

      <Route
        path="*"
        element={
          <Navigate
            to={
              !isAuthenticated
                ? '/login'
                : auth?.role === 'RECRUITER'
                ? '/recruiter/postings'
                : '/opportunities'
            }
            replace
          />
        }
      />
    </Routes>
  );
}
