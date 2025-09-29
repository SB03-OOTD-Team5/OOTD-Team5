import React, { useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '@/lib/stores/useAuthStore.ts';
import { useProfileStore } from '@/lib/stores/useProfileStore.ts';

interface ProfileMenuProps {
  isOpen: boolean;
  onClose: () => void;
  anchorElement?: HTMLElement | null;
}

export default function ProfileMenu({ isOpen, onClose, anchorElement }: ProfileMenuProps) {
  const navigate = useNavigate();
  const menuRef = useRef<HTMLDivElement>(null);
  const { data: auth, signOut } = useAuthStore();
  const { clear: clearProfile } = useProfileStore();

  // 외부 클릭 감지
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (
        isOpen &&
        menuRef.current &&
        !menuRef.current.contains(event.target as Node) &&
        anchorElement &&
        !anchorElement.contains(event.target as Node)
      ) {
        onClose();
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [isOpen, onClose, anchorElement]);

  // ESC 키로 닫기
  useEffect(() => {
    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape' && isOpen) {
        onClose();
      }
    };

    document.addEventListener('keydown', handleKeyDown);
    return () => document.removeEventListener('keydown', handleKeyDown);
  }, [isOpen, onClose]);

  if (!isOpen) return null;

  // 포지셔닝 계산 - Figma 디자인에 맞게 수정
  const getMenuStyle = (): React.CSSProperties => {
    if (!anchorElement) return {};
    
    const rect = anchorElement.getBoundingClientRect();
    
    return {
      position: 'fixed' as const,
      top: rect.bottom + 4,
      right: window.innerWidth - rect.right,
      zIndex: 1000,
    };
  };

  const handleMenuClick = (action: string) => {
    switch (action) {
      case 'feeds':
        navigate(`/feeds?authorIdEqual=${auth?.userDto.id}`);
        break;
      case 'profile':
        navigate('/settings');
        break;
      case 'logout':
        // 로그아웃 처리
        signOut();
        clearProfile();
        navigate('/auth/login');
        break;
    }
    onClose();
  };

  return (
    <div
      ref={menuRef}
      className="bg-white box-border content-stretch flex flex-col gap-4 items-start justify-start p-[18px] rounded-[10px]"
      style={getMenuStyle()}
    >
      {/* Figma 디자인에 맞는 보더 및 그림자 */}
      <div className="absolute border border-gray-300 border-solid inset-0 pointer-events-none rounded-[10px] shadow-[0px_2px_4px_0px_rgba(55,55,64,0.03)]" />
      
      {/* 내 피드 */}
      <button
        onClick={() => handleMenuClick('feeds')}
        className="font-semibold leading-none not-italic text-gray-700 text-base tracking-[-0.4px] hover:text-gray-900 transition-colors"
      >
        <p className="leading-normal">내 피드</p>
      </button>

      {/* 프로필 수정 */}
      <button
        onClick={() => handleMenuClick('profile')}
        className="font-semibold leading-none not-italic text-gray-700 text-base text-nowrap tracking-[-0.4px] hover:text-gray-900 transition-colors"
      >
        <p className="leading-normal whitespace-pre">프로필 수정</p>
      </button>

      {/* 로그아웃 */}
      <button
        onClick={() => handleMenuClick('logout')}
        className="font-semibold leading-none not-italic text-gray-700 text-base text-nowrap tracking-[-0.4px] hover:text-red-500 transition-colors"
      >
        <p className="leading-normal whitespace-pre">로그아웃</p>
      </button>
    </div>
  );
}