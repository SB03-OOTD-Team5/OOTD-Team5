// 로컬 에셋 import
import { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import weatherIcon from '@/assets/illust_logos/il_Weather.svg';
import feedIcon from '@/assets/illust_logos/il_feed.svg';
import settingIcon from '@/assets/illust_logos/il_setting.svg';
import setting2Icon from '@/assets/illust_logos/il_setting-2.svg';
import closetIcon from '@/assets/illust_logos/il_closet.svg';
import profileIcon from '@/assets/icons/profile.svg';
import LogoSvg from "@/assets/illust_logos/Logo.svg";
import {useAuthStore} from "@/lib/stores/useAuthStore.ts";

interface SideMenuBtnProps {
  label?: string;
  isActive?: boolean;
  onClick?: () => void;
  icon?: React.ReactNode;
}

function SideMenuBtn({ label = "Label", isActive = false, onClick, icon }: SideMenuBtnProps) {
  const baseClass = "box-border content-stretch flex gap-[11px] h-[54px] items-center justify-start p-[10px] relative shrink-0 w-full cursor-pointer";
  const activeClass = "bg-white rounded-[10px] shadow-[0px_3px_10px_0px_rgba(0,0,0,0.03)] border border-gray-200";
  
  return (
    <div 
      className={`${baseClass} ${isActive ? activeClass : ""}`}
      onClick={onClick}
    >
      <div className="bg-white box-border content-stretch flex gap-2 items-center justify-center overflow-clip px-[7px] py-[3px] relative rounded-[6px] shrink-0 size-[30px]">
        {icon || (
          <div className="overflow-clip relative shrink-0 size-6">
            <img alt="" className="block max-w-none size-full" src={settingIcon} />
          </div>
        )}
      </div>
      <div className={`font-semibold leading-[0] not-italic relative shrink-0 text-[16px] text-nowrap tracking-[-0.4px] ${isActive ? 'text-gray-900' : 'text-gray-800'}`}>
        <p className="leading-[normal] whitespace-pre">{label}</p>
      </div>
    </div>
  );
}

function WeatherIcon() {
  return (
    <div className="overflow-clip relative shrink-0 size-6">
      <img alt="날씨" className="block max-w-none size-full" src={weatherIcon} />
    </div>
  );
}

function FeedIcon() {
  return (
    <div className="overflow-clip relative shrink-0 size-6">
      <img alt="피드" className="block max-w-none size-full" src={feedIcon} />
    </div>
  );
}

function ClosetIcon() {
  return (
    <div className="overflow-clip relative shrink-0 size-6">
      <img alt="옷장" className="block max-w-none size-full" src={closetIcon} />
    </div>
  );
}

function ProfileIcon() {
  return (
    <div className="overflow-clip relative shrink-0 size-6">
      <img alt="프로필" className="block max-w-none size-full" src={profileIcon} />
    </div>
  );
}

function SettingIcon() {
  return (
    <div className="overflow-clip relative shrink-0 size-6">
      <img alt="설정" className="block max-w-none size-full" src={settingIcon} />
    </div>
  );
}

function Setting2Icon() {
  return (
    <div className="overflow-clip relative shrink-0 size-6">
      <img alt="설정" className="block max-w-none size-full" src={setting2Icon} />
    </div>
  );
}

// 메뉴 아이템 정의
const menuItems = [
  {
    id: 'recommendations',
    label: '날씨별 옷 추천',
    path: '/recommendations',
    icon: <WeatherIcon />
  },
  {
    id: 'closet',
    label: '옷장',
    path: '/closet',
    icon: <ClosetIcon />
  },
  {
    id: 'feeds',
    label: '피드',
    path: '/feeds',
    icon: <FeedIcon />
  },
  {
    id: 'profiles',
    label: '프로필',
    path: '/profiles',
    icon: <ProfileIcon />
  },
  {
    id: 'users',
    label: '사용자 관리',
    path: '/admin/users',
    icon: <Setting2Icon />
  },
  {
    id: 'attributes',
    label: '속성 관리',
    path: '/admin/clothes-attributes',
    icon: <SettingIcon />
  }
];

export default function SideMenu() {
  const navigate = useNavigate();
  const location = useLocation();
  const [activeMenu, setActiveMenu] = useState<string>('recommendations');
  const isAdmin = useAuthStore(state => state.data?.userDto.role === 'ADMIN');

  // 현재 경로에 따라 활성 메뉴 업데이트
  useEffect(() => {
    const currentPath = location.pathname;
    const activeItem = menuItems.find(item => 
      currentPath === item.path || 
      (item.path !== '/' && currentPath.startsWith(item.path))
    );
    
    if (activeItem) {
      setActiveMenu(activeItem.id);
    }
  }, [location.pathname]);

  // 메뉴 클릭 핸들러
  const handleMenuClick = (menuId: string, path: string) => {
    setActiveMenu(menuId);
    navigate(path);
  };

  return (
    <div className="bg-gray-50 relative size-full">
      <div className="box-border content-stretch flex flex-col gap-3 items-start justify-start pb-6 pt-3.5 px-[26px] relative size-full">
        <div className="absolute top-0 left-0 right-0 h-[70px] z-10">
          <div className="absolute left-5 top-1/2 -translate-y-1/2 w-[200px] h-[56.934px]">
            <img src={LogoSvg} alt="옷장을 부탁해" className="h-full object-contain" />
          </div>
        </div>
        
        <div className="content-stretch flex flex-col gap-2 items-start justify-start relative shrink-0 w-full mt-[70px]">
          {menuItems
          .filter(item => {
            if (isAdmin) return true;
            return !item.path.includes('/admin/');
          })
          .map((item) => (
            <SideMenuBtn
              key={item.id}
              label={item.label}
              isActive={activeMenu === item.id}
              icon={item.icon}
              onClick={() => handleMenuClick(item.id, item.path)}
            />
          ))}
        </div>
      </div>
      
      {/* 우측 보더 */}
      <div className="absolute border-r border-gray-200 inset-0 pointer-events-none shadow-[6px_0px_10px_0px_rgba(0,0,0,0.01)]" />
    </div>
  );
}