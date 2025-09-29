import { Slider } from '@/components/ui/slider';

interface TemperatureSensitivitySliderProps {
  value?: number;
  onValueChange: (value: number) => void;
  className?: string;
}

export default function TemperatureSensitivitySlider({
  value = 3,
  onValueChange,
  className = ""
}: TemperatureSensitivitySliderProps) {
  const handleSliderChange = (values: number[]) => {
    onValueChange(values[0]);
  };

  return (
    <div className={`space-y-2.5 ${className}`}>
      <div className="font-[var(--font-weight-bold)] text-[var(--font-size-body-3)] text-[var(--color-gray-500)] tracking-[-0.35px]">
        온도 민감도
      </div>
      
      <div className="space-y-2">
        <div className="relative w-full">
          <Slider
            value={[value]}
            onValueChange={handleSliderChange}
            max={5}
            min={1}
            step={1}
            className="w-full [&_[data-slot=slider-track]]:bg-[var(--color-gray-200)] [&_[data-slot=slider-range]]:bg-[var(--color-blue-500)] [&_[data-slot=slider-thumb]]:bg-[var(--color-blue-500)] [&_[data-slot=slider-thumb]]:border-[var(--color-blue-500)]"
          />
        </div>
        
        {/* 슬라이더 라벨 */}
        <div className="flex justify-between items-end text-[var(--color-gray-500)]">
          <div className="text-center text-left leading-[1.4]">
            <div className="text-[13px] font-[var(--font-weight-semibold)] tracking-[-0.325px]">
              1
            </div>
            <div className="text-[12px] font-[var(--font-weight-bold)] tracking-[-0.3px]">
              (추위 많이 탐)
            </div>
          </div>
          <div className="text-center text-right leading-[1.4]">
            <div className="text-[13px] font-[var(--font-weight-semibold)] tracking-[-0.325px]">
              5
            </div>
            <div className="text-[12px] tracking-[-0.3px]">
              (더위 많이 탐)
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}