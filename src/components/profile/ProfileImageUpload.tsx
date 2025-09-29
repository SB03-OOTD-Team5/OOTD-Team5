import { useState, useRef } from 'react';
import { Button } from '@/components/ui/button';
import profileIcon from '@/assets/icons/profile.svg';

interface ProfileImageUploadProps {
  currentImageUrl?: string;
  name?: string;
  onImageSelect: (file: File | null) => void;
  className?: string;
}

export default function ProfileImageUpload({
  currentImageUrl,
  name,
  onImageSelect,
  className = ""
}: ProfileImageUploadProps) {
  const [preview, setPreview] = useState<string | null>(null);
  const [imageError, setImageError] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleImageError = () => {
    setImageError(true);
  };

  const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (file) {
      // 파일 크기 제한 (5MB)
      if (file.size > 5 * 1024 * 1024) {
        alert('파일 크기는 5MB 이하로 선택해주세요.');
        return;
      }

      // 파일 타입 제한
      if (!file.type.startsWith('image/')) {
        alert('이미지 파일만 선택해주세요.');
        return;
      }

      // 미리보기 생성
      const reader = new FileReader();
      reader.onload = (e) => {
        setPreview(e.target?.result as string);
      };
      reader.readAsDataURL(file);

      onImageSelect(file);
    } else {
      setPreview(null);
      onImageSelect(null);
    }
  };

  const handleButtonClick = () => {
    fileInputRef.current?.click();
  };

  const displayImage = preview || currentImageUrl;

  return (
    <div className={`flex flex-col items-center ${className}`}>
      <div className="relative w-[140px] flex flex-col items-end pb-[27px]">
        {/* 프로필 이미지 */}
        <div className="aspect-square bg-[var(--color-gray-400)] rounded-full w-full overflow-hidden border border-[var(--color-gray-300)] shadow-[0px_2px_4px_0px_rgba(55,55,64,0.03)] mb-[-27px]">
          {displayImage && !imageError ? (
            <img
              src={displayImage}
              alt={name || '프로필'}
              className="w-full h-full object-cover"
              onError={handleImageError}
            />
          ) : (
            <img
              src={profileIcon}
              alt={name || '프로필'}
              className="w-full h-full object-cover"
            />
          )}
        </div>

        {/* 변경 버튼 */}
        <div className="mb-[-27px] z-10">
          <Button
            type="button"
            onClick={handleButtonClick}
            variant="primary"
            size="sm"
            className="bg-[var(--color-blue-500)] text-white hover:bg-[var(--color-blue-500)]/90 rounded-full px-4 py-2 text-[var(--font-size-body-2)] font-[var(--font-weight-bold)] tracking-[-0.4px] shadow-sm !text-white"
          >
            변경
          </Button>
        </div>
      </div>

      {/* 숨겨진 파일 입력 */}
      <input
        ref={fileInputRef}
        type="file"
        accept="image/*"
        onChange={handleFileChange}
        className="hidden"
      />
    </div>
  );
}