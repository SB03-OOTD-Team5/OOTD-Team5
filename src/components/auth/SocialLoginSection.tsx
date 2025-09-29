import {SocialButton} from "@/components/ui/social-button";
import GoogleIcon from "@/assets/icons/ic_google.svg";
import KakaoIcon from "@/assets/icons/ic_kakao.svg";
import type {OAuthProvider} from "@/lib/api";
const basename = import.meta.env.VITE_PUBLIC_PATH || '';
export default function SocialLoginSection() {
  const handleLogin = (provider: OAuthProvider) => {

    window.location.href = `${window.location.origin}${basename}/oauth2/authorization/${provider}`
  }
  const handleGoogleLogin = () => {
    handleLogin('google');
  };

  const handleKakaoLogin = () => {
    handleLogin('kakao');
  };

  return (
    <div className="flex flex-col gap-4 w-full">
      <SocialButton
        icon={GoogleIcon}
        iconAlt="Google"
        label="구글로 로그인하기"
        onClick={handleGoogleLogin}
      />
      
      <SocialButton
        icon={KakaoIcon}
        iconAlt="Kakao"
        label="카카오 로그인하기"
        onClick={handleKakaoLogin}
      />
    </div>
  );
}