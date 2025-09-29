import type { NotificationDto } from '@/lib/api/types.ts';

interface NotificationItemProps {
  notification: NotificationDto;
  onClick?: (id: string) => void;
}

import icSend from '@/assets/icons/ic_send.svg';
import icSettingsPlus from '@/assets/icons/ic_settings-plus.svg';
import icHanger from '@/assets/icons/ic_hanger.svg';
import icHeart from '@/assets/icons/ic_heart.svg';
import icChangeUser from '@/assets/icons/ic_change_user.svg';
import icMessage from '@/assets/icons/ic_message.svg';

// 알림 유형별 아이콘 매핑
const getNotificationIcon = (title: string, content: string) => {
  if (title.includes('메시지') || content.includes('메시지')) {
    return icSend;
  }
  if (title.includes('속성') || content.includes('속성')) {
    return icSettingsPlus;
  }
  if (title.includes('피드') || content.includes('피드')) {
    return icHanger;
  }
  if (title.includes('좋아요') || content.includes('좋아요')) {
    return icHeart;
  }
  if (title.includes('권한') || content.includes('권한')) {
    return icChangeUser;
  }
  if (title.includes('댓글') || content.includes('댓글')) {
    return icMessage;
  }
  // 기본값
  return icMessage;
};

// 시간 포맷팅 함수
const formatTimeAgo = (createdAt: string) => {
  const now = new Date();
  const created = new Date(createdAt);
  const diffMs = now.getTime() - created.getTime();
  const diffMinutes = Math.floor(diffMs / (1000 * 60));
  
  if (diffMinutes < 1) return '방금 전';
  if (diffMinutes < 60) return `${diffMinutes}분 전`;
  
  const diffHours = Math.floor(diffMinutes / 60);
  if (diffHours < 24) return `${diffHours}시간 전`;
  
  const diffDays = Math.floor(diffHours / 24);
  if (diffDays < 7) return `${diffDays}일 전`;
  
  return created.toLocaleDateString('ko-KR');
};

export const NotificationItem = ({ notification, onClick }: NotificationItemProps) => {
  const iconSrc = getNotificationIcon(notification.title, notification.content);
  const timeAgo = formatTimeAgo(notification.createdAt);

  return (
    <div 
      className={`box-border flex gap-2.5 items-start justify-start p-5 relative w-full cursor-pointer hover:bg-[#f0f9ff] transition-colors`}
      onClick={() => onClick?.(notification.id)}
    >
      {/* 알림 아이콘 */}
      <div className="overflow-hidden relative shrink-0 w-6 h-6">
        <img 
          src={iconSrc} 
          alt="알림 아이콘" 
          className="w-full h-full"
        />
      </div>
      
      {/* 알림 내용 */}
      <div className="basis-0 flex flex-col gap-3 grow items-start justify-start min-h-px min-w-px relative shrink-0">
        {/* 제목과 내용 */}
        <div className="flex flex-col gap-2 items-start justify-start relative shrink-0 w-full">
          <div className="flex items-center justify-start relative shrink-0 text-[var(--color-gray-800)] text-[16px] text-nowrap tracking-[-0.4px] w-full">
            <p className="font-[var(--font-weight-bold)] leading-normal">
              {notification.title}
            </p>
          </div>
          {notification.content && (
            <div className="font-[var(--font-weight-semibold)] min-w-full relative shrink-0 text-[var(--color-gray-500)] text-[16px] tracking-[-0.4px]">
              <p className="leading-normal">
                {notification.content}
              </p>
            </div>
          )}
        </div>
        
        {/* 시간 */}
        <div className="font-[var(--font-weight-semibold)] relative shrink-0 text-[var(--color-gray-400)] text-[14px] tracking-[-0.35px] w-full">
          <p className="leading-normal">{timeAgo}</p>
        </div>
      </div>
    </div>
  );
};