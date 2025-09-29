import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Link } from "react-router-dom";
import SocialLoginSection from "./SocialLoginSection";
import {createUser} from "@/lib/api";

export default function RegisterForm() {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    email: "",
    name: "",
    password: "",
    confirmPassword: "",
  });
  const [errors, setErrors] = useState<{
    email?: string;
    name?: string;
    password?: string;
    confirmPassword?: string;
    submit?: string;
  }>({});
  const [loading, setLoading] = useState(false);

  // 이메일 형식 검사
  const isValidEmail = (email: string): boolean => {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  };

  // 비밀번호 형식 검사 (최소 6자, 영문+숫자 조합)
  const isValidPassword = (password: string): boolean => {
    const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d@$!%*?&]{6,}$/;
    return passwordRegex.test(password);
  };

  // 이름 유효성 검사 (2-20자, 한글/영문/숫자)
  const isValidName = (name: string): boolean => {
    const nameRegex = /^[가-힣a-zA-Z0-9]{2,20}$/;
    return nameRegex.test(name);
  };

  const validateForm = () => {
    const newErrors: { email?: string; name?: string; password?: string; confirmPassword?: string } = {};
    
    if (!formData.email.trim()) {
      newErrors.email = "이메일을 입력해주세요";
    } else if (!isValidEmail(formData.email)) {
      newErrors.email = "유효하지 않은 이메일입니다.";
    }
    
    if (!formData.name.trim()) {
      newErrors.name = "이름을 입력해주세요";
    } else if (!isValidName(formData.name)) {
      newErrors.name = "이름은 2-20자의 한글, 영문, 숫자만 가능합니다";
    }
    
    if (!formData.password.trim()) {
      newErrors.password = "비밀번호를 입력해주세요";
    } else if (!isValidPassword(formData.password)) {
      newErrors.password = "6자 이상 입력해주세요";
    }
    
    if (!formData.confirmPassword.trim()) {
      newErrors.confirmPassword = "비밀번호를 다시 입력해주세요";
    } else if (formData.password !== formData.confirmPassword) {
      newErrors.confirmPassword = "비밀번호가 다릅니다.";
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
    const newErrors: { email?: string; name?: string; password?: string; confirmPassword?: string } = { ...errors };
    
    if (field === 'email') {
      if (value.trim() === '') {
        delete newErrors.email;
      } else if (!isValidEmail(value)) {
        newErrors.email = "유효하지 않은 이메일입니다.";
      } else {
        delete newErrors.email;
      }
    }
    
    if (field === 'name') {
      if (value.trim() === '') {
        delete newErrors.name;
      } else if (!isValidName(value)) {
        newErrors.name = "이름은 2-20자의 한글, 영문, 숫자만 가능합니다";
      } else {
        delete newErrors.name;
      }
    }
    
    if (field === 'password') {
      if (value.trim() === '') {
        delete newErrors.password;
      } else if (!isValidPassword(value)) {
        newErrors.password = "6자 이상 입력해주세요";
      } else {
        delete newErrors.password;
      }
      
      // 비밀번호가 변경되면 확인 비밀번호도 다시 검사
      if (formData.confirmPassword && value !== formData.confirmPassword) {
        newErrors.confirmPassword = "비밀번호가 다릅니다.";
      } else if (formData.confirmPassword && value === formData.confirmPassword) {
        delete newErrors.confirmPassword;
      }
    }
    
    if (field === 'confirmPassword') {
      if (value.trim() === '') {
        delete newErrors.confirmPassword;
      } else if (formData.password !== value) {
        newErrors.confirmPassword = "비밀번호가 다릅니다.";
      } else {
        delete newErrors.confirmPassword;
      }
    }
    
    setErrors(newErrors);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!validateForm()) return;

    setLoading(true);

    try {
      await createUser({
        email: formData.email,
        name: formData.name,
        password: formData.password
      });
      
      // 성공 시 로그인 페이지로 이동
      navigate("/auth/login");
    } catch (err) {
      // 서버 에러 처리
      setErrors({ submit: "회원가입에 실패했습니다. 다시 시도해주세요." });
    } finally {
      setLoading(false);
    }
  };

  const isFormValid = 
    formData.email.trim() && 
    formData.name.trim() && 
    formData.password.trim() && 
    formData.confirmPassword.trim() &&
    isValidEmail(formData.email) &&
    isValidName(formData.name) &&
    isValidPassword(formData.password) &&
    formData.password === formData.confirmPassword;

  return (
    <div className="flex flex-col gap-[30px] w-full">
      <h1 className="text-gray-700 text-2xl font-extrabold tracking-[-0.6px] text-center">
        나만의 옷장을 만들어보세요
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
              value={formData.email}
              onChange={handleInputChange("email")}
              error={errors.email}
            />
          </div>
          
          <div className="flex flex-col gap-2.5 w-full">
            <label 
              htmlFor="name"
              className="text-gray-500 text-sm font-bold tracking-[-0.35px]"
            >
              이름
            </label>
            <Input
              id="name"
              type="text"
              placeholder="이름을 입력해주세요"
              value={formData.name}
              onChange={handleInputChange("name")}
              error={errors.name}
            />
          </div>
          
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
        </div>

        {errors.submit && (
            <div className="text-red-500 text-sm font-semibold text-center">
              {errors.submit}
            </div>
        )}
        
        <Button
          type="submit"
          disabled={!isFormValid || loading}
          className="w-full"
        >
          {loading ? "가입 중..." : "가입하기"}
        </Button>
      </form>
      
      <div className="flex gap-[7px] items-center justify-center text-base font-semibold tracking-[-0.4px]">
        <span className="text-gray-500">
          이미 계정이 있으신가요?
        </span>
        <Link 
          to="/auth/login"
          className="text-blue-500 hover:underline"
        >
          로그인
        </Link>
      </div>
      
      <SocialLoginSection />
    </div>
  );
}