import * as React from "react";
import { cn } from "@/lib/utils";
import EyesOnIcon from "@/assets/icons/ic_eyes_on.svg";
import EyesOffIcon from "@/assets/icons/ic_eyes_off.svg";
export interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  showPasswordToggle?: boolean;
  error?: string;
  icon?: React.ReactNode
}

const Input = React.forwardRef<HTMLInputElement, InputProps>(
  ({ className, type, showPasswordToggle = false, error, icon, ...props }, ref) => {
    const [showPassword, setShowPassword] = React.useState(false);
    const inputType = showPasswordToggle ? (showPassword ? "text" : "password") : type;

    return (
      <div className="relative w-full">
        {icon}
        <div 
          className={cn(
            "bg-white box-border flex items-center justify-between px-5 py-3.5 rounded-[12px] border border-[var(--color-gray-200)] shadow-[0px_2px_4px_0px_rgba(55,55,64,0.03)]",
            error && "border-[var(--color-red-500)]",
            className
          )}
        >
          <input
            type={inputType}
            className={cn(
              "flex-1 bg-transparent outline-none placeholder:text-[var(--color-gray-400)] text-[var(--color-gray-700)]",
              "text-[var(--font-size-body-2)] font-[var(--font-weight-semibold)] tracking-[-0.4px]"
            )}
            ref={ref}
            {...props}
          />
          
          {showPasswordToggle && (
            <button
              type="button"
              onClick={() => setShowPassword(!showPassword)}
              className="ml-2 flex items-center justify-center w-6 h-6"
            >
              <img 
                src={showPassword ? EyesOnIcon : EyesOffIcon}
                alt={showPassword ? "비밀번호 보기" : "비밀번호 숨기기"}
                className="w-full h-full"
              />
            </button>
          )}
        </div>
        
        {error && (
          <p className="mt-1 text-red-500 text-xs font-semibold">
            {error}
          </p>
        )}
      </div>
    );
  }
);
Input.displayName = "Input";

export { Input };