import WeatherIconsHeader from '@/components/auth/WeatherIconsHeader';
import LoginForm from '@/components/auth/LoginForm';
import SocialLoginSection from '@/components/auth/SocialLoginSection';
import AuthFooterLinks from '@/components/auth/AuthFooterLinks';

export default function LoginPage() {
  return (
    <div className="flex flex-col items-center">
      {/* Weather Icons Header */}
      <WeatherIconsHeader />
      
      {/* Login Card */}
      <div className="bg-white rounded-[30px] shadow-[0px_0px_30px_0px_rgba(130,188,255,0.2)] p-[60px] w-full relative z-[1] mb-[-40px]">
        <div className="flex flex-col gap-[30px] items-center">
          {/* Login Form */}
          <LoginForm />
          
          {/* Social Login Section */}
          <SocialLoginSection />
          
          {/* Footer Links */}
          <AuthFooterLinks />
        </div>
      </div>
    </div>
  );
}