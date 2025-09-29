import { useState, useEffect } from "react";

interface TimerDisplayProps {
  initialSeconds: number;
  onExpire: () => void;
  isActive: boolean;
}

export default function TimerDisplay({ initialSeconds, onExpire, isActive }: TimerDisplayProps) {
  const [seconds, setSeconds] = useState(initialSeconds);

  useEffect(() => {
    if (!isActive) return;

    const interval = setInterval(() => {
      setSeconds((prev) => {
        if (prev <= 1) {
          clearInterval(interval);
          onExpire();
          return 0;
        }
        return prev - 1;
      });
    }, 1000);

    return () => clearInterval(interval);
  }, [isActive, onExpire]);

  // MM:SS 형식으로 변환
  const formatTime = (totalSeconds: number): string => {
    const minutes = Math.floor(totalSeconds / 60);
    const remainingSeconds = totalSeconds % 60;
    return `${minutes.toString().padStart(2, '0')}:${remainingSeconds.toString().padStart(2, '0')}`;
  };

  return (
    <div className="flex gap-1 items-center justify-center">
      {/* 타이머 아이콘 */}
      <div className="w-[22px] h-[22px] relative">
        <svg width="22" height="22" viewBox="0 0 22 22" fill="none">
          <path 
            d="M11 3C6.58172 3 3 6.58172 3 11C3 15.4183 6.58172 19 11 19C15.4183 19 19 15.4183 19 11C19 6.58172 15.4183 3 11 3ZM11 17C7.68629 17 5 14.3137 5 11C5 7.68629 7.68629 5 11 5C14.3137 5 17 7.68629 17 11C17 14.3137 14.3137 17 11 17ZM11.5 7H10V11.5L13.5 13.5L14.5 11.9L11.5 10.5V7Z" 
            fill="#1E89F4"
          />
        </svg>
      </div>
      
      <div className="text-blue-500 text-xl font-extrabold tracking-[-0.5px] text-center">
        {formatTime(seconds)}
      </div>
    </div>
  );
}