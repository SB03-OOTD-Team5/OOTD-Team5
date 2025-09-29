import {useEffect, useState, useRef} from 'react';
import { useNotificationStore } from '@/lib/stores/useNotificationStore.ts';
import { NotificationList } from './NotificationList.tsx';
import bellIcon from '@/assets/icons/ic_bell.svg';
import {useSseStore} from "@/lib/stores/sseStore.ts";
import type {NotificationDto} from "@/lib/api";

export default function NotificationIcon() {
  const [isHovered, setIsHovered] = useState(false);
  const [isListOpen, setIsListOpen] = useState(false);
  const iconRef = useRef<HTMLDivElement>(null);
  const { count, fetch, clear, add } = useNotificationStore();
  const { isConnected, subscribe } = useSseStore();

  useEffect(() => {
    fetch();
    return () => {
      clear();
    };
  }, [fetch, clear]);

  useEffect(() => {
    if (isConnected) {
      subscribe('notifications', (notification: NotificationDto) => {
        add(notification);
      });
    }
  }, [isConnected, subscribe, add]);

  const handleClick = () => {
    setIsListOpen(!isListOpen);
  };

  const handleCloseList = () => {
    setIsListOpen(false);
  };

  const totalCount = count();

  return (
    <>
      <div 
        ref={iconRef}
        className="relative cursor-pointer"
        onClick={handleClick}
        onMouseEnter={() => setIsHovered(true)}
        onMouseLeave={() => setIsHovered(false)}
      >
        {/* 알림 아이콘 */}
        <div className={`overflow-clip relative shrink-0 size-6 transition-opacity ${isHovered ? 'opacity-80' : 'opacity-100'}`}>
          <img alt="알림" className="block max-w-none size-full" src={bellIcon} />
        </div>
        
        {/* 알림 배지 */}
        {(totalCount > 0) && (
          <div className="absolute bg-red-500 overflow-clip -right-1 -top-1 rounded-full size-4 flex items-center justify-center">
            <div className="flex flex-col font-bold justify-center leading-none not-italic text-white text-[9px] text-nowrap tracking-[-0.18px]">
              <p className="leading-normal whitespace-pre">{totalCount > 99 ? "99+" : totalCount.toString()}</p>
            </div>
          </div>
        )}
      </div>
      
      {/* 알림 목록 드롭다운 */}
      <NotificationList 
        isOpen={isListOpen}
        onClose={handleCloseList}
        anchorElement={iconRef.current}
      />
    </>
  );
}