import { useState } from "react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Link } from "react-router-dom";
import TimerDisplay from "./TimerDisplay";
import { useAuthStore } from "@/lib/stores/useAuthStore";

interface TemporaryPasswordFormProps {
  email: string;
  onNext: () => void;
  onTimeout: () => void;
}

export default function TemporaryPasswordForm({ email, onNext, onTimeout }: TemporaryPasswordFormProps) {
  const [temporaryPassword, setTemporaryPassword] = useState("");
  const [error, setError] = useState("");
  const [isTimerActive, setIsTimerActive] = useState(true);
  const [isExpired, setIsExpired] = useState(false);
  const { loading, signIn } = useAuthStore();

  // 이메일 마스킹 함수
  const maskEmail = (email: string): string => {
    const [localPart, domain] = email.split('@');
    if (localPart.length <= 2) {
      return `${localPart[0]}*@${domain}`;
    }
    const visibleStart = localPart.substring(0, 2);
    const visibleEnd = localPart.substring(localPart.length - 1);
    const masked = '*'.repeat(Math.max(localPart.length - 3, 1));
    return `${visibleStart}${masked}${visibleEnd}@${domain}`;
  };

  const handleTimerExpire = () => {
    setIsExpired(true);
    setIsTimerActive(false);
    setError("인증 시간이 만료되었습니다. 다시 시도해주세요.");
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (isExpired) {
      onTimeout();
      return;
    }
    
    if (!temporaryPassword.trim()) {
      setError("임시 비밀번호를 입력해주세요");
      return;
    }

    setError("");

    try {
      await signIn(email, temporaryPassword);

      setIsTimerActive(false);
      onNext();
    } catch (err) {
      console.error(err)
      setError("임시 비밀번호 확인에 실패했습니다. 다시 시도해주세요.");
    }
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    setTemporaryPassword(value);
    
    // 실시간 에러 클리어
    if (error && !isExpired) {
      setError("");
    }
  };

  const isFormValid = temporaryPassword.trim() && !isExpired;

  return (
    <div className="flex flex-col gap-[30px] w-full">
      <div className="flex flex-col gap-4 items-center">
        <h1 className="text-gray-700 text-2xl font-extrabold leading-[1.6] tracking-[-0.6px] text-center">
          <div>{maskEmail(email)}으로</div>
          <div>전달 받은 임시 비밀번호를 입력해주세요</div>
        </h1>
        
        <TimerDisplay 
          initialSeconds={180} // 3분
          onExpire={handleTimerExpire}
          isActive={isTimerActive}
        />
      </div>
      
      <form onSubmit={handleSubmit} className="flex flex-col gap-[26px] w-full">
        <div className="flex flex-col gap-[18px] w-full">
          <div className="flex flex-col gap-2.5 w-full">
            <label 
              htmlFor="temporaryPassword"
              className="text-gray-500 text-sm font-bold tracking-[-0.35px]"
            >
              임시 비밀번호 입력
            </label>
            <Input
              id="temporaryPassword"
              type="password"
              placeholder="임시 비밀번호를 입력해주세요"
              value={temporaryPassword}
              onChange={handleInputChange}
              error={error}
              disabled={isExpired}
            />
          </div>
        </div>
        
        <Button
          type="submit"
          disabled={!isFormValid || loading}
          className="w-full"
        >
          {loading ? "확인 중..." : isExpired ? "처음부터 다시하기" : "다음"}
        </Button>
      </form>
      
      <div className="flex justify-center">
        <Link 
          to="/auth/login"
          className="text-blue-500 text-base font-semibold tracking-[-0.4px] hover:underline"
        >
          로그인 화면으로 돌아가기
        </Link>
      </div>
    </div>
  );
}