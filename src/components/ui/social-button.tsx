import * as React from "react";
import { cn } from "@/lib/utils";

export interface SocialButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  icon: string;
  iconAlt: string;
  label: string;
  showArrow?: boolean;
}

const SocialButton = React.forwardRef<HTMLButtonElement, SocialButtonProps>(
  ({ className, icon, iconAlt, label, showArrow = true, ...props }, ref) => {
    return (
      <button
        className={cn(
          "w-full bg-white border border-[var(--color-gray-300)] rounded-[12px] flex items-center justify-between px-[18px] py-2.5 h-[46px] transition-colors hover:bg-[var(--color-gray-50)] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50",
          className
        )}
        ref={ref}
        {...props}
      >
        <div className="w-5 h-5 flex-shrink-0">
          <img src={icon} alt={iconAlt} className="w-full h-full object-contain" />
        </div>
        
        <span className="flex-1 text-center text-[var(--color-gray-600)] text-[var(--font-size-body-2)] font-[var(--font-weight-semibold)] tracking-[-0.4px]">
          {label}
        </span>
        
        {showArrow && (
          <div className="w-5 h-5 flex-shrink-0 flex items-center justify-center">
            <svg 
              width="8" 
              height="13" 
              viewBox="0 0 8 13" 
              fill="none" 
              className="text-[var(--color-gray-400)]"
            >
              <path 
                d="M1.5 2L6 6.5L1.5 11" 
                stroke="currentColor" 
                strokeWidth="2" 
                strokeLinecap="round" 
                strokeLinejoin="round"
              />
            </svg>
          </div>
        )}
      </button>
    );
  }
);
SocialButton.displayName = "SocialButton";

export { SocialButton };