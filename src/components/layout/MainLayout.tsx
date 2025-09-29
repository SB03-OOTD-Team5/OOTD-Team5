import {Outlet, useLocation} from 'react-router-dom';
import SideMenu from './SideMenu';
import GNB from './GNB';
import WebSocket from "@/components/layout/WebSocket.tsx";
import Sse from "@/components/layout/Sse.tsx";

export default function MainLayout() {
  const location = useLocation();

  const bgColor = location.pathname.includes("/recommendations") ? 'bg-gray-100' : 'bg-white';

  return (
    <div className={`h-full overflow-hidden ${bgColor} flex`}>
      {/* 사이드 메뉴 - 280px 고정폭 */}
      <div className="w-[280px] h-full flex-shrink-0">
        <SideMenu />
      </div>
      
      {/* 메인 컨텐츠 영역 */}
      <div className="flex-1 flex flex-col h-full min-w-0">
        {/* GNB - 60px 높이 */}
        <div className="h-[60px] flex-shrink-0">
          <GNB />
        </div>
        
        {/* 페이지 컨텐츠 - 남은 공간 모두 차지 */}
        <main className="flex-1 min-h-0">
          <Outlet />
        </main>
        <WebSocket/>
        <Sse/>
      </div>
    </div>
  );
}