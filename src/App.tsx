import { HashRouter, Routes, Route, Navigate } from 'react-router-dom';
import { Toaster } from '@/components/ui/sonner';

import AuthLayout from '@/components/layout/AuthLayout';
import MainLayout from '@/components/layout/MainLayout';
import ProtectedRoute from '@/components/layout/ProtectedRoute';

import LoginPage from '@/pages/auth/login/page';
import RegisterPage from '@/pages/auth/register/page';
import ForgotPasswordPage from '@/pages/auth/forgot-password/page';
import RecommendationsPage from '@/pages/recommendations/page';
import ClosetPage from '@/pages/closet/page';
import FeedsPage from '@/pages/feeds/page';
import ProfilePage from '@/pages/profile/page';
import UserManagementPage from '@/pages/admin/users/page';
import ClothesAttributeManagementPage from '@/pages/admin/clothes-attributes/page';
import MyProfileSettingsPage from '@/pages/settings/page';
import NotFoundPage from '@/pages/404/page';
import CsrfInitializer from "@/components/auth/CsrfInitializer.tsx";

const basename = import.meta.env.VITE_PUBLIC_PATH || '/';

function App() {


  return (
    <HashRouter basename={basename}>
      <CsrfInitializer/>
      <Routes>
        {/* Public Routes */}
        <Route path="/auth/*" element={<AuthLayout />}>
          <Route path="login" element={<LoginPage />} />
          <Route path="register" element={<RegisterPage />} />
          <Route path="forgot-password" element={<ForgotPasswordPage />} />
        </Route>
        
        {/* Protected Routes */}
        <Route path="/" element={<ProtectedRoute><MainLayout /></ProtectedRoute>}>
          <Route index element={<Navigate to="/recommendations" replace />} />
          <Route path="recommendations" element={<RecommendationsPage />} />
          <Route path="closet" element={<ClosetPage />} />
          <Route path="feeds" element={<FeedsPage />} />
          <Route path="profiles" element={<ProfilePage />} />
          <Route path="admin/users" element={<UserManagementPage />} />
          <Route path="admin/clothes-attributes" element={<ClothesAttributeManagementPage />} />
          <Route path="settings" element={<MyProfileSettingsPage />} />
        </Route>
        
        <Route path="/404" element={<NotFoundPage />} />
        <Route path="*" element={<Navigate to="/404" replace />} />
      </Routes>
      <Toaster />
    </HashRouter>
  )
}

export default App
