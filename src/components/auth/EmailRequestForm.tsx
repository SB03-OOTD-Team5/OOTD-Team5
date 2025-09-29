import { useState } from "react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Link } from "react-router-dom";
import {resetPassword} from "@/lib/api";

interface EmailRequestFormProps {
  onNext: (email: string) => void;
}

export default function EmailRequestForm({ onNext }: EmailRequestFormProps) {
  const [email, setEmail] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  // 이메일 형식 검사
  const isValidEmail = (email: string): boolean => {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!email.trim()) {
      setError("이메일을 입력해주세요");
      return;
    }
    
    if (!isValidEmail(email)) {
      setError("유효하지 않은 이메일입니다.");
      return;
    }

    setLoading(true);
    setError("");

    try {
      await resetPassword({email});
      onNext(email);
    } catch (err) {
      console.log(err)
      setError("이메일 발송에 실패했습니다. 다시 시도해주세요.");
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    setEmail(value);
    
    // 실시간 에러 클리어
    if (error) {
      setError("");
    }
  };

  const isFormValid = email.trim() && isValidEmail(email);

  return (
    <div className="flex flex-col gap-[30px] w-full">
      <h1 className="text-gray-700 text-2xl font-extrabold tracking-[-0.6px] text-center">
        임시 비밀번호 받을 이메일을 입력해주세요
      </h1>
      
      <form onSubmit={handleSubmit} className="flex flex-col gap-[26px] w-full">
        <div className="flex flex-col gap-[18px] w-full">
          <div className="flex flex-col gap-2.5 w-full">
            <label 
              htmlFor="email"
              className="text-gray-500 text-sm font-bold tracking-[-0.35px]"
            >
              이메일
            </label>
            <Input
              id="email"
              type="email"
              placeholder="codeit@codeit.com"
              value={email}
              onChange={handleInputChange}
              error={error}
            />
          </div>
        </div>
        
        <Button
          type="submit"
          disabled={!isFormValid || loading}
          className="w-full"
        >
          {loading ? "전송 중..." : "다음"}
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