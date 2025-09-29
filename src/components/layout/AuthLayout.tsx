import { Outlet } from 'react-router-dom';
import LoginBgImage from '@/assets/illust_logos/Login BG.svg';
import LogoSvg from '@/assets/illust_logos/Logo.svg';

export default function AuthLayout() {
  return (
    <div className="relative min-h-screen bg-white">
      {/* Background */}
      <div className="absolute inset-0 bg-center bg-cover bg-no-repeat" style={{ backgroundImage: `url('${LoginBgImage}')` }} />
      
      {/* GNB */}
      <div className="absolute top-0 left-0 right-0 h-[70px] z-10">
        <div className="absolute left-5 top-1/2 -translate-y-1/2 w-[200px] h-[56.934px]">
          <img src={LogoSvg} alt="옷장을 부탁해" className="h-full object-contain" />
        </div>
      </div>
      
      {/* Main Content */}
      <div className="relative min-h-screen flex flex-col items-center justify-center px-4 py-10">
        <div className="w-full max-w-[546px]">
          <Outlet />
        </div>
      </div>
    </div>
  );
}