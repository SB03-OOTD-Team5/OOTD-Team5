import WeatherIconsHeader from "@/components/auth/WeatherIconsHeader";
import RegisterForm from "@/components/auth/RegisterForm";

export default function RegisterPage() {
  return (
    <div className="flex flex-col items-center">
      {/* Weather Icons Header */}
      <WeatherIconsHeader />
      
      {/* Register Form Card */}
      <div className="bg-white rounded-[30px] shadow-[0px_0px_30px_0px_rgba(130,188,255,0.2)] p-[60px] w-full relative z-[1] mb-[-40px]">
        <div className="flex flex-col gap-[30px] items-center">
          <RegisterForm />
        </div>
      </div>
    </div>
  );
}