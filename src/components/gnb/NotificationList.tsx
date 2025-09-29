import React, { useEffect, useRef } from 'react';
import { useNotificationStore } from '@/lib/stores/useNotificationStore.ts';
import { readNotification } from '@/lib/api/notifications.ts';
import { NotificationItem } from './NotificationItem.tsx';
import { useInfiniteScroll } from '@/lib/hooks/useInfiniteScroll.ts';

interface NotificationListProps {
  isOpen: boolean;
  onClose: () => void;
  anchorElement?: HTMLElement | null;
}

export const NotificationList = ({ isOpen, onClose, anchorElement }: NotificationListProps) => {
  const { data: notifications, loading, fetchMore, delete: removeNotification } = useNotificationStore();
  const listRef = useRef<HTMLDivElement>(null);

  const { ref: infiniteScrollRef } = useInfiniteScroll({
    onLoadMore: () => fetchMore(),
  });

  // 외부 클릭 감지
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (
        isOpen &&
        listRef.current &&
        !listRef.current.contains(event.target as Node) &&
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

  // 알림 클릭 핸들러
  const handleNotificationClick = async (notificationId: string) => {
    try {
      await readNotification(notificationId);
      removeNotification(notificationId);
    } catch (error) {
      console.error('알림 읽음 처리 실패:', error);
    }
  };


  if (!isOpen) return null;

  // 포지셔닝 계산
  const getPopupStyle = (): React.CSSProperties => {
    if (!anchorElement) return {};
    
    const rect = anchorElement.getBoundingClientRect();
    const popupWidth = 450; // 팝업 너비
    const popupHeight = 530; // 고정 높이 (헤더 50px + 알림목록 480px)
    
    return {
      position: 'fixed' as const,
      top: rect.bottom + 8, // 버튼 아래 8px
      right: window.innerWidth - rect.right, // 오른쪽 정렬
      width: popupWidth,
      height: popupHeight,
      zIndex: 1000,
    };
  };

  return (
    <div 
      ref={listRef}
      className="bg-white rounded-[20px] border border-[var(--color-gray-200)] shadow-[0px_2px_10px_0px_rgba(41,52,57,0.14)] overflow-hidden"
      style={getPopupStyle()}
    >
      {/* 알림 목록 */}
      <div 
        className="flex flex-col gap-0.5 h-[500px] overflow-y-auto"
      >
        {notifications.length === 0 ? (
          <div className="flex items-center justify-center h-full text-[var(--color-gray-400)]">
            알림이 없습니다
          </div>
        ) : (
          <>
            {notifications.map((notification, index) => {
              const isFirst = index === 0;
              const isLast = index === notifications.length - 1;
              let roundedClass = '';
              
              if (isFirst && isLast) {
                roundedClass = 'rounded-tl-[24px] rounded-tr-[24px] rounded-bl-[24px] rounded-br-[24px]';
              } else if (isFirst) {
                roundedClass = 'rounded-tl-[24px] rounded-tr-[24px]';
              } else if (isLast) {
                roundedClass = 'rounded-bl-[24px] rounded-br-[24px]';
              }

              return (
                <div key={notification.id} className={roundedClass}>
                  <NotificationItem
                    notification={notification}
                    onClick={handleNotificationClick}
                  />
                </div>
              );
            })}

            <div ref={infiniteScrollRef} className="h-1" />
            
            {loading && notifications.length > 0 && (
              <div className="p-4 text-center text-[var(--color-gray-400)] text-sm">
                더 불러오는 중...
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
};