import {useEffect, useRef, useState} from 'react';
import ProfileMenu from './ProfileMenu.tsx';
import profileIcon from '@/assets/icons/profile.svg';
import {useMyProfileStore} from "@/lib/stores/useMyProfileStore.ts";
import {useAuthStore} from "@/lib/stores/useAuthStore.ts";

export default function ProfileIcon() {
  const [isHovered, setIsHovered] = useState(false);
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const [imageError, setImageError] = useState(false);
  const iconRef = useRef<HTMLDivElement>(null);
  const { data: profile, updateParams } = useMyProfileStore();
  const { data: auth } = useAuthStore();

  const imageUrl = profile?.profileImageUrl;
  const name = profile?.name;

  useEffect(() => {
    if (auth) {
      updateParams({userId: auth.userDto.id})
    }
  }, [auth, updateParams]);

  const handleImageError = () => {
    setImageError(true);
  };

  const handleClick = () => {
    setIsMenuOpen(!isMenuOpen);
  };

  const handleCloseMenu = () => {
    setIsMenuOpen(false);
  };



  return (
    <>
      <div 
        ref={iconRef}
        className={`relative shrink-0 size-[30px] cursor-pointer transition-transform ${isHovered ? 'scale-105' : 'scale-100'}`}
        onClick={handleClick}
        onMouseEnter={() => setIsHovered(true)}
        onMouseLeave={() => setIsHovered(false)}
        title={name || '프로필'}
      >
        {imageUrl && !imageError ? (
          <img 
            src={imageUrl} 
            alt={name || '프로필'}
            className="w-full h-full rounded-full object-cover border border-gray-300 shadow-sm"
            onError={handleImageError}
          />
        ) : (
          <img 
            src={profileIcon} 
            alt={name || '프로필'}
            className="w-full h-full"
          />
        )}
      </div>

      {/* 프로필 메뉴 드롭다운 */}
      <ProfileMenu
        isOpen={isMenuOpen}
        onClose={handleCloseMenu}
        anchorElement={iconRef.current}
      />
    </>
  );
}