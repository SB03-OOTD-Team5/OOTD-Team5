import { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button";
import {useAuthStore} from "@/lib/stores/useAuthStore.ts";

export default function PasswordChangeSuccess() {
  const navigate = useNavigate();
  const {clear, clearError} = useAuthStore();

  useEffect(() => {
    clear();
    clearError();
  }, [clear, clearError]);

  useEffect(() => {
    // 5초 후 자동으로 로그인 페이지로 이동
    const timer = setTimeout(() => {
      navigate("/auth/login");
    }, 5000);

    return () => clearTimeout(timer);
  }, [navigate]);

  const handleGoToLogin = () => {
    navigate("/auth/login");
  };

  return (
    <div className="flex flex-col gap-[30px] w-full items-center">
      {/* 성공 아이콘 */}
      <div className="flex flex-col gap-6 items-center">
        <div className="w-20 h-20 bg-green-100 rounded-full flex items-center justify-center">
          <svg 
            width="40" 
            height="40" 
            viewBox="0 0 24 24" 
            fill="none" 
            className="text-green-600"
          >
            <path 
              d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" 
              stroke="currentColor" 
              strokeWidth="2" 
              strokeLinecap="round" 
              strokeLinejoin="round"
            />
          </svg>
        </div>
        
        <div className="flex flex-col gap-4 text-center">
          <h1 className="text-gray-700 text-2xl font-extrabold tracking-[-0.6px]">
            비밀번호가 성공적으로 변경되었습니다
          </h1>
          
          <p className="text-gray-500 text-base font-medium">
            새로운 비밀번호로 로그인해주세요
          </p>
        </div>
      </div>
      
      <div className="flex flex-col gap-4 w-full">
        <Button
          onClick={handleGoToLogin}
          className="w-full"
        >
          로그인하러 가기
        </Button>
        
        <div className="text-center">
          <p className="text-gray-400 text-sm">
            5초 후 자동으로 로그인 페이지로 이동합니다
          </p>
        </div>
      </div>
    </div>
  );
}