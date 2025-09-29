import { useState } from "react";
import WeatherIconsHeader from "@/components/auth/WeatherIconsHeader";
import EmailRequestForm from "@/components/auth/EmailRequestForm";
import TemporaryPasswordForm from "@/components/auth/TemporaryPasswordForm";
import NewPasswordForm from "@/components/auth/NewPasswordForm";
import PasswordChangeSuccess from "@/components/auth/PasswordChangeSuccess";

type Step = 1 | 2 | 3 | 4;

export default function ForgotPasswordPage() {
  const [currentStep, setCurrentStep] = useState<Step>(1);
  const [email, setEmail] = useState("");

  const handleEmailNext = (userEmail: string) => {
    setEmail(userEmail);
    setCurrentStep(2);
  };

  const handleTemporaryPasswordNext = () => {
    setCurrentStep(3);
  };

  const handleTemporaryPasswordTimeout = () => {
    // 타이머 만료 시 처음으로 돌아가기
    setCurrentStep(1);
    setEmail("");
  };

  const handleNewPasswordComplete = () => {
    setCurrentStep(4);
  };

  const renderStepContent = () => {
    switch (currentStep) {
      case 1:
        return <EmailRequestForm onNext={handleEmailNext} />;
      case 2:
        return (
          <TemporaryPasswordForm 
            email={email}
            onNext={handleTemporaryPasswordNext}
            onTimeout={handleTemporaryPasswordTimeout}
          />
        );
      case 3:
        return <NewPasswordForm onComplete={handleNewPasswordComplete} />;
      case 4:
        return <PasswordChangeSuccess />;
      default:
        return <EmailRequestForm onNext={handleEmailNext} />;
    }
  };

  return (
    <div className="flex flex-col items-center">
      {/* Weather Icons Header - 성공 화면에서는 표시하지 않음 */}
      {currentStep !== 4 && <WeatherIconsHeader />}
      
      {/* Main Form Card */}
      <div className="bg-white rounded-[30px] shadow-[0px_0px_30px_0px_rgba(130,188,255,0.2)] p-[60px] w-full relative z-[1] mb-[-40px]">
        <div className="flex flex-col gap-[30px] items-center">
          {renderStepContent()}
        </div>
      </div>
    </div>
  );
}