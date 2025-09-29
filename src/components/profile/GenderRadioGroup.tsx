import { type Gender } from '@/lib/api/types';

interface GenderRadioGroupProps {
  value?: Gender;
  onValueChange: (value: Gender) => void;
  className?: string;
}

export default function GenderRadioGroup({
  value,
  onValueChange,
  className = ""
}: GenderRadioGroupProps) {
  const genderOptions: { value: Gender; label: string }[] = [
    { value: 'MALE', label: '남성' },
    { value: 'FEMALE', label: '여성' },
    { value: 'OTHER', label: '기타' }
  ];

  return (
    <div className={`flex gap-3 items-center ${className}`}>
      {genderOptions.map((option) => (
        <button
          key={option.value}
          type="button"
          onClick={() => onValueChange(option.value)}
          className={`
            w-[60px] h-[34px] rounded-full text-[var(--font-size-body-3)] font-[var(--font-weight-bold)] tracking-[-0.35px] transition-colors flex items-center justify-center
            ${
              value === option.value
                ? 'bg-[var(--color-gray-800)] text-white'
                : 'bg-[var(--color-gray-200)] text-[var(--color-gray-700)] hover:bg-[var(--color-gray-300)]'
            }
          `}
        >
          {option.label}
        </button>
      ))}
    </div>
  );
}