import NotificationIcon from '../gnb/NotificationIcon.tsx';
import ProfileIcon from '../gnb/ProfileIcon.tsx';

export default function GNB() {
  return (
    <div className="box-border content-stretch flex items-center justify-end pb-4 pt-3.5 px-8 relative size-full">
      <div className="content-stretch flex gap-[30px] items-center justify-start relative shrink-0">
        {/* 알림 아이콘 */}
        <NotificationIcon />
        
        {/* 프로필 아이콘 */}
        <ProfileIcon />
      </div>
    </div>
  );
}