import { useState } from "react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Link } from "react-router-dom";
import {changePassword} from "@/lib/api";
import {useAuthStore} from "@/lib/stores/useAuthStore.ts";

interface NewPasswordFormProps {
  onComplete: () => void;
}

export default function NewPasswordForm({ onComplete }: NewPasswordFormProps) {
  const [formData, setFormData] = useState({
    password: "",
    confirmPassword: "",
  });
  const [errors, setErrors] = useState<{
    password?: string;
    confirmPassword?: string;
  }>({});
  const [loading, setLoading] = useState(false);
  const { data: authentication } = useAuthStore();

  // 비밀번호 형식 검사 (최소 6자, 영문+숫자 조합)
  const isValidPassword = (password: string): boolean => {
    const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d@$!%*?&]{6,}$/;
    return passwordRegex.test(password);
  };

  const validateForm = () => {
    const newErrors: { password?: string; confirmPassword?: string } = {};
    
    if (!formData.password.trim()) {
      newErrors.password = "비밀번호를 입력해주세요";
    } else if (!isValidPassword(formData.password)) {
      newErrors.password = "올바르지 않은 비밀번호입니다.";
    }
    
    if (!formData.confirmPassword.trim()) {
      newErrors.confirmPassword = "비밀번호를 다시 입력해주세요";
    } else if (formData.password !== formData.confirmPassword) {
      newErrors.confirmPassword = "비밀번호가 일치하지 않습니다.";
    }
    
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleInputChange = (field: keyof typeof formData) => (
    e: React.ChangeEvent<HTMLInputElement>
  ) => {
    const value = e.target.value;
    setFormData(prev => ({
      ...prev,
      [field]: value
    }));
    
    // 실시간 유효성 검사
    const newErrors: { password?: string; confirmPassword?: string } = { ...errors };
    
    if (field === 'password') {
      if (value.trim() === '') {
        delete newErrors.password;
      } else if (!isValidPassword(value)) {
        newErrors.password = "올바르지 않은 비밀번호입니다.";
      } else {
        delete newErrors.password;
      }
      
      // 비밀번호가 변경되면 확인 비밀번호도 다시 검사
      if (formData.confirmPassword && value !== formData.confirmPassword) {
        newErrors.confirmPassword = "비밀번호가 일치하지 않습니다.";
      } else if (formData.confirmPassword && value === formData.confirmPassword) {
        delete newErrors.confirmPassword;
      }
    }
    
    if (field === 'confirmPassword') {
      if (value.trim() === '') {
        delete newErrors.confirmPassword;
      } else if (formData.password !== value) {
        newErrors.confirmPassword = "비밀번호가 일치하지 않습니다.";
      } else {
        delete newErrors.confirmPassword;
      }
    }
    
    setErrors(newErrors);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!validateForm()) return;

    const userId = authentication?.userDto.id;
    if (!userId) {
      setErrors({ password: "사용자 정보가 없습니다. 다시 시도해주세요." });
      return;
    }

    setLoading(true);

    try {
      await changePassword(userId, {password: formData.password});
      
      onComplete();
    } catch (err) {
      // 일반적인 에러 처리
      setErrors({ password: "비밀번호 변경에 실패했습니다. 다시 시도해주세요." });
    } finally {
      setLoading(false);
    }
  };

  const isFormValid = 
    formData.password.trim() && 
    formData.confirmPassword.trim() && 
    isValidPassword(formData.password) && 
    formData.password === formData.confirmPassword;

  return (
    <div className="flex flex-col gap-[30px] w-full">
      <h1 className="text-gray-700 text-2xl font-extrabold tracking-[-0.6px] text-center">
        새로운 비밀번호로 변경해주세요
      </h1>
      
      <form onSubmit={handleSubmit} className="flex flex-col gap-[26px] w-full">
        <div className="flex flex-col gap-2.5 w-full">
          <label 
            htmlFor="password"
            className="text-gray-500 text-sm font-bold tracking-[-0.35px]"
          >
            비밀번호
          </label>
          <Input
            id="password"
            type="password"
            placeholder="비밀번호를 입력해주세요"
            value={formData.password}
            onChange={handleInputChange("password")}
            showPasswordToggle
            error={errors.password}
          />
        </div>
        
        <div className="flex flex-col gap-2.5 w-full">
          <label 
            htmlFor="confirmPassword"
            className="text-gray-500 text-sm font-bold tracking-[-0.35px]"
          >
            비밀번호 확인
          </label>
          <Input
            id="confirmPassword"
            type="password"
            placeholder="비밀번호를 다시 입력해주세요"
            value={formData.confirmPassword}
            onChange={handleInputChange("confirmPassword")}
            showPasswordToggle
            error={errors.confirmPassword}
          />
        </div>
        
        <Button
          type="submit"
          disabled={!isFormValid || loading}
          className="w-full"
        >
          {loading ? "변경 중..." : "완료"}
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