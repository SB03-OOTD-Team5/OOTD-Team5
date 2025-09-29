import {useNavigate} from 'react-router-dom';
import { useAuthStore } from '@/lib/stores/useAuthStore';
import {useEffect} from "react";

interface ProtectedRouteProps {
  children: React.ReactNode;
}

export default function ProtectedRoute({ children }: ProtectedRouteProps) {
  const {isAuthenticated, fetch} = useAuthStore();
  const navigate = useNavigate();

  useEffect(() => {
    if (!isAuthenticated()) {
      fetch({throwError: true}).catch(err => {
        console.error(err);
        navigate('/auth/login', { replace: true })
      } );
    }
  }, [isAuthenticated, fetch, navigate]);

  if (!isAuthenticated()) {
    return null; // or a loading spinner, or redirect to login
  }
  return <>{children}</>;
}